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
 * 2.3C-16: strict selected ACTUAL Gradle-loaded NeoForge 21.1.215 ABI.
 * P09 FrontAndTop, P10 ROTATION_16, P35 context-aware BlockState transforms.
 * Does not prove Mixin weaving, mod binaries or gameplay acceptance.
 */
final class Phase23C16PropertyOrientationTransformAsmContractTest {
    private static final String N = "net/minecraft/";
    private static final String W = N + "world/level/";
    private static final String B = W + "block/";
    private static final String ST = B + "state/";
    private static final String SP = ST + "properties/";
    private static final String SC = W + "levelgen/structure/templatesystem/StructureTemplate";
    private static final String F = N + "core/FrontAndTop";
    private static final String D = "Lnet/minecraft/core/Direction;";
    private static final String BS = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String R = "Lnet/minecraft/world/level/block/Rotation;";
    private static final String M = "Lnet/minecraft/world/level/block/Mirror;";
    private static final String BP = "Lnet/minecraft/core/BlockPos;";
    private static final String LA = "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String PL = "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String A_ROT = "(" + BS + R + ")" + BS;
    private static final String A_MIRROR = "(" + BS + M + ")" + BS;
    private static final String CONTEXT_ROT = "(" + LA + BP + R + ")" + BS;

    private record Call(String owner, String method, String descriptor) {}

    @Test void frontAndTopHasTwelveOrthonormalNamedStatesAndAResolvingPair() throws IOException {
        List<String> members = enumFields(F);
        assertEquals(12, members.size(), "FrontAndTop must not collapse to six facing states");
        assertTrue(members.containsAll(List.of("NORTH_UP", "SOUTH_UP", "EAST_UP", "WEST_UP",
                "UP_NORTH", "DOWN_NORTH")));
        assertDeclares(F, "front", "()" + D);
        assertDeclares(F, "top", "()" + D);
        assertDeclares(F, "fromFrontAndTop", "(" + D + D + ")L" + F + ";");
        assertField(SP + "BlockStateProperties", "ORIENTATION");
    }

    @Test void crafterAndJigsawAuthorAndTransformBothOrientationAxes() throws IOException {
        for (String owner : List.of(B + "CrafterBlock", B + "JigsawBlock")) {
            assertDeclares(owner, "getStateForPlacement", "(" + PL + ")" + BS);
            assertDeclares(owner, "rotate", A_ROT);
            assertDeclares(owner, "mirror", A_MIRROR);
            assertCall(owner, "getStateForPlacement", "fromFrontAndTop");
            assertCall(owner, "rotate", "rotation");
            assertCall(owner, "mirror", "rotation");
        }
        assertDeclares(B + "JigsawBlock", "getFrontFacing", "(" + BS + ")" + D);
        assertDeclares(B + "JigsawBlock", "getTopFacing", "(" + BS + ")" + D);
        assertDeclares(B + "JigsawBlock", "canAttach", null);
    }

    @Test void sixteenSegmentStateHasIndependentConcreteAuthors() throws IOException {
        assertField(SP + "BlockStateProperties", "ROTATION_16");
        String seg = SP + "RotationSegment";
        assertDeclares(seg, "convertToSegment", "(F)I");
        assertDeclares(seg, "convertToDegrees", "(I)F");
        for (String owner : List.of(B + "StandingSignBlock", B + "CeilingHangingSignBlock",
                B + "BannerBlock", B + "SkullBlock")) {
            assertField(owner, "ROTATION");
            assertDeclares(owner, "getStateForPlacement", "(" + PL + ")" + BS);
            assertDeclares(owner, "rotate", A_ROT);
            assertDeclares(owner, "mirror", A_MIRROR);
            assertCall(owner, "getStateForPlacement", "convertToSegment");
            assertCall(owner, "rotate", "rotate");
            assertCall(owner, "mirror", "mirror");
        }
        // Wall mounting deliberately uses four-way FACING, NOT 16 yaw positions.
        for (String owner : List.of(B + "WallSignBlock", B + "WallHangingSignBlock",
                B + "WallBannerBlock")) {
            assertField(owner, "FACING");
            assertDeclares(owner, "getStateForPlacement", "(" + PL + ")" + BS);
        }
    }

