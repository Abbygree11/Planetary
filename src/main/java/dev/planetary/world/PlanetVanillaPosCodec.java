package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * Reversible bridge between canonical PlanetBlockPos and ordinary vanilla BlockPos.
 *
 * <p>Each gravity face owns a virtual vanilla coordinate cell. The canonical
 * face occupies the middle third of that cell; one full face-width of guard
 * space is left around it. That guard space is important because vanilla code
 * frequently performs BlockPos.relative()/offset() before asking Level for a
 * block. A one-step position beyond a face edge can therefore still be decoded
 * through Planetary topology instead of accidentally becoming another atlas
 * face.</p>
 *
 * <p>Local radial Y is remapped into BlockPos' 12-bit packed Y range so encoded
 * positions survive BlockPos.asLong()/BlockPos.of(long).</p>
 */
public final class PlanetVanillaPosCodec {
    public static final int PACKED_MIN_Y = -2048;
    public static final int PACKED_MAX_Y = 2047;
    public static final int PACKED_Y_SPAN = PACKED_MAX_Y - PACKED_MIN_Y + 1;

    private static final int SAFE_HORIZONTAL_MIN = -30_000_000;
    private static final int SAFE_HORIZONTAL_MAX = 29_999_999;
    private static final int FACE_COUNT = 6;
    private static final int CELL_WIDTH_IN_FACES = 3;

    private final int faceSizeBlocks;
    private final int minLocalY;
    private final int maxLocalY;
    private final int faceCellSize;
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

        long faceCellSizeLong = (long) faceSizeBlocks * CELL_WIDTH_IN_FACES;
        long totalWidth = faceCellSizeLong * FACE_COUNT;
        long xOriginLong = -(totalWidth / 2L);
        long xLast = xOriginLong + totalWidth - 1L;

        long zOriginLong = -(faceCellSizeLong / 2L);
        long zLast = zOriginLong + faceCellSizeLong - 1L;

        if (faceCellSizeLong > Integer.MAX_VALUE
                || xOriginLong < SAFE_HORIZONTAL_MIN
                || xLast > SAFE_HORIZONTAL_MAX
                || zOriginLong < SAFE_HORIZONTAL_MIN
                || zLast > SAFE_HORIZONTAL_MAX) {
            throw new IllegalArgumentException(
                    "faceSizeBlocks is too large for the safe vanilla virtual coordinate envelope"
            );
        }

        this.faceSizeBlocks = faceSizeBlocks;
        this.minLocalY = minLocalY;
        this.maxLocalY = maxLocalY;
        this.faceCellSize = Math.toIntExact(faceCellSizeLong);
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
        int vanillaX = xOrigin
                + faceIndex * faceCellSize
                + faceSizeBlocks
                + planetPos.x();
        int vanillaY = PACKED_MIN_Y + (planetPos.y() - minLocalY);
        int vanillaZ = zOrigin + faceSizeBlocks + planetPos.z();

        return new BlockPos(vanillaX, vanillaY, vanillaZ);
    }

    public PlanetBlockPos decode(BlockPos vanillaPos) {
        return tryDecode(vanillaPos).orElseThrow(
                () -> new IllegalArgumentException(
                        "BlockPos is outside Planetary virtual coordinate space: " + vanillaPos
                )
        );
    }

    /**
     * Decodes both canonical encodings and guard-space aliases produced by
     * vanilla BlockPos arithmetic near a planet edge.
     */
    public Optional<PlanetBlockPos> tryDecode(BlockPos vanillaPos) {
        Objects.requireNonNull(vanillaPos, "vanillaPos");

        long relativeX = (long) vanillaPos.getX() - xOrigin;
        long totalWidth = (long) faceCellSize * FACE_COUNT;
        if (relativeX < 0 || relativeX >= totalWidth) {
            return Optional.empty();
        }

        long relativeZ = (long) vanillaPos.getZ() - zOrigin;
        if (relativeZ < 0 || relativeZ >= faceCellSize) {
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

        int faceIndex = (int) (relativeX / faceCellSize);
        int insideCellX = (int) (relativeX % faceCellSize);

        int virtualLocalX = insideCellX - faceSizeBlocks;
        int virtualLocalZ = (int) relativeZ - faceSizeBlocks;

        PlanetFace sourceFace = faceFromIndex(faceIndex);
        int anchor = faceSizeBlocks / 2;

        PlanetBlockPos anchorPos = new PlanetBlockPos(
                sourceFace,
                anchor,
                localY,
                anchor
        );

        return Optional.of(
                PlanetBlockTopology.offset(
                        anchorPos,
                        virtualLocalX - anchor,
                        0,
                        virtualLocalZ - anchor,
                        faceSizeBlocks
                )
        );
    }

    public boolean contains(BlockPos vanillaPos) {
        return tryDecode(vanillaPos).isPresent();
    }

    /**
     * Returns whether a vanilla chunk overlaps the horizontal virtual-atlas
     * envelope used by this codec.
     *
     * <p>LevelAccessor#hasChunk loses Y before it reaches ChunkSource, so this
     * intentionally answers only the X/Z part of the question. Vertical
     * validity is still enforced by the normal BlockPos resolution path.</p>
     */
    public boolean containsChunk(int chunkX, int chunkZ) {
        long chunkMinX = ((long) chunkX) << 4;
        long chunkMinZ = ((long) chunkZ) << 4;
        long chunkMaxX = chunkMinX + 15L;
        long chunkMaxZ = chunkMinZ + 15L;

        long atlasMinX = xOrigin;
        long atlasMaxX = (long) xOrigin
                + (long) faceCellSize * FACE_COUNT
                - 1L;
        long atlasMinZ = zOrigin;
        long atlasMaxZ = (long) zOrigin
                + faceCellSize
                - 1L;

        return chunkMaxX >= atlasMinX
                && chunkMinX <= atlasMaxX
                && chunkMaxZ >= atlasMinZ
                && chunkMinZ <= atlasMaxZ;
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
