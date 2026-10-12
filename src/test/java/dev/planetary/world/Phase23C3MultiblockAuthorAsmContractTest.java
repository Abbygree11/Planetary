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
 * Stage 2.3C-3: bytecode and compiled handler contracts of two-cell authors.
 * Exact Gradle-loaded 1.21.1/NeoForge 21.1.215 only. This never proves
 * that Mixins transform in a running client or that all Planet faces work.
 */
final class Phase23C3MultiblockAuthorAsmContractTest {
    private static final String ITEM = "net/minecraft/world/item/";
    private static final String BLOCK = "net/minecraft/world/level/block/";
    private static final String MIXIN = "dev/planetary/mixin/";
    private static final String BI = ITEM + "BlockItem";
    private static final String BED_ITEM = ITEM + "BedItem";
    private static final String DOUBLE_ITEM = ITEM + "DoubleHighBlockItem";
    private static final String BED = BLOCK + "BedBlock";
    private static final String PLANT = BLOCK + "DoublePlantBlock";
    private static final String DOOR = BLOCK + "DoorBlock";
    private static final String STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String DIR = "Lnet/minecraft/core/Direction;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String READER = "Lnet/minecraft/world/level/LevelReader;";
    private static final String ACCESS = "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String ENTITY = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String PLACE_CONTEXT =
            "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String CI =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String CIR =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String PLACEMENT = "(" + PLACE_CONTEXT + ")" + STATE;
    private static final String PLACE_BLOCK = "(" + PLACE_CONTEXT + STATE + ")Z";
    private static final String PLACED_BY =
            "(" + LEVEL + POS + STATE + ENTITY + STACK + ")V";
    private static final String SURVIVE =
            "(" + STATE + READER + POS + ")Z";
    private static final String UPDATE =
            "(" + STATE + DIR + STATE + ACCESS + POS + POS + ")" + STATE;
    private static final String SET_BLOCK = "(" + POS + STATE + "I)Z";
    private static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String INJECT =
            "Lorg/spongepowered/asm/mixin/injection/Inject;";

    private record Method(String name, String desc, int access) {}
    private record Call(int opcode, String owner, String name, String desc) {}

    @Test
    void itemOverridesHaveDifferentTwoCellWriteOwnership() throws IOException {
        assertEquals(BI, read(BED_ITEM).getSuperName());
        assertEquals(BI, read(DOUBLE_ITEM).getSuperName());
        assertTrue(declares(BED_ITEM, "placeBlock", PLACE_BLOCK));
        assertTrue(declares(DOUBLE_ITEM, "placeBlock", PLACE_BLOCK));

        List<Call> bed = calls(BED_ITEM, "placeBlock", PLACE_BLOCK);
        assertEquals(1, count(bed, "net/minecraft/world/level/Level",
                "setBlock", SET_BLOCK, Opcodes.INVOKEVIRTUAL));
        assertEquals(0, bed.stream().filter(c ->
                c.owner.equals(BI) && c.name.equals("placeBlock")).count(),
                "BedItem writes its foot itself, not BlockItem.super.placeBlock");

        List<Call> doubleItem = calls(DOUBLE_ITEM, "placeBlock", PLACE_BLOCK);
        assertEquals(1, count(doubleItem, "net/minecraft/core/BlockPos",
                "above", "()" + POS, Opcodes.INVOKEVIRTUAL));
        assertEquals(1, count(doubleItem, "net/minecraft/world/level/Level",
                "setBlock", SET_BLOCK, Opcodes.INVOKEVIRTUAL));
        assertEquals(1, count(doubleItem, BI, "placeBlock",
                PLACE_BLOCK, Opcodes.INVOKESPECIAL));
        assertTrue(indexOf(doubleItem, "setBlock") < indexOf(doubleItem, "placeBlock"),
                "Pre-clear of physical above() MUST precede lower-half placement");
    }

