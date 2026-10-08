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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A version-sensitive contract for two AbstractCandleBlock iterable paths.
 * Never load the Mixin class directly: the NeoForge transformer reserves the
 * dev.planetary.mixin package.
 */
final class CandleParticleEmitterInvocationTest {
    private static final String VANILLA =
            "net/minecraft/world/level/block/AbstractCandleBlock";
    private static final String MIXIN =
            "dev/planetary/mixin/AbstractCandleParticleEmitterGravityMixin";
    private static final String FOR_EACH =
            "Ljava/lang/Iterable;forEach(Ljava/util/function/Consumer;)V";
    private static final String ANIMATE_ARGS =
            "(Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/util/RandomSource;)V";
    private static final String EXTINGUISH_ARGS =
            "(Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/LevelAccessor;"
                    + "Lnet/minecraft/core/BlockPos;)V";
    private static final String AT =
            "Lorg/spongepowered/asm/mixin/injection/At;";

    @Test
    void bothVanillaCandlePathsInvokeSameIterableForEachContract()
            throws IOException {
        List<String> actual = new ArrayList<>();
        try (InputStream bytes = open(VANILLA + ".class")) {
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
                            boolean animate = "animateTick".equals(name)
                                    && ANIMATE_ARGS.equals(descriptor);
                            boolean extinguish = "extinguish".equals(name)
                                    && EXTINGUISH_ARGS.equals(descriptor);
                            if (!animate && !extinguish) {
                                return null;
                            }

                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitMethodInsn(
                                        int opcode,
                                        String owner,
                                        String method,
                                        String descriptor,
                                        boolean isInterface
                                ) {
                                    if ("forEach".equals(method)) {
                                        assertEquals(Opcodes.INVOKEINTERFACE,
                                                opcode);
                                        actual.add(name + ":L" + owner
                                                + ";" + method + descriptor);
                                    }
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
            );
        }

        actual.sort(String::compareTo);
        assertEquals(
                List.of(
                        "animateTick:" + FOR_EACH,
                        "extinguish:" + FOR_EACH
                ),
                actual,
                "A candle source method or its Iterable.forEach owner drifted"
        );
    }

    @Test
    void compiledMixinTargetsBaseCandleAndBothRequiredHooks()
            throws IOException {
        List<String> targets = new ArrayList<>();
        List<String> anchors = new ArrayList<>();

        try (InputStream bytes = open(MIXIN + ".class")) {
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
                                public AnnotationVisitor visitArray(
                                        String name
                                ) {
                                    if (!"value".equals(name)) {
                                        return null;
                                    }
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public void visit(
                                                String name,
                                                Object value
                                        ) {
                                            if (value instanceof Type type) {
                                                targets.add(type.getInternalName());
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
                                    String expectedKind;
                                    if ("Lorg/spongepowered/asm/mixin/injection/"
                                            .concat("ModifyArg;")
                                            .equals(descriptor)) {
                                        expectedKind = "ModifyArg";
                                    } else if (
                                            "Lorg/spongepowered/asm/mixin/"
                                            .concat("injection/Redirect;")
                                            .equals(descriptor)
                                    ) {
                                        expectedKind = "Redirect";
                                    } else {
                                        return null;
                                    }

                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public AnnotationVisitor visitAnnotation(
                                                String name,
                                                String descriptor
                                        ) {
                                            if (!"at".equals(name)
                                                    || !AT.equals(descriptor)) {
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
                                                    } else if ("target".equals(name)) {
                                                        target = (String) entry;
                                                    }
                                                }

                                                @Override
                                                public void visitEnd() {
                                                    if ("INVOKE".equals(value)) {
                                                        anchors.add(
                                                                expectedKind + ":" + target
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

        assertEquals(List.of(VANILLA), targets);
        anchors.sort(String::compareTo);
        assertEquals(
                List.of(
                        "ModifyArg:" + FOR_EACH,
                        "Redirect:" + FOR_EACH
                ),
                anchors,
                "Compiled candle Mixin @At owners must match concrete bytecode"
        );

        try (InputStream bytes = open("planetary.mixins.json")) {
            String json = new String(
                    bytes.readAllBytes(), StandardCharsets.UTF_8
            );
            assertTrue(
                    json.contains(
                            "\"AbstractCandleParticleEmitterGravityMixin\""
                    ),
                    "Candle emitter must be registered as a client Mixin"
            );
            assertTrue(json.contains("\"defaultRequire\": 1"));
        }
    }

    private static InputStream open(String resource) {
        InputStream bytes = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resource);
        assertNotNull(bytes, "Missing resource: " + resource);
        return bytes;
    }
}
