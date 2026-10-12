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
 * 2.3C-17: selected actual Gradle-resolved NeoForge 21.1.215 JVM ABI
 * for complete independent rail graph algorithm, not applied Mixin/runtime.
 */
final class Phase23C17RailGraphNeoForgeAsmContractTest {
    private static final String MC = "net/minecraft/";
    private static final String B = MC + "world/level/block/";
    private static final String RAIL = B + "RailState";
    private static final String BASE = B + "BaseRailBlock";
    private static final String STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String READER = "Lnet/minecraft/world/level/BlockGetter;";
    private static final String SHAPE = "Lnet/minecraft/world/level/block/state/properties/RailShape;";
    private static final String CART = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;";
    private static final String ROT = "Lnet/minecraft/world/level/block/Rotation;";
    private static final String MIRROR = "Lnet/minecraft/world/level/block/Mirror;";
    private static final String EXT = "net/neoforged/neoforge/common/extensions/IBaseRailBlockExtension";
    private static final String CART_EXT = "net/neoforged/neoforge/common/extensions/IAbstractMinecartExtension";

    private record Call(String owner, String name, String desc) {}

    @Test void originalRailShapeHasTenPhysicalShapesAndAscendingSubdomain() throws IOException {
        List<String> vals = enumFields(B + "state/properties/RailShape");
        assertEquals(10, vals.size(), "RailShape is not just two physical horizontal directions");
        assertTrue(vals.containsAll(List.of("NORTH_SOUTH", "EAST_WEST",
                "ASCENDING_NORTH", "ASCENDING_SOUTH", "ASCENDING_EAST", "ASCENDING_WEST",
                "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST")));
        assertDeclared(B + "state/properties/RailShape", "isAscending", "()Z");
        assertDeclared(BASE, "getShapeProperty", "()Lnet/minecraft/world/level/block/state/properties/Property;");
    }

    @Test void railStateUsesPhysicalWorldXZAndWorldAboveBelowInSeparateGraphMethods() throws IOException {
        assertDeclared(RAIL, "<init>", "(" + LEVEL + POS + STATE + ")V");
        assertDeclared(RAIL, "updateConnections", "(" + SHAPE + ")V");
        assertDeclared(RAIL, "hasRail", "(" + POS + ")Z");
        assertDeclared(RAIL, "getRail", "(" + POS + ")L" + RAIL + ";");
        assertDeclared(RAIL, "hasConnection", "(" + POS + ")Z");
        assertDeclared(RAIL, "countPotentialConnections", "()I");
        assertDeclared(RAIL, "removeSoftConnections", "()V");
        assertDeclared(RAIL, "canConnectTo", "(L" + RAIL + ";)Z");
        assertDeclared(RAIL, "connectTo", "(L" + RAIL + ";)V");
        assertDeclared(RAIL, "place", "(ZZ" + SHAPE + ")L" + RAIL + ";");
        assertDeclared(RAIL, "getState", "()" + STATE);
        assertCalls(RAIL, "updateConnections", "above");
        assertCalls(RAIL, "getRail", "above");
        assertCalls(RAIL, "getRail", "below");
        assertCalls(RAIL, "hasRail", "above");
        assertCalls(RAIL, "hasRail", "below");
        assertCalls(RAIL, "hasConnection", "getX");
        assertCalls(RAIL, "hasConnection", "getZ");
        assertCalls(RAIL, "countPotentialConnections", "relative");
        assertCalls(RAIL, "place", "setBlock");
        assertCalls(RAIL, "connectTo", "setBlock");
    }

    @Test void actualNeoForgeRailExtensionRequiresPerBlockValidSlopeAndDirectionSemantics()
            throws IOException {
        assertDeclared(EXT, "isFlexibleRail", "(" + STATE + READER + POS + ")Z");
        assertDeclared(EXT, "canMakeSlopes", "(" + STATE + READER + POS + ")Z");
        assertDeclared(EXT, "getRailDirection", "(" + STATE + READER + POS + CART + ")" + SHAPE);
        assertDeclared(EXT, "isValidRailShape", "(" + SHAPE + ")Z");
        assertDeclared(EXT, "onMinecartPass", "(" + STATE + LEVEL + POS + CART + ")V");
        assertDeclared(EXT, "getRailMaxSpeed", "(" + STATE + LEVEL + POS + CART + ")F");
        assertDeclared(BASE, "isFlexibleRail", "(" + STATE + READER + POS + ")Z");
        assertDeclared(BASE, "getRailDirection", "(" + STATE + READER + POS + CART + ")" + SHAPE);
        assertField(RAIL, "canMakeSlopes");
        assertCalls(RAIL, "<init>", "getRailDirection");
        assertCalls(RAIL, "<init>", "isFlexibleRail");
        assertCalls(RAIL, "<init>", "canMakeSlopes");
        assertCalls(RAIL, "connectTo", "isValidRailShape");
        assertCalls(RAIL, "place", "isValidRailShape");
        assertCalls(BASE, "neighborChanged", "getRailDirection");
    }

