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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Version-specific bytecode/handler checks for the first Phase-2 family
 * package. Never directly load reserved dev.planetary.mixin classes.
 */
final class Phase2PlacementFamilyMixinContractTest {
    private static final String VANILLA =
            "net/minecraft/world/level/block/";
    private static final String MIXINS = "dev/planetary/mixin/";
    private static final String STATE =
            "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String READER =
            "Lnet/minecraft/world/level/LevelReader;";
    private static final String ACCESSOR =
            "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String DIRECTION = "Lnet/minecraft/core/Direction;";
    private static final String CI =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String PLACE =
            "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String SURVIVE =
            "(" + STATE + READER + POS + ")Z";
    private static final String UPDATE =
            "(" + STATE + DIRECTION + STATE + ACCESSOR + POS
                    + POS + ")" + STATE;
    private static final String PLACE_METHOD = "(" + PLACE + ")" + STATE;
    private static final String BELOW =
            "Lnet/minecraft/core/BlockPos;below()" + POS;

    @Test
    void vanillaDeclaredMembersAndSupportInvokeShapesMatchAdapters()
            throws IOException {
        assertEquals(List.of(BELOW),
                invokes("BushBlock", "canSurvive", SURVIVE, "below"));
        assertEquals(List.of(BELOW),
                invokes("CakeBlock", "canSurvive", SURVIVE, "below"));
        assertEquals(List.of(BELOW),
                invokes("CandleCakeBlock", "canSurvive", SURVIVE, "below"));
        assertEquals(List.of(BELOW),
                invokes("CandleBlock", "canSurvive", SURVIVE, "below"));

        assertEquals(
                List.of("Lnet/minecraft/core/BlockPos;above()" + POS),
                invokes("SporeBlossomBlock", "canSurvive", SURVIVE, "above")
        );

        for (String block : List.of(
                "CandleBlock", "CakeBlock",
                "CandleCakeBlock", "SporeBlossomBlock")) {
            assertTrue(declares(block, "updateShape", UPDATE),
                    block + " must own the updateShape injection");
        }
        for (String block : List.of("EndRodBlock", "EnderChestBlock")) {
            assertTrue(declares(block, "getStateForPlacement", PLACE_METHOD),
                    block + " must declare its own placement handler");
        }
    }

    @Test
    void everyPhase2MixinHasExactCompiledCallbackDescriptors()
            throws IOException {
        Map<String, Map<String, String>> contracts = Map.of(
                "BushBlockLocalSupportMixin", Map.of(
                        "planetary$localSoilBelow",
                        "(" + POS + STATE + READER + POS + ")" + POS
                ),
                "CakeFamilyLocalSupportMixin", Map.of(
                        "planetary$localBelow",
                        "(" + POS + STATE + READER + POS + ")" + POS,
                        "planetary$physicalSupportNeighbor",
                        "(" + UPDATE.substring(1, UPDATE.indexOf(')'))
                                + CI + ")V"
                ),
                "CandleBlockLocalSupportMixin", Map.of(
                        "planetary$localCandleSupport",
                        "(" + STATE + READER + POS + CI + ")V",
                        "planetary$dropOnPhysicalSupportRemoval",
                        "(" + UPDATE.substring(1, UPDATE.indexOf(')'))
                                + CI + ")V"
                ),
                "SporeBlossomLocalSupportMixin", Map.of(
                        "planetary$localCeilingSupport",
                        "(" + STATE + READER + POS + CI + ")V",
                        "planetary$physicalCeilingNeighbor",
                        "(" + UPDATE.substring(1, UPDATE.indexOf(')'))
                                + CI + ")V"
                ),
                "EndRodLocalPlacementMixin", Map.of(
                        "planetary$localRodDirection",
                        "(" + PLACE + CI + ")V"
                ),
                "EnderChestLocalPlacementMixin", Map.of(
                        "planetary$localEnderChestFacing",
                        "(" + PLACE + CI + ")V"
                )
        );

        for (Map.Entry<String, Map<String, String>> mixin :
                contracts.entrySet()) {
            Map<String, String> actual = new java.util.HashMap<>();
            try (InputStream stream = open(
                    MIXINS + mixin.getKey() + ".class")) {
                new ClassReader(stream).accept(
                        new ClassVisitor(Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    int access, String name,
                                    String descriptor, String signature,
                                    String[] exceptions
                            ) {
                                if (name.startsWith("planetary$")) {
                                    actual.put(name, descriptor);
                                    assertEquals(
                                            0,
                                            access & Opcodes.ACC_STATIC,
                                            "Expected instance injection in "
                                                    + mixin.getKey() + "/" + name
                                    );
                                }
                                return null;
                            }
                        },
                        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                                | ClassReader.SKIP_FRAMES
                );
            }
            assertEquals(mixin.getValue(), actual, mixin.getKey());
        }

