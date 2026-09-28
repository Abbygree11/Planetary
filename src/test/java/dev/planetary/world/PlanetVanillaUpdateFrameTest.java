package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetVanillaUpdateFrameTest {
    private static final int FACE_SIZE = 4096;

    @Test
    void everyCubeEdgeIsReframedIntoTheTargetFace() {
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

                    PlanetBlockPos target = world.relative(source, edge);
                    BlockPos sourceCanonical =
                            world.vanillaPosCodec().encode(source);
                    BlockPos targetAlias = sourceCanonical.relative(edge);

                    PlanetVanillaUpdateFrame.AdjacentFrame frame =
                            PlanetVanillaUpdateFrame.resolve(
                                    level,
                                    targetAlias,
                                    sourceCanonical
                            ).orElseThrow();

                    assertEquals(
                            target,
                            frame.target(),
                            face + " " + edge
                    );
                    assertEquals(
                            source,
                            frame.source(),
                            face + " " + edge
                    );
                    assertEquals(
                            world.vanillaPosCodec().encode(target),
                            frame.targetPos(),
                            face + " " + edge
                    );
                    assertNotEquals(
                            targetAlias,
                            frame.targetPos(),
                            face + " " + edge
                    );
                    assertEquals(
                            frame.sourceAliasPos(),
                            frame.targetPos().relative(
                                    frame.directionToSource()
                            ),
                            face + " " + edge
                    );
                    assertEquals(
                            source,
                            world.vanillaPosCodec().decode(
                                    frame.sourceAliasPos()
                            ),
                            face + " " + edge
                    );
                    assertEquals(
                            source,
                            world.relative(
                                    target,
                                    frame.directionToSource()
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
    void ordinaryAdjacentPositionsInsideAFaceAlreadyUseTheCanonicalFrame() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            PlanetBlockPos source = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    100,
                    20,
                    100
            );
            PlanetBlockPos target = world.relative(
                    source,
                    Direction.EAST
            );

            BlockPos sourcePos = world.vanillaPosCodec().encode(source);
            BlockPos targetPos = world.vanillaPosCodec().encode(target);

            PlanetVanillaUpdateFrame.AdjacentFrame frame =
                    PlanetVanillaUpdateFrame.resolve(
                            level,
                            targetPos,
                            sourcePos
                    ).orElseThrow();

            assertEquals(targetPos, frame.targetPos());
            assertEquals(sourcePos, frame.sourceAliasPos());
            assertEquals(Direction.WEST, frame.directionToSource());
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void nonAdjacentPlanetPositionsAreNotInventedIntoANeighborRelationship() {
        PlanetWorldAccess world = new PlanetWorldAccess(FACE_SIZE);
        Level level = mock(Level.class);
        world.bindLevel(level);

        try {
            PlanetBlockPos target = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    100,
                    20,
                    100
            );
            PlanetBlockPos source = new PlanetBlockPos(
                    PlanetFace.POS_Y,
                    105,
                    20,
                    100
            );

            assertTrue(
                    PlanetVanillaUpdateFrame.resolve(
                            level,
                            world.vanillaPosCodec().encode(target),
                            world.vanillaPosCodec().encode(source)
                    ).isEmpty()
            );
        } finally {
            world.unbindLevel();
        }
    }

    @Test
    void reentryPermitSkipsOnlyTheImmediateReframedCall() {
        assertFalse(PlanetVanillaUpdateFrame.consumeReentryPermit());

        PlanetVanillaUpdateFrame.runWithReentryPermit(() -> {
            assertTrue(PlanetVanillaUpdateFrame.consumeReentryPermit());
            assertFalse(PlanetVanillaUpdateFrame.consumeReentryPermit());
        });

        assertFalse(PlanetVanillaUpdateFrame.consumeReentryPermit());
    }
}
