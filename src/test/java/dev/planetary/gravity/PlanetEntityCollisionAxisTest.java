package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetEntityCollisionAxisTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void floorCollisionClipsLocalDownOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            AABB player = localBoxToWorld(
                    frame,
                    -0.3D, 0.0D, -0.3D,
                    0.3D, 1.8D, 0.3D
            );
            AABB floor = localBoxToWorld(
                    frame,
                    -1.0D, -1.0D, -1.0D,
                    1.0D, 0.0D, 1.0D
            );

            Vec3 down =
                    PlanetEntityGeometry.localOffsetToWorld(
                            frame,
                            0.0D,
                            -0.08D,
                            0.0D
                    );

            Vec3 clipped =
                    PlanetEntityCollision.collideWithShapes(
                            down,
                            player,
                            List.of(Shapes.create(floor)),
                            frame
                    );

            assertEquals(
                    0.0D,
                    clipped.lengthSqr(),
                    EPSILON,
                    face.toString()
            );
        }
    }

    @Test
    void localUpRemainsFreeOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            AABB player = localBoxToWorld(
                    frame,
                    -0.3D, 0.0D, -0.3D,
                    0.3D, 1.8D, 0.3D
            );
            AABB floor = localBoxToWorld(
                    frame,
                    -1.0D, -1.0D, -1.0D,
                    1.0D, 0.0D, 1.0D
            );

            Vec3 up =
                    PlanetEntityGeometry.localOffsetToWorld(
                            frame,
                            0.0D,
                            0.08D,
                            0.0D
                    );

            Vec3 actual =
                    PlanetEntityCollision.collideWithShapes(
                            up,
                            player,
                            List.of(Shapes.create(floor)),
                            frame
                    );

            assertEquals(up.x, actual.x, EPSILON, face + " x");
            assertEquals(up.y, actual.y, EPSILON, face + " y");
            assertEquals(up.z, actual.z, EPSILON, face + " z");
        }
    }

    private static AABB localBoxToWorld(
            PlanetGravityFrame frame,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        double worldMinX = Double.POSITIVE_INFINITY;
        double worldMinY = Double.POSITIVE_INFINITY;
        double worldMinZ = Double.POSITIVE_INFINITY;
        double worldMaxX = Double.NEGATIVE_INFINITY;
        double worldMaxY = Double.NEGATIVE_INFINITY;
        double worldMaxZ = Double.NEGATIVE_INFINITY;

        for (double x : new double[]{minX, maxX}) {
            for (double y : new double[]{minY, maxY}) {
                for (double z : new double[]{minZ, maxZ}) {
                    PlanetFrameVector world =
                            frame.localToWorld(
                                    new PlanetFrameVector(
                                            x, y, z
                                    )
                            );

                    worldMinX = Math.min(worldMinX, world.x());
                    worldMinY = Math.min(worldMinY, world.y());
                    worldMinZ = Math.min(worldMinZ, world.z());
                    worldMaxX = Math.max(worldMaxX, world.x());
                    worldMaxY = Math.max(worldMaxY, world.y());
                    worldMaxZ = Math.max(worldMaxZ, world.z());
                }
            }
        }

        return new AABB(
                worldMinX,
                worldMinY,
                worldMinZ,
                worldMaxX,
                worldMaxY,
                worldMaxZ
        );
    }
}
