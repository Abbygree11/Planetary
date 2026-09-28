package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetSidedQueryFrameTest {
    private static final int FACE_SIZE = 4096;

    @Test
    void sidedQueriesAreReframedAcrossEveryCubeEdge() {
        PlanetWorldAccess world = new PlanetWorldAccess(
                FACE_SIZE,
                -3000,
                999
        );
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            Direction[] edges = {
                    Direction.NORTH,
                    Direction.SOUTH,
                    Direction.WEST,
                    Direction.EAST
            };

            for (PlanetFace face : PlanetFace.values()) {
                for (Direction edge : edges) {
                    PlanetBlockPos source = switch (edge) {
                        case WEST -> new PlanetBlockPos(
                                face,
                                0,
                                -20,
                                FACE_SIZE / 3
                        );
                        case EAST -> new PlanetBlockPos(
                                face,
                                FACE_SIZE - 1,
                                -20,
                                FACE_SIZE / 3
                        );
                        case NORTH -> new PlanetBlockPos(
                                face,
                                FACE_SIZE / 3,
                                -20,
                                0
                        );
                        case SOUTH -> new PlanetBlockPos(
                                face,
                                FACE_SIZE / 3,
                                -20,
                                FACE_SIZE - 1
                        );
                        default -> throw new IllegalStateException(
                                "Not a horizontal edge: " + edge
                        );
                    };

                    PlanetNeighborRef expectedNeighbor =
                            world.neighbor(source, edge);

                    BlockPos sourcePos =
                            world.vanillaPosCodec().encode(source);
                    BlockPos targetAlias =
                            sourcePos.relative(edge);

                    // This is the exact pattern used by mods such as Pipez:
                    // target = pos.relative(side), context = side.opposite().
                    Direction callerFrameSide = edge.getOpposite();

                    PlanetSidedQueryFrame.SidedFrame frame =
                            PlanetSidedQueryFrame.resolve(
                                    level,
                                    targetAlias,
                                    callerFrameSide
                            ).orElseThrow();

                    assertEquals(
                            expectedNeighbor.position(),
                            frame.target(),
                            face + " " + edge
                    );
                    assertEquals(
                            source,
                            frame.reference(),
                            face + " " + edge
                    );
                    assertEquals(
                            expectedNeighbor.directionBackToSource(),
                            frame.targetSide(),
                            face + " " + edge
                    );
                    assertEquals(
                            world.vanillaPosCodec().encode(
                                    expectedNeighbor.position()
                            ),
                            frame.targetPos(),
                            face + " " + edge
                    );

                    assertEquals(
                            source,
                            world.relative(
                                    frame.target(),
                                    frame.targetSide()
                            ),
                            face + " " + edge
                    );
                }
            }
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void ordinarySidedQueryInsideFaceRemainsUnchanged() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            PlanetBlockPos target = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    100,
                    10,
                    100
            );
            BlockPos targetPos = world.vanillaPosCodec().encode(target);

            PlanetSidedQueryFrame.SidedFrame frame =
                    PlanetSidedQueryFrame.resolve(
                            level,
                            targetPos,
                            Direction.WEST
                    ).orElseThrow();

            assertEquals(targetPos, frame.targetPos());
            assertEquals(Direction.WEST, frame.targetSide());
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void canonicalizeCollapsesGuardAliasesOntoOwnerFace() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            PlanetBlockPos source = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    FACE_SIZE - 1,
                    0,
                    FACE_SIZE / 2
            );
            BlockPos sourcePos = world.vanillaPosCodec().encode(source);
            BlockPos targetAlias = sourcePos.east();

            PlanetBlockPos target = world.relative(
                    source,
                    Direction.EAST
            );

            assertEquals(
                    world.vanillaPosCodec().encode(target),
                    PlanetSidedQueryFrame.canonicalize(
                            level,
                            targetAlias
                    ).orElseThrow()
            );
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void reentryPermitIsConsumedOnlyOnce() {
        assertFalse(PlanetSidedQueryFrame.consumeReentryPermit());

        Boolean value = PlanetSidedQueryFrame.callWithReentryPermit(() -> {
            assertTrue(PlanetSidedQueryFrame.consumeReentryPermit());
            assertFalse(PlanetSidedQueryFrame.consumeReentryPermit());
            return Boolean.TRUE;
        });

        assertTrue(value);
        assertFalse(PlanetSidedQueryFrame.consumeReentryPermit());
    }
}
