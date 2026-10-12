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

/** Exact classpath ABI audit for 2.3C-5, NOT game/client/server Mixin acceptance. */
final class Phase23C5TemporalTicksAsmContractTest {
    private static final String SERVER = "net/minecraft/server/level/ServerLevel";
    private static final String LT = "net/minecraft/world/ticks/LevelTicks";
    private static final String TICK = "net/minecraft/world/ticks/ScheduledTick";
    private static final String STATE =
            "net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String FLUID_STATE = "net/minecraft/world/level/material/FluidState";
    private static final String FLUID = "net/minecraft/world/level/material/Fluid";
    private static final String FLOW = "net/minecraft/world/level/material/FlowingFluid";
    private static final String TEMPORAL = "net/minecraft/world/level/block/ChangeOverTimeBlock";
    private static final String MIXIN = "dev/planetary/mixin/";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String BLOCK = "Lnet/minecraft/world/level/block/Block;";
    private static final String FLUID_TYPE = "Lnet/minecraft/world/level/material/Fluid;";
    private static final String BS = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String FS = "Lnet/minecraft/world/level/material/FluidState;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String SL = "Lnet/minecraft/server/level/ServerLevel;";
    private static final String RAND = "Lnet/minecraft/util/RandomSource;";
    private static final String SUPPLIER = "Ljava/util/function/BooleanSupplier;";
    private static final String BI = "Ljava/util/function/BiConsumer;";
    private static final String CI =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String CIR_BOOL =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String LEVEL_TICK = "(JI" + BI + ")V";
    private static final String SERVER_TICK = "(" + SUPPLIER + ")V";
    private static final String CHUNK_TICK =
            "(Lnet/minecraft/world/level/chunk/LevelChunk;I)V";
    private static final String RANDOM = "(" + SL + POS + RAND + ")V";
    private static final String FLUID_RANDOM = "(" + LEVEL + POS + RAND + ")V";
    private static final String BLOCK_SCHEDULED = "(" + POS + BLOCK + ")V";
    private static final String FLUID_SCHEDULED = "(" + POS + FLUID_TYPE + ")V";
    private static final String SCHEDULE = "(Lnet/minecraft/world/ticks/ScheduledTick;)V";
    private static final String HAS = "(" + POS + "Ljava/lang/Object;)Z";
    private static final String FLUID_TICK = "(" + LEVEL + POS + ")V";

    private record Call(int opcode, String owner, String name, String desc) {}
    private record Member(String name, String desc, int access) {}

    @Test
    void serverTickHasTwoDistinctOrderedVanillaScheduledCallbacks()
            throws IOException {
        List<Call> invocations = calls(SERVER, "tick", SERVER_TICK);
        List<Call> ticks = invocations.stream()
                .filter(c -> c.owner.equals(LT) && c.name.equals("tick")
                        && c.desc.equals(LEVEL_TICK)).toList();
        assertEquals(2, ticks.size(),
                "Ordinal 0=block; ordinal 1=fluid: verify target if drifted");
        assertTrue(has(calls(SERVER, "tickBlock", BLOCK_SCHEDULED),
                "getBlockState"));
        assertTrue(has(calls(SERVER, "tickBlock", BLOCK_SCHEDULED),
                "tick"));
        assertTrue(has(calls(SERVER, "tickFluid", FLUID_SCHEDULED),
                "getFluidState"));
        assertTrue(has(calls(SERVER, "tickFluid", FLUID_SCHEDULED),
                "tick"));
    }

    @Test
    void randomBlockAndFluidTicksAreSelectedOnlyInIndependentChunkPath()
            throws IOException {
        List<Call> chunk = calls(SERVER, "tickChunk", CHUNK_TICK);
        assertTrue(has(chunk, "isRandomlyTicking"));
        assertTrue(has(chunk, "randomTick"));
        assertTrue(chunk.stream().anyMatch(c ->
                        c.owner.equals(BS.substring(1, BS.length()-1))
                                && c.name.equals("randomTick")),
                "Independent BlockState random tick missing");
        assertTrue(chunk.stream().anyMatch(c ->
                        c.owner.equals(FLUID_STATE) && c.name.equals("randomTick")),
                "Independent FluidState random tick missing");
        assertFalse(has(calls(SERVER, "tick", SERVER_TICK), "randomTick"),
                "Random tick was folded into scheduled tick driver");
    }

