package dev.planetary.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 2.3C-6: EXACT Gradle-resolved NeoForge 21.1.215 BE lifecycle and
 * compiled Planetary Mixin-annotation contracts. Does not weave Mixins.
 */
final class Phase23C6BlockEntityLifecycleAsmContractTest {
    private static final String LEVEL = "net/minecraft/world/level/Level";
    private static final String SERVER = "net/minecraft/server/level/ServerLevel";
    private static final String CHUNK = "net/minecraft/world/level/chunk/LevelChunk";
    private static final String BOUND = CHUNK + "$BoundTickingBlockEntity";
    private static final String WRAPPER = CHUNK + "$RebindableTickingBlockEntityWrapper";
    private static final String BE = "net/minecraft/world/level/block/entity/BlockEntity";
    private static final String BE_TICKER = "net/minecraft/world/level/block/entity/BlockEntityTicker";
    private static final String STATE_BASE =
            "net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String MIXIN_LEVEL = "dev/planetary/mixin/LevelMixin";
    private static final String MIXIN_SERVER = "dev/planetary/mixin/ServerLevelMixin";
    private static final String P = "Lnet/minecraft/core/BlockPos;";
    private static final String S = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String L = "Lnet/minecraft/world/level/Level;";
    private static final String B = "Lnet/minecraft/world/level/block/entity/BlockEntity;";
    private static final String TYPE =
            "Lnet/minecraft/world/level/block/entity/BlockEntityType;";
    private static final String TICKER =
            "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;";
    private static final String TICKING =
            "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;";
    private static final String PROVIDER =
            "Lnet/minecraft/core/HolderLookup$Provider;";
    private static final String NBT = "Lnet/minecraft/nbt/CompoundTag;";
    private static final String CI =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String CIR =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String LISTENER =
            "Lnet/neoforged/neoforge/capabilities/ICapabilityInvalidationListener;";
    private static final String INJECT =
            "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT =
            "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String SET_BE = "(" + B + ")V";
    private static final String GET_BE = "(" + P + ")" + B;
    private static final String TICKER_GET = "(" + L + TYPE + ")" + TICKER;
    private static final String SAVE = "(" + PROVIDER + ")" + NBT;
    private static final String REGISTER_CAP = "(" + P + LISTENER + ")V";

    private record Call(int opcode, String owner, String name, String desc) {}
    private record Member(String name, String desc, int access) {}

    @Test
    void stateOwnerSelectsModTickerAndChunkRegistersOrRebindsIt()
            throws IOException {
        assertTrue(declares(STATE_BASE, "getTicker", TICKER_GET));
        List<Call> select = calls(STATE_BASE, "getTicker", TICKER_GET);
        assertTrue(has(select, "getBlock"));
        assertTrue(has(select, "getTicker"),
                "BlockStateBase must dynamically select EntityBlock.getTicker");

        assertTrue(declares(CHUNK, "updateBlockEntityTicker", SET_BE));
        List<Call> register = calls(CHUNK, "updateBlockEntityTicker", SET_BE);
        assertTrue(has(register, "getTicker"));
        assertTrue(has(register, "compute"));
        assertTrue(has(register, "removeBlockEntityTicker"));
        assertTrue(declares(CHUNK, "createTicker",
                "(" + B + TICKER + ")" + TICKING));
        assertTrue(declares(LEVEL, "addBlockEntityTicker", "(" + TICKING + ")V"));
        assertTrue(has(calls(CHUNK, "removeBlockEntity",
                "(" + P + ")V"), "removeBlockEntityTicker"),
                "Removing BE must prevent stale tickers");
        assertTrue(declares(WRAPPER, "tick", "()V"));
        assertTrue(has(calls(WRAPPER, "tick", "()V"), "tick"));
    }

    @Test
    void boundTickerGuardsValidityLoadAndFailureBeforeCallingMod()
            throws IOException {
        assertTrue(declares(BOUND, "tick", "()V"));
        List<Call> body = calls(BOUND, "tick", "()V");
        assertTrue(has(body, "isRemoved"));
        assertTrue(has(body, "hasLevel"));
        assertTrue(has(body, "isTicking"));
        assertTrue(has(body, "getBlockState"));
        assertTrue(has(body, "isValid"));
        assertTrue(body.stream().anyMatch(c ->
                c.owner.equals(BE_TICKER) && c.name.equals("tick")
                        && c.opcode == Opcodes.INVOKEINTERFACE),
                "Ticker callback must invoke dynamic mod/vanilla implementation");
    }

