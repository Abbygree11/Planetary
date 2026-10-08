package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetTopology;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Non-client contract of a three-axis local block offset, including
 * transported tangent basis after crossing a physical cube seam.
 */
final class PlanetLocalBlockOffsetTest {
    private static final int SHELL = 20;
    private static final PlanetCore CORE =
            new PlanetCore(3, 100, -7, SHELL);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void allSixFacesMatchDirectIntegerFrameInsideOneFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockFrameContext source = context(
                    physical(face, SHELL, 2, -3),
                    face
            );

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    for (int y = -1; y <= 1; y++) {
                        PlanetBlockFrameContext result =
                                PlanetLocalBlockOffset.traverse(
                                        source, x, y, z
                                );

                        PlanetGravityFrame frame =
                                new PlanetGravityFrame(face);
                        PlanetVector east = frame.worldEast();
                        PlanetVector up = frame.worldUp();
                        PlanetVector south = frame.worldSouth();

                        BlockPos expected = source.pos().offset(
                                east.x() * x + up.x() * y + south.x() * z,
                                east.y() * x + up.y() * y + south.y() * z,
                                east.z() * x + up.z() * y + south.z() * z
                        );

                        assertEquals(expected, result.pos());
                        assertEquals(face, result.face());
                    }
                }
            }
        }
    }

    @Test
    void allSixFacesMatchExistingOneAxisWalkAtEveryDirectedEdge() {
        for (PlanetFace face : PlanetFace.values()) {
            for (Direction local : new Direction[]{
                    Direction.EAST,
                    Direction.WEST,
                    Direction.SOUTH,
                    Direction.NORTH
            }) {
                PlanetBlockFrameContext source =
                        context(nearEdge(face, local), face);

                PlanetBlockFrameContext projected = switch (local) {
                    case EAST -> PlanetLocalBlockOffset.traverse(
                            source, 3, 0, 0);
                    case WEST -> PlanetLocalBlockOffset.traverse(
                            source, -3, 0, 0);
                    case SOUTH -> PlanetLocalBlockOffset.traverse(
                            source, 0, 0, 3);
                    case NORTH -> PlanetLocalBlockOffset.traverse(
                            source, 0, 0, -3);
                    default -> throw new AssertionError(local);
                };

                PlanetBlockFrameContext existing = source.walk(
                        local, 3
                ).target();

                assertEquals(existing.pos(), projected.pos(),
                        face + " / " + local);
                assertEquals(existing.face(), projected.face(),
                        face + " / " + local);
            }
        }
    }

    @Test
    void xThenZUsesTransformedZAxisAfterEastEdgeCrossing() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockFrameContext source = context(
                    nearEdge(face, Direction.EAST), face
            );

            PlanetBlockFrameContext afterX =
                    source.walk(Direction.EAST, 2).target();

            FaceTransform crossing =
                    PlanetTopology.edgeTransform(
                            face, PlanetDirection.EAST
                    );
            Direction carriedSouth =
                    PlanetVanillaDirection.toVanilla(
                            crossing.transformDirection(
                                    PlanetDirection.SOUTH
                            )
                    );

            PlanetBlockFrameContext expected =
                    afterX.walk(carriedSouth, 2).target();

            PlanetBlockFrameContext actual =
                    PlanetLocalBlockOffset.traverse(source, 2, 0, 2);

            assertEquals(expected.pos(), actual.pos(), face.toString());
            assertEquals(expected.face(), actual.face(), face.toString());
            assertNotEquals(source.face(), afterX.face());
        }
    }

    @Test
    void zeroOffsetsPreserveOriginalTraversalChartEvenAtASeam() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockFrameContext source = context(
                    physical(face, SHELL, SHELL, 0),
                    face
            );

            PlanetBlockFrameContext result =
                    PlanetLocalBlockOffset.traverse(source, 0, 0, 0);

            assertEquals(source.pos(), result.pos());
            assertEquals(source.face(), result.face());
        }
    }

    @Test
    void minimumIntegerOffsetIsRejectedBeforeTraversal() {
        PlanetBlockFrameContext source = context(
                physical(PlanetFace.POS_Y, SHELL, 0, 0),
                PlanetFace.POS_Y
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> PlanetLocalBlockOffset.traverse(
                        source, Integer.MIN_VALUE, 0, 0)
        );
    }

    private static PlanetBlockFrameContext context(
            BlockPos pos,
            PlanetFace preferredFace
    ) {
        return PlanetBlockFrameContext.resolve(
                FIELD, pos, preferredFace
        ).orElseThrow();
    }

    private static BlockPos nearEdge(
            PlanetFace face,
            Direction localEdge
    ) {
        PlanetGravityFrame frame = new PlanetGravityFrame(face);
        PlanetVector edge = frame.worldAxis(
                PlanetVanillaDirection.fromVanilla(localEdge)
        );
        PlanetVector up = frame.worldUp();

        return new BlockPos(
                CORE.x() + up.x() * SHELL + edge.x() * (SHELL - 1),
                CORE.y() + up.y() * SHELL + edge.y() * (SHELL - 1),
                CORE.z() + up.z() * SHELL + edge.z() * (SHELL - 1)
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
                CORE.x() + up.x() * upScore
                        + east.x() * eastScore + south.x() * southScore,
                CORE.y() + up.y() * upScore
                        + east.y() * eastScore + south.y() * southScore,
                CORE.z() + up.z() * upScore
                        + east.z() * eastScore + south.z() * southScore
        );
    }
}
