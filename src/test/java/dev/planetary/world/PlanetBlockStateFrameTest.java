package dev.planetary.world;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class PlanetBlockStateFrameTest {
    private static final int SHELL = 20;
    private static final PlanetCore CORE =
            new PlanetCore(3, 100, -7, SHELL);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void edgeTraversalChartsCanDifferButStateFrameIsPositionCanonical() {
        BlockPos edge =
                new BlockPos(
                        CORE.x() + SHELL,
                        CORE.y() + SHELL,
                        CORE.z()
                );

        PlanetBlockFrameContext topChart =
                PlanetBlockFrameContext.resolve(
                        FIELD,
                        edge,
                        PlanetFace.POS_Y
                ).orElseThrow();
        PlanetBlockFrameContext sideChart =
                PlanetBlockFrameContext.resolve(
                        FIELD,
                        edge,
                        PlanetFace.POS_X
                ).orElseThrow();

        assertNotEquals(
                topChart.face(),
                sideChart.face()
        );

        PlanetBlockStateFrame first =
                PlanetBlockStateFrame.resolve(
                        FIELD,
                        topChart.pos()
                ).orElseThrow();
        PlanetBlockStateFrame second =
                PlanetBlockStateFrame.resolve(
                        FIELD,
                        sideChart.pos()
                ).orElseThrow();

        assertEquals(PlanetFace.POS_X, first.face());
        assertEquals(first.face(), second.face());
        assertEquals(first.pos(), second.pos());
    }

    @Test
    void cornerStateFrameUsesCanonicalAxisPriorityIndependentOfTraversalChart() {
        BlockPos corner =
                new BlockPos(
                        CORE.x() + SHELL,
                        CORE.y() + SHELL,
                        CORE.z() + SHELL
                );

        for (PlanetFace preferred : new PlanetFace[]{
                PlanetFace.POS_X,
                PlanetFace.POS_Y,
                PlanetFace.POS_Z
        }) {
            PlanetBlockFrameContext traversal =
                    PlanetBlockFrameContext.resolve(
                            FIELD,
                            corner,
                            preferred
                    ).orElseThrow();

            assertEquals(
                    preferred,
                    traversal.face()
            );
            assertEquals(
                    PlanetFace.POS_X,
                    PlanetBlockStateFrame.resolve(
                            FIELD,
                            traversal.pos()
                    ).orElseThrow().face()
            );
        }
    }

    @Test
    void canonicalShapeOrientationDoesNotDependOnTraversalChart() {
        BlockPos edge =
                new BlockPos(
                        CORE.x() + SHELL,
                        CORE.y() + SHELL,
                        CORE.z()
                );

        VoxelShape localShape =
                Shapes.box(
                        1.0D / 16.0D,
                        2.0D / 16.0D,
                        3.0D / 16.0D,
                        5.0D / 16.0D,
                        7.0D / 16.0D,
                        11.0D / 16.0D
                );

        PlanetBlockStateFrame stateFrame =
                PlanetBlockStateFrame.resolve(
                        FIELD,
                        edge
                ).orElseThrow();

        VoxelShape first =
                stateFrame.rotateShape(localShape);
        VoxelShape second =
                PlanetBlockStateFrame.resolve(
                        FIELD,
                        edge
                ).orElseThrow()
                        .rotateShape(localShape);

        assertSame(first, second);
        assertEquals(
                Direction.Axis.X,
                stateFrame.localAxisToWorld(
                        Direction.Axis.Y
                )
        );
        assertEquals(
                Direction.UP,
                stateFrame.worldToLocal(
                        Direction.EAST
                )
        );
    }
}