    @Test
    void levelTickerIteratesAndNeoForgeFreshLifecycleRemainsSeparate()
            throws IOException {
        assertTrue(declares(LEVEL, "tickBlockEntities", "()V"));
        List<Call> body = calls(LEVEL, "tickBlockEntities", "()V");
        assertTrue(has(body, "shouldTickBlocksAt"));
        assertTrue(has(body, "runsNormally"));
        assertTrue(has(body, "isRemoved"));
        assertTrue(has(body, "tick"));
        // NeoForge 21.1.215 may add onLoad for freshly registered BEs.
        // Pin the original target behavior and do not infer it from later patches.
        // Java compiles the freshEntities.forEach(be -> be.onLoad())
        // callback into a synthetic lambda body, not necessarily directly
        // into Level.tickBlockEntities. Check actual declaring class callsites.
        assertTrue(methods(LEVEL).stream().anyMatch(m -> {
            try {
                return has(calls(LEVEL, m.name, m.desc), "onLoad");
            } catch (IOException exception) {
                throw new AssertionError(exception);
            }
        }), "NeoForge onLoad invocation absent from exact Level class");
        assertTrue(declares(LEVEL, "addFreshBlockEntities",
                "(Ljava/util/Collection;)V"));
        assertTrue(has(calls(CHUNK, "addAndRegisterBlockEntity", SET_BE),
                "addFreshBlockEntities"),
                "NeoForge onLoad queue was not wired by LevelChunk");
    }

    @Test
    void persistenceUsesFullMetadataComponentsAndModExtensibility()
            throws IOException {
        assertTrue(declares(BE, "saveWithFullMetadata", SAVE));
        assertTrue(declares(BE, "saveWithoutMetadata", SAVE));
        assertTrue(declares(BE, "loadWithComponents",
                "(" + NBT + PROVIDER + ")V"));
        assertTrue(has(calls(BE, "saveWithFullMetadata", SAVE), "saveWithoutMetadata"));
        assertTrue(has(calls(BE, "saveWithoutMetadata", SAVE), "saveAdditional"));
        assertTrue(has(calls(BE, "loadWithComponents",
                "(" + NBT + PROVIDER + ")V"), "loadAdditional"));
        assertTrue(has(calls(CHUNK, "getBlockEntityNbtForSaving",
                "(" + P + PROVIDER + ")" + NBT), "saveWithFullMetadata"));
        assertTrue(has(calls(CHUNK, "setBlockState",
                "(" + P + S + "Z)" + S), "isValidBlockState"),
                "BE replacement must reject mismatched state before retained object use");
    }

    @Test
    void neoforgeCapabilityLifecycleIsConnectedToRemoval()
            throws IOException {
        assertTrue(declares(SERVER, "invalidateCapabilities", "(" + P + ")V"));
        assertTrue(declares(SERVER, "registerCapabilityListener", REGISTER_CAP));
        assertTrue(has(calls(BE, "setRemoved", "()V"), "invalidateCapabilities"),
                "NeoForge BE removal must invalidate capabilities");
        assertTrue(has(calls(BE, "clearRemoved", "()V"), "invalidateCapabilities"),
                "NeoForge BE placement must invalidate capabilities");
        assertTrue(has(calls(CHUNK, "removeBlockEntity", "(" + P + ")V"),
                "setRemoved"));
        assertTrue(has(calls(CHUNK, "setBlockEntity", SET_BE), "clearRemoved"));
    }

