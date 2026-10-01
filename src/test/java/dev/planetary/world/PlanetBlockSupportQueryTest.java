package dev.planetary.world;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetBlockSupportQueryTest {
    private static final int SHELL = 20;
    private static final PlanetCore CORE =
            new PlanetCore(3, 100, -7, SHELL);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void ordinarySupportQueriesAreOppositeLocalFacesOnAllSixFrames() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos sourcePos =
                    physical(
                            face,
                            SHELL,
                            2,
                            -3
                    );

            for (Direction localToSupport : Direction.values()) {
                PlanetBlockSupportQuery query =
                        PlanetBlockSupportQuery.resolve(
                                FIELD,
                                sourcePos,
                                localToSupport
                        ).orElseThrow();

                assertEquals(
                        face,
                        query.sourceStateFrame().face(),
                        face + " / " + localToSupport
                );
                assertEquals(
                        query.sourceStateFrame()
                                .localToWorld(localToSupport),
                        query.physicalDirectionToSupport(),
                        face + " / " + localToSupport
                );
                assertEquals(
                        sourcePos.relative(
                                query.physicalDirectionToSupport()
                        ),
                        query.supportPos(),
                        face + " / " + localToSupport
                );
                assertEquals(
                        face,
                        query.supportStateFrame().face(),
                        face + " / " + localToSupport
                );
                assertEquals(
                        localToSupport.getOpposite(),
                        query.supportLocalSideTowardSource(),
                        face + " / " + localToSupport
                );
                assertFalse(
                        query.crossedTraversalBoundary(),
                        face + " / " + localToSupport
                );
            }
        }
    }

    @Test
    void localDownOnExactEdgeCanHitTangentSideOfCanonicalSupportBlock() {
        BlockPos sourcePos =
                new BlockPos(
                        CORE.x() + SHELL,
                        CORE.y() + SHELL,
                        CORE.z()
                );

        PlanetBlockSupportQuery query =
                PlanetBlockSupportQuery.resolve(
                        FIELD,
                        sourcePos,
                        Direction.DOWN
                ).orElseThrow();

        assertEquals(
                PlanetFace.POS_X,
                query.sourceStateFrame().face()
        );
        assertEquals(
                Direction.WEST,
                query.physicalDirectionToSupport()
        );
        assertEquals(
                new BlockPos(
                        CORE.x() + SHELL - 1,
                        CORE.y() + SHELL,
                        CORE.z()
                ),
                query.supportPos()
        );
        assertEquals(
                PlanetFace.POS_Y,
                query.supportStateFrame().face()
        );
        assertEquals(
                Direction.EAST,
                query.supportLocalSideTowardSource()
        );
        assertTrue(query.crossedTraversalBoundary());

        assertEquals(
                Direction.EAST,
                query.supportStateFrame()
                        .localToWorld(
                                query.supportLocalSideTowardSource()
                        )
        );
    }

    @Test
    void localDownAtExactCornerUsesCanonicalSupportTiePolicy() {
        BlockPos sourcePos =
                new BlockPos(
                        CORE.x() + SHELL,
                        CORE.y() + SHELL,
                        CORE.z() + SHELL
                );

        PlanetBlockSupportQuery query =
                PlanetBlockSupportQuery.resolve(
                        FIELD,
                        sourcePos,
                        Direction.DOWN
                ).orElseThrow();

        assertEquals(
                PlanetFace.POS_X,
                query.sourceStateFrame().face()
        );
        assertEquals(
                Direction.WEST,
                query.physicalDirectionToSupport()
        );
        assertEquals(
                new BlockPos(
                        CORE.x() + SHELL - 1,
                        CORE.y() + SHELL,
                        CORE.z() + SHELL
                ),
                query.supportPos()
        );
        assertEquals(
                PlanetFace.POS_Y,
                query.supportStateFrame().face()
        );
        assertEquals(
                Direction.EAST,
                query.supportLocalSideTowardSource()
        );
        assertTrue(query.crossedTraversalBoundary());
    }

    @Test
    void supportQueryCanTerminateAtZeroGravityCoreBlock() {
        PlanetCore tinyCore =
                new PlanetCore(0, 0, 0, 1);
        PlanetGravityField tinyField =
                new PlanetGravityField(tinyCore);

        PlanetBlockSupportQuery query =
                PlanetBlockSupportQuery.resolve(
                        tinyField,
                        new BlockPos(1, 0, 0),
                        Direction.DOWN
                ).orElseThrow();

        assertEquals(
                PlanetFace.POS_X,
                query.sourceStateFrame().face()
        );
        assertEquals(
                new BlockPos(0, 0, 0),
                query.supportPos()
        );
        assertEquals(
                PlanetFace.POS_Y,
                query.supportStateFrame().face()
        );
        assertEquals(
                Direction.EAST,
                query.supportLocalSideTowardSource()
        );
    }

    @Test
    void localSupportDirectionIsUndefinedWhenSourceItselfIsCore() {
        PlanetCore tinyCore =
                new PlanetCore(0, 0, 0, 1);
        PlanetGravityField tinyField =
                new PlanetGravityField(tinyCore);

        assertTrue(
                PlanetBlockSupportQuery.resolve(
                        tinyField,
                        new BlockPos(0, 0, 0),
                        Direction.DOWN
                ).isEmpty()
        );
    }

    private static BlockPos physical(
            PlanetFace face,
            int upScore,
            int eastScore,
            int southScore
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);

        PlanetVector up = frame.worldUp();
        PlanetVector east = frame.worldEast();
        PlanetVector south = frame.worldSouth();

        return new BlockPos(
                CORE.x()
                        + up.x() * upScore
                        + east.x() * eastScore
                        + south.x() * southScore,
                CORE.y()
                        + up.y() * upScore
                        + east.y() * eastScore
                        + south.y() * southScore,
                CORE.z()
                        + up.z() * upScore
                        + east.z() * eastScore
                        + south.z() * southScore
        );
    }
}
