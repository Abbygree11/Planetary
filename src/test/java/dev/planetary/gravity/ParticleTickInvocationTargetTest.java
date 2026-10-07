package dev.planetary.gravity;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for the startup-blocking Mixin INVOKE mismatch.
 *
 * <p>Inherited Particle.move is invoked from each concrete tick method with
 * the concrete tick class as the bytecode invocation owner. This must agree
 * with the eight exact @At(target=...) descriptors in their respective mixins.
 * Compilation of a mixin alone does not verify the target bytecode.</p>
 */
final class ParticleTickInvocationTargetTest {
    private static final List<String> CUSTOM_TICK_OWNERS =
            List.of(
                    "DripParticle",
                    "WaterDropParticle",
                    "BubblePopParticle",
                    "WakeParticle",
                    "CampfireSmokeParticle",
                    "BubbleParticle",
                    "WaterCurrentDownParticle",
                    "DragonBreathParticle"
            );

    @Test
    void customTicksInvokeInheritedMoveWithTheirConcreteBytecodeOwner()
            throws IOException {
        for (String particleClass : CUSTOM_TICK_OWNERS) {
            String owner =
                    "net/minecraft/client/particle/" + particleClass;
            ArrayList<String> moveCallOwners =
                    new ArrayList<>();
            boolean[] tickFound = {false};

            try (InputStream bytes =
                         Thread.currentThread()
                                 .getContextClassLoader()
                                 .getResourceAsStream(owner + ".class")) {
                assertTrue(
                        bytes != null,
                        "Missing Minecraft 1.21.1 class " + owner
                );

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
                                if (!"tick".equals(name)
                                        || !"()V".equals(descriptor)) {
                                    return null;
                                }

                                tickFound[0] = true;
                                return new MethodVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            int opcode,
                                            String invokeOwner,
                                            String invokeName,
                                            String invokeDescriptor,
                                            boolean isInterface
                                    ) {
                                        if (opcode == Opcodes.INVOKEVIRTUAL
                                                && "move".equals(invokeName)
                                                && "(DDD)V".equals(
                                                        invokeDescriptor
                                                )) {
                                            moveCallOwners.add(invokeOwner);
                                        }
                                    }
                                };
                            }
                        },
                        ClassReader.SKIP_DEBUG
                                | ClassReader.SKIP_FRAMES
                );
            }

            assertTrue(tickFound[0], particleClass + " lacks tick()");
            assertEquals(
                    List.of(owner),
                    moveCallOwners,
                    particleClass + " changed its tick move invocation; "
                            + "review the matching Mixin @At target"
            );
        }
    }

    @Test
    void allEightBridgesRegisteredAndOldMultiTargetRemoved()
            throws IOException {
        try (InputStream bytes =
                     getClass().getClassLoader().getResourceAsStream(
                             "planetary.mixins.json"
                     )) {
            assertTrue(bytes != null, "Missing mixin registration JSON");
            String json = new String(
                    bytes.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            assertFalse(json.contains(
                    "\"SemanticTickDeltaParticleMixin\""
            ));
            for (String name : CUSTOM_TICK_OWNERS) {
                assertTrue(
                        json.contains("\"" + mixinNameFor(name) + "\""),
                        "Missing registered bridge " + mixinNameFor(name)
                );
            }
        }
    }

    /**
     * Avoid only testing the vanilla method owner: a Mixin can compile and be
     * registered while its actual @At(target) points to the WRONG owner.
     * Read class-file annotations without loading Mixin classes directly.
     */
    @Test
    void allEightMixinInvokeAnchorsMatchVanillaBytecode()
            throws IOException {
        for (String particleClass : CUSTOM_TICK_OWNERS) {
            String mixinName = mixinNameFor(particleClass);
            String resource =
                    "dev/planetary/mixin/" + mixinName + ".class";
            List<String> invokeTargets = new ArrayList<>();

            try (InputStream bytes =
                         getClass().getClassLoader()
                                 .getResourceAsStream(resource)) {
                assertTrue(bytes != null, "Missing Mixin bytecode " + resource);

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
                                            String annotationDescriptor,
                                            boolean visible
                                    ) {
                                        if (!"Lorg/spongepowered/asm/mixin/injection/Inject;"
                                                .equals(annotationDescriptor)) {
                                            return null;
                                        }

                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override
                                            public AnnotationVisitor visitArray(String name) {
                                                if (!"at".equals(name)) {
                                                    return null;
                                                }

                                                return new AnnotationVisitor(Opcodes.ASM9) {
                                                    @Override
                                                    public AnnotationVisitor visitAnnotation(
                                                            String ignored,
                                                            String descriptor
                                                    ) {
                                                        if (!"Lorg/spongepowered/asm/mixin/injection/At;"
                                                                .equals(descriptor)) {
                                                            return null;
                                                        }

                                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                                            private String atValue;
                                                            private String atTarget;

                                                            @Override
                                                            public void visit(String name, Object value) {
                                                                if ("value".equals(name)) {
                                                                    atValue = (String) value;
                                                                } else if ("target".equals(name)) {
                                                                    atTarget = (String) value;
                                                                }
                                                            }

                                                            @Override
                                                            public void visitEnd() {
                                                                if ("INVOKE".equals(atValue)) {
                                                                    invokeTargets.add(atTarget);
                                                                }
                                                            }
                                                        };
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

            assertEquals(
                    List.of(
                            "Lnet/minecraft/client/particle/"
                                    + particleClass + ";move(DDD)V"
                    ),
                    invokeTargets,
                    mixinName + " must bind concrete inherited move owner"
            );
        }
    }

    private static String mixinNameFor(String particleClass) {
        return switch (particleClass) {
            case "WaterCurrentDownParticle" ->
                    "WaterCurrentDownParticleGravityMixin";
            case "DragonBreathParticle" ->
                    "DragonBreathParticleGravityMixin";
            default -> particleClass + "TickDeltaMixin";
        };
    }
}
