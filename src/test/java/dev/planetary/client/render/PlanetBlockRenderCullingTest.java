package dev.planetary.client.render;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class PlanetBlockRenderCullingTest {
    private static final int R = 20;
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(
                    new PlanetCore(
                            0,
                            0,
                            0,
                            R
                    )
            );

    @Test
    void seamCullingUsesEachBlocksCanonicalLocalSide() {
        Level level = mock(Level.class);
        BlockState sourceState =
                mock(BlockState.class);
        BlockState targetState =
                mock(BlockState.class);

        BlockPos sourcePos =
                new BlockPos(
                        R,
                        R,
                        0
                );
        BlockPos targetPos =
                sourcePos.west();

        PlanetGravityRuntime.bind(
                level,
                FIELD
        );

        try {
            when(level.getBlockState(targetPos))
                    .thenReturn(targetState);

            when(sourceState.skipRendering(
                    targetState,
                    Direction.DOWN
            )).thenReturn(false);

            when(targetState.hidesNeighborFace(
                    level,
                    targetPos,
                    sourceState,
                    Direction.EAST
            )).thenReturn(false);

            when(targetState.canOcclude())
                    .thenReturn(true);

            when(sourceState.getFaceOcclusionShape(
                    level,
                    sourcePos,
                    Direction.DOWN
            )).thenReturn(
                    Shapes.block()
            );

            when(targetState.getFaceOcclusionShape(
                    level,
                    targetPos,
                    Direction.EAST
            )).thenReturn(
                    Shapes.empty()
            );

            assertTrue(
                    PlanetBlockRenderCulling
                            .shouldRenderFace(
                                    sourceState,
                                    level,
                                    sourcePos,
                                    Direction.WEST,
                                    targetPos
                            )
                            .orElseThrow()
            );

            verify(sourceState)
                    .skipRendering(
                            targetState,
                            Direction.DOWN
                    );
            verify(targetState)
                    .hidesNeighborFace(
                            level,
                            targetPos,
                            sourceState,
                            Direction.EAST
                    );
            verify(sourceState)
                    .getFaceOcclusionShape(
                            level,
                            sourcePos,
                            Direction.DOWN
                    );
            verify(targetState)
                    .getFaceOcclusionShape(
                            level,
                            targetPos,
                            Direction.EAST
                    );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }
}
