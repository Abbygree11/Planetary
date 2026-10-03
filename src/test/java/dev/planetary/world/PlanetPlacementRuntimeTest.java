package dev.planetary.world;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetPlacementRuntimeTest {
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(
                    new PlanetCore(
                            0,
                            0,
                            0,
                            20
                    )
            );

    @Test
    void reframingToTargetPreservesPhysicalDirectionAcrossCanonicalSeam() {
        Level level =
                mock(Level.class);
        PlanetGravityRuntime.bind(
                level,
                FIELD
        );

        try {
            BlockPos source =
                    new BlockPos(
                            20,
                            20,
                            0
                    );

            PlanetBlockNeighborQuery query =
                    PlanetBlockRuntime.neighbor(
                            level,
                            source,
                            Direction.WEST
                    ).orElseThrow();

            assertTrue(
                    query.sourceStateFrame().face()
                            != query.targetStateFrame().face()
            );

            Direction sourceLocal =
                    Direction.SOUTH;
            Direction targetLocal =
                    PlanetPlacementRuntime.reframeToTarget(
                            query,
                            sourceLocal
                    );

            assertEquals(
                    query.sourceStateFrame()
                            .localToWorld(
                                    sourceLocal
                            ),
                    query.targetStateFrame()
                            .localToWorld(
                                    targetLocal
                            )
            );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }
}
