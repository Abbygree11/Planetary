package dev.planetary.world;

import org.junit.jupiter.api.Test;
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
 * 2.3C-2 exact Gradle-loaded NeoForge 21.1.215 owner/call-site contracts.
 * JVM-level checks only: they do not imply game, Mixin weave or mod acceptance.
 * Keep outside the reserved dev.planetary.mixin package.
 */
final class Phase23C2PlacementContextAsmContractTest {
    private static final String C = "net/minecraft/world/item/context/";
    private static final String I = "net/minecraft/world/item/";
    private static final String USE = C + "UseOnContext";
    private static final String PLACE = C + "BlockPlaceContext";
    private static final String DIRECTIONAL = C + "DirectionalPlaceContext";
    private static final String SCAFFOLD = I + "ScaffoldingBlockItem";
    private static final String WATER = I + "PlaceOnWaterBlockItem";
    private static final String SOLID = I + "SolidBucketItem";
    private static final String BLOCK_ITEM = I + "BlockItem";

    private static final String D = "Lnet/minecraft/core/Direction;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String PLAYER = "Lnet/minecraft/world/entity/player/Player;";
    private static final String HAND = "Lnet/minecraft/world/InteractionHand;";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String HIT = "Lnet/minecraft/world/phys/BlockHitResult;";
    private static final String STATE =
            "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String USE_CONTEXT =
            "Lnet/minecraft/world/item/context/UseOnContext;";
    private static final String PLACE_CONTEXT =
            "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String RESULT = "Lnet/minecraft/world/InteractionResult;";
    private static final String RESULT_HOLDER =
            "Lnet/minecraft/world/InteractionResultHolder;";
    private static final String SOUND = "Lnet/minecraft/sounds/SoundEvent;";

    private static final String USE_ON = "(" + USE_CONTEXT + ")" + RESULT;
    private static final String UPDATE = "(" + PLACE_CONTEXT + ")" + PLACE_CONTEXT;
    private static final String USE_ITEM = "(" + LEVEL + PLAYER + HAND + ")" + RESULT_HOLDER;
    private static final String EMPTY =
            "(" + PLAYER + LEVEL + POS + HIT + ")Z";
    private static final String PLACE_SOUND =
            "(" + STATE + LEVEL + POS + PLAYER + ")" + SOUND;

    @Test
    void contextInheritanceAndOriginalPhysicalHitAccessors() throws IOException {
        assertEquals(USE, parent(PLACE));
        assertEquals(PLACE, parent(DIRECTIONAL));
        assertEquals(BLOCK_ITEM, parent(SCAFFOLD));
        assertEquals(BLOCK_ITEM, parent(WATER));
        assertEquals(BLOCK_ITEM, parent(SOLID));
        for (String method : List.of("getClickedPos", "getClickedFace")) {
            assertTrue(methods(USE).stream().anyMatch(m -> m.name.equals(method)));
        }

        assertEquals(1L, count(calls(USE, "getClickedPos", "()" + POS),
                "net/minecraft/world/phys/BlockHitResult",
                "getBlockPos", "()" + POS, Opcodes.INVOKEVIRTUAL));
        assertEquals(1L, count(calls(USE, "getClickedFace", "()" + D),
                "net/minecraft/world/phys/BlockHitResult",
                "getDirection", "()" + D, Opcodes.INVOKEVIRTUAL));
        assertTrue(declares(USE, "getHorizontalDirection", "()" + D));
        assertTrue(declares(USE, "getRotation", "()F"));
    }

