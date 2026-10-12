package dev.planetary.world;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class PlanetTrapDoorPlacementTest {
    private static final PlanetCore CORE = new PlanetCore(3, 100, -7, 20);
    private static final PlanetGravityField FIELD = new PlanetGravityField(CORE);

    @Test
    void allSixFramesAllLocalClickedSidesAndTwoVerticalOffsetsMatchVanillaAtLocalY() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = center(face);
            PlanetBlockStateFrame state = PlanetBlockStateFrame.resolve(FIELD, target).orElseThrow();
            for (Direction localSide : Direction.values()) {
                Direction clickedPhysical = state.localToWorld(localSide);
                for (boolean upper : new boolean[]{false, true}) {
                    Vec3 worldHit = worldOffset(target, face, new Vec3(
                            .3, upper ? .75 : .25, .4));
                    PlanetBlockPlacementFrame placement = PlanetBlockPlacementFrame.resolve(
                            FIELD, target, clickedPhysical, worldHit).orElseThrow();

                    var result = PlanetTrapDoorPlacement.orient(
                            placement, false, Optional.of(Direction.NORTH)).orElseThrow();
                    if (localSide.getAxis().isHorizontal()) {
                        assertEquals(localSide, result.facing(), face + "/" + localSide);
                        assertEquals(upper ? Half.TOP : Half.BOTTOM, result.half());
                    } else {
                        assertEquals(Direction.SOUTH, result.facing());
                        assertEquals(localSide == Direction.UP ? Half.BOTTOM : Half.TOP,
                                result.half());
                    }

                    var replacing = PlanetTrapDoorPlacement.orient(
                            placement, true, Optional.of(Direction.EAST)).orElseThrow();
                    assertEquals(Direction.WEST, replacing.facing());
                    assertEquals(localSide == Direction.UP ? Half.BOTTOM : Half.TOP,
                            replacing.half());

                    if (localSide.getAxis().isVertical()) {
                        assertTrue(PlanetTrapDoorPlacement.orient(
                                placement, false, Optional.empty()).isEmpty());
                    }
                }
            }
        }
    }

    @Test
    void allTwelveEdgesAndEightCornersUseTargetNotAnArbitrarySourceChart() {
        int count = 0;
        for (int a = 0; a < 3; a++) for (int b = a + 1; b < 3; b++) {
            for (int x : new int[]{-20,20}) for (int y : new int[]{-20,20}) {
                int[] offset = new int[3];
                offset[a]=x; offset[b]=y;
                checkBoundary(new BlockPos(CORE.x()+offset[0],CORE.y()+offset[1],CORE.z()+offset[2]));
                count++;
            }
        }
        assertEquals(12,count);
        count=0;
        for (int x : new int[]{-20,20}) for (int y : new int[]{-20,20})
            for (int z : new int[]{-20,20}) {
                checkBoundary(new BlockPos(CORE.x()+x,CORE.y()+y,CORE.z()+z));
                count++;
            }
        assertEquals(8,count);
    }

    @Test
    void returnAdapterPreservesPowerWaterOpenAndActualConcreteBlockAcrossMaterials() {
        Level level = mock(Level.class);
        BlockPlaceContext context = mock(BlockPlaceContext.class);
        BlockPos target = center(PlanetFace.POS_X);
        PlanetBlockStateFrame state = PlanetBlockStateFrame.resolve(FIELD,target).orElseThrow();
        when(context.getLevel()).thenReturn(level);
        when(context.getClickedPos()).thenReturn(target);
        when(context.getClickedFace()).thenReturn(state.localToWorld(Direction.WEST));
        when(context.getClickLocation()).thenReturn(Vec3.atCenterOf(target));
        when(context.replacingClickedOnBlock()).thenReturn(false);
        PlanetGravityRuntime.bind(level,FIELD);
        try {
            for (BlockState base : new BlockState[]{
                    Blocks.OAK_TRAPDOOR.defaultBlockState(),
                    Blocks.IRON_TRAPDOOR.defaultBlockState(),
                    Blocks.COPPER_TRAPDOOR.defaultBlockState(),
                    Blocks.WAXED_COPPER_TRAPDOOR.defaultBlockState()
            }) {
                BlockState vanilla = base
                        .setValue(TrapDoorBlock.OPEN,true)
                        .setValue(TrapDoorBlock.POWERED,true)
                        .setValue(TrapDoorBlock.WATERLOGGED,true);
                BlockState actual = PlanetTrapDoorPlacement.reframe(context,vanilla)
                        .orElseThrow();
                assertSame(vanilla.getBlock(),actual.getBlock());
                assertEquals(Direction.WEST,actual.getValue(TrapDoorBlock.FACING));
                assertEquals(Half.BOTTOM,actual.getValue(TrapDoorBlock.HALF));
                assertTrue(actual.getValue(TrapDoorBlock.OPEN));
                assertTrue(actual.getValue(TrapDoorBlock.POWERED));
                assertTrue(actual.getValue(TrapDoorBlock.WATERLOGGED));
            }
        } finally {
            PlanetGravityRuntime.unbind(level,FIELD);
        }
    }

    @Test
    void unknownBodyCannotWriteGuessedDirectionAndOrdinaryWorldIsVanilla() {
        Level level = mock(Level.class);
        BlockPlaceContext ctx = mock(BlockPlaceContext.class);
        BlockPos target = center(PlanetFace.POS_X);
        PlanetBlockStateFrame frame = PlanetBlockStateFrame.resolve(FIELD,target).orElseThrow();
        when(ctx.getLevel()).thenReturn(level);
        when(ctx.getClickedPos()).thenReturn(target);
        when(ctx.getClickedFace()).thenReturn(frame.localToWorld(Direction.UP));
        when(ctx.getClickLocation()).thenReturn(Vec3.atCenterOf(target));
        when(ctx.replacingClickedOnBlock()).thenReturn(true);
        BlockState vanilla = Blocks.OAK_TRAPDOOR.defaultBlockState();
        assertTrue(PlanetTrapDoorPlacement.reframe(ctx,vanilla).isEmpty());

        PlanetGravityRuntime.bind(level,FIELD);
        try {
            // Null-player and unknown item authors never fabricate a body.
            assertTrue(PlanetTrapDoorPlacement.reframe(ctx,vanilla).isEmpty());
        } finally {
            PlanetGravityRuntime.unbind(level,FIELD);
        }
    }

    private static void checkBoundary(BlockPos target) {
        PlanetBlockStateFrame frame = PlanetBlockStateFrame.resolve(FIELD,target).orElseThrow();
        for (Direction local : Direction.Plane.HORIZONTAL) {
            Direction physical = frame.localToWorld(local);
            var placement = PlanetBlockPlacementFrame.resolve(
                    FIELD,target,physical,Vec3.atCenterOf(target)).orElseThrow();
            var out = PlanetTrapDoorPlacement.orient(
                    placement,false,Optional.empty()).orElseThrow();
            assertEquals(local,out.facing());
            assertEquals(Half.BOTTOM,out.half());
            assertEquals(physical,frame.localToWorld(out.facing()));
        }
    }

    private static Vec3 worldOffset(BlockPos target, PlanetFace face, Vec3 offset) {
        PlanetFrameVector vec = new PlanetGravityFrame(face).localToWorld(
                new PlanetFrameVector(offset.x-.5,offset.y-.5,offset.z-.5));
        return Vec3.atCenterOf(target).add(vec.x(),vec.y(),vec.z());
    }

    private static BlockPos center(PlanetFace face) {
        PlanetVector up = new PlanetGravityFrame(face).worldUp();
        return new BlockPos(CORE.x()+20*up.x(),CORE.y()+20*up.y(),CORE.z()+20*up.z());
    }
}