    @Test
    void existingLevelMixinsInterceptBeStorageWithoutTickerRegistration()
            throws IOException {
        head(MIXIN_LEVEL, "planetary$getBlockEntity", "getBlockEntity",
                "(" + P + CIR + ")V", GET_BE, true);
        head(MIXIN_LEVEL, "planetary$setBlockEntity", "setBlockEntity",
                "(" + B + CI + ")V", SET_BE, true);
        head(MIXIN_LEVEL, "planetary$removeBlockEntity", "removeBlockEntity",
                "(" + P + CI + ")V", "(" + P + ")V", true);
        assertTrue(declares(MIXIN_LEVEL, "planetary$setBlockEntity",
                "(" + B + CI + ")V"));
        List<Call> body = calls(MIXIN_LEVEL, "planetary$setBlockEntity",
                "(" + B + CI + ")V");
        assertTrue(has(body, "put"), "Planet BE storage must remain visible");
        assertFalse(has(body, "addAndRegisterBlockEntity"),
                "A direct hashmap store is not LevelChunk ticker registration");
        assertFalse(has(body, "updateBlockEntityTicker"),
                "This Mixin cannot claim to install mod ticker callbacks");
    }

    @Test
    void serverCapabilitiesMixinsKeepStrictCorrectNeoForgeSignatures()
            throws IOException {
        head(MIXIN_SERVER, "planetary$invalidateCapabilities",
                "invalidateCapabilities",
                "(" + P + CI + ")V", "(" + P + ")V", true);
        head(MIXIN_SERVER, "planetary$registerCapabilityListener",
                "registerCapabilityListener",
                "(" + P + LISTENER + CI + ")V", REGISTER_CAP, true);
        assertTrue(declares(SERVER, "invalidateCapabilities", "(" + P + ")V"));
        assertTrue(declares(SERVER, "registerCapabilityListener", REGISTER_CAP));
    }

    private static void head(String mixin, String handler, String target,
            String handlerDesc, String targetDesc, boolean cancellable)
            throws IOException {
        Member member = methods(mixin).stream().filter(m ->
                m.name.equals(handler) && m.desc.equals(handlerDesc))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Missing compiled Mixin handler " + mixin + ":" + handler));
        assertEquals(0, member.access & Opcodes.ACC_STATIC);
        String targetOwner = mixin.equals(MIXIN_LEVEL) ? LEVEL : SERVER;
        assertTrue(declares(targetOwner, target, targetDesc));

        List<String> methods = new ArrayList<>();
        List<String> at = new ArrayList<>();
        List<Boolean> cancels = new ArrayList<>();
        read(mixin).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String sig, String[] exceptions) {
                if (!name.equals(handler) || !descriptor.equals(handlerDesc)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(String desc,
                            boolean visible) {
                        if (!desc.equals(INJECT)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public AnnotationVisitor visitArray(String key) {
                                if (key.equals("method")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String unused, Object value) {
                                        methods.add((String)value);
                                    }
                                };
                                if (key.equals("at")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public AnnotationVisitor visitAnnotation(String unused,
                                            String desc) {
                                        if (!desc.equals(AT)) return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public void visit(String field, Object value) {
                                                if (field.equals("value")) at.add((String)value);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String key, Object value) {
                                if (key.equals("cancellable")) cancels.add((Boolean)value);
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(List.of(target), methods);
        assertEquals(List.of("HEAD"), at);
        assertEquals(List.of(cancellable), cancels);
    }

    private static boolean has(List<Call> calls, String method) {
        return calls.stream().anyMatch(c -> c.name.equals(method));
    }

    private static boolean declares(String className, String method, String desc)
            throws IOException {
        return methods(className).stream().anyMatch(m ->
                m.name.equals(method) && m.desc.equals(desc));
    }

    private static List<Member> methods(String className) throws IOException {
        ArrayList<Member> methods = new ArrayList<>();
        read(className).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name,
                    String desc, String signature, String[] exceptions) {
                methods.add(new Member(name, desc, access));
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return methods;
    }

    private static List<Call> calls(String className, String method, String descriptor)
            throws IOException {
        ArrayList<Call> calls = new ArrayList<>();
        boolean[] found = {false};
        read(className).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int flags, String name,
                    String desc, String signature, String[] exceptions) {
                if (!name.equals(method) || !desc.equals(descriptor)) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String owner,
                            String method, String desc, boolean itf) {
                        calls.add(new Call(opcode, owner, method, desc));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Exact target method missing: " + className + "::" + method + descriptor);
        return calls;
    }

    private static ClassReader read(String clazz) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(clazz + ".class")) {
            assertNotNull(in, "Absent exact target class bytecode " + clazz);
            return new ClassReader(in);
        }
    }
}