    @Test
    void bedSecondHalfIsAuthoredByBlockCallbackNotBedItem() throws IOException {
        assertTrue(declares(BED, "getStateForPlacement", PLACEMENT));
        assertTrue(declares(BED, "setPlacedBy", PLACED_BY));
        assertTrue(declares(BED, "updateShape", UPDATE));
        assertTrue(declares(BED, "newBlockEntity",
                "(" + POS + STATE + ")Lnet/minecraft/world/level/block/entity/BlockEntity;"));

        List<Call> head = calls(BED, "setPlacedBy", PLACED_BY);
        assertEquals(1, count(head, "net/minecraft/core/BlockPos", "relative",
                "(" + DIR + ")" + POS, Opcodes.INVOKEVIRTUAL));
        assertEquals(1, count(head, "net/minecraft/world/level/Level",
                "setBlock", SET_BLOCK, Opcodes.INVOKEVIRTUAL));
        assertTrue(has(head, "blockUpdated"));
        assertTrue(has(head, "updateNeighbourShapes"));
        assertTrue(has(calls(BED, "getStateForPlacement", PLACEMENT), "getHorizontalDirection"));
        assertTrue(has(calls(BED, "updateShape", UPDATE), "getNeighbourDirection"));
        assertTrue(declares(BED, "playerWillDestroy",
                "(" + LEVEL + POS + STATE +
                "Lnet/minecraft/world/entity/player/Player;)" + STATE),
                "Bed teardown is a second owner to inspect");
    }

    @Test
    void doublePlantOwnsUpperAuthorSurvivalAndNonItemPlaceAt() throws IOException {
        assertTrue(declares(PLANT, "setPlacedBy", PLACED_BY));
        assertTrue(declares(PLANT, "getStateForPlacement", PLACEMENT));
        assertTrue(declares(PLANT, "canSurvive", SURVIVE));
        assertTrue(declares(PLANT, "updateShape", UPDATE));
        String placeAt = "(" + ACCESS + STATE + POS + "I)V";
        assertTrue(declares(PLANT, "placeAt", placeAt));
        assertEquals(1, count(calls(PLANT, "setPlacedBy", PLACED_BY),
                "net/minecraft/core/BlockPos", "above",
                "()" + POS, Opcodes.INVOKEVIRTUAL));
        assertEquals(1, count(calls(PLANT, "setPlacedBy", PLACED_BY),
                "net/minecraft/world/level/Level", "setBlock",
                SET_BLOCK, Opcodes.INVOKEVIRTUAL));
        assertEquals(2, calls(PLANT, "placeAt", placeAt).stream()
                .filter(c -> c.name.equals("setBlock")).count(),
                "Nonitem worldgen/bonemeal direct author must write both halves");
        assertTrue(has(calls(PLANT, "setPlacedBy", PLACED_BY), "copyWaterloggedFrom"));
        assertTrue(has(calls(PLANT, "canSurvive", SURVIVE), "below"));
        assertTrue(has(calls(PLANT, "updateShape", UPDATE), "getAxis"));
    }

    @Test
    void doorIsIndependentMultiblockOwnerNotDoublePlantSubclass() throws IOException {
        assertFalse(read(DOOR).getSuperName().equals(PLANT));
        for (Method m : List.of(
                new Method("getStateForPlacement", PLACEMENT, 0),
                new Method("setPlacedBy", PLACED_BY, 0),
                new Method("updateShape", UPDATE, 0),
                new Method("canSurvive", SURVIVE, 0))) {
            assertTrue(declares(DOOR, m.name, m.desc),
                    "Door-specific algorithm must not be inferred from plant: " + m.name);
        }
        assertTrue(has(calls(DOOR, "setPlacedBy", PLACED_BY), "above"));
        assertTrue(has(calls(DOOR, "setPlacedBy", PLACED_BY), "setBlock"));
    }

    @Test
    void originalBlockItemLifecycleCallsPlacedByAfterStateComponents()
            throws IOException {
        String place = "(" + PLACE_CONTEXT + ")Lnet/minecraft/world/InteractionResult;";
        List<Call> pipeline = calls(BI, "place", place);
        assertTrue(indexOf(pipeline, "placeBlock")
                        < indexOf(pipeline, "updateBlockStateFromTag"));
        assertTrue(indexOf(pipeline, "updateBlockStateFromTag")
                        < indexOf(pipeline, "setPlacedBy"));
        assertTrue(has(pipeline, "updateCustomBlockEntityTag"));
        assertTrue(has(pipeline, "updateBlockEntityComponents"));
        // A synthetic dispenser or a mod bypassing BlockItem.place does NOT
        // inherit this lifecycle automatically.
    }

