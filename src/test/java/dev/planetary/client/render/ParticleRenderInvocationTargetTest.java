package dev.planetary.client.render;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prevents client startup crashes caused by a @ModifyArg target for an
 * inherited method having the declaring superclass as its symbolic owner.
 *
 * <p>Use class-file ASM: loading an ordinary Mixin class through reflection
 * is not legal in NeoForge's reserved Mixin package.</p>
 */
final class ParticleRenderInvocationTargetTest {
    private static final String SHRIEK =
            "net/minecraft/client/particle/ShriekParticle";
    private static final String QUAD_DESCRIPTOR =
            "(Lcom/mojang/blaze3d/vertex/VertexConsumer;"
                    + "Lnet/minecraft/client/Camera;"
                    + "Lorg/joml/Quaternionf;F)V";
    private static final String EXPECTED =
            "L" + SHRIEK + ";renderRotatedQuad" + QUAD_DESCRIPTOR;

    @Test
    void shriekRenderInvokesInheritedQuadMethodTwiceViaShriekOwner()
            throws IOException {
        List<String> owners = new ArrayList<>();
        try (InputStream bytes = open(SHRIEK + ".class")) {
            new ClassReader(bytes).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(
                                int access,
                                String name,
                                String descriptor,
                                String signature,
                                String[] exceptions
                        ) {
                            if (!"render".equals(name)
                                    || !"(Lcom/mojang/blaze3d/vertex/VertexConsumer;"
                                    .concat("Lnet/minecraft/client/Camera;F)V")
                                    .equals(descriptor)) {
                                return null;
                            }

                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitMethodInsn(
                                        int opcode,
                                        String owner,
                                        String name,
                                        String descriptor,
                                        boolean isInterface
                                ) {
                                    if (opcode == Opcodes.INVOKEVIRTUAL
                                            && "renderRotatedQuad".equals(name)
                                            && QUAD_DESCRIPTOR.equals(descriptor)) {
                                        owners.add("L" + owner
                                                + ";renderRotatedQuad"
                                                + descriptor);
                                    }
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
            );
        }
        assertEquals(
                List.of(EXPECTED, EXPECTED),
                owners,
                "Minecraft 1.21.1 ShriekParticle.render changed its invokes"
        );
    }

    @Test
    void shriekMixinModifyArgAnnotationTargetsExactInvoke()
            throws IOException {
        List<String> anchors = new ArrayList<>();
        try (InputStream bytes = open(
                "dev/planetary/mixin/"
                        + "ShriekParticleRenderGravityMixin.class"
        )) {
            new ClassReader(bytes).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(
                                int access,
                                String name,
                                String descriptor,
                                String signature,
                                String[] exceptions
                        ) {
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public AnnotationVisitor visitAnnotation(
                                        String descriptor,
                                        boolean visible
                                ) {
                                    if (!"Lorg/spongepowered/asm/mixin/"
                                            .concat("injection/ModifyArg;")
                                            .equals(descriptor)) {
                                        return null;
                                    }
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public AnnotationVisitor visitAnnotation(
                                                String name,
                                                String descriptor
                                        ) {
                                            if (!"at".equals(name)
                                                    || !"Lorg/spongepowered/asm/mixin/"
                                                    .concat("injection/At;")
                                                    .equals(descriptor)) {
                                                return null;
                                            }

                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                private String value;
                                                private String target;

                                                @Override
                                                public void visit(
                                                        String name,
                                                        Object entry
                                                ) {
                                                    if ("value".equals(name)) {
                                                        value = (String) entry;
                                                    }
                                                    if ("target".equals(name)) {
                                                        target = (String) entry;
                                                    }
                                                }

                                                @Override
                                                public void visitEnd() {
                                                    if ("INVOKE".equals(value)) {
                                                        anchors.add(target);
                                                    }
                                                }
                                            };
                                        }
                                    };
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_CODE
                            | ClassReader.SKIP_DEBUG
                            | ClassReader.SKIP_FRAMES
            );
        }
        assertEquals(List.of(EXPECTED), anchors);
    }

    private static InputStream open(String resource) {
        InputStream bytes = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resource);
        assertNotNull(bytes, "Missing bytecode " + resource);
        return bytes;
    }
}