    @Test
    void blockPlaceContextKeepsRelativePhysicalPositionAndRetargeting()
            throws IOException {
        String constructor =
                "(" + LEVEL + PLAYER + HAND + STACK + HIT + ")V";
        assertTrue(declares(PLACE, "<init>", constructor));
        assertEquals(1L, count(calls(PLACE, "<init>", constructor),
                "net/minecraft/core/BlockPos", "relative",
                "(" + D + ")" + POS, Opcodes.INVOKEVIRTUAL));

        String retarget =
                "(" + PLACE_CONTEXT + POS + D + ")" + PLACE_CONTEXT;
        assertTrue(declares(PLACE, "at", retarget));
        assertTrue(declares(PLACE, "getClickedPos", "()" + POS));
        assertTrue(declares(PLACE, "canPlace", "()Z"));
        assertTrue(declares(PLACE, "replacingClickedOnBlock", "()Z"));
        assertTrue(declares(PLACE, "getNearestLookingDirections",
                "()[Lnet/minecraft/core/Direction;"));
        assertEquals(1L, count(calls(PLACE, "at", retarget),
                PLACE, "<init>", constructor, Opcodes.INVOKESPECIAL));
    }

    @Test
    void nullPlayerDirectionalContextOwnsIndependentDirectionOrder()
            throws IOException {
        String ctor = "(" + LEVEL + POS + D + STACK + D + ")V";
        assertTrue(declares(DIRECTIONAL, "<init>", ctor));
        assertEquals(1L, count(calls(DIRECTIONAL, "<init>", ctor),
                PLACE, "<init>",
                "(" + LEVEL + PLAYER + HAND + STACK + HIT + ")V",
                Opcodes.INVOKESPECIAL));

        for (Method method : List.of(
                new Method("getClickedPos", "()" + POS),
                new Method("canPlace", "()Z"),
                new Method("replacingClickedOnBlock", "()Z"),
                new Method("getNearestLookingDirections",
                        "()[Lnet/minecraft/core/Direction;"),
                new Method("getHorizontalDirection", "()" + D),
                new Method("getRotation", "()F"),
                new Method("isSecondaryUseActive", "()Z")
        )) assertTrue(declares(DIRECTIONAL, method.name, method.desc),
                "Independent synthetic override missing: " + method.name);

        // The supplier is deliberately fixed physical order; no player
        // orientation API belongs in this method.
        List<Call> order = calls(DIRECTIONAL, "getNearestLookingDirections",
                "()[Lnet/minecraft/core/Direction;");
        assertTrue(order.stream().noneMatch(c ->
                c.name.equals("orderedByNearest") ||
                c.name.equals("getDirection") ||
                c.name.equals("getYRot")));
    }

    @Test
    void scaffoldingRetargetsContextRatherThanEditingPhysicalHit()
            throws IOException {
        assertTrue(declares(SCAFFOLD, "updatePlacementContext", UPDATE));
        List<Call> calls = calls(SCAFFOLD, "updatePlacementContext", UPDATE);
        assertEquals(1L, count(calls, PLACE, "at",
                "(" + PLACE_CONTEXT + POS + D + ")" + PLACE_CONTEXT,
                Opcodes.INVOKESTATIC));
        assertTrue(calls.stream().anyMatch(c ->
                c.name.equals("getDistance")), "Scaffolding support distance lost");
        assertTrue(calls.stream().anyMatch(c ->
                c.name.equals("move")), "Physical mutable BlockPos movement lost");
        assertTrue(calls.stream().anyMatch(c ->
                c.name.equals("getHorizontalDirection")));
        assertTrue(declares(SCAFFOLD, "mustSurvive", "()Z"));
    }

    @Test
    void waterRaycastPathBypassesUseOnAndConstructsSecondaryPlacement()
            throws IOException {
        assertTrue(declares(WATER, "useOn", USE_ON));
        assertTrue(declares(WATER, "use", USE_ITEM));
        List<Call> direct = calls(WATER, "use", USE_ITEM);
        assertEquals(1L, count(direct, "net/minecraft/core/BlockPos",
                "above", "()" + POS, Opcodes.INVOKEVIRTUAL));
        assertEquals(1L, count(direct, "net/minecraft/world/phys/BlockHitResult",
                "withPosition", "(" + POS + ")" + HIT, Opcodes.INVOKEVIRTUAL));
        assertEquals(1L, count(direct, BLOCK_ITEM, "useOn",
                USE_ON, Opcodes.INVOKESPECIAL));
        assertTrue(direct.stream().anyMatch(c ->
                        c.name.equals("getPlayerPOVHitResult")),
                "Item raycast on water source disappeared");
        assertFalse(calls(WATER, "useOn", USE_ON).stream()
                        .anyMatch(c -> c.name.equals("place") ||
                                c.name.equals("setBlock")),
                "Click-on-block PASS unexpectedly became a placement writer");
    }