    @Test void railPlacementSurvivalAndNeighborAuthoringAreIndependentFromItemUse() throws IOException {
        assertDeclared(BASE, "getStateForPlacement",
                "(Lnet/minecraft/world/item/context/BlockPlaceContext;)" + STATE);
        assertDeclared(BASE, "canSurvive", "(" + STATE +
                "Lnet/minecraft/world/level/LevelReader;" + POS + ")Z");
        assertDeclared(BASE, "neighborChanged", "(" + STATE + LEVEL + POS +
                "Lnet/minecraft/world/level/block/Block;" + POS + "Z)V");
        assertDeclared(BASE, "updateDir", "(" + LEVEL + POS + STATE + "Z)" + STATE);
        assertCalls(BASE, "updateDir", "place");
        assertCalls(BASE, "neighborChanged", "shouldBeRemoved");
        assertCalls(BASE, "onPlace", "updateState");
        assertCalls(BASE, "updateShape", "scheduleTick");
        assertCalls(BASE, "getStateForPlacement", "getFluidState");
        assertCalls(BASE, "shouldBeRemoved", "isAscending");
    }

    @Test void allThreeConcreteVanillaOwnersRotateAndMirrorDifferentRailPropertyDomains()
            throws IOException {
        for (String cls : List.of("RailBlock", "PoweredRailBlock", "DetectorRailBlock")) {
            String owner = B + cls;
            assertDeclared(owner, "rotate", "(" + STATE + ROT + ")" + STATE);
            assertDeclared(owner, "mirror", "(" + STATE + MIRROR + ")" + STATE);
        }
        assertDeclared(B + "RailBlock", "getShapeProperty",
                "()Lnet/minecraft/world/level/block/state/properties/Property;");
        assertDeclared(B + "PoweredRailBlock", "getShapeProperty",
                "()Lnet/minecraft/world/level/block/state/properties/Property;");
        assertDeclared(B + "DetectorRailBlock", "getShapeProperty",
                "()Lnet/minecraft/world/level/block/state/properties/Property;");
        assertDeclared(B + "PoweredRailBlock", "findPoweredRailSignal", null);
        assertDeclared(B + "PoweredRailBlock", "isSameRailWithPower", null);
        assertDeclared(B + "DetectorRailBlock", "checkPressed", null);
        assertDeclared(B + "DetectorRailBlock", "updatePowerToConnected", null);
        assertCalls(B + "PoweredRailBlock", "findPoweredRailSignal", "isSameRailWithPower");
        assertCalls(B + "DetectorRailBlock", "checkPressed", "updatePowerToConnected");
    }

    @Test void neoForgeMinecartMovementIsASeparateRailConsumerAndPhase7Owner() throws IOException {
        String owner = MC + "world/entity/vehicle/AbstractMinecart";
        assertDeclared(owner, "moveAlongTrack", "(" + POS + STATE + ")V");
        assertDeclared(CART_EXT, "moveMinecartOnRail", "(" + POS + ")V");
        assertDeclared(CART_EXT, "shouldDoRailFunctions", "()Z");
        assertDeclared(CART_EXT, "canUseRail", "()Z");
        assertDeclared(CART_EXT, "getCurrentRailPosition", "()" + POS);
        assertCalls(owner, "moveAlongTrack", "getRailDirection");
        assertCalls(owner, "moveAlongTrack", "onMinecartPass");
    }

    private static void assertField(String cls, String target) throws IOException {
        final boolean[] found = {false};
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int a, String n, String d,
                    String sig, Object val) {
                if (n.equals(target)) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], cls + " missing rail extension field " + target);
    }

    private static List<String> enumFields(String cls) throws IOException {
        List<String> fields = new ArrayList<>();
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int a, String n, String d,
                    String sig, Object val) {
                if ((a & Opcodes.ACC_ENUM) != 0) fields.add(n);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        return fields;
    }

    private static void assertDeclared(String cls, String name, String signature)
            throws IOException {
        final boolean[] found = {false};
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] except) {
                if (n.equals(name) && (signature == null || d.equals(signature))) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing actual target rail method " + cls + "::" + name +
                (signature == null ? "" : signature));
    }

    private static void assertCalls(String cls, String method, String callee) throws IOException {
        final boolean[] found = {false};
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                    String sig, String[] except) {
                if (!n.equals(method)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String owner,
                            String target, String descriptor, boolean isInterface) {
                        if (target.equals(callee)) found[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing actual target callsite " + cls + "::" + method + " -> " + callee);
    }

    private static ClassReader reader(String cls) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(cls + ".class")) {
            assertNotNull(stream, "Missing actual Gradle classpath owner: " + cls);
            return new ClassReader(stream);
        }
    }
}
