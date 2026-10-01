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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    void enteringEveryEdgeFacesTheSourceThroughTargetLocalDown() {
        int transitions = 0;

        for (PlanetFace face : PlanetFace.values()) {
            for (PlanetDirection edge : HORIZONTAL) {
                transitions++;

                PlanetBlockFrameContext source =
                        contextAt(
                                oneBeforeEdge(
                                        face,
                                        edge,
                                        4
                                ),
                                face
                        );

                PlanetBlockStep step =
                        source.step(
                                PlanetVanillaDirection.toVanilla(edge)
                        );

                Direction targetSide =
                        step.targetLocalSideTowardSource();

                /*
                 * Entering a shared edge cell moves physically along the
                 * adjacent face's local UP axis. Therefore the geometric face
                 * of the target block that points back to the source is local
                 * DOWN. This is deliberately NOT the same concept as the
                 * transported horizontal direction used to continue walking
                 * around the cube.
                 */
                assertEquals(
                        Direction.DOWN,
                        targetSide,
                        face + " / " + edge
                );
                assertEquals(
                        step.physicalDirection()
                                .getOpposite(),
                        step.target().localToWorld(targetSide),
                        face + " / " + edge
                );
                assertEquals(
                        source.pos(),
                        step.target().pos().relative(
                                step.target().localToWorld(
                                        targetSide
                                )
                        ),
                        face + " / " + edge
                );
            }
        }

        assertEquals(24, transitions);
    }

    @Test
    void targetSideTowardSourceIsOrdinaryOppositeInsideOneFace() {
        PlanetBlockFrameContext source =
                contextAt(
                        physical(
                                PlanetFace.POS_Y,
                                SHELL,
                                0,
                                0
                        ),
                        PlanetFace.POS_Y
                );

        for (Direction local : Direction.values()) {
            PlanetBlockStep step = source.step(local);

            if (!step.crossedGravityBoundary()) {
                assertEquals(
                        local.getOpposite(),
                        step.targetLocalSideTowardSource(),
                        local.toString()
                );
            }
        }
    }

    @Test
    void zeroStepWalkPreservesContextAndDirection() {
        PlanetBlockFrameContext source =
                contextAt(
                        physical(
                                PlanetFace.POS_Z,
                                SHELL,
                                3,
                                -2
                        ),
                        PlanetFace.POS_Z
                );

        PlanetBlockWalk walk =
                source.walk(Direction.NORTH, 0);

        assertEquals(source.pos(), walk.target().pos());
        assertEquals(source.face(), walk.target().face());
        assertEquals(
                Direction.NORTH,
                walk.transportedDirection()
        );
        assertEquals(0, walk.gravityBoundaryCrossings());
        assertFalse(walk.crossedGravityBoundary());
    }

    @Test
    void straightWalkCrossesTwoEdgesAndTransportsDirectionTwice() {
        PlanetBlockFrameContext source =
                contextAt(
                        oneBeforeEdge(
                                PlanetFace.POS_Y,
                                PlanetDirection.EAST,
                                0
                        ),
                        PlanetFace.POS_Y
                );

        int steps = 1 + 2 * SHELL;

        PlanetBlockWalk walk =
                source.walk(
                        Direction.EAST,
                        steps
                );

        assertEquals(
                new BlockPos(
                        CORE.x() + SHELL,
                        CORE.y() - SHELL,
                        CORE.z()
                ),
                walk.target().pos()
        );
        assertEquals(
                PlanetFace.NEG_Y,
                walk.target().face()
        );
        assertEquals(
                Direction.WEST,
                walk.transportedDirection()
        );
        assertEquals(
                2,
                walk.gravityBoundaryCrossings()
        );
        assertTrue(walk.crossedGravityBoundary());

        PlanetBlockWalk reverse =
                walk.target().walk(
                        walk.transportedDirection()
                                .getOpposite(),
                        steps
                );

        assertEquals(
                source.pos(),
                reverse.target().pos()
        );
        assertEquals(
                source.face(),
                reverse.target().face()
        );
        assertEquals(
                Direction.WEST,
                reverse.transportedDirection()
        );
        assertEquals(
                2,
                reverse.gravityBoundaryCrossings()
        );
    }

    @Test
    void tangentStepsAtThreeFaceCornerKeepFourLogicalTransitionsButThreePhysicalTargets() {
        for (PlanetFace face : PlanetFace.values()) {
            for (int eastSign : new int[]{-1, 1}) {
                for (int southSign : new int[]{-1, 1}) {
                    PlanetBlockFrameContext source =
                            contextAt(
                                    physical(
                                            face,
                                            SHELL,
                                            eastSign * SHELL,
                                            southSign * SHELL
                                    ),
                                    face
                            );

                    List<PlanetBlockStep> steps =
                            source.tangentSteps();

                    assertEquals(
                            List.of(
                                    Direction.NORTH,
                                    Direction.SOUTH,
                                    Direction.WEST,
                                    Direction.EAST
                            ),
                            steps.stream()
                                    .map(
                                            PlanetBlockStep::localDirection
                                    )
                                    .toList(),
                            face + " corner "
                                    + eastSign + "/"
                                    + southSign
                    );

                    Set<BlockPos> physicalTargets =
                            new HashSet<>();
                    Set<String> chartTargets =
                            new HashSet<>();

                    for (PlanetBlockStep step : steps) {
                        physicalTargets.add(
                                step.target().pos()
                        );
                        chartTargets.add(
                                step.target().pos()
                                        + "@"
                                        + step.target().face()
                        );

                        assertEquals(
                                source.pos().relative(
                                        step.physicalDirection()
                                ),
                                step.target().pos(),
                                face + " / "
                                        + step.localDirection()
                        );
                    }

                    /*
                     * A cube vertex has three physical surface edges. Relative
                     * to one preferred face there are still four logical local
                     * tangent directions, but the two outward directions fold
                     * onto the same physical edge block through different
                     * target charts.
                     */
                    assertEquals(
                            3,
                            physicalTargets.size(),
                            face + " corner physical valence"
                    );
                    assertEquals(
                            4,
                            chartTargets.size(),
                            face + " corner logical chart valence"
                    );

                    Direction outwardEastWest =
                            eastSign > 0
                                    ? Direction.EAST
                                    : Direction.WEST;
                    Direction outwardNorthSouth =
                            southSign > 0
                                    ? Direction.SOUTH
                                    : Direction.NORTH;

                    PlanetBlockStep firstOutward =
                            stepFor(
                                    steps,
                                    outwardEastWest
                            );
                    PlanetBlockStep secondOutward =
                            stepFor(
                                    steps,
                                    outwardNorthSouth
                            );

                    assertEquals(
                            firstOutward.target().pos(),
                            secondOutward.target().pos(),
                            face + " corner outward fold"
                    );
                    assertEquals(
                            firstOutward.physicalDirection(),
                            secondOutward.physicalDirection(),
                            face + " corner common physical edge"
                    );

                    assertEquals(
                            PlanetTopology.edgeTransform(
                                    face,
                                    PlanetVanillaDirection.fromVanilla(
                                            outwardEastWest
                                    )
                            ).targetFace(),
                            firstOutward.target().face(),
                            face + " first outward chart"
                    );
                    assertEquals(
                            PlanetTopology.edgeTransform(
                                    face,
                                    PlanetVanillaDirection.fromVanilla(
                                            outwardNorthSouth
                                    )
                            ).targetFace(),
                            secondOutward.target().face(),
                            face + " second outward chart"
                    );
                }
            }
        }
    }

    private static PlanetBlockStep stepFor(
            List<PlanetBlockStep> steps,
            Direction localDirection
    ) {
        return steps.stream()
                .filter(step ->
                        step.localDirection()
                                == localDirection
                )
                .findFirst()
                .orElseThrow();
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