    @Test
    void levelTicksOwnsQueueCollectionExecutionAndVisibility() throws IOException {
        for (Member m : List.of(
                new Member("schedule", SCHEDULE, 0),
                new Member("tick", LEVEL_TICK, 0),
                new Member("hasScheduledTick", HAS, 0),
                new Member("willTickThisTick", HAS, 0),
                new Member("count", "()I", 0)))
            assertTrue(declares(LT, m.name, m.desc), "Missing LevelTicks ABI " + m);
        List<Call> body = calls(LT, "tick", LEVEL_TICK);
        assertTrue(has(body, "collectTicks"));
        assertTrue(has(body, "runCollectedTicks"));
        assertTrue(has(body, "cleanupAfterTick"));
        assertTrue(index(body, "collectTicks") < index(body, "runCollectedTicks"));
        assertTrue(index(body, "runCollectedTicks") < index(body, "cleanupAfterTick"));
        assertTrue(declares(TICK, "probe",
                "(Ljava/lang/Object;" + POS + ")Lnet/minecraft/world/ticks/ScheduledTick;"));
    }

    @Test
    void blockAndFluidPolymorphicCallbacksMustNotBeConflated()
            throws IOException {
        assertTrue(has(calls(STATE, "tick", RANDOM), "tick"));
        assertTrue(has(calls(STATE, "randomTick", RANDOM), "randomTick"));
        assertTrue(has(calls(FLUID_STATE, "tick", FLUID_TICK), "tick"));
        assertTrue(has(calls(FLUID_STATE, "randomTick", FLUID_RANDOM),
                "randomTick"));
        assertTrue(declares(FLUID, "tick",
                "(" + LEVEL + POS + FS + ")V"));
        assertTrue(declares(FLOW, "tick",
                "(" + LEVEL + POS + FS + ")V"));
        List<Call> flowing = calls(FLOW, "tick", "(" + LEVEL + POS + FS + ")V");
        assertTrue(has(flowing, "getNewLiquid"));
        assertTrue(has(flowing, "spread"));
        assertTrue(has(flowing, "scheduleTick"),
                "Fluid rescheduling is a distinct source-side algorithm");
    }

    @Test
    void copperLikeChangeOverTimeIsIndependentFromTickDriver()
            throws IOException {
        String params = "(" + BS + SL + POS + RAND + ")";
        assertTrue(declares(TEMPORAL, "changeOverTime", params + "V"));
        assertTrue(declares(TEMPORAL, "getNextState",
                params + "Ljava/util/Optional;"));
        assertTrue(has(calls(TEMPORAL, "changeOverTime", params + "V"),
                "getNextState"));
        assertTrue(has(calls(TEMPORAL, "getNextState",
                params + "Ljava/util/Optional;"), "withinManhattan"),
                "Physical Manhattan scan (4) must not be globally rotated");
    }

    @Test
    void compiledPlanetaryLevelTicksRoutingUsesExactSelectorsAndHandlers()
            throws IOException {
        String mixin = MIXIN + "LevelTicksMixin";
        checkInjection(mixin, "planetary$schedule", "schedule",
                "(" + "Lnet/minecraft/world/ticks/ScheduledTick;" + CI + ")V",
                "HEAD", null, true);
        checkInjection(mixin, "planetary$hasScheduledTick", "hasScheduledTick",
                "(" + POS + "Ljava/lang/Object;" + CIR_BOOL + ")V",
                "HEAD", null, true);
        checkInjection(mixin, "planetary$willTickThisTick", "willTickThisTick",
                "(" + POS + "Ljava/lang/Object;" + CIR_BOOL + ")V",
                "HEAD", null, true);
        checkInjection(mixin, "planetary$count", "count",
                "(" + CIR_BOOL + ")V", "RETURN", null, true);
    }

    @Test
    void compiledServerLevelOrdinalInjectionsPreserveTwoScheduledTickPositions()
            throws IOException {
        String mixin = MIXIN + "ServerLevelMixin";
        String handlerDesc = "(" + SUPPLIER + CI + ")V";
        checkInjection(mixin, "planetary$runScheduledBlockTicks", "tick",
                handlerDesc, "INVOKE", 0, false);
        checkInjection(mixin, "planetary$runScheduledFluidTicks", "tick",
                handlerDesc, "INVOKE", 1, false);
        // The @At target is pinned separately as its exact owner/descriptor;
        // if the target changes the bytecode assertion above detects drift.
        for (String name : List.of("planetary$runScheduledBlockTicks",
                "planetary$runScheduledFluidTicks")) {
            List<String> targets = atTargets(mixin, name, handlerDesc);
            assertEquals(List.of("L" + LT + ";tick" + LEVEL_TICK), targets);
        }
    }

