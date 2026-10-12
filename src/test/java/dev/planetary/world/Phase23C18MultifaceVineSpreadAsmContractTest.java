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
 * 2.3C-18 selected true Gradle-resolved NeoForge 21.1.215 bytecode.
 * P26 vanilla six-face MultifaceBlock vs five-face independent VineBlock;
 * three geometry spread operations, mod-extensible SpreadConfig, worldgen.
 * Neither loaded Mixin transformation nor gameplay/client/mod-JAR acceptance.
 */
final class Phase23C18MultifaceVineSpreadAsmContractTest {
    private static final String ROOT = "net/minecraft/";
    private static final String B = ROOT + "world/level/block/";
    private static final String MULTI = B + "MultifaceBlock";
    private static final String VINE = B + "VineBlock";
    private static final String SPREADER = B + "MultifaceSpreader";
    private static final String GLOW = B + "GlowLichenBlock";
    private static final String SCULK = B + "SculkVeinBlock";
    private static final String CFG = SPREADER + "$SpreadConfig";
    private static final String DEFAULT = SPREADER + "$DefaultSpreaderConfig";
    private static final String SPOS = SPREADER + "$SpreadPos";
    private static final String TYPE = SPREADER + "$SpreadType";
    private static final String GROWTH = ROOT + "world/level/levelgen/feature/MultifaceGrowthFeature";
    private static final String STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String DIR = "Lnet/minecraft/core/Direction;";
    private static final String READER = "Lnet/minecraft/world/level/BlockGetter;";
    private static final String ACCESS = "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String PLACEMENT = "Lnet/minecraft/world/item/context/BlockPlaceContext;";

    @Test void multifaceSixFacePropertiesAndIndependentVineFiveFaceVocabulary()
            throws IOException {
        assertSuperclass(MULTI, B + "Block");
        assertSuperclass(VINE, B + "Block"); // Vine DOES NOT extend MultifaceBlock!
        assertField(MULTI, "DIRECTIONS");
        assertField(MULTI, "PROPERTY_BY_DIRECTION");
        assertField(VINE, "PROPERTY_BY_DIRECTION");
        for (String property : List.of("UP", "NORTH", "EAST", "SOUTH", "WEST")) {
            assertField(VINE, property);
        }
        assertMethod(MULTI, "getFaceProperty", "(" + DIR +
                ")Lnet/minecraft/world/level/block/state/properties/BooleanProperty;");
        assertMethod(VINE, "getPropertyForFace", "(" + DIR +
                ")Lnet/minecraft/world/level/block/state/properties/BooleanProperty;");
        assertMethod(MULTI, "hasFace", "(" + STATE + DIR + ")Z");
        assertMethod(MULTI, "availableFaces", "(" + STATE + ")Ljava/util/Set;");
        assertMethod(MULTI, "pack", "(Ljava/util/Collection;)B");
        assertMethod(MULTI, "unpack", "(B)Ljava/util/Set;");
        assertCalls(VINE, "getPropertyForFace", "get");
    }

    @Test void multifaceSupportPlacementRemovalAndRotateMirrorAreConcreteAlgorithms()
            throws IOException {
        assertMethod(MULTI, "isFaceSupported", "(" + DIR + ")Z");
        assertMethod(MULTI, "getStateForPlacement", "(" + PLACEMENT + ")" + STATE);
        assertMethod(MULTI, "getStateForPlacement",
                "(" + STATE + READER + POS + DIR + ")" + STATE);
        assertMethod(MULTI, "isValidStateForPlacement",
                "(" + READER + STATE + POS + DIR + ")Z");
        assertMethod(MULTI, "canAttachTo", "(" + READER + DIR + POS + STATE + ")Z");
        assertCalls(MULTI, "canAttachTo", "getBlockSupportShape");
        assertCalls(MULTI, "canAttachTo", "getCollisionShape");
        assertCalls(MULTI, "canSurvive", "canAttachTo");
        assertCalls(MULTI, "isValidStateForPlacement", "canAttachTo");
        assertCalls(MULTI, "updateShape", "removeFace");
        assertCalls(MULTI, "getStateForPlacement", "getNearestLookingDirections");
        assertCalls(MULTI, "rotate", "mapDirections");
        assertCalls(MULTI, "mirror", "mapDirections");
    }

