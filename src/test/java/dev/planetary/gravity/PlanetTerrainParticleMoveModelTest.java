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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetTerrainParticleMoveModelTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void floorContactIsRotationallyEquivalentAcrossAllFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            AABB particle =
                    localBoxToWorld(
                            frame,
                            -0.1D,
                            0.02D,
                            -0.1D,
                            0.1D,
                            0.22D,
                            0.1D
                    );
            AABB floor =
                    localBoxToWorld(
                            frame,
                            -2.0D,
                            -1.0D,
                            -2.0D,
                            2.0D,
                            0.0D,
                            2.0D
                    );

            Vec3 requested =
                    worldVector(
                            frame,
                            0.06D,
                            -0.08D,
                            -0.04D
                    );

            Vec3 actual =
                    PlanetEntityCollision.collideWithShapes(
                            requested,
                            particle,
                            List.of(Shapes.create(floor)),
                            frame
                    );

            PlanetFrameVector actualLocal =
                    frame.worldToLocal(
                            new PlanetFrameVector(
                                    actual.x,
                                    actual.y,
                                    actual.z
                            )
                    );

            assertEquals(
                    0.06D,
                    actualLocal.x(),
                    EPSILON,
                    face.name() + " tangent X"
            );
            assertEquals(
                    -0.02D,
                    actualLocal.y(),
                    EPSILON,
                    face.name() + " local DOWN"
            );
            assertEquals(
                    -0.04D,
                    actualLocal.z(),
                    EPSILON,
                    face.name() + " tangent Z"
            );

            PlanetParticleCollisionResponse.Result response =
                    PlanetParticleCollisionResponse.apply(
                            requested,
                            actual,
                            requested,
                            frame
                    );

            assertTrue(
                    response.onGround(),
                    face.name()
            );
        }
    }

    @Test
    void nextDownwardMoveAtFloorTriggersSameStickyStopOnEveryFace() {
        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            AABB particle =
                    localBoxToWorld(
                            frame,
                            -0.1D,
                            0.0D,
                            -0.1D,
                            0.1D,
                            0.2D,
                            0.1D
                    );
            AABB floor =
                    localBoxToWorld(
                            frame,
                            -2.0D,
                            -1.0D,
                            -2.0D,
                            2.0D,
                            0.0D,
                            2.0D
                    );

            Vec3 requested =
                    worldVector(
                            frame,
                            0.03D,
                            -0.05D,
                            0.02D
                    );

            Vec3 actual =
                    PlanetEntityCollision.collideWithShapes(
                            requested,
                            particle,
                            List.of(Shapes.create(floor)),
                            frame
                    );

            PlanetParticleCollisionResponse.Result response =
                    PlanetParticleCollisionResponse.apply(
                            requested,
                            actual,
                            requested,
                            frame
                    );

            assertTrue(
                    response.stoppedByCollision(),
                    face.name()
            );
            assertTrue(
                    response.onGround(),
                    face.name()
            );
        }
    }

    private static Vec3 worldVector(
            PlanetGravityFrame frame,
            double x,
            double y,
            double z
    ) {
        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                x,
                                y,
                                z
                        )
                );

        return new Vec3(
                world.x(),
                world.y(),
                world.z()
        );
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
                                            x,
                                            y,
                                            z
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
