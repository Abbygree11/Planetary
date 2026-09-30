package dev.planetary.gravity;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Surface-aware random target generation for land mobs.
 *
 * <p>Vanilla RandomPos treats world X/Z as horizontal and world Y as vertical.
 * On a cube planet that produces invalid targets on side/bottom faces. We pick
 * a random direction in the mob's local tangent plane, then project the result
 * back to the same Chebyshev shell. Before an edge this is an ordinary tangent
 * move; beyond an edge the projection naturally wraps the target onto the
 * adjacent cube face.</p>
 */
public final class PlanetMobRandomTarget {
    private static final int ATTEMPTS = 10;
    private static final double MIN_DISTANCE_SQR = 1.0D;

    private PlanetMobRandomTarget() {
    }

    public static Optional<Vec3> randomStroll(
            PathfinderMob mob,
            int radius
    ) {
        if (radius <= 0 || mob.hasRestriction()) {
            return Optional.empty();
        }

        Optional<PlanetGravityField> fieldOptional =
                PlanetGravityRuntime.findFor(mob);
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) mob)
                        .planetary$gravityFrame();

        if (fieldOptional.isEmpty()
                || frameOptional.isEmpty()) {
            return Optional.empty();
        }

        PlanetCore core = fieldOptional.get().core();
        PlanetGravityFrame frame = frameOptional.get();
        Vec3 origin = mob.position();

        for (int i = 0; i < ATTEMPTS; i++) {
            double angle =
                    mob.getRandom().nextDouble()
                            * Math.PI
                            * 2.0D;
            double distance =
                    Math.sqrt(mob.getRandom().nextDouble())
                            * radius;

            double localX =
                    Math.cos(angle) * distance;
            double localZ =
                    Math.sin(angle) * distance;

            Vec3 target = projectTangentToShell(
                    core,
                    frame,
                    origin,
                    localX,
                    localZ
            );

            if (target.distanceToSqr(origin)
                    >= MIN_DISTANCE_SQR) {
                return Optional.of(target);
            }
        }

        return Optional.empty();
    }

    static Vec3 projectTangentToShell(
            PlanetCore core,
            PlanetGravityFrame frame,
            Vec3 origin,
            double localX,
            double localZ
    ) {
        double rx = origin.x - core.centerX();
        double ry = origin.y - core.centerY();
        double rz = origin.z - core.centerZ();

        double shell = maxAbs(rx, ry, rz);
        if (shell <= 1.0E-12D) {
            return origin;
        }

        PlanetFrameVector tangent =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localX,
                                0.0D,
                                localZ
                        )
                );

        double cx = rx + tangent.x();
        double cy = ry + tangent.y();
        double cz = rz + tangent.z();

        double candidateShell =
                maxAbs(cx, cy, cz);
        if (candidateShell <= 1.0E-12D) {
            return origin;
        }

        double scale = shell / candidateShell;

        return new Vec3(
                core.centerX() + cx * scale,
                core.centerY() + cy * scale,
                core.centerZ() + cz * scale
        );
    }

    private static double maxAbs(
            double x,
            double y,
            double z
    ) {
        return Math.max(
                Math.abs(x),
                Math.max(
                        Math.abs(y),
                        Math.abs(z)
                )
        );
    }
}