    @Test void threeSpreaderModesAndConfigAreWorldAndPerDirectionAware() throws IOException {
        List<String> modes = enumConstants(TYPE);
        assertEquals(List.of("SAME_POSITION", "SAME_PLANE", "WRAP_AROUND"), modes);
        assertMethod(TYPE, "getSpreadPos",
                "(" + POS + DIR + DIR + ")L" + SPOS + ";");
        assertMethod(SPOS, "pos", "()" + POS);
        assertMethod(SPOS, "face", "()" + DIR);
        assertMethod(SPREADER, "spreadAll", null);
        assertMethod(SPREADER, "spreadFromRandomFaceTowardRandomDirection", null);
        assertMethod(SPREADER, "spreadFromFaceTowardDirection", null);
        assertMethod(SPREADER, "getSpreadFromFaceTowardDirection", null);
        assertMethod(SPREADER, "spreadToFace", null);
        assertCalls(SPREADER, "getSpreadFromFaceTowardDirection", "getSpreadTypes");
        assertCalls(SPREADER, "getSpreadFromFaceTowardDirection", "getSpreadPos");
        assertCalls(SPREADER, "spreadToFace", "placeBlock");
        assertMethod(CFG, "getStateForPlacement", null);
        assertMethod(CFG, "canSpreadInto", null);
        assertMethod(CFG, "canSpreadFrom", null);
        assertMethod(CFG, "getSpreadTypes", null);
        assertMethod(CFG, "isOtherBlockValidAsSource", null);
        assertMethod(CFG, "placeBlock", null);
        assertCalls(CFG, "placeBlock", "setBlock");
        assertCalls(DEFAULT, "canSpreadInto", "isValidStateForPlacement");
    }

    @Test void vineHasIndependentGrowthAndNeoForgeLoadedAreaSafety() throws IOException {
        assertMethod(VINE, "getUpdatedState", "(" + STATE + READER + POS + ")" + STATE);
        assertMethod(VINE, "canSupportAtFace", "(" + READER + POS + DIR + ")Z");
        assertMethod(VINE, "isAcceptableNeighbour", "(" + READER + POS + DIR + ")Z");
        assertMethod(VINE, "randomTick", null);
        assertCalls(VINE, "randomTick", "isAreaLoaded");
        assertCalls(VINE, "randomTick", "getPropertyForFace");
        assertCalls(VINE, "randomTick", "setBlock");
        assertCalls(VINE, "getUpdatedState", "above");
        assertCalls(VINE, "getUpdatedState", "isAcceptableNeighbour");
        assertCalls(VINE, "updateShape", "getUpdatedState");
        assertCalls(VINE, "canSurvive", "getUpdatedState");
        assertCalls(VINE, "isAcceptableNeighbour", "canAttachTo");
        assertCalls(VINE, "rotate", "setValue");
        assertCalls(VINE, "mirror", "setValue");
        assertMethod(VINE, "getStateForPlacement", "(" + PLACEMENT + ")" + STATE);
    }

    @Test void glowLichenAndSculkVeinPreserveIndependentPropagationOwners()
            throws IOException {
        assertSuperclass(GLOW, MULTI);
        assertSuperclass(SCULK, MULTI);
        assertMethod(GLOW, "getSpreader", "()L" + SPREADER + ";");
        assertMethod(GLOW, "performBonemeal", null);
        assertCalls(GLOW, "performBonemeal", "spreadFromRandomFaceTowardRandomDirection");
        assertMethod(SCULK, "getSpreader", "()L" + SPREADER + ";");
        assertMethod(SCULK, "getSameSpaceSpreader", "()L" + SPREADER + ";");
        assertMethod(SCULK, "regrow", null);
        assertMethod(SCULK, "attemptUseCharge", null);
        assertMethod(SCULK, "onDischarged", null);
        assertCalls(SCULK, "regrow", "canAttachTo");
        assertCalls(SCULK, "attemptPlaceSculk", "spreadAll");
        assertMethod(SCULK + "$SculkVeinSpreaderConfig", "getSpreadTypes", null);
        assertMethod(SCULK + "$SculkVeinSpreaderConfig", "isOtherBlockValidAsSource", null);
    }

    @Test void worldgenMayWriteMultifaceWithoutItemPlacementAndCanThenSpread()
            throws IOException {
        assertMethod(GROWTH, "placeGrowthIfPossible", null);
        assertCalls(GROWTH, "placeGrowthIfPossible", "spreadFromFaceTowardRandomDirection");
        assertCalls(GROWTH, "placeGrowthIfPossible", "getStateForPlacement");
        assertCalls(GROWTH, "placeGrowthIfPossible", "setBlock");
    }

    private static void assertSuperclass(String owner, String expected) throws IOException {
        assertEquals(expected, reader(owner).getSuperName(),
                "Wrong concrete owner inheritance " + owner);
    }

    private static void assertField(String owner, String key) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int a, String n, String d,
                    String sig, Object val) {
                if (n.equals(key)) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing compiled field " + owner + "." + key);
    }

    private static List<String> enumConstants(String owner) throws IOException {
        List<String> out = new ArrayList<>();
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int a, String n, String d,
                    String sig, Object val) {
                if ((a & Opcodes.ACC_ENUM) != 0) out.add(n);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        return out;
    }

    private static void assertMethod(String owner, String method, String desc) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] throwsList) {
                if (n.equals(method) && (desc == null || desc.equals(d))) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing actual target method " + owner +
                "::" + method + (desc == null ? "" : desc));
    }

    private static void assertCalls(String owner, String source, String target)
            throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] throwsList) {
                if (!source.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String calleeOwner,
                            String name, String descriptor, boolean isInterface) {
                        if (target.equals(name)) found[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing actual target invoke " + owner +
                "::" + source + " -> " + target);
    }

    private static ClassReader reader(String owner) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(in, "Missing actual resolved NeoForge21.1.215 class " + owner);
            return new ClassReader(in);
        }
    }
}
