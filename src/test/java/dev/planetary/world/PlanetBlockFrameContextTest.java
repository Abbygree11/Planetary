package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetTopology;
import dev.planetary.topology.PlanetVector;
import dev.planetary.topology.PlanetCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetBlockFrameContextTest {
    private static final int SHELL = 20;

    private static final PlanetCore CORE =
            new PlanetCore(3, 100, -7, SHELL);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    private static final List<PlanetDirection> HORIZONTAL =
            List.of(
                    PlanetDirection.NORTH,
                    PlanetDirection.SOUTH,
                    PlanetDirection.WEST,
                    PlanetDirection.EAST
            );

    @Test
    void localAndWorldDirectionsRoundTripOnAllSixFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockFrameContext context =
                    contextAt(
                            physical(
                                    face,
                                    SHELL,
                                    2,
                                    -3
                            ),
                            face
                    );

            for (Direction local : Direction.values()) {
                assertEquals(
                        local,
                        context.worldToLocal(
                                context.localToWorld(local)
                        ),
                        face + " / " + local
                );
            }
        }
    }

    @Test
    void enteringEveryDirectedEdgeTransportsFrameAtSharedCell() {
        int transitions = 0;

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : HORIZONTAL) {
                transitions++;

                FaceTransform transform =
                        PlanetTopology.edgeTransform(
                                face,
                                edge
                        );

                BlockPos sourcePos =
                        oneBeforeEdge(
                                face,
                                edge,
                                3
                        );

                PlanetBlockFrameContext source =
                        contextAt(sourcePos, face);

                Direction local =
                        PlanetVanillaDirection.toVanilla(edge);

                PlanetBlockStep step =
                        source.step(local);

                assertTrue(
                        step.crossedGravityBoundary(),
                        transform.toString()
                );
                assertEquals(
                        transform.targetFace(),
                        step.target().face(),
                        transform.toString()
                );
                assertEquals(
                        PlanetVanillaDirection.toVanilla(
                                transform.transformDirection(
                                        edge
                                )
                        ),
                        step.transportedDirection(),
                        transform.toString()
                );

                BlockPos expectedPhysical =
                        sourcePos.relative(
                                source.localToWorld(local)
                        );
                assertEquals(
                        expectedPhysical,
                        step.target().pos(),
                        transform.toString()
                );

                assertTrue(
                        FIELD.candidateFaces(
                                expectedPhysical.getX(),
                                expectedPhysical.getY(),
                                expectedPhysical.getZ()
                        ).contains(face),
                        "shared edge must still belong to source face"
                );
                assertTrue(
                        FIELD.candidateFaces(
                                expectedPhysical.getX(),
                                expectedPhysical.getY(),
                                expectedPhysical.getZ()
                        ).contains(
                                transform.targetFace()
                        ),
                        "shared edge must belong to target face"
                );
            }
        }

        assertEquals(24, transitions);
    }

    @Test
    void steppingForwardFromSharedEdgeFoldsOntoTargetFace() {
        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : HORIZONTAL) {
                FaceTransform transform =
                        PlanetTopology.edgeTransform(
                                face,
                                edge
                        );

                BlockPos edgePos =
                        onEdge(
                                face,
                                edge,
                                -4
                        );
                PlanetBlockFrameContext source =
                        contextAt(edgePos, face);

                Direction local =
                        PlanetVanillaDirection.toVanilla(edge);
                Direction transported =
                        PlanetVanillaDirection.toVanilla(
                                transform.transformDirection(edge)
                        );

                PlanetBlockStep step =
                        source.step(local);

                PlanetGravityFrame targetFrame =
                        new PlanetGravityFrame(
                                transform.targetFace()
                        );
                BlockPos expected =
                        edgePos.relative(
                                PlanetVanillaDirection.localToWorld(
                                        targetFrame,
                                        transported
                                )
                        );

                assertEquals(expected, step.target().pos());
                assertEquals(
                        transform.targetFace(),
                        step.target().face()
                );
                assertEquals(
                        transported,
                        step.transportedDirection()
                );
                assertTrue(step.crossedGravityBoundary());
            }
        }
    }

    @Test
    void edgeStepAndTransportedOppositeStepAreExactlyReversible() {
        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : HORIZONTAL) {
                BlockPos sourcePos =
                        oneBeforeEdge(
                                face,
                                edge,
                                5
                        );

                PlanetBlockFrameContext source =
                        contextAt(sourcePos, face);

                PlanetBlockStep toEdge =
                        source.step(
                                PlanetVanillaDirection.toVanilla(edge)
                        );

                PlanetBlockStep back =
                        toEdge.target().step(
                                toEdge.transportedDirection()
                                        .getOpposite()
                        );

                assertEquals(
                        source.pos(),
                        back.target().pos(),
                        face + " / " + edge
                );
                assertEquals(
                        source.face(),
                        back.target().face(),
                        face + " / " + edge
                );
            }
        }
    }

    @Test
    void continuedDirectionWalksAroundTheCornerWithoutAliasCell() {
        PlanetFace sourceFace = PlanetFace.POS_Y;
        PlanetDirection edge = PlanetDirection.EAST;

        PlanetBlockFrameContext source =
                contextAt(
                        oneBeforeEdge(
                                sourceFace,
                                edge,
                                0
                        ),
                        sourceFace
                );

        PlanetBlockStep enterEdge =
                source.step(Direction.EAST);
        PlanetBlockStep leaveEdge =
                enterEdge.target().step(
                        enterEdge.transportedDirection()
                );

        assertEquals(
                PlanetFace.POS_X,
                enterEdge.target().face()
        );
        assertEquals(
                PlanetFace.POS_X,
                leaveEdge.target().face()
        );

        BlockPos edgePos = enterEdge.target().pos();

        assertEquals(
                edgePos.relative(Direction.DOWN),
                leaveEdge.target().pos()
        );
        assertFalse(
                FIELD.candidateFaces(
                        leaveEdge.target().pos().getX(),
                        leaveEdge.target().pos().getY(),
                        leaveEdge.target().pos().getZ()
                ).contains(PlanetFace.POS_Y)
        );
    }

    @Test
    void preferredFaceSelectsEitherChartOnExactBoundary() {
        BlockPos edge =
                onEdge(
                        PlanetFace.POS_Y,
                        PlanetDirection.EAST,
                        2
                );

        assertEquals(
                PlanetFace.POS_Y,
                contextAt(
                        edge,
                        PlanetFace.POS_Y
                ).face()
        );
        assertEquals(
                PlanetFace.POS_X,
                contextAt(
                        edge,
                        PlanetFace.POS_X
                ).face()
        );
    }

    @Test
    void verticalMovementUsesPhysicalLocalAxisAndCanChangeRegion() {
        BlockPos edge =
                onEdge(
                        PlanetFace.POS_Y,
                        PlanetDirection.EAST,
                        0
                );

        PlanetBlockFrameContext top =
                contextAt(edge, PlanetFace.POS_Y);

        PlanetBlockStep inward =
                top.step(Direction.DOWN);

        assertEquals(
                edge.relative(Direction.DOWN),
                inward.target().pos()
        );
        assertEquals(
                PlanetFace.POS_X,
                inward.target().face()
        );
        assertEquals(
                Direction.DOWN,
                inward.transportedDirection()
        );
    }

    private static PlanetBlockFrameContext contextAt(
            BlockPos pos,
            PlanetFace preferred
    ) {
        return PlanetBlockFrameContext.resolve(
                FIELD,
                pos,
                preferred
        ).orElseThrow();
    }

    /**
     * Creates a physical block from coefficients in a face frame:
     * upScore * local UP + eastScore * local EAST +
     * southScore * local SOUTH.
     */
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

    private static BlockPos oneBeforeEdge(
            PlanetFace face,
            PlanetDirection edge,
            int seamCoordinate
    ) {
        return physicalAtEdgeScores(
                face,
                edge,
                SHELL - 1,
                seamCoordinate
        );
    }

    private static BlockPos onEdge(
            PlanetFace face,
            PlanetDirection edge,
            int seamCoordinate
    ) {
        return physicalAtEdgeScores(
                face,
                edge,
                SHELL,
                seamCoordinate
        );
    }

    private static BlockPos physicalAtEdgeScores(
            PlanetFace face,
            PlanetDirection edge,
            int edgeScore,
            int seamCoordinate
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);

        PlanetVector edgeAxis =
                frame.worldAxis(edge);

        PlanetDirection seamDirection =
                switch (edge) {
                    case EAST, WEST ->
                            PlanetDirection.SOUTH;
                    case NORTH, SOUTH ->
                            PlanetDirection.EAST;
                    default ->
                            throw new IllegalArgumentException(
                                    "Not an edge: " + edge
                            );
                };

        PlanetVector seamAxis =
                frame.worldAxis(seamDirection);
        PlanetVector up =
                frame.worldUp();

        return new BlockPos(
                CORE.x()
                        + up.x() * SHELL
                        + edgeAxis.x() * edgeScore
                        + seamAxis.x() * seamCoordinate,
                CORE.y()
                        + up.y() * SHELL
                        + edgeAxis.y() * edgeScore
                        + seamAxis.y() * seamCoordinate,
                CORE.z()
                        + up.z() * SHELL
                        + edgeAxis.z() * edgeScore
                        + seamAxis.z() * seamCoordinate
        );
    }
}
