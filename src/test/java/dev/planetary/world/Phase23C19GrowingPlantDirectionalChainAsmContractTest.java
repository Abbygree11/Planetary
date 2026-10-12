package dev.planetary.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 2.3C-19: actual Gradle-loaded NeoForge 21.1.215 selected classfiles.
 * Exact growing-head/body physical direction and CropsGrow hook paths.
 * Does not verify Planet Mixin weaving, gameplay or third-party binaries.
 */
final class Phase23C19GrowingPlantDirectionalChainAsmContractTest {
    private static final String MC = "net/minecraft/";
    private static final String BLOCK = MC + "world/level/block/";
    private static final String BASE = BLOCK + "GrowingPlantBlock";
    private static final String HEAD = BLOCK + "GrowingPlantHeadBlock";
    private static final String BODY = BLOCK + "GrowingPlantBodyBlock";
    private static final String S = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String DIR = "Lnet/minecraft/core/Direction;";
    private static final String READER = "Lnet/minecraft/world/level/LevelReader;";
    private static final String ACCESS = "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String PLACE = "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String CROP_HOOK = "net/neoforged/neoforge/common/CommonHooks";
    private static final String UTIL = MC + "BlockUtil";

    @Test void ownerHierarchyAndVanillaDirectionalSurvivalKeepPhysicalWorldPosition()
            throws IOException {
        assertSuperclass(BASE, BLOCK + "Block");
        assertSuperclass(HEAD, BASE);
        assertSuperclass(BODY, BASE);
        assertField(BASE, "growthDirection");
        assertField(BASE, "scheduleFluidTicks");
        assertMethod(BASE, "getStateForPlacement", "(" + PLACE + ")" + S);
        assertMethod(BASE, "getStateForPlacement", "(" + ACCESS + ")" + S);
        assertMethod(BASE, "canSurvive", "(" + S + READER + POS + ")Z");
        assertMethod(BASE, "tick", null);
        assertMethod(BASE, "getHeadBlock", "()L" + HEAD + ";");
        assertMethod(BASE, "getBodyBlock", "()L" + BLOCK + "Block;");
        assertCalls(BASE, "getStateForPlacement", "relative");
        assertCalls(BASE, "canSurvive", "getOpposite");
        assertCalls(BASE, "canSurvive", "relative");
        assertCalls(BASE, "canSurvive", "isFaceSturdy");
        assertCalls(BASE, "tick", "destroyBlock");
    }

    @Test void newHeadOwnsAgeRandomGrowthAndNeoForgePrePostEventLifecycle()
            throws IOException {
        assertField(HEAD, "AGE");
        assertField(HEAD, "MAX_AGE");
        assertMethod(HEAD, "randomTick", null);
        assertMethod(HEAD, "isRandomlyTicking", "(" + S + ")Z");
        assertMethod(HEAD, "getMaxAgeState", "(" + S + ")" + S);
        assertMethod(HEAD, "getGrowIntoState", null);
        assertMethod(HEAD, "canGrowInto", "(" + S + ")Z");
        assertMethod(HEAD, "getBlocksToGrowWhenBonemealed",
                "(Lnet/minecraft/util/RandomSource;)I");
        assertCalls(HEAD, "randomTick", "relative");
        assertCalls(HEAD, "randomTick", "canGrowInto");
        assertCalls(HEAD, "randomTick", "setBlockAndUpdate");
        assertInvokeOwner(HEAD, "randomTick", CROP_HOOK, "canCropGrow");
        assertInvokeOwner(HEAD, "randomTick", CROP_HOOK, "fireCropGrowPost");
        assertCalls(HEAD, "getGrowIntoState", "cycle");
        assertCalls(HEAD, "updateShape", "updateBodyAfterConvertedFromHead");
        assertCalls(HEAD, "updateShape", "scheduleTick");
        assertCalls(HEAD, "performBonemeal", "getBlocksToGrowWhenBonemealed");
        assertCalls(HEAD, "performBonemeal", "setBlockAndUpdate");
        assertCalls(HEAD, "performBonemeal", "relative");
    }

    @Test void bodyToHeadConversionSearchAndBonemealForwardingAreIndependent()
            throws IOException {
        assertMethod(BODY, "getHeadPos", null);
        assertMethod(BODY, "updateHeadAfterConvertedFromBody",
                "(" + S + S + ")" + S);
        assertMethod(BODY, "getCloneItemStack", null);
        assertMethod(BODY, "performBonemeal", null);
        assertCalls(BODY, "updateShape", "updateHeadAfterConvertedFromBody");
        assertCalls(BODY, "updateShape", "scheduleTick");
        assertInvokeOwner(BODY, "getHeadPos", UTIL, "getTopConnectedBlock");
        assertCalls(BODY, "isValidBonemealTarget", "getHeadPos");
        assertCalls(BODY, "performBonemeal", "getHeadPos");
        assertCalls(BODY, "performBonemeal", "performBonemeal");
        assertCalls(BODY, "canBeReplaced", "getItemInHand");
    }

