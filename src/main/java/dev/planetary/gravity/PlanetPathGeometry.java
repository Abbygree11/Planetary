package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.phys.Vec3;

/**
 * Path-node geometry in a local gravity frame.
 */
public final class PlanetPathGeometry {
    /**
     * Small outward push used only while a path node already belongs to the
     * next gravity face but the entity still belongs to the previous one.
     *
     * <p>A normal side-face feet anchor lies exactly on the cube surface. An
     * AI mob stops at that exact anchor before its center crosses the gravity
     * hysteresis plane. A player keeps holding movement and naturally pushes
     * past it. This temporary target gives AI the same continuation.</p>
     */
    public static final double EDGE_CROSSING_OVERSHOOT = 0.35D;

    private PlanetPathGeometry() {
    }

    public static Vec3 nodeAnchor(
            int nodeX,
            int nodeY,
            int nodeZ,
            PlanetGravityFrame nodeFrame,
            PlanetFace currentFace
    ) {
        PlanetVector up = nodeFrame.worldUp();

        double outward =
                currentFace != null
                        && currentFace != nodeFrame.face()
                        ? EDGE_CROSSING_OVERSHOOT
                        : 0.0D;

        double upOffset = -0.5D + outward;

        return new Vec3(
                nodeX + 0.5D + up.x() * upOffset,
                nodeY + 0.5D + up.y() * upOffset,
                nodeZ + 0.5D + up.z() * upOffset
        );
    }
}