        try (InputStream stream = open("planetary.mixins.json")) {
            String json = new String(
                    stream.readAllBytes(), StandardCharsets.UTF_8
            );
            for (String name : contracts.keySet()) {
                assertTrue(json.contains("\"" + name + "\""),
                        "Missing " + name + " in mixin configuration");
            }
            assertTrue(json.contains("\"defaultRequire\": 1"));
        }
    }

    @Test
    void stairOwnerHasBothExactNeoForgeMethodsAndReturnOnlyHooks() throws IOException {
        assertTrue(declares("StairBlock", "getStateForPlacement", PLACE_METHOD));
        assertTrue(declares("StairBlock", "updateShape", UPDATE));
        assertTrue(declares("StairBlock", "getStairsShape",
                "(" + STATE + "Lnet/minecraft/world/level/BlockGetter;" + POS
                        + ")Lnet/minecraft/world/level/block/state/properties/StairsShape;"));

        Map<String, String> expected = Map.of(
                "planetary$canonicalStairPlacement", "(" + PLACE + CI + ")V",
                "planetary$canonicalStairNeighbor",
                "(" + STATE + DIRECTION + STATE + ACCESSOR + POS + POS + CI + ")V"
        );
        Map<String, String> actual = new java.util.HashMap<>();
        try (InputStream in = open(MIXINS + "StairBlockLocalPlacementMixin.class")) {
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                        String sig, String[] exceptions) {
                    if (!name.startsWith("planetary$canonicalStair")) {
                        return null;
                    }
                    actual.put(name, desc);
                    assertEquals(0, access & Opcodes.ACC_STATIC);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                            if (!"Lorg/spongepowered/asm/mixin/injection/Inject;".equals(annotation)) {
                                return null;
                            }
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override public void visit(String key, Object value) {
                                    if ("cancellable".equals(key)) {
                                        assertEquals(Boolean.TRUE, value);
                                    }
                                }
                                @Override public AnnotationVisitor visitArray(String key) {
                                    if ("method".equals(key)) return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public void visit(String unused, Object value) {
                                            assertTrue(value.equals("getStateForPlacement" + PLACE_METHOD)
                                                    || value.equals("updateShape" + UPDATE));
                                        }
                                    };
                                    if ("at".equals(key)) return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public AnnotationVisitor visitAnnotation(String unused,
                                                String desc) {
                                            if (!"Lorg/spongepowered/asm/mixin/injection/At;".equals(desc)) return null;
                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override public void visit(String k, Object value) {
                                                    if ("value".equals(k)) assertEquals("RETURN", value);
                                                }
                                            };
                                        }
                                    };
                                    return null;
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertEquals(expected, actual);
        try (InputStream input = open("planetary.mixins.json")) {
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"StairBlockLocalPlacementMixin\""));
        }
    }

    @Test
    void fenceGateFullOwnerAndInteractionCallsiteMatchCompiledNeoForgeAbi() throws IOException {
        final String gateOwner = "FenceGateBlock";
        assertTrue(declares(gateOwner, "getStateForPlacement", PLACE_METHOD));
        assertTrue(declares(gateOwner, "updateShape", UPDATE));
        String interact = "(" + STATE
                + "Lnet/minecraft/world/level/Level;" + POS
                + "Lnet/minecraft/world/entity/player/Player;"
                + "Lnet/minecraft/world/phys/BlockHitResult;)"
                + "Lnet/minecraft/world/InteractionResult;";
        assertTrue(declares(gateOwner, "useWithoutItem", interact));

        // The Java receiver is a Player, NOT the Entity defining its
        // inherited getDirection; inspect the actual 1.21.1 invoke owner.
        assertEquals(List.of(
                "Lnet/minecraft/world/entity/player/Player;getDirection()" + DIRECTION),
                invokes(gateOwner, "useWithoutItem", interact, "getDirection"));

        Map<String, String> contract = Map.of(
                "planetary$gatePlacement", "(" + PLACE + CI + ")V",
                "planetary$gateWallUpdate",
                "(" + STATE + DIRECTION + STATE + ACCESSOR + POS + POS + CI + ")V",
                "planetary$gateInteractionHeading",
                "(Lnet/minecraft/world/entity/player/Player;" + STATE
                        + "Lnet/minecraft/world/level/Level;" + POS
                        + "Lnet/minecraft/world/entity/player/Player;"
                        + "Lnet/minecraft/world/phys/BlockHitResult;)" + DIRECTION
        );
        java.util.Map<String, String> actual = new java.util.HashMap<>();
        try (InputStream stream = open(MIXINS + "FenceGateBlockLocalSemanticsMixin.class")) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String name,
                        String desc, String signature, String[] exceptions) {
                    if (!name.startsWith("planetary$gate")) return null;
                    assertEquals(0, access & Opcodes.ACC_STATIC);
                    actual.put(name, desc);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public AnnotationVisitor visitAnnotation(String ann, boolean visible) {
                            if ("Lorg/spongepowered/asm/mixin/injection/Inject;".equals(ann)) {
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String key, Object value) {
                                        if ("cancellable".equals(key)) assertEquals(Boolean.TRUE, value);
                                    }
                                    @Override public AnnotationVisitor visitArray(String key) {
                                        if ("at".equals(key)) return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public AnnotationVisitor visitAnnotation(String ignored, String type) {
                                                if (!"Lorg/spongepowered/asm/mixin/injection/At;".equals(type)) return null;
                                                return new AnnotationVisitor(Opcodes.ASM9) {
                                                    @Override public void visit(String k, Object val) {
                                                        if ("value".equals(k)) assertEquals("RETURN", val);
                                                    }
                                                };
                                            }
                                        };
                                        if ("method".equals(key)) return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public void visit(String ignored, Object val) {
                                                assertTrue(("getStateForPlacement" + PLACE_METHOD).equals(val)
                                                        || ("updateShape" + UPDATE).equals(val));
                                            }
                                        };
                                        return null;
                                    }
                                };
                            }
                            if ("Lorg/spongepowered/asm/mixin/injection/Redirect;".equals(ann)) {
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public AnnotationVisitor visitAnnotation(String key, String type) {
                                        if (!"at".equals(key)) return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public void visit(String k, Object val) {
                                                if ("value".equals(k)) assertEquals("INVOKE", val);
                                                if ("target".equals(k)) assertEquals(
                                                        "Lnet/minecraft/world/entity/player/Player;getDirection()"
                                                                + DIRECTION, val);
                                            }
                                        };
                                    }
                                    @Override public AnnotationVisitor visitArray(String key) {
                                        if (!"method".equals(key)) return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public void visit(String ignored,Object value) {
                                                assertEquals("useWithoutItem" + interact,value);
                                            }
                                        };
                                    }
                                };
                            }
                            return null;
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        }
        assertEquals(contract, actual);
        try (InputStream stream = open("planetary.mixins.json")) {
            assertTrue(new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .contains("\"FenceGateBlockLocalSemanticsMixin\""));
        }
    }

    @Test
    void abstractFurnaceSharedPlacementOwnerAndThinReturnAdapterMatchNeoForge() throws IOException {
        // One inherited owner, no duplicate patches for each material.
        assertTrue(declares("AbstractFurnaceBlock", "getStateForPlacement", PLACE_METHOD));
        for (String subclass : List.of("FurnaceBlock", "SmokerBlock", "BlastFurnaceBlock")) {
            assertTrue(!declares(subclass, "getStateForPlacement", PLACE_METHOD),
                    subclass + " must inherit common placement owner");
        }

        java.util.Map<String, String> actual = new java.util.HashMap<>();
        boolean[] mixinTargetSeen = {false};
        boolean[] injectReturn = {false};
        boolean[] cancellable = {false};
        try (InputStream stream = open(MIXINS + "AbstractFurnaceBlockLocalPlacementMixin.class")) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public AnnotationVisitor visitAnnotation(String name, boolean visible) {
                    if (!"Lorg/spongepowered/asm/mixin/Mixin;".equals(name)) return null;
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override public AnnotationVisitor visitArray(String key) {
                            if (!"value".equals(key)) return null;
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override public void visit(String k, Object value) {
                                    if (value instanceof org.objectweb.asm.Type t
                                            && t.getInternalName().equals(VANILLA + "AbstractFurnaceBlock")) {
                                        mixinTargetSeen[0] = true;
                                    }
                                }
                            };
                        }
                    };
                }
                @Override public MethodVisitor visitMethod(
                        int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if (!name.equals("planetary$canonicalFurnacePlacement")) return null;
                    assertEquals(0, access & Opcodes.ACC_STATIC);
                    actual.put(name, descriptor);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                            if (!"Lorg/spongepowered/asm/mixin/injection/Inject;".equals(annotation)) {
                                return null;
                            }
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override public void visit(String key, Object value) {
                                    if ("cancellable".equals(key)) {
                                        assertEquals(Boolean.TRUE, value);
                                        cancellable[0] = true;
                                    }
                                }
                                @Override public AnnotationVisitor visitArray(String key) {
                                    if ("method".equals(key)) return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public void visit(String k, Object value) {
                                            assertEquals("getStateForPlacement" + PLACE_METHOD, value);
                                        }
                                    };
                                    if ("at".equals(key)) return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public AnnotationVisitor visitAnnotation(String k, String type) {
                                            if (!"Lorg/spongepowered/asm/mixin/injection/At;".equals(type)) return null;
                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override public void visit(String k, Object value) {
                                                    if ("value".equals(k)) {
                                                        assertEquals("RETURN", value);
                                                        injectReturn[0] = true;
                                                    }
                                                }
                                            };
                                        }
                                    };
                                    return null;
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertEquals(Map.of("planetary$canonicalFurnacePlacement", "(" + PLACE + CI + ")V"), actual);
        assertTrue(mixinTargetSeen[0]);
        assertTrue(injectReturn[0]);
        assertTrue(cancellable[0]);
        try (InputStream stream = open("planetary.mixins.json")) {
            assertTrue(new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .contains("\"AbstractFurnaceBlockLocalPlacementMixin\""));
        }
    }

    private static boolean declares(String block, String method,
                                    String descriptor) throws IOException {
        boolean[] found = {false};
        try (InputStream stream = open(VANILLA + block + ".class")) {
            new ClassReader(stream).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(
                                int access, String name, String desc,
                                String signature, String[] exceptions
                        ) {
                            if (name.equals(method) && desc.equals(descriptor)) {
                                found[0] = true;
                            }
                            return null;
                        }
                    },
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                            | ClassReader.SKIP_FRAMES
            );
        }
        return found[0];
    }

    private static List<String> invokes(
            String block, String method, String descriptor, String invoked
    ) throws IOException {
        List<String> result = new ArrayList<>();
        boolean[] found = {false};
        try (InputStream stream = open(VANILLA + block + ".class")) {
            new ClassReader(stream).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(
                                int access, String name, String desc,
                                String signature, String[] exceptions
                        ) {
                            if (!name.equals(method)
                                    || !desc.equals(descriptor)) {
                                return null;
                            }
                            found[0] = true;
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitMethodInsn(
                                        int opcode, String owner,
                                        String called, String targetDesc,
                                        boolean isInterface
                                ) {
                                    if (called.equals(invoked)) {
                                        result.add("L" + owner + ";"
                                                + called + targetDesc);
                                    }
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
            );
        }
        assertTrue(found[0], block + "::" + method + " missing");
        return result;
    }

    private static InputStream open(String path) {
        InputStream result = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(path);
        assertNotNull(result, "Missing class " + path);
        return result;
    }
}
