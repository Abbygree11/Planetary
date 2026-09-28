package dev.planetary.topology;

import java.util.Objects;

/**
 * Rigid embedding of the six face-local Minecraft grids into one physical
 * cube-shaped planetoid.
 *
 * <p>The topology and the embedding intentionally stay separate:
 * {@link PlanetTopology} decides which logical cells are adjacent, while this
 * class decides where a face-local point is drawn in physical 3D space.</p>
 *
 * <p>Every face is an undeformed orthonormal grid. Crossing an edge therefore
 * means rotating around the shared seam line; blocks themselves are never
 * sheared into wedges or rhombi.</p>
 */
public final class PlanetEmbedding {
    private final int faceSize;
    private final double halfSize;

    public PlanetEmbedding(int faceSize) {
        if (faceSize <= 0) {
            throw new IllegalArgumentException("faceSize must be > 0");
        }
        this.faceSize = faceSize;
        this.halfSize = faceSize / 2.0;
    }

    public int faceSize() {
        return faceSize;
    }

    public double halfSize() {
        return halfSize;
    }

    /**
     * Maps a continuous point in a face-local Minecraft frame into physical
     * planetoid space relative to the planetoid center.
     *
     * <p>local X/EAST and Z/SOUTH run along the face. local Y/UP runs away
     * from the core. The surface itself is localY == 0.</p>
     */
    public PlanetWorldPoint toWorld(
            PlanetFace face,
            double localX,
            double localY,
            double localZ
    ) {
        Objects.requireNonNull(face, "face");

        PlanetVector east = face.worldVector(PlanetDirection.EAST);
        PlanetVector up = face.worldVector(PlanetDirection.UP);
        PlanetVector south = face.worldVector(PlanetDirection.SOUTH);

        double x = localX - halfSize;
        double y = halfSize + localY;
        double z = localZ - halfSize;

        return new PlanetWorldPoint(
                east.x() * x + up.x() * y + south.x() * z,
                east.y() * x + up.y() * y + south.y() * z,
                east.z() * x + up.z() * y + south.z() * z
        );
    }

    /**
     * Projects a physical point into the chosen face-local frame.
     *
     * <p>This is the exact inverse of {@link #toWorld} for that face. It does
     * not choose a face automatically; callers such as player gravity can keep
     * the current face stable while crossing a seam.</p>
     */
    public LocalPoint toLocal(
            PlanetFace face,
            PlanetWorldPoint point
    ) {
        Objects.requireNonNull(face, "face");
        Objects.requireNonNull(point, "point");

        PlanetVector east = face.worldVector(PlanetDirection.EAST);
        PlanetVector up = face.worldVector(PlanetDirection.UP);
        PlanetVector south = face.worldVector(PlanetDirection.SOUTH);

        double localX = dot(point, east) + halfSize;
        double localY = dot(point, up) - halfSize;
        double localZ = dot(point, south) + halfSize;

        return new LocalPoint(face, localX, localY, localZ);
    }

    /**
     * Returns a point on the physical seam line of one face.
     *
     * @param seam coordinate along the edge, in the continuous [0, faceSize]
     *             interval
     */
    public PlanetWorldPoint surfaceEdgePoint(
            PlanetFace face,
            PlanetDirection edge,
            double seam
    ) {
        Objects.requireNonNull(face, "face");
        Objects.requireNonNull(edge, "edge");
        if (!edge.isHorizontal()) {
            throw new IllegalArgumentException(
                    "Only horizontal directions identify a face edge: " + edge
            );
        }
        if (seam < 0.0 || seam > faceSize) {
            throw new IllegalArgumentException(
                    "seam must be inside [0, " + faceSize + "]: " + seam
            );
        }

        return switch (edge) {
            case WEST -> toWorld(face, 0.0, 0.0, seam);
            case EAST -> toWorld(face, faceSize, 0.0, seam);
            case NORTH -> toWorld(face, seam, 0.0, 0.0);
            case SOUTH -> toWorld(face, seam, 0.0, faceSize);
            default -> throw new IllegalStateException(
                    "Vertical direction cannot be a face edge"
            );
        };
    }

    /**
     * Converts a continuous seam coordinate through a topology edge transform.
     * Cell indices use faceSize-1 because they address cells; continuous edge
     * coordinates use faceSize because they address the geometric boundary.
     */
    public double transformSeamCoordinate(
            FaceTransform transform,
            double seam
    ) {
        Objects.requireNonNull(transform, "transform");
        if (seam < 0.0 || seam > faceSize) {
            throw new IllegalArgumentException(
                    "seam must be inside [0, " + faceSize + "]: " + seam
            );
        }

        return transform.reversesSeamCoordinate()
                ? faceSize - seam
                : seam;
    }

    private static double dot(
            PlanetWorldPoint point,
            PlanetVector axis
    ) {
        return point.x() * axis.x()
                + point.y() * axis.y()
                + point.z() * axis.z();
    }

    public record LocalPoint(
            PlanetFace face,
            double x,
            double y,
            double z
    ) {
        public LocalPoint {
            Objects.requireNonNull(face, "face");
        }
    }

    public record PlanetWorldPoint(
            double x,
            double y,
            double z
    ) {
    }
}
