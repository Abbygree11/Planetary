package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * Reversible bridge between canonical PlanetBlockPos and an ordinary vanilla BlockPos.
 *
 * <p>This is a <strong>virtual</strong> coordinate space used for vanilla APIs whose
 * contracts require BlockPos (not the physical rendered cube coordinates). Each
 * gravity face occupies its own non-overlapping horizontal strip. Local radial Y
 * is remapped into BlockPos' 12-bit packed Y range so positions also survive
 * BlockPos.asLong()/BlockPos.of(long), which many vanilla systems use internally.</p>
 */
public final class PlanetVanillaPosCodec {
    public static final int PACKED_MIN_Y = -2048;
    public static final int PACKED_MAX_Y = 2047;
    public static final int PACKED_Y_SPAN = PACKED_MAX_Y - PACKED_MIN_Y + 1;

    // Stay inside the conventional vanilla horizontal world-border envelope.
    private static final int SAFE_HORIZONTAL_MIN = -30_000_000;
    private static final int SAFE_HORIZONTAL_MAX = 29_999_999;
    private static final int FACE_COUNT = 6;

    private final int faceSizeBlocks;
    private final int minLocalY;
    private final int maxLocalY;
    private final int xOrigin;
    private final int zOrigin;

    public PlanetVanillaPosCodec(
            int faceSizeBlocks,
            int minLocalY,
            int maxLocalY
    ) {
        if (faceSizeBlocks <= 0) {
            throw new IllegalArgumentException("faceSizeBlocks must be > 0");
        }
        if (maxLocalY < minLocalY) {
            throw new IllegalArgumentException("maxLocalY must be >= minLocalY");
        }

        long verticalSpan = (long) maxLocalY - minLocalY + 1L;
        if (verticalSpan > PACKED_Y_SPAN) {
            throw new IllegalArgumentException(
                    "Virtual vanilla Y span must be <= " + PACKED_Y_SPAN
                            + " blocks so BlockPos.asLong() remains lossless"
            );
        }

        long totalWidth = (long) faceSizeBlocks * FACE_COUNT;
        long xOriginLong = -(totalWidth / 2L);
        long xLast = xOriginLong + totalWidth - 1L;

        long zOriginLong = -(faceSizeBlocks / 2L);
        long zLast = zOriginLong + faceSizeBlocks - 1L;

        if (xOriginLong < SAFE_HORIZONTAL_MIN || xLast > SAFE_HORIZONTAL_MAX
                || zOriginLong < SAFE_HORIZONTAL_MIN || zLast > SAFE_HORIZONTAL_MAX) {
            throw new IllegalArgumentException(
                    "faceSizeBlocks is too large for the safe vanilla virtual coordinate envelope"
            );
        }

        this.faceSizeBlocks = faceSizeBlocks;
        this.minLocalY = minLocalY;
        this.maxLocalY = maxLocalY;
        this.xOrigin = Math.toIntExact(xOriginLong);
        this.zOrigin = Math.toIntExact(zOriginLong);
    }

    public int faceSizeBlocks() {
        return faceSizeBlocks;
    }

    public int minLocalY() {
        return minLocalY;
    }

    public int maxLocalY() {
        return maxLocalY;
    }

    public BlockPos encode(PlanetBlockPos planetPos) {
        Objects.requireNonNull(planetPos, "planetPos");
        validatePlanetPosition(planetPos);

        int faceIndex = faceIndex(planetPos.face());
        int vanillaX = xOrigin + faceIndex * faceSizeBlocks + planetPos.x();
        int vanillaY = PACKED_MIN_Y + (planetPos.y() - minLocalY);
        int vanillaZ = zOrigin + planetPos.z();

        return new BlockPos(vanillaX, vanillaY, vanillaZ);
    }

    public PlanetBlockPos decode(BlockPos vanillaPos) {
        return tryDecode(vanillaPos).orElseThrow(
                () -> new IllegalArgumentException(
                        "BlockPos is outside Planetary virtual coordinate space: " + vanillaPos
                )
        );
    }

    public Optional<PlanetBlockPos> tryDecode(BlockPos vanillaPos) {
        Objects.requireNonNull(vanillaPos, "vanillaPos");

        long relativeX = (long) vanillaPos.getX() - xOrigin;
        long totalWidth = (long) faceSizeBlocks * FACE_COUNT;
        if (relativeX < 0 || relativeX >= totalWidth) {
            return Optional.empty();
        }

        int relativeZ = vanillaPos.getZ() - zOrigin;
        if (relativeZ < 0 || relativeZ >= faceSizeBlocks) {
            return Optional.empty();
        }

        int packedY = vanillaPos.getY();
        if (packedY < PACKED_MIN_Y || packedY > PACKED_MAX_Y) {
            return Optional.empty();
        }

        int localY = minLocalY + (packedY - PACKED_MIN_Y);
        if (localY < minLocalY || localY > maxLocalY) {
            return Optional.empty();
        }

        int faceIndex = (int) (relativeX / faceSizeBlocks);
        int localX = (int) (relativeX % faceSizeBlocks);

        return Optional.of(new PlanetBlockPos(
                faceFromIndex(faceIndex),
                localX,
                localY,
                relativeZ
        ));
    }

    public boolean contains(BlockPos vanillaPos) {
        return tryDecode(vanillaPos).isPresent();
    }

    private void validatePlanetPosition(PlanetBlockPos pos) {
        if (pos.x() < 0 || pos.x() >= faceSizeBlocks
                || pos.z() < 0 || pos.z() >= faceSizeBlocks) {
            throw new IllegalArgumentException(
                    "Planet face-local X/Z must be in [0, "
                            + (faceSizeBlocks - 1) + "]: " + pos
            );
        }
        if (pos.y() < minLocalY || pos.y() > maxLocalY) {
            throw new IllegalArgumentException(
                    "Planet local Y must be in [" + minLocalY + ", "
                            + maxLocalY + "]: " + pos
            );
        }
    }

    private static int faceIndex(PlanetFace face) {
        return switch (face) {
            case POS_X -> 0;
            case NEG_X -> 1;
            case POS_Y -> 2;
            case NEG_Y -> 3;
            case POS_Z -> 4;
            case NEG_Z -> 5;
        };
    }

    private static PlanetFace faceFromIndex(int index) {
        return switch (index) {
            case 0 -> PlanetFace.POS_X;
            case 1 -> PlanetFace.NEG_X;
            case 2 -> PlanetFace.POS_Y;
            case 3 -> PlanetFace.NEG_Y;
            case 4 -> PlanetFace.POS_Z;
            case 5 -> PlanetFace.NEG_Z;
            default -> throw new IllegalArgumentException("Unknown virtual face index: " + index);
        };
    }
}
