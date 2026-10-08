package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prevents silent source drift and earlier mixin bootstrap crashes by checking
 * BOTH actual 1.21.1 vanilla INVOKE owners and exact compiled injector
 * callback descriptors. Never directly load reserved Mixin package classes.
 */
final class VolumeParticleMixinContractTest {
    private static final String BLOCKS =
            "net/minecraft/world/level/block/";
    private static final String MIXINS = "dev/planetary/mixin/";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String STATE =
            "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String RANDOM = "Lnet/minecraft/util/RandomSource;";
    private static final String ADD_PARTICLE =
            "Lnet/minecraft/world/level/Level;addParticle("
                    + "Lnet/minecraft/core/particles/ParticleOptions;"
                    + "DDDDDD)V";
    private static final String ANIMATE =
            "(" + STATE + LEVEL + POS + RANDOM + ")V";
    private static final String SHELF = "(" + LEVEL + POS + POS + ")Z";
    private static final String OFFSET_BLOCK =
            "Lnet/minecraft/core/BlockPos;offset(" +
                    "Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;";
    private static final String OFFSET_COORDS =
            "Lnet/minecraft/core/BlockPos;offset(III)"
                    + "Lnet/minecraft/core/BlockPos;";

    @Test
    void vanillaBookshelfPredicateAndParticleBodiesPreserveTargets()
            throws IOException {
        assertEquals(
                List.of(OFFSET_BLOCK, OFFSET_BLOCK, OFFSET_COORDS),
                callsites(BLOCKS + "EnchantingTableBlock.class",
                        "isValidBookShelf", SHELF, "offset")
        );
        assertEquals(
                List.of(ADD_PARTICLE),
                callsites(BLOCKS + "EnchantingTableBlock.class",
                        "animateTick", ANIMATE, "addParticle")
        );
        assertEquals(
                List.of(ADD_PARTICLE, ADD_PARTICLE),
                callsites(BLOCKS + "SporeBlossomBlock.class",
                        "animateTick", ANIMATE, "addParticle")
        );
    }

    @Test
    void allFourVolumeMixinHandlersHaveExactSignatureAndAtContract()
            throws IOException {
        Map<String, Hook> bookshelf = hooks(
                MIXINS + "EnchantingTableBookshelfGravityMixin.class"
        );
        assertEquals(
                Map.of(
                        "planetary$projectBookshelfProvider",
                        new Hook(
                                "(" + POS + "Lnet/minecraft/core/Vec3i;"
                                        + LEVEL + POS + POS + ")" + POS,
                                true, "Redirect", "isValidBookShelf",
                                "INVOKE", OFFSET_BLOCK, false
                        ),
                        "planetary$projectBookshelfTransmitter",
                        new Hook(
                                "(" + POS + "III" + LEVEL + POS
                                        + POS + ")" + POS,
                                true, "Redirect", "isValidBookShelf",
                                "INVOKE", OFFSET_COORDS, false
                        )
                ),
                bookshelf
        );

        Map<String, Hook> enchant = hooks(
                MIXINS + "EnchantingTableParticleGravityMixin.class"
        );
        assertEquals(
                Map.of(
                        "planetary$reframeBookshelfEnchantParticle",
                        new Hook(
                                "(Lorg/spongepowered/asm/mixin/injection/"
                                        + "invoke/arg/Args;" + STATE + LEVEL
                                        + POS + RANDOM + ")V",
                                false, "ModifyArgs", "animateTick",
                                "INVOKE", ADD_PARTICLE, false
                        )
                ),
                enchant
        );

        Map<String, Hook> spore = hooks(
                MIXINS + "SporeBlossomParticleGravityMixin.class"
        );
        assertEquals(
                Map.of(
                        "planetary$emitLocalSporeVolume",
                        new Hook(
                                "(" + STATE + LEVEL + POS + RANDOM
                                        + "Lorg/spongepowered/asm/mixin/"
                                        + "injection/callback/CallbackInfo;)V",
                                false, "Inject", "animateTick",
                                "HEAD", null, true
                        )
                ),
                spore
        );
    }