    @Test void polymorphicBlockStateTransformAndNeoForgeLevelAwareExtensionAreDistinct() throws IOException {
        String base = ST + "BlockBehaviour$BlockStateBase";
        assertDeclares(base, "rotate", "(" + R + ")" + BS);
        assertDeclares(base, "mirror", "(" + M + ")" + BS);
        assertCall(base, "rotate", "rotate");
        assertCall(base, "mirror", "mirror");
        String stateExtension = "net/neoforged/neoforge/common/extensions/IBlockStateExtension";
        String blockExtension = "net/neoforged/neoforge/common/extensions/IBlockExtension";
        assertDeclares(stateExtension, "rotate", CONTEXT_ROT);
        assertDeclares(blockExtension, "rotate", "(" + BS + LA + BP + R + ")" + BS);
        assertCall(stateExtension, "rotate", "rotate");
        assertCall(blockExtension, "rotate", "rotate");
        // The world-aware NeoForge overload is not the same signature as pure
        // BlockStateBase.rotate(Rotation); do not substitute either globally.
    }

    @Test void originalStructureTemplateAuthorAppliesMirrorBeforeRotateAndKeepsPhysicalPos() throws IOException {
        String place = "(" + "Lnet/minecraft/world/level/ServerLevelAccessor;" + BP + BP +
                "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;" +
                "Lnet/minecraft/util/RandomSource;I)Z";
        assertDeclares(SC, "placeInWorld", place);
        assertCall(SC, "placeInWorld", "mirror");
        assertCall(SC, "placeInWorld", "rotate");
        assertCall(SC, "placeInWorld", "setBlock");
        assertDeclares(SC, "calculateRelativePosition", null);
        assertDeclares(SC, "processBlockInfos", null); // NeoForge template-aware processors
    }

    @Test void vanillaRotationMirrorIntegerAndDirectionContractsCoexist() throws IOException {
        assertDeclares(B + "Rotation", "rotate", "(II)I");
        assertDeclares(B + "Rotation", "rotate", "(" + D + ")" + D);
        assertDeclares(B + "Mirror", "mirror", "(II)I");
        assertDeclares(B + "Mirror", "mirror", "(" + D + ")" + D);
        assertDeclares(B + "Mirror", "getRotation", "(" + D + ")" + R);
    }

    private static void assertField(String owner, String name) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int access, String n, String d,
                    String signature, Object value) {
                if (n.equals(name)) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Missing selected field " + owner + "#" + name);
    }

    private static List<String> enumFields(String owner) throws IOException {
        List<String> result = new ArrayList<>();
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int access, String n, String d,
                    String signature, Object value) {
                if ((access & Opcodes.ACC_ENUM) != 0) result.add(n);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return result;
    }

    private static void assertDeclares(String owner, String name, String desc) throws IOException {
        assertDeclares(owner, name, desc, true);
    }

    private static void assertDeclares(String owner, String name, String desc, boolean expected)
            throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String n, String d,
                    String signature, String[] exceptions) {
                if (n.equals(name) && (desc == null || d.equals(desc))) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(expected, found[0], "Unexpected declared method " + owner +
                "::" + name + (desc == null ? "" : desc));
    }

    private static void assertCall(String owner, String sourceMethod, String targetName)
            throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String desc,
                    String signature, String[] exceptions) {
                if (!name.equals(sourceMethod)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String calleeOwner,
                            String callee, String descriptor, boolean itf) {
                        if (callee.equals(targetName)) found[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing declared method call " +
                owner + "::" + sourceMethod + " -> " + targetName);
    }

    private static ClassReader reader(String owner) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(in, "Missing actual Gradle-loaded 21.1.215 owner " + owner);
            return new ClassReader(in);
        }
    }
}