    @Test
    void solidBucketHasBothNormalBlockItemAndDispenserWriters()
            throws IOException {
        assertTrue(interfaces(SOLID).contains(I + "DispensibleContainerItem"));
        assertTrue(declares(SOLID, "useOn", USE_ON));
        assertTrue(declares(SOLID, "emptyContents", EMPTY));
        assertEquals(1L, count(calls(SOLID, "useOn", USE_ON),
                BLOCK_ITEM, "useOn", USE_ON, Opcodes.INVOKESPECIAL));
        List<Call> direct = calls(SOLID, "emptyContents", EMPTY);
        assertTrue(direct.stream().anyMatch(c ->
                c.name.equals("setBlock")), "Direct dispenser world writer missing");
        assertFalse(direct.stream().anyMatch(c ->
                c.name.equals("place") || c.name.equals("placeBlock")),
                "Dispenser path now delegates to BlockItem: audit semantics");
    }

    @Test
    void neoforge215SolidBucketMissingLaterSensitiveSoundBridge()
            throws IOException {
        // Evidence from the ACTUAL Gradle-resolved NeoForge 21.1.215 bytecode:
        // SolidBucketItem owns the old override, but NOT the context-sensitive
        // getPlaceSound(BlockState, Level, BlockPos, Player) override.
        // This is a confirmed VERSION DELTA vs NeoForge official later source
        // 3cae645, which adds the bridge. Do NOT claim that the later patch
        // is installed or that the powder-snow sound is gameplay-verified.
        assertTrue(declares(SOLID, "getPlaceSound", "(" + STATE + ")" + SOUND));
        assertFalse(declares(SOLID, "getPlaceSound", PLACE_SOUND),
                "Target's known missing 21.1.215 bridge has changed; re-audit");
        assertTrue(declares(BLOCK_ITEM, "getPlaceSound", PLACE_SOUND),
                "Base NeoForge sensitive sound ABI unexpectedly absent");
        String place = "(" + PLACE_CONTEXT + ")" + RESULT;
        assertEquals(1L, count(calls(BLOCK_ITEM, "place", place),
                BLOCK_ITEM, "getPlaceSound", PLACE_SOUND,
                Opcodes.INVOKEVIRTUAL),
                "BlockItem.place expected to invoke contextual base method");
    }

    private record Method(String name, String desc) {}
    private record Call(int opcode, String owner, String name, String desc) {}

    private static ClassReader read(String name) throws IOException {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name + ".class")) {
            assertNotNull(stream, "Class not on exact Gradle test classpath: " + name);
            return new ClassReader(stream);
        }
    }

    private static String parent(String name) throws IOException {
        return read(name).getSuperName();
    }

    private static List<String> interfaces(String name) throws IOException {
        return List.of(read(name).getInterfaces());
    }

    private static List<Method> methods(String name) throws IOException {
        List<Method> result = new ArrayList<>();
        read(name).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String method,
                    String desc, String signature, String[] exceptions) {
                result.add(new Method(method, desc));
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return result;
    }

    private static boolean declares(String name, String method, String desc)
            throws IOException {
        return methods(name).contains(new Method(method, desc));
    }

    private static List<Call> calls(String clazz, String method, String desc)
            throws IOException {
        List<Call> result = new ArrayList<>();
        boolean[] found = {false};
        read(clazz).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String signature, String generic, String[] exceptions) {
                if (!method.equals(name) || !desc.equals(signature)) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner,
                            String member, String signature, boolean isInterface) {
                        result.add(new Call(opcode, owner, member, signature));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], clazz + "::" + method + desc + " missing");
        return result;
    }

    private static long count(List<Call> calls, String owner, String method,
            String desc, int opcode) {
        return calls.stream().filter(c ->
                c.owner.equals(owner) && c.name.equals(method) &&
                c.desc.equals(desc) && c.opcode == opcode).count();
    }
}
