package dev.planetary.gravity;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetMobRandomTargetTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void tangentTargetKeepsSameCubeShell() {
        PlanetCore core =
                new PlanetCore(0, 128, 0, 48);
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_Y
                );

        Vec3 origin = new Vec3(
                core.centerX(),
                core.centerY() + 48.5D,
                core.centerZ()
        );

        Vec3 target =
                PlanetMobRandomTarget
                        .projectTangentToShell(
                                core,
                                frame,
                                origin,
                                12.0D,
                                7.0D
                        );

        assertEquals(
                48.5D,
                shell(core, target),
                EPSILON
        );
    }

    @Test
    void tangentTargetWrapsAcrossCubeEdge() {
        PlanetCore core =
                new PlanetCore(0, 128, 0, 48);
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        PlanetFace.POS_Y
                );

        Vec3 origin = new Vec3(
                core.centerX(),
                core.centerY() + 48.5D,
                core.centerZ()
        );

        Vec3 target =
                PlanetMobRandomTarget
                        .projectTangentToShell(
                                core,
                                frame,
                                origin,
                                80.0D,
                                0.0D
                        );

        double rx =
                target.x - core.centerX();
        double ry =
                target.y - core.centerY();

        assertEquals(
                48.5D,
                Math.abs(rx),
                EPSILON
        );
        assertTrue(
                Math.abs(ry) < 48.5D,
                "target should wrap from top face onto an X face"
        );
        assertEquals(
                48.5D,
                shell(core, target),
                EPSILON
        );
    }

    private static double shell(
            PlanetCore core,
            Vec3 point
    ) {
        return Math.max(
                Math.abs(
                        point.x - core.centerX()
                ),
                Math.max(
                        Math.abs(
                                point.y - core.centerY()
                        ),
                        Math.abs(
                                point.z - core.centerZ()
                        )
                )
        );
    }
}
