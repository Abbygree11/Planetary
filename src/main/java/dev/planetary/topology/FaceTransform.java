package dev.planetary.topology;

import java.util.Objects;

/**
 * The exact 90-degree topology transform for one directed cube edge.
 *
 * <p>The transform deliberately contains no Minecraft classes. It is the
 * mathematical contract that later adapters for BlockPos, Direction,
 * entities, rendering and chunk loading will reuse.</p>
 */
public final class FaceTransform {
    private final PlanetFace sourceFace;
    private final PlanetDirection sourceEdge;
    private final PlanetFace targetFace;
    private final PlanetDirection targetEdge;
    private final boolean reverseSeamCoordinate;
    private final PlanetDirection sourceSeamPositive;
    private final PlanetDirection targetSeamPositive;

    private FaceTransform(
            PlanetFace sourceFace,
            PlanetDirection sourceEdge,
            PlanetFace targetFace,
            PlanetDirection targetEdge,
            boolean reverseSeamCoordinate,
            PlanetDirection sourceSeamPositive,
            PlanetDirection targetSeamPositive
    ) {
        this.sourceFace = sourceFace;
        this.sourceEdge = sourceEdge;
        this.targetFace = targetFace;
        this.targetEdge = targetEdge;
        this.reverseSeamCoordinate = reverseSeamCoordinate;
        this.sourceSeamPositive = sourceSeamPositive;
        this.targetSeamPositive = targetSeamPositive;
    }

    public static FaceTransform across(PlanetFace sourceFace, PlanetDirection sourceEdge) {
        Objects.requireNonNull(sourceFace, "sourceFace");
        Objects.requireNonNull(sourceEdge, "sourceEdge");
        if (!sourceEdge.isHorizontal()) {
            throw new IllegalArgumentException("Face transitions exist only for horizontal edges: " + sourceEdge);
        }

        PlanetFace targetFace = sourceFace.neighborAcross(sourceEdge);

        PlanetDirection targetEdge = targetFace.localDirectionOf(sourceFace.upAxis());

        PlanetDirection sourceSeamPositive = seamPositive(sourceEdge);
        PlanetDirection targetSeamPositive = seamPositive(targetEdge);

        AxisVector sourceSeamAxis = sourceFace.axisFor(sourceSeamPositive);
        AxisVector targetSeamAxis = targetFace.axisFor(targetSeamPositive);
        int seamDot = sourceSeamAxis.dot(targetSeamAxis);
        if (Math.abs(seamDot) != 1) {
            throw new IllegalStateException("Cube edge axes are not parallel");
        }

        return new FaceTransform(
                sourceFace,
                sourceEdge,
                targetFace,
                targetEdge,
                seamDot < 0,
                sourceSeamPositive,
                targetSeamPositive
        );
    }

    private static PlanetDirection seamPositive(PlanetDirection edge) {
        return switch (edge) {
            case EAST, WEST -> PlanetDirection.SOUTH;
            case NORTH, SOUTH -> PlanetDirection.EAST;
            default -> throw new IllegalArgumentException("Not a face edge: " + edge);
        };
    }

    public PlanetFace sourceFace() {
        return sourceFace;
    }

    public PlanetDirection sourceEdge() {
        return sourceEdge;
    }

    public PlanetFace targetFace() {
        return targetFace;
    }

    public PlanetDirection targetEdge() {
        return targetEdge;
    }

    public boolean reversesSeamCoordinate() {
        return reverseSeamCoordinate;
    }

    public PlanetDirection transformDirection(PlanetDirection direction) {
        Objects.requireNonNull(direction, "direction");

        if (direction == PlanetDirection.UP || direction == PlanetDirection.DOWN) {
            return direction;
        }
        if (direction == sourceEdge) {
            return targetEdge.opposite();
        }
        if (direction == sourceEdge.opposite()) {
            return targetEdge;
        }
        if (direction == sourceSeamPositive) {
            return reverseSeamCoordinate ? targetSeamPositive.opposite() : targetSeamPositive;
        }
        if (direction == sourceSeamPositive.opposite()) {
            return reverseSeamCoordinate ? targetSeamPositive : targetSeamPositive.opposite();
        }

        throw new IllegalStateException("Unhandled direction " + direction);
    }

