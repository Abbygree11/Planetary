package dev.planetary.topology;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Topology of the six face-local grids that form the cube planet shell.
 *
 * <p>This class handles only adjacency and frame rotation. It intentionally
 * does not decide chunk storage, gravity strength, rendering or world
 * generation.</p>
 */
public final class PlanetTopology {
    private static final Map<PlanetFace, Map<PlanetDirection, FaceTransform>> TRANSFORMS = buildTransforms();

    private PlanetTopology() {
    }

    public static FaceTransform edgeTransform(PlanetFace face, PlanetDirection edge) {
        Objects.requireNonNull(face, "face");
        Objects.requireNonNull(edge, "edge");
        if (!edge.isHorizontal()) {
            throw new IllegalArgumentException("Only horizontal directions identify a face edge: " + edge);
        }
        return TRANSFORMS.get(face).get(edge);
    }

    public static PlanetPos step(PlanetPos pos, PlanetDirection direction, int faceSize) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(direction, "direction");
        FaceTransform.validateFaceSize(faceSize);
        FaceTransform.requireInsideFace(pos, faceSize);

        if (!direction.isHorizontal()) {
            return pos.offsetLocal(direction);
        }

        int nextX = pos.x() + direction.dx();
        int nextZ = pos.z() + direction.dz();
        if (nextX >= 0 && nextX < faceSize && nextZ >= 0 && nextZ < faceSize) {
            return pos.offsetLocal(direction);
        }

        return edgeTransform(pos.face(), direction).crossBoundaryCell(pos, faceSize);
    }

    public static PlanetDirection transformDirection(
            PlanetFace face,
            PlanetDirection edge,
            PlanetDirection direction
    ) {
        return edgeTransform(face, edge).transformDirection(direction);
    }

    private static Map<PlanetFace, Map<PlanetDirection, FaceTransform>> buildTransforms() {
        EnumMap<PlanetFace, Map<PlanetDirection, FaceTransform>> result = new EnumMap<>(PlanetFace.class);
        for (PlanetFace face : PlanetFace.values()) {
            EnumMap<PlanetDirection, FaceTransform> byEdge = new EnumMap<>(PlanetDirection.class);
            for (PlanetDirection edge : new PlanetDirection[]{
                    PlanetDirection.NORTH,
                    PlanetDirection.SOUTH,
                    PlanetDirection.WEST,
                    PlanetDirection.EAST
            }) {
                byEdge.put(edge, FaceTransform.across(face, edge));
            }
            result.put(face, Map.copyOf(byEdge));
        }
        return Map.copyOf(result);
    }
}
