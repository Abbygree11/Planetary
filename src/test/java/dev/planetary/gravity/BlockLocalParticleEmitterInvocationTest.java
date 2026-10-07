package dev.planetary.gravity;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Source-version integration contract: all seven concrete animateTick bodies
 * really invoke the Level.addParticle call intercepted by the family Mixin.
 * Also checks compiled Mixin annotations without loading a reserved-package
 * class through the NeoForge transforming ClassLoader.
 */
final class BlockLocalParticleEmitterInvocationTest {
    private static final String VANILLA_BLOCK_PACKAGE =
            "net/minecraft/world/level/block/";
    private static final List<String> BLOCKS = List.of(
            "FurnaceBlock",
            "BlastFurnaceBlock",
            "SmokerBlock",
            "BrewingStandBlock",
            "EndRodBlock",
            "RespawnAnchorBlock",
            "EnderChestBlock"
    );
    private static final String ADD_PARTICLE =
            "Lnet/minecraft/world/level/Level;addParticle("
                    + "Lnet/minecraft/core/particles/ParticleOptions;"
                    + "DDDDDD)V";
    private static final String ANIMATE_DESCRIPTOR =
            "(Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/util/RandomSource;)V";
    private static final String MIXIN_PATH =
            "dev/planetary/mixin/BlockLocalParticleEmitterGravityMixin.class";

    @Test
    void everyFamilyMemberDeclaresAndInvokesTheExactParticleTarget()
            throws IOException {
        for (String block : BLOCKS) {
            String resource = VANILLA_BLOCK_PACKAGE + block + ".class";
            List<String> targets = new ArrayList<>();
            boolean[] found = {false};

            try (InputStream bytes = open(resource)) {
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
                                if (!"animateTick".equals(name)
                                        || !ANIMATE_DESCRIPTOR.equals(descriptor)) {
                                    return null;
                                }

                                found[0] = true;
                                return new MethodVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            int opcode,
                                            String owner,
                                            String method,
                                            String descriptor,
                                            boolean isInterface
                                    ) {
                                        if ("addParticle".equals(method)) {
                                            targets.add("L" + owner
                                                    + ";" + method + descriptor);
                                        }
                                    }
                                };
                            }
                        },
                        ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
                );
            }

            assertTrue(found[0], block + " no longer declares animateTick");
            int expectedCalls = "FurnaceBlock".equals(block) ? 2 : 1;
            assertEquals(
                    java.util.Collections.nCopies(expectedCalls, ADD_PARTICLE),
                    targets,
                    block + ".animateTick particle call shape changed"
            );
        }
    }

    @Test
    void compiledMixinTargetsExactlyTheseSevenBlocksAndTheCorrectCall()
            throws IOException {
        Set<String> actualTargetTypes = new HashSet<>();
        List<String> actualInvokeAnchors = new ArrayList<>();

        try (InputStream bytes = open(MIXIN_PATH)) {
            new ClassReader(bytes).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(
                                String descriptor,
                                boolean visible
                        ) {
                            if (!"Lorg/spongepowered/asm/mixin/Mixin;"
                                    .equals(descriptor)) {
                                return null;
                            }

                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public AnnotationVisitor visitArray(String name) {
                                    if (!"value".equals(name)) {
                                        return null;
                                    }

                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof Type type) {
                                                actualTargetTypes.add(
                                                        type.getInternalName()
                                                );
                                            }
                                        }
                                    };
                                }
                            };
                        }

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
                                            .concat("injection/ModifyArgs;")
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
                                                    || !"Lorg/spongepowered/asm/"
                                                    .concat("mixin/injection/At;")
                                                    .equals(descriptor)) {
                                                return null;
                                            }

                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                String atValue;
                                                String atTarget;

                                                @Override
                                                public void visit(
                                                        String name,
                                                        Object value
                                                ) {
                                                    if ("value".equals(name)) {
                                                        atValue = (String) value;
                                                    } else if ("target".equals(name)) {
                                                        atTarget = (String) value;
                                                    }
                                                }

                                                @Override
                                                public void visitEnd() {
                                                    if ("INVOKE".equals(atValue)) {
                                                        actualInvokeAnchors.add(
                                                                atTarget
                                                        );
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

        Set<String> expectedTargets = new HashSet<>();
        for (String block : BLOCKS) {
            expectedTargets.add(VANILLA_BLOCK_PACKAGE + block);
        }

        assertEquals(expectedTargets, actualTargetTypes);
        assertEquals(List.of(ADD_PARTICLE), actualInvokeAnchors);

        try (InputStream bytes = open("planetary.mixins.json")) {
            String config = new String(
                    bytes.readAllBytes(), StandardCharsets.UTF_8
            );
            assertTrue(
                    config.contains("\"BlockLocalParticleEmitterGravityMixin\""),
                    "Missing registered emitter Mixin"
            );
        }
    }

    private static InputStream open(String resource) {
        InputStream bytes = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource);
        assertNotNull(bytes, "Missing class resource " + resource);
        return bytes;
    }
}
