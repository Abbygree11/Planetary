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
 * Phase 2.3C-4: real NeoForge 21.1.215 classpath owner + compiled Planetary
 * Mixin contracts. Bytecode PASS is NOT woven runtime/gameplay acceptance.
 */
final class Phase23C4NeighborLifecycleAsmContractTest {
    private static final String BLOCK = "net/minecraft/world/level/block/Block";
    private static final String STATE_BASE =
            "net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String LEVEL = "net/minecraft/world/level/Level";
    private static final String SERVER = "net/minecraft/server/level/ServerLevel";
    private static final String NEIGHBOR = "net/minecraft/world/level/redstone/NeighborUpdater";
    private static final String INSTANT = "net/minecraft/world/level/redstone/InstantNeighborUpdater";
    private static final String COLLECT = "net/minecraft/world/level/redstone/CollectingNeighborUpdater";
    private static final String SHAPE_UPDATE = COLLECT + "$ShapeUpdate";
    private static final String MIXIN = "dev/planetary/mixin/";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String DIR = "Lnet/minecraft/core/Direction;";
    private static final String STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String BLOCK_T = "Lnet/minecraft/world/level/block/Block;";
    private static final String LEVEL_T = "Lnet/minecraft/world/level/Level;";
    private static final String LEVEL_ACCESS = "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String LEVEL_READER = "Lnet/minecraft/world/level/LevelReader;";
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String CI =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String SHAPE_DESC =
            "(" + DIR + STATE + POS + POS + "II)V";
    private static final String EXEC_SHAPE =
            "(" + LEVEL_ACCESS + DIR + STATE + POS + POS + "II)V";
    private static final String FULL_CHANGE =
            "(" + STATE + POS + BLOCK_T + POS + "Z)V";
    private static final String EXEC_CHANGE =
            "(" + LEVEL_T + STATE + POS + BLOCK_T + POS + "Z)V";
    private static final String UPDATE_SHAPE =
            "(" + DIR + STATE + LEVEL_ACCESS + POS + POS + ")" + STATE;
    private static final String SURVIVE =
            "(" + LEVEL_READER + POS + ")Z";
    private static final String UPDATE_OR_DESTROY =
            "(" + STATE + STATE + LEVEL_ACCESS + POS + "II)V";
    private static final String UPDATE_AT =
            "(" + POS + BLOCK_T + ")V";
    private static final String UPDATE_EXCEPT =
            "(" + POS + BLOCK_T + DIR + ")V";
    private static final String CHANGE =
            "(" + POS + BLOCK_T + POS + ")V";
    private static final String SHAPE_CHANGED =
            "(" + DIR + STATE + POS + POS + "II)V";

    private record Call(int opcode, String owner, String name, String desc) {}
    private record Member(String name, String desc, int access) {}

    @Test
    void stateBaseDelegatesDynamicallyToConcreteModBlock() throws IOException {
        List<Call> shape = calls(STATE_BASE, "updateShape", UPDATE_SHAPE);
        assertTrue(has(shape, "getBlock"), "BlockStateBase must resolve concrete owner");
        assertTrue(has(shape, "updateShape"),
                "BlockStateBase virtual Block updateShape dispatch removed");
        List<Call> survival = calls(STATE_BASE, "canSurvive", SURVIVE);
        assertTrue(has(survival, "getBlock"));
        assertTrue(has(survival, "canSurvive"));
        String changeDesc = "(" + LEVEL_T + POS + BLOCK_T + POS + "Z)V";
        assertTrue(has(calls(STATE_BASE, "handleNeighborChanged", changeDesc),
                "neighborChanged"), "Block polymorphic neighbor callback absent");
        assertTrue(declares(STATE_BASE, "updateNeighbourShapes",
                "(" + LEVEL_ACCESS + POS + "II)V"));
        assertTrue(declares(STATE_BASE, "updateIndirectNeighbourShapes",
                "(" + LEVEL_ACCESS + POS + "II)V"));
    }

