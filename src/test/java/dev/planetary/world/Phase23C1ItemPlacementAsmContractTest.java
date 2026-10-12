package dev.planetary.world;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage 2.3C-1: exact classpath bytecode contracts, not gameplay acceptance.
 * Never Class.forName() a class in the reserved Mixin package.
 */
final class Phase23C1ItemPlacementAsmContractTest {
    private static final String ITEM = "net/minecraft/world/item/";
    private static final String MIXIN = "dev/planetary/mixin/";
    private static final String BLOCK_ITEM = ITEM + "BlockItem";
    private static final String STANDING = ITEM + "StandingAndWallBlockItem";
    private static final String CONTEXT =
            "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String USE_CONTEXT =
            "Lnet/minecraft/world/item/context/UseOnContext;";
    private static final String STATE =
            "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String PLAYER =
            "Lnet/minecraft/world/entity/player/Player;";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String RESULT =
            "Lnet/minecraft/world/InteractionResult;";
    private static final String SOUND =
            "Lnet/minecraft/sounds/SoundEvent;";
    private static final String DIRECTIONS =
            "[Lnet/minecraft/core/Direction;";
    private static final String PLACE = "(" + CONTEXT + ")" + RESULT;
    private static final String PLACEMENT = "(" + CONTEXT + ")" + STATE;
    private static final String REGISTER = "(Ljava/util/Map;Lnet/minecraft/world/item/Item;)V";
    private static final String REDIRECT =
            "Lorg/spongepowered/asm/mixin/injection/Redirect;";
    private static final String AT =
            "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String INVOKER =
            "Lorg/spongepowered/asm/mixin/gen/Invoker;";

    @Test
    void blockItemHasRealSharedVirtualPlacementPipeline() throws IOException {
        assertTrue(declares(BLOCK_ITEM, "useOn", "(" + USE_CONTEXT + ")" + RESULT));
        assertTrue(declares(BLOCK_ITEM, "place", PLACE));
        assertTrue(declares(BLOCK_ITEM, "updatePlacementContext",
                "(" + CONTEXT + ")" + CONTEXT));
        assertTrue(declares(BLOCK_ITEM, "getPlacementState", PLACEMENT));
        assertTrue(declares(BLOCK_ITEM, "placeBlock",
                "(" + CONTEXT + STATE + ")Z"));

        List<Call> calls = calls(BLOCK_ITEM, "place", PLACE);
        for (String name : List.of("updatePlacementContext",
                "getPlacementState", "placeBlock")) {
            assertEquals(1L, calls.stream()
                    .filter(c -> c.owner.equals(BLOCK_ITEM) && c.name.equals(name)
                            && c.opcode == Opcodes.INVOKEVIRTUAL)
                    .count(), "Unexpected BlockItem.place virtual call: " + name);
        }

        // State/components/BE/placement notification must not be lost by a
        // copied/reimplemented generic placement method.
        for (String name : List.of("updateBlockStateFromTag",
                "updateCustomBlockEntityTag", "updateBlockEntityComponents",
                "setPlacedBy", "gameEvent")) {
            assertTrue(calls.stream().anyMatch(c -> c.name.equals(name)),
                    "Post-placement lifecycle call disappeared: " + name);
        }
    }

    @Test
    void exactNeoForgeSensitiveSoundExtensionIsPresent() throws IOException {
        // This is a *test of the real Gradle-loaded 21.1.215 class*,
        // not an assertion that a later official git patch is identical.
        String sensitive = "(" + STATE + LEVEL + POS + PLAYER + ")" + SOUND;
        assertTrue(declares(BLOCK_ITEM, "getPlaceSound", sensitive),
                "NeoForge sensitive place-sound method absent/change of ABI");
        assertTrue(calls(BLOCK_ITEM, "place", PLACE).stream()
                        .anyMatch(c -> c.owner.equals(BLOCK_ITEM)
                                && c.name.equals("getPlaceSound")
                                && c.desc.equals(sensitive)),
                "BlockItem.place no longer invokes sensitive sound hook");
    }

    @Test
    void standingWallOwnerAndSingleCallsiteMatchRedirect() throws IOException {
        assertEquals(BLOCK_ITEM, superName(STANDING));
        assertTrue(declares(STANDING, "getPlacementState", PLACEMENT));
        assertTrue(declares(STANDING, "registerBlocks", REGISTER));
        String nearest = "()" + DIRECTIONS;
        List<Call> matches = calls(STANDING, "getPlacementState", PLACEMENT)
                .stream()
                .filter(c -> c.name.equals("getNearestLookingDirections"))
                .toList();
        assertEquals(List.of(new Call(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/item/context/BlockPlaceContext",
                        "getNearestLookingDirections", nearest)),
                matches, "Exact Mixin @At JVM owner/descriptor/count changed");
    }