    @Test
    void bothClientAndCommonMixinRegistrationsArePresent() throws IOException {
        String config;
        try (InputStream in = open("planetary.mixins.json")) {
            config = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(config.contains(
                "\"EnchantingTableBookshelfGravityMixin\""));
        assertTrue(config.contains(
                "\"EnchantingTableParticleGravityMixin\""));
        assertTrue(config.contains(
                "\"SporeBlossomParticleGravityMixin\""));
        assertTrue(config.contains("\"defaultRequire\": 1"));
    }

    private static List<String> callsites(
            String classResource,
            String targetName,
            String targetDescriptor,
            String invokedName
    ) throws IOException {
        List<String> calls = new ArrayList<>();
        boolean[] found = {false};

        try (InputStream in = open(classResource)) {
            new ClassReader(in).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(
                                int access, String name, String descriptor,
                                String signature, String[] exceptions
                        ) {
                            if (!targetName.equals(name)
                                    || !targetDescriptor.equals(descriptor)) {
                                return null;
                            }
                            found[0] = true;
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitMethodInsn(
                                        int opcode, String owner,
                                        String method, String descriptor,
                                        boolean isInterface
                                ) {
                                    if (invokedName.equals(method)) {
                                        calls.add("L" + owner + ";" + method
                                                + descriptor);
                                    }
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
            );
        }

        assertTrue(found[0], "Vanilla method missing "
                + classResource + "::" + targetName + targetDescriptor);
        return calls;
    }

    private record Hook(
            String handlerDescriptor,
            boolean isStatic,
            String kind,
            String targetMethod,
            String atValue,
            String atTarget,
            boolean cancellable
    ) {
    }

    private static Map<String, Hook> hooks(String classResource)
            throws IOException {
        Map<String, Hook> result = new HashMap<>();

        try (InputStream in = open(classResource)) {
            new ClassReader(in).accept(
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
                                        String annotationDesc, boolean visible
                                ) {
                                    String kind;
                                    String prefix =
                                            "Lorg/spongepowered/asm/"
                                                    + "mixin/injection/";
                                    if ((prefix + "Inject;").equals(
                                            annotationDesc)) {
                                        kind = "Inject";
                                    } else if ((prefix + "Redirect;").equals(
                                            annotationDesc)) {
                                        kind = "Redirect";
                                    } else if ((prefix + "ModifyArgs;").equals(
                                            annotationDesc)) {
                                        kind = "ModifyArgs";
                                    } else {
                                        return null;
                                    }

                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        // Mixin @Redirect/@Inject/@ModifyArgs
                                        // method() is declared as String[],
                                        // so javac encodes even one name as an
                                        // annotation array, not a scalar.
                                        private final List<String> methods =
                                                new ArrayList<>();
                                        private String atValue;
                                        private String atTarget;
                                        private boolean cancellable;

                                        @Override
                                        public void visit(String field,
                                                Object value) {
                                            if ("method".equals(field)) {
                                                // Defensive support for tools
                                                // that emit scalar annotation
                                                // values instead of arrays.
                                                methods.add((String) value);
                                            } else if ("cancellable".equals(field)) {
                                                cancellable = (Boolean) value;
                                            }
                                        }

                                        @Override
                                        public AnnotationVisitor visitArray(
                                                String field
                                        ) {
                                            if (!"method".equals(field)) {
                                                return null;
                                            }
                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override
                                                public void visit(
                                                        String ignored,
                                                        Object value
                                                ) {
                                                    methods.add((String) value);
                                                }
                                            };
                                        }

                                        @Override
                                        public AnnotationVisitor visitAnnotation(
                                                String field, String atDescriptor
                                        ) {
                                            if (!"at".equals(field)
                                                    || !(prefix + "At;")
                                                    .equals(atDescriptor)) {
                                                return null;
                                            }

                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override
                                                public void visit(
                                                        String field,
                                                        Object value
                                                ) {
                                                    if ("value".equals(field)) {
                                                        atValue = (String) value;
                                                    } else if (
                                                            "target".equals(field)) {
                                                        atTarget = (String) value;
                                                    }
                                                }
                                            };
                                        }

                                        @Override
                                        public void visitEnd() {
                                            result.put(name, new Hook(
                                                    descriptor,
                                                    (access & Opcodes.ACC_STATIC)
                                                            != 0,
                                                    kind,
                                                    methods.size() == 1
                                                            ? methods.get(0)
                                                            : methods.toString(),
                                                    atValue,
                                                    atTarget, cancellable
                                            ));
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

        return result;
    }

    private static InputStream open(String resource) {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource);
        assertNotNull(in, "Missing resource " + resource);
        return in;
    }
}