    /**
     * Parallel-transports an arbitrary vector expressed in the source local
     * frame into the target local frame while folding across this edge.
     *
     * <p>Local UP/DOWN remain UP/DOWN relative to the player's body, while
     * horizontal axes are remapped according to the exact edge transition.
     * This is the transform needed for view/control orientation when gravity
     * switches faces.</p>
     */
    public PlanetFrameVector transformVector(
            PlanetFrameVector sourceVector
    ) {
        Objects.requireNonNull(sourceVector, "sourceVector");

        PlanetDirection mappedEast =
                transformDirection(PlanetDirection.EAST);
        PlanetDirection mappedUp =
                transformDirection(PlanetDirection.UP);
        PlanetDirection mappedSouth =
                transformDirection(PlanetDirection.SOUTH);

        return new PlanetFrameVector(
                sourceVector.x() * mappedEast.dx()
                        + sourceVector.y() * mappedUp.dx()
                        + sourceVector.z() * mappedSouth.dx(),
                sourceVector.x() * mappedEast.dy()
                        + sourceVector.y() * mappedUp.dy()
                        + sourceVector.z() * mappedSouth.dy(),
                sourceVector.x() * mappedEast.dz()
                        + sourceVector.y() * mappedUp.dz()
                        + sourceVector.z() * mappedSouth.dz()
        );
    }

    public PlanetPos crossBoundaryCell(PlanetPos source, int faceSize) {
        Objects.requireNonNull(source, "source");
        validateFaceSize(faceSize);
        if (source.face() != sourceFace) {
            throw new IllegalArgumentException("Expected source face " + sourceFace + " but got " + source.face());
        }
        requireInsideFace(source, faceSize);
        requireOnSourceEdge(source, faceSize);

        int seam = switch (sourceEdge) {
            case EAST, WEST -> source.z();
            case NORTH, SOUTH -> source.x();
            default -> throw new IllegalStateException("Vertical direction cannot be an edge");
        };
        int mappedSeam = reverseSeamCoordinate ? faceSize - 1 - seam : seam;

        int targetX;
        int targetZ;
        switch (targetEdge) {
            case WEST -> {
                targetX = 0;
                targetZ = mappedSeam;
            }
            case EAST -> {
                targetX = faceSize - 1;
                targetZ = mappedSeam;
            }
            case NORTH -> {
                targetX = mappedSeam;
                targetZ = 0;
            }
            case SOUTH -> {
                targetX = mappedSeam;
                targetZ = faceSize - 1;
            }
            default -> throw new IllegalStateException("Vertical direction cannot be an edge");
        }

        return new PlanetPos(targetFace, targetX, source.y(), targetZ);
    }

    public FaceTransform inverse() {
        return across(targetFace, targetEdge);
    }

    private void requireOnSourceEdge(PlanetPos source, int faceSize) {
        boolean onEdge = switch (sourceEdge) {
            case WEST -> source.x() == 0;
            case EAST -> source.x() == faceSize - 1;
            case NORTH -> source.z() == 0;
            case SOUTH -> source.z() == faceSize - 1;
            default -> false;
        };
        if (!onEdge) {
            throw new IllegalArgumentException("Position " + source + " is not on source edge " + sourceEdge);
        }
    }

    static void requireInsideFace(PlanetPos pos, int faceSize) {
        if (pos.x() < 0 || pos.x() >= faceSize || pos.z() < 0 || pos.z() >= faceSize) {
            throw new IllegalArgumentException("Position is outside face bounds: " + pos + ", size=" + faceSize);
        }
    }

    static void validateFaceSize(int faceSize) {
        if (faceSize <= 0) {
            throw new IllegalArgumentException("faceSize must be > 0");
        }
    }

    @Override
    public String toString() {
        return sourceFace + ":" + sourceEdge + " -> " + targetFace + ":" + targetEdge
                + (reverseSeamCoordinate ? " (reversed)" : "");
    }
}