    @Test void eightDistinctRealVanillaRegisteredSubclassesUseUpAndDownAxes()
            throws IOException {
        for (String n : List.of("KelpBlock", "TwistingVinesBlock",
                "WeepingVinesBlock", "CaveVinesBlock")) {
            String owner = BLOCK + n;
            assertSuperclass(owner, HEAD);
            assertMethod(owner, "canGrowInto", "(" + S + ")Z");
            assertMethod(owner, "getBlocksToGrowWhenBonemealed",
                    "(Lnet/minecraft/util/RandomSource;)I");
            assertMethod(owner, "getBodyBlock", "()L" + BLOCK + "Block;");
        }
        for (String n : List.of("KelpPlantBlock", "TwistingVinesPlantBlock",
                "WeepingVinesPlantBlock", "CaveVinesPlantBlock")) {
            String owner = BLOCK + n;
            assertSuperclass(owner, BODY);
            assertMethod(owner, "getHeadBlock", "()L" + HEAD + ";");
        }
        for (String n : List.of("KelpBlock", "KelpPlantBlock",
                "TwistingVinesBlock", "TwistingVinesPlantBlock")) {
            assertGetStatic(BLOCK + n, "<init>", MC + "core/Direction", "UP");
        }
        for (String n : List.of("WeepingVinesBlock", "WeepingVinesPlantBlock",
                "CaveVinesBlock", "CaveVinesPlantBlock")) {
            assertGetStatic(BLOCK + n, "<init>", MC + "core/Direction", "DOWN");
        }
    }

    @Test void kelpAndGlowBerriesOwnDifferentFluidAndHarvestSemantics()
            throws IOException {
        assertMethod(BLOCK + "KelpBlock", "getStateForPlacement", "(" + PLACE + ")" + S);
        assertMethod(BLOCK + "KelpBlock", "getFluidState", null);
        assertMethod(BLOCK + "KelpPlantBlock", "getFluidState", null);
        assertMethod(BLOCK + "KelpBlock", "canAttachTo", "(" + S + ")Z");
        assertMethod(BLOCK + "KelpPlantBlock", "canAttachTo", "(" + S + ")Z");
        assertField(BLOCK + "CaveVinesBlock", "CODEC");
        assertMethod(BLOCK + "CaveVinesBlock", "getGrowIntoState", null);
        assertMethod(BLOCK + "CaveVinesBlock", "updateBodyAfterConvertedFromHead",
                "(" + S + S + ")" + S);
        assertMethod(BLOCK + "CaveVinesPlantBlock", "updateHeadAfterConvertedFromBody",
                "(" + S + S + ")" + S);
        for (String n : List.of("CaveVinesBlock", "CaveVinesPlantBlock")) {
            assertMethod(BLOCK + n, "useWithoutItem", null);
            assertMethod(BLOCK + n, "performBonemeal", null);
            assertCalls(BLOCK + n, "performBonemeal", "setBlock");
        }
    }

    @Test void vanillaWorldgenWritesHeadBodyWithoutBlockItemAndUsesPhysicalY()
            throws IOException {
        for (String feature : List.of("KelpFeature", "TwistingVinesFeature",
                "WeepingVinesFeature")) {
            String owner = MC + "world/level/levelgen/feature/" + feature;
            assertMethod(owner, "place", null);
            assertInvokesSomeMethod(owner, "setBlock");
        }
        assertInvokeOwner(BODY, "getHeadPos", UTIL, "getTopConnectedBlock");
    }

    private static void assertField(String owner, String field) throws IOException {
        final boolean[] yes = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int a, String n, String d, String sig, Object v) {
                if (field.equals(n)) yes[0] = true;
                return null;
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
        assertTrue(yes[0], "Actual target class field missing " + owner + "." + field);
    }

    private static void assertSuperclass(String owner, String parent) throws IOException {
        assertEquals(parent, reader(owner).getSuperName(), "Wrong growing plant hierarchy " + owner);
    }

    private static void assertMethod(String owner, String name, String descriptor)
            throws IOException {
        final boolean[] yes = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] exc) {
                if (name.equals(n) && (descriptor == null || descriptor.equals(d))) yes[0] = true;
                return null;
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
        assertTrue(yes[0], "Actual target method missing " + owner + "::" + name
                + (descriptor == null ? "" : descriptor));
    }

    private static void assertCalls(String owner, String method, String callee)
            throws IOException {
        assertCalls(owner, method, callee, true);
    }

    private static void assertCalls(String owner, String method, String callee,
            boolean expected) throws IOException {
        final boolean[] yes = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] exc) {
                if (!method.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int op, String caller, String n,
                            String desc, boolean itf) {
                        if (callee.equals(n)) yes[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(expected, yes[0], "Unexpected selected actual callsite "
                + owner + "::" + method + " -> " + callee);
    }

    private static void assertInvokeOwner(String owner, String method,
            String calleeOwner, String callee) throws IOException {
        final boolean[] yes = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] exc) {
                if (!method.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int op, String caller, String n,
                            String desc, boolean itf) {
                        if (calleeOwner.equals(caller) && callee.equals(n)) yes[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(yes[0], "Actual callsite absent " + owner + "::" + method
                + " -> " + calleeOwner + "::" + callee);
    }

    private static void assertGetStatic(String owner, String method,
            String fieldOwner, String fieldName) throws IOException {
        final boolean[] yes = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] exc) {
                if (!method.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitFieldInsn(int op, String caller, String n,
                            String desc) {
                        if (op == Opcodes.GETSTATIC && fieldOwner.equals(caller)
                                && fieldName.equals(n)) yes[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(yes[0], "No actual growthDirection constant " + owner + "." + fieldName);
    }

    private static void assertInvokesSomeMethod(String owner, String method)
            throws IOException {
        final boolean[] yes = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] exc) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int op, String caller, String n,
                            String desc, boolean itf) {
                        if (method.equals(n)) yes[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(yes[0], "Expected state write absent from owner " + owner);
    }

    private static ClassReader reader(String owner) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(in, "Actual Gradle NeoForge 21.1.215 class missing " + owner);
            return new ClassReader(in);
        }
    }
}
