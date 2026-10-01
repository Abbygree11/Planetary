package dev.planetary.api;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