    @Test
    void neighborShapeAndSignalNotificationsRemainDifferentPipelines()
            throws IOException {
        assertTrue(declares(NEIGHBOR, "shapeUpdate", SHAPE_DESC));
        assertTrue(declares(NEIGHBOR, "neighborChanged", CHANGE));
        assertTrue(declares(NEIGHBOR, "neighborChanged", FULL_CHANGE));
        assertTrue(declares(NEIGHBOR, "executeShapeUpdate", EXEC_SHAPE));
        assertTrue(declares(NEIGHBOR, "executeUpdate", EXEC_CHANGE));

        List<Call> shape = calls(NEIGHBOR, "executeShapeUpdate", EXEC_SHAPE);
        assertTrue(has(shape, "getBlockState"));
        assertTrue(has(shape, "updateShape"));
        assertEquals(1, count(shape, BLOCK, "updateOrDestroy",
                UPDATE_OR_DESTROY, Opcodes.INVOKESTATIC));

        List<Call> signal = calls(NEIGHBOR, "executeUpdate", EXEC_CHANGE);
        assertTrue(has(signal, "handleNeighborChanged"));
        assertFalse(has(signal, "updateOrDestroy"),
                "Signal callback unexpectedly writes transformed shape state");
    }

    @Test
    void immediateAndQueuedStrategiesKeepTheirDistinctExecution()
            throws IOException {
        List<Call> immediate = calls(INSTANT, "shapeUpdate", SHAPE_DESC);
        assertEquals(1, count(immediate, NEIGHBOR, "executeShapeUpdate",
                EXEC_SHAPE, Opcodes.INVOKESTATIC));
        assertTrue(has(calls(COLLECT, "shapeUpdate", SHAPE_DESC), "addAndRun"));
        assertEquals(1, count(calls(SHAPE_UPDATE, "runNext", "(" + LEVEL_T + ")Z"),
                NEIGHBOR, "executeShapeUpdate",
                EXEC_SHAPE, Opcodes.INVOKESTATIC));
        assertTrue(declares(COLLECT, "updateNeighborsAtExceptFromFacing",
                UPDATE_EXCEPT));
    }

    @Test
    void updateOrDestroyPreservesDropsFlagsAndRecursionLimit()
            throws IOException {
        assertTrue(declares(BLOCK, "updateOrDestroy",
                "(" + STATE + STATE + LEVEL_ACCESS + POS + "I)V"));
        List<Call> calls = calls(BLOCK, "updateOrDestroy", UPDATE_OR_DESTROY);
        assertTrue(has(calls, "isAir"));
        assertTrue(has(calls, "destroyBlock"),
                "Air transition must preserve proper drops and destroy hooks");
        assertTrue(has(calls, "setBlock"),
                "Non-air transition must preserve LevelAccessor write");
        assertTrue(declares(BLOCK, "updateFromNeighbourShapes",
                "(" + STATE + LEVEL_ACCESS + POS + ")" + STATE));
        assertTrue(has(calls(BLOCK, "updateFromNeighbourShapes",
                "(" + STATE + LEVEL_ACCESS + POS + ")" + STATE),
                "updateShape"));
    }

    @Test
    void neoforgeNeighborNotifyExtensionIsPresentOnActualLoadedVersion()
            throws IOException {
        // Pin *actual* 21.1.215 bytecode, never infer from newer NeoForge patch.
        assertTrue(declares(LEVEL, "updateNeighborsAt", UPDATE_AT));
        assertTrue(declares(SERVER, "updateNeighborsAt", UPDATE_AT));
        assertTrue(declares(SERVER, "updateNeighborsAtExceptFromFacing",
                UPDATE_EXCEPT));
        assertTrue(has(calls(LEVEL, "updateNeighborsAt", UPDATE_AT),
                "onNeighborNotify"),
                "NeoForge Level.onNeighborNotify absent: inspect target version");
        assertTrue(has(calls(SERVER, "updateNeighborsAt", UPDATE_AT),
                "onNeighborNotify"),
                "NeoForge ServerLevel.onNeighborNotify absent: inspect version");
        assertTrue(has(calls(SERVER, "updateNeighborsAtExceptFromFacing",
                UPDATE_EXCEPT), "onNeighborNotify"),
                "NeoForge except-facing event path changed");
        assertTrue(declares(LEVEL, "neighborShapeChanged", SHAPE_CHANGED));
        assertTrue(has(calls(LEVEL, "neighborShapeChanged", SHAPE_CHANGED),
                "shapeUpdate"));
    }

