package dev.planetary.world;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetVanillaPosCodecTest {
    private static final int FACE_SIZE = 4096;
    private static final int MIN_Y = -3000;
    private static final int MAX_Y = 999;

    private final PlanetVanillaPosCodec codec =
            new PlanetVanillaPosCodec(FACE_SIZE, MIN_Y, MAX_Y);

    @Test
    void allSixFacesRoundTripAtCornersAndVerticalExtremes() {
        int[] horizontal = {0, 1, FACE_SIZE / 2, FACE_SIZE - 2, FACE_SIZE - 1};
        int[] vertical = {MIN_Y, MIN_Y + 1, 0, MAX_Y - 1, MAX_Y};

        for (PlanetFace face : PlanetFace.values()) {
            for (int x : horizontal) {
                for (int z : horizontal) {
                    for (int y : vertical) {
                        PlanetBlockPos source = new PlanetBlockPos(face, x, y, z);
                        assertEquals(source, codec.decode(codec.encode(source)));
                    }
                }
            }
        }
    }

    @Test
    void encodedPositionsSurviveVanillaPackedLongRoundTrip() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetBlockPos source = new PlanetBlockPos(
                    face,
                    FACE_SIZE - 1,
                    MIN_Y + face.ordinal() * 100,
                    FACE_SIZE / 2
            );

            BlockPos encoded = codec.encode(source);
            BlockPos unpacked = BlockPos.of(encoded.asLong());

            assertEquals(encoded, unpacked);
            assertEquals(source, codec.decode(unpacked));
        }
    }

    @Test
    void sameLocalCoordinatesOnDifferentFacesNeverAlias() {
        Set<BlockPos> encoded = new HashSet<>();

        for (PlanetFace face : PlanetFace.values()) {
            encoded.add(codec.encode(new PlanetBlockPos(
                    face,
                    123,
                    -20,
                    456
            )));
        }

        assertEquals(6, encoded.size());
    }

    @Test
    void faceIdentityIsEncodedExplicitlyAndStably() {
        PlanetBlockPos posX = new PlanetBlockPos(PlanetFace.POS_X, 10, 0, 10);
        PlanetBlockPos posY = new PlanetBlockPos(PlanetFace.POS_Y, 10, 0, 10);

        BlockPos encodedX = codec.encode(posX);
        BlockPos encodedY = codec.encode(posY);

        assertNotEquals(encodedX.getX(), encodedY.getX());
        assertEquals(encodedX.getY(), encodedY.getY());
        assertEquals(encodedX.getZ(), encodedY.getZ());
    }

    @Test
    void oneStepVanillaBlockPosArithmeticCrossesEveryCubeEdgeCorrectly() {
        Direction[] edges = {
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };

        for (PlanetFace face : PlanetFace.values()) {
            for (Direction edge : edges) {
                PlanetDirection planetEdge = PlanetVanillaDirection.fromVanilla(edge);

                PlanetBlockPos source = switch (edge) {
                    case WEST -> new PlanetBlockPos(face, 0, -20, FACE_SIZE / 3);
                    case EAST -> new PlanetBlockPos(face, FACE_SIZE - 1, -20, FACE_SIZE / 3);
                    case NORTH -> new PlanetBlockPos(face, FACE_SIZE / 3, -20, 0);
                    case SOUTH -> new PlanetBlockPos(face, FACE_SIZE / 3, -20, FACE_SIZE - 1);
                    default -> throw new IllegalStateException("Not a horizontal edge: " + edge);
                };

                PlanetBlockPos expected = PlanetBlockTopology.step(
                        source,
                        planetEdge,
                        FACE_SIZE
                );

                BlockPos vanillaNeighbor = codec.encode(source).relative(edge);

                assertEquals(
                        expected,
                        codec.decode(vanillaNeighbor),
                        face + " " + edge
                );
            }
        }
    }

    @Test
    void guardSpaceAlsoPreservesSeveralVanillaStepsPastAnEdge() {
        PlanetBlockPos source = new PlanetBlockPos(
                PlanetFace.POS_Y,
                FACE_SIZE - 1,
                -100,
                FACE_SIZE / 2
        );

        BlockPos vanilla = codec.encode(source).offset(5, 0, 0);

        assertEquals(
                PlanetBlockTopology.offset(
                        source,
                        5,
                        0,
                        0,
                        FACE_SIZE
                ),
                codec.decode(vanilla)
        );
    }

    @Test
    void positionsOutsideTheVirtualStripAreRejected() {
        BlockPos valid = codec.encode(new PlanetBlockPos(
                PlanetFace.POS_Y,
                0,
                0,
                0
        ));

        BlockPos outside = new BlockPos(
                valid.getX(),
                valid.getY(),
                valid.getZ() - FACE_SIZE * 2
        );

        assertFalse(codec.contains(outside));
        assertTrue(codec.tryDecode(outside).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> codec.decode(outside));
    }

    @Test
    void verticalSpanGreaterThanPackedBlockPosCapacityIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlanetVanillaPosCodec(FACE_SIZE, -4000, 1000)
        );
    }

    @Test
    void arbitraryFourThousandBlockRadialWindowFitsLosslessly() {
        PlanetVanillaPosCodec deepCodec =
                new PlanetVanillaPosCodec(FACE_SIZE, -3200, 799);

        PlanetBlockPos coreSide = new PlanetBlockPos(
                PlanetFace.NEG_Z,
                FACE_SIZE / 2,
                -3200,
                FACE_SIZE / 2
        );
        PlanetBlockPos highSide = new PlanetBlockPos(
                PlanetFace.NEG_Z,
                FACE_SIZE / 2,
                799,
                FACE_SIZE / 2
        );

        assertEquals(coreSide, deepCodec.decode(deepCodec.encode(coreSide)));
        assertEquals(highSide, deepCodec.decode(deepCodec.encode(highSide)));
    }
}