    @Test
    void anonymousNeoForgeRegistrationMustBeCheckedSeparately()
            throws IOException {
        // 2.3B original census: big_dripleaf and cauldron are Items$1.
        String anonymous = ITEM + "Items$1";
        assertEquals(BLOCK_ITEM, superName(anonymous));
        assertTrue(declares(anonymous, "registerBlocks", REGISTER));
        // Secondary block-to-item entries are registry bookkeeping, not
        // additional BlockItem.place target candidates.
        assertFalse(calls(anonymous, "registerBlocks", REGISTER).isEmpty());
    }

    @Test
    void compiledMixinHandlerAndAnnotationExactlyFitInvoke() throws IOException {
        String handler = "planetary$localVariantOrder";
        String desc = "(" + CONTEXT + ")" + DIRECTIONS;
        String mixin = MIXIN + "StandingAndWallBlockItemMixin";
        assertTrue(declares(mixin, handler, desc));
        assertEquals(0, access(mixin, handler, desc) & Opcodes.ACC_STATIC,
                "Instance INVOKE redirect needs a non-static receiver handler");

        List<String> methods = new ArrayList<>();
        List<String> atValues = new ArrayList<>();
        List<String> targets = new ArrayList<>();
        read(mixin).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int flags, String name,
                    String descriptor, String generic, String[] exceptions) {
                if (!name.equals(handler) || !descriptor.equals(desc)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(
                            String annotation, boolean visible) {
                        if (!annotation.equals(REDIRECT)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public AnnotationVisitor visitArray(String key) {
                                if (!key.equals("method")) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visit(String ignored, Object value) {
                                        methods.add((String) value);
                                    }
                                };
                            }

                            @Override
                            public AnnotationVisitor visitAnnotation(
                                    String key, String annotationType) {
                                if (!key.equals("at") || !annotationType.equals(AT))
                                    return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visit(String key, Object value) {
                                        if (key.equals("value")) atValues.add((String) value);
                                        if (key.equals("target")) targets.add((String) value);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertEquals(List.of("getPlacementState" + PLACEMENT), methods);
        assertEquals(List.of("INVOKE"), atValues);
        assertEquals(List.of("Lnet/minecraft/world/item/context/"
                + "BlockPlaceContext;getNearestLookingDirections()" + DIRECTIONS),
                targets);

        // The @Invoker is a second, independent declaration contract.
        String accessor = MIXIN + "StandingAndWallBlockItemAccessor";
        assertTrue(declares(accessor,
                "planetary$invokeGetPlacementState", PLACEMENT));
        List<String> invokers = new ArrayList<>();
        read(accessor).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int flags, String name,
                    String descriptor, String signature, String[] exceptions) {
                if (!name.equals("planetary$invokeGetPlacementState")
                        || !descriptor.equals(PLACEMENT)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(
                            String type, boolean visible) {
                        if (!type.equals(INVOKER)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String key, Object value) {
                                if (key.equals("value")) invokers.add((String) value);
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(List.of("getPlacementState"), invokers);
    }

    private record Call(int opcode, String owner, String name, String desc) {}

    private static ClassReader read(String className) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(className + ".class")) {
            assertNotNull(in, "Missing exact classpath bytecode: " + className);
            return new ClassReader(in);
        }
    }

    private static String superName(String type) throws IOException {
        return read(type).getSuperName();
    }

    private static boolean declares(String type, String method, String desc)
            throws IOException {
        return access(type, method, desc) != -1;
    }

    private static int access(String type, String method, String desc)
            throws IOException {
        int[] found = {-1};
        read(type).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int flags, String name,
                    String descriptor, String signature, String[] exceptions) {
                if (name.equals(method) && descriptor.equals(desc))
                    found[0] = flags;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return found[0];
    }

    private static List<Call> calls(String type, String method, String desc)
            throws IOException {
        List<Call> result = new ArrayList<>();
        boolean[] found = {false};
        read(type).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int flags, String name,
                    String descriptor, String signature, String[] exceptions) {
                if (!name.equals(method) || !descriptor.equals(desc)) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int op, String owner,
                            String target, String signature, boolean isInterface) {
                        result.add(new Call(op, owner, target, signature));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], type + "::" + method + desc + " not declared");
        return result;
    }
}