    @Test
    void registeredBedAndDoorMixinsHaveExactInjectHandlers()
            throws IOException {
        checkHeadInjection(MIXIN + "BedBlockGravityMixin",
                "planetary$localPlacement", PLACEMENT,
                "(" + PLACE_CONTEXT + CIR + ")V");
        checkHeadInjection(MIXIN + "BedBlockGravityMixin",
                "planetary$placeHead", PLACED_BY,
                "(" + LEVEL + POS + STATE + ENTITY + STACK + CI + ")V");
        checkHeadInjection(MIXIN + "BedBlockGravityMixin",
                "planetary$localPairUpdate", UPDATE,
                "(" + STATE + DIR + STATE + ACCESS + POS + POS + CIR + ")V");

        checkHeadInjection(MIXIN + "DoorBlockGravityMixin",
                "planetary$localPlacement", PLACEMENT,
                "(" + PLACE_CONTEXT + CIR + ")V");
        checkHeadInjection(MIXIN + "DoorBlockGravityMixin",
                "planetary$placeUpperHalf", PLACED_BY,
                "(" + LEVEL + POS + STATE + ENTITY + STACK + CI + ")V");
        checkHeadInjection(MIXIN + "DoorBlockGravityMixin",
                "planetary$localSurvival", SURVIVE,
                "(" + STATE + READER + POS + CIR + ")V");
        checkHeadInjection(MIXIN + "DoorBlockGravityMixin",
                "planetary$localPairUpdate", UPDATE,
                "(" + STATE + DIR + STATE + ACCESS + POS + POS + CIR + ")V");
    }

    private static void checkHeadInjection(String cls, String name,
            String targetDesc, String handlerDesc) throws IOException {
        Method method = methods(cls).stream()
                .filter(m -> m.name.equals(name) && m.desc.equals(handlerDesc))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Missing compiled Mixin handler: " + cls + "/" + name));
        assertEquals(0, method.access & Opcodes.ACC_STATIC,
                "HEAD instance injector unexpectedly static: " + name);
        List<String> selector = new ArrayList<>();
        List<String> anchors = new ArrayList<>();
        List<Boolean> cancellation = new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName,
                    String descriptor, String sig, String[] ex) {
                if (!methodName.equals(name) || !descriptor.equals(handlerDesc))
                    return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String type, boolean visible) {
                        if (!type.equals(INJECT)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public AnnotationVisitor visitArray(String key) {
                                if (!key.equals("method")) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visit(String name, Object value) {
                                        selector.add((String) value);
                                    }
                                };
                            }
                            @Override
                            public void visit(String key, Object value) {
                                if (key.equals("cancellable")) cancellation.add((Boolean)value);
                            }
                            @Override
                            public AnnotationVisitor visitAnnotation(String key, String desc) {
                                if (!key.equals("at") || !desc.equals(AT)) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String key, Object value) {
                                        if (key.equals("value")) anchors.add((String)value);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(List.of((name.equals("planetary$localPlacement") ? "getStateForPlacement" :
                 name.equals("planetary$placeHead") ||
                 name.equals("planetary$placeUpperHalf") ? "setPlacedBy" :
                 name.equals("planetary$localSurvival") ? "canSurvive" : "updateShape")
                + targetDesc), selector);
        assertEquals(List.of("HEAD"), anchors);
        assertEquals(List.of(Boolean.TRUE), cancellation);
    }

    private static boolean has(List<Call> calls, String method) {
        return calls.stream().anyMatch(c -> c.name.equals(method));
    }

    private static int indexOf(List<Call> calls, String method) {
        for (int i = 0; i < calls.size(); i++) {
            if (calls.get(i).name.equals(method)) return i;
        }
        return -1;
    }

    private static long count(List<Call> calls, String owner,
            String method, String desc, int opcode) {
        return calls.stream().filter(c -> c.owner.equals(owner) &&
                c.name.equals(method) && c.desc.equals(desc) &&
                c.opcode == opcode).count();
    }

    private static ClassReader read(String clazz) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(clazz + ".class")) {
            assertNotNull(in, "Missing compiled NeoForge/Mixin class: " + clazz);
            return new ClassReader(in);
        }
    }

    private static List<Method> methods(String cls) throws IOException {
        List<Method> all = new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                all.add(new Method(name, descriptor, access));
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return all;
    }

    private static boolean declares(String cls, String method, String desc)
            throws IOException {
        return methods(cls).stream()
                .anyMatch(m -> m.name.equals(method) && m.desc.equals(desc));
    }

    private static List<Call> calls(String cls, String method, String desc)
            throws IOException {
        List<Call> all = new ArrayList<>();
        boolean[] found = {false};
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                if (!name.equals(method) || !descriptor.equals(desc)) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner,
                            String invoked, String descriptor, boolean isInterface) {
                        all.add(new Call(opcode, owner, invoked, descriptor));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Absent owner: " + cls + "/" + method + desc);
        return all;
    }
}