    private static List<String> atTargets(String cls, String handler,
            String desc) throws IOException {
        ArrayList<String> targets = new ArrayList<>();
        visitInject(cls, handler, desc, new InjectParts() {
            @Override public void at(String value, String target, Integer ordinal,
                    String shift) {
                if (target != null) targets.add(target);
            }
        });
        return targets;
    }

    private static void checkInjection(String cls, String handler, String target,
            String descriptor, String atValue, Integer ordinal, boolean cancellable)
            throws IOException {
        Member method = methods(cls).stream()
                .filter(m -> m.name.equals(handler) && m.desc.equals(descriptor))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Compiled Mixin handler missing: " + handler));
        assertEquals(0, method.access & Opcodes.ACC_STATIC);
        List<String> names = new ArrayList<>();
        List<String> anchors = new ArrayList<>();
        List<Integer> ordinals = new ArrayList<>();
        List<String> shifts = new ArrayList<>();
        List<Boolean> cancels = new ArrayList<>();
        visitInject(cls, handler, descriptor, new InjectParts() {
            @Override public void method(String value) { names.add(value); }
            @Override public void cancellable(boolean v) { cancels.add(v); }
            @Override public void at(String value, String t, Integer ord, String shift) {
                anchors.add(value);
                if (ord != null) ordinals.add(ord);
                if (shift != null) shifts.add(shift);
            }
        });
        assertEquals(List.of(target), names);
        assertEquals(List.of(atValue), anchors);
        // The Java/Mixin annotation default is cancellable=false. When an
        // injector omits that default, ASM has NO "cancellable" member.
        // The positive case must be explicit; the negative case must not
        // declare true. Keep selector, handler and ordinal assertions strict.
        if (cancellable) {
            assertEquals(List.of(true), cancels);
        } else {
            assertTrue(cancels.isEmpty() || cancels.equals(List.of(false)),
                    "Noncancellable @Inject unexpectedly declares true");
        }
        if (ordinal != null) {
            assertEquals(List.of(ordinal), ordinals);
            assertEquals(List.of("AFTER"), shifts);
        }
    }

    private interface InjectParts {
        default void method(String value) {}
        default void cancellable(boolean value) {}
        default void at(String value, String target, Integer ordinal, String shift) {}
    }

    private static void visitInject(String cls, String name, String desc,
            InjectParts consumer) throws IOException {
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String n,
                    String d, String generic, String[] exceptions) {
                if (!n.equals(name) || !d.equals(desc)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(
                            String type, boolean visible) {
                        if (!type.equals(INJECT)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public AnnotationVisitor visitArray(String field) {
                                if (field.equals("method")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String ignored, Object value) {
                                        consumer.method((String) value);
                                    }
                                };
                                if (field.equals("at")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public AnnotationVisitor visitAnnotation(
                                            String ignored, String type) {
                                        if (!type.equals(AT)) return null;
                                        return new AtVisitor(consumer);
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String field, Object value) {
                                if (field.equals("cancellable"))
                                    consumer.cancellable((Boolean)value);
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private static final class AtVisitor extends AnnotationVisitor {
        private final InjectParts consumer;
        private String value, target, shift;
        private Integer ordinal;
        AtVisitor(InjectParts consumer) {
            super(Opcodes.ASM9);
            this.consumer = consumer;
        }
        @Override public void visit(String name, Object item) {
            switch (name) {
                case "value" -> value = (String) item;
                case "target" -> target = (String) item;
                case "ordinal" -> ordinal = (Integer) item;
                default -> {}
            }
        }
        @Override public void visitEnum(String name, String descriptor, String item) {
            if (name.equals("shift")) shift = item;
        }
        @Override public void visitEnd() {
            consumer.at(value, target, ordinal, shift);
        }
    }

    private static int index(List<Call> calls, String method) {
        for (int i = 0; i < calls.size(); i++)
            if (calls.get(i).name.equals(method)) return i;
        return -1;
    }

    private static boolean has(List<Call> calls, String method) {
        return calls.stream().anyMatch(c -> c.name.equals(method));
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
        ArrayList<Call> out = new ArrayList<>();
        boolean[] found = {false};
        read(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String n,
                    String d, String generic, String[] exceptions) {
                if (!n.equals(name) || !d.equals(desc)) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode,
                            String targetOwner, String targetName, String targetDesc,
                            boolean isInterface) {
                        out.add(new Call(opcode, targetOwner, targetName, targetDesc));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Target method missing: " + owner + "::" + name + desc);
        return out;
    }

    private static ClassReader read(String owner) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(stream, "Target class missing: " + owner);
            return new ClassReader(stream);
        }
    }
}
