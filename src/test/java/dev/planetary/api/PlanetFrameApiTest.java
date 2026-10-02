package dev.planetary.api;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

final class PlanetFrameApiTest {
    private static final int R = 20;
    private static final PlanetCore CORE =
            new PlanetCore(0, 100, 0, R);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void createLikeRawRelativeDiffersFromLocalNeighborOnRotatedFace() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos source = new BlockPos(R, 100, 0);

            PlanetFrameApi.BlockNeighbor neighbor =
                    PlanetFrameApi.localNeighbor(
                            level,
                            source,
                            Direction.UP
                    ).orElseThrow();

            BlockPos rawForeignTarget =
                    source.relative(Direction.UP);

            assertNotEquals(
                    rawForeignTarget,
                    neighbor.targetPos()
            );
            assertEquals(
                    source.east(),
                    neighbor.targetPos()
            );
            assertEquals(
                    Direction.EAST,
                    neighbor.physicalDirection()
            );
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void placementFrameConvertsPhysicalHitFaceAndLocalHitCoordinates() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos target =
                    new BlockPos(R, 100, 0);

            PlanetFrameApi.PlacementFrame placement =
                    PlanetFrameApi.placementFrame(
                            level,
                            target,
                            Direction.EAST,
                            new Vec3(
                                    target.getX() + 0.8D,
                                    target.getY() + 0.8D,
                                    target.getZ() + 0.3D
                            )
                    ).orElseThrow();

            assertEquals(PlanetFace.POS_X, placement.targetFace());
            assertEquals(Direction.UP, placement.localClickedFace());
            assertTrue(placement.localHitUpperHalf());
            assertEquals(
                    0.2D,
                    placement.localHitOffset().x,
                    1.0E-9D
            );
            assertEquals(
                    0.8D,
                    placement.localHitOffset().y,
                    1.0E-9D
            );
            assertEquals(
                    0.3D,
                    placement.localHitOffset().z,
                    1.0E-9D
            );
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void interactiveNearestDirectionsReorderUsingLocalClickedFace() {
        Level level = mock(Level.class);
        Player player = mock(
                Player.class,
                withSettings().extraInterfaces(
                        PlanetGravityEntity.class
                )
        );
        BlockPlaceContext context =
                mock(BlockPlaceContext.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos target =
                    new BlockPos(R, 100, 0);

            when(context.getLevel()).thenReturn(level);
            when(context.getClickedPos()).thenReturn(target);
            when(context.getClickedFace()).thenReturn(Direction.EAST);
            when(context.getClickLocation())
                    .thenReturn(Vec3.atCenterOf(target));
            when(context.getPlayer()).thenReturn(player);
            when(context.replacingClickedOnBlock())
                    .thenReturn(false);

            when(player.getViewXRot(1.0F)).thenReturn(0.0F);
            when(player.getViewYRot(1.0F)).thenReturn(0.0F);
            when(((PlanetGravityEntity) player)
                    .planetary$gravityFrame())
                    .thenReturn(
                            java.util.Optional.of(
                                    new PlanetGravityFrame(
                                            PlanetFace.POS_X
                                    )
                            )
                    );

            List<Direction> directions =
                    PlanetFrameApi.localNearestLookingDirections(
                            context
                    ).orElseThrow();

            // POS_X: physical EAST is local UP, so its opposite is local DOWN.
            assertEquals(Direction.DOWN, directions.get(0));
            assertEquals(6, new HashSet<>(directions).size());
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void nearestLookingDirectionsReframePlayerBodyToTargetCanonicalFrame() {
        Level level = mock(Level.class);
        Player player = mock(
                Player.class,
                withSettings().extraInterfaces(
                        PlanetGravityEntity.class
                )
        );
        BlockPlaceContext context =
                mock(BlockPlaceContext.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos target =
                    new BlockPos(R, 100, 0);

            when(context.getLevel()).thenReturn(level);
            when(context.getClickedPos()).thenReturn(target);
            when(context.getClickedFace()).thenReturn(Direction.EAST);
            when(context.getClickLocation())
                    .thenReturn(Vec3.atCenterOf(target));
            when(context.getPlayer()).thenReturn(player);
            when(context.replacingClickedOnBlock())
                    .thenReturn(true);

            // Looking straight up in a POS_Y body frame.
            when(player.getViewXRot(1.0F)).thenReturn(-90.0F);
            when(player.getViewYRot(1.0F)).thenReturn(0.0F);
            when(((PlanetGravityEntity) player)
                    .planetary$gravityFrame())
                    .thenReturn(
                            java.util.Optional.of(
                                    new PlanetGravityFrame(
                                            PlanetFace.POS_Y
                                    )
                            )
                    );

            List<Direction> directions =
                    PlanetFrameApi.localNearestLookingDirections(
                            context
                    ).orElseThrow();

            // POS_Y local UP = physical UP = POS_X target local WEST.
            assertEquals(Direction.WEST, directions.get(0));
            assertEquals(6, new HashSet<>(directions).size());
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void playerlessNearestDirectionsAreNotGuessed() {
        Level level = mock(Level.class);
        BlockPlaceContext context =
                mock(BlockPlaceContext.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos target =
                    new BlockPos(R, 100, 0);

            when(context.getLevel()).thenReturn(level);
            when(context.getClickedPos()).thenReturn(target);
            when(context.getClickedFace()).thenReturn(Direction.EAST);
            when(context.getClickLocation())
                    .thenReturn(Vec3.atCenterOf(target));
            when(context.getPlayer()).thenReturn(null);

            assertTrue(
                    PlanetFrameApi.localNearestLookingDirections(
                            context
                    ).isEmpty()
            );
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void edgeNeighborReportsPhysicalTargetAndCanonicalTargetSide() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos edge =
                    new BlockPos(R, 100 + R, 0);

            PlanetFrameApi.BlockNeighbor neighbor =
                    PlanetFrameApi.localNeighbor(
                            level,
                            edge,
                            Direction.DOWN
                    ).orElseThrow();

            assertEquals(PlanetFace.POS_X, neighbor.sourceFace());
            assertEquals(Direction.WEST, neighbor.physicalDirection());
            assertEquals(edge.west(), neighbor.targetPos());
            assertEquals(PlanetFace.POS_Y, neighbor.targetFace());
            assertEquals(
                    Direction.EAST,
                    neighbor.targetLocalSideTowardSource()
            );
            assertTrue(neighbor.crossedGravityBoundary());
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }
}