    @Test
    void compiledPlanetMixinsInterceptEventBearingDispatchAtHead()
            throws IOException {
        checkHeadInject(MIXIN + "LevelMixin", "planetary$updateNeighborsAt",
                "updateNeighborsAt", UPDATE_AT, "(" + POS + BLOCK_T + CI + ")V");
        checkHeadInject(MIXIN + "LevelMixin",
                "planetary$updateNeighborsAtExceptFromFacing",
                "updateNeighborsAtExceptFromFacing", UPDATE_EXCEPT,
                "(" + POS + BLOCK_T + DIR + CI + ")V");
        checkHeadInject(MIXIN + "ServerLevelMixin", "planetary$updateNeighborsAt",
                "updateNeighborsAt", UPDATE_AT, "(" + POS + BLOCK_T + CI + ")V");
        checkHeadInject(MIXIN + "ServerLevelMixin",
                "planetary$updateNeighborsAtExceptFromFacing",
                "updateNeighborsAtExceptFromFacing", UPDATE_EXCEPT,
                "(" + POS + BLOCK_T + DIR + CI + ")V");
        // Existing Planetary routing cancels at HEAD on a matching world.
        // This could bypass NeoForge onNeighborNotify; ASM proof is not game
        // event dispatch acceptance. Preserve this explicit integration risk.
    }

    private static void checkHeadInject(String owner, String handler, String target,
            String targetDesc, String desc) throws IOException {
        Member member = methods(owner).stream()
                .filter(m -> m.name.equals(handler) && m.desc.equals(desc))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Mixin handler missing " + owner + "/" + handler));
        assertEquals(0, member.access & Opcodes.ACC_STATIC);
        List<String> selectors = new ArrayList<>();
        List<String> anchors = new ArrayList<>();
        List<Boolean> cancel = new ArrayList<>();
        read(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int flags, String name,
                    String descriptor, String signature, String[] exceptions) {
                if (!name.equals(handler) || !descriptor.equals(desc)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(
                            String annotation, boolean visible) {
                        if (!annotation.equals(INJECT)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public AnnotationVisitor visitArray(String field) {
                                if (field.equals("method")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String ignored, Object value) {
                                        selectors.add((String) value);
                                    }
                                };
                                if (field.equals("at")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public AnnotationVisitor visitAnnotation(
                                            String ignored, String descriptor) {
                                        if (!descriptor.equals(AT)) return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public void visit(String property, Object value) {
                                                if (property.equals("value")) anchors.add((String)value);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String field, Object value) {
                                if (field.equals("cancellable")) cancel.add((Boolean) value);
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(List.of(target), selectors,
                "Target method selector changed on Mixin " + owner + "/" + handler);
        assertEquals(List.of("HEAD"), anchors);
        assertEquals(List.of(true), cancel);
        assertTrue(declares(owner.equals(MIXIN + "ServerLevelMixin") ? SERVER : LEVEL,
                target, targetDesc), "Target owner lacks the expected callback");
    }

    private static boolean has(List<Call> calls, String method) {
        return calls.stream().anyMatch(c -> c.name.equals(method));
    }

    private static long count(List<Call> calls, String owner, String name,
            String desc, int opcode) {
        return calls.stream().filter(c -> c.owner.equals(owner) &&
                c.name.equals(name) && c.desc.equals(desc) &&
                c.opcode == opcode).count();
    }

    private static boolean declares(String owner, String name, String desc)
            throws IOException {
        return methods(owner).stream().anyMatch(m ->
                m.name.equals(name) && m.desc.equals(desc));
    }

    private static List<Member> methods(String owner) throws IOException {
        List<Member> result = new ArrayList<>();
        read(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name,
                    String desc, String signature, String[] exceptions) {
                result.add(new Member(name, desc, access));
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return result;
    }

    private static List<Call> calls(String owner, String name, String desc)
            throws IOException {
        List<Call> result = new ArrayList<>();
        boolean[] found = {false};
        read(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String n,
                    String d, String signature, String[] exceptions) {
                if (!name.equals(n) || !desc.equals(d)) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode,
                            String calledOwner, String method, String descriptor,
                            boolean isInterface) {
                        result.add(new Call(opcode, calledOwner, method, descriptor));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], owner + "/" + name + desc + " missing");
        return result;
    }

    private static ClassReader read(String owner) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(stream, "Missing real classpath bytecode: " + owner);
            return new ClassReader(stream);
        }
    }
}
