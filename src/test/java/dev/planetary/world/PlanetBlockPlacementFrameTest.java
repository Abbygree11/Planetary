package dev.planetary.world;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetBlockPlacementFrameTest {
    private static final int R = 20;
    private static final PlanetCore CORE =
            new PlanetCore(3, 100, -7, R);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);
    private static final double EPS = 1.0E-9D;

    @Test
    void physicalHitFacesRoundTripThroughCanonicalLocalFrameOnAllSixFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            PlanetBlockStateFrame stateFrame =
                    PlanetBlockStateFrame.resolve(
                            FIELD,
                            target
                    ).orElseThrow();

            for (Direction local : Direction.values()) {
                Direction physical =
                        stateFrame.localToWorld(local);

                PlanetBlockPlacementFrame placement =
                        PlanetBlockPlacementFrame.resolve(
                                FIELD,
                                target,
                                physical,
                                Vec3.atCenterOf(target)
                        ).orElseThrow();

                assertEquals(face, placement.targetStateFrame().face());
                assertEquals(physical, placement.physicalClickedFace());
                assertEquals(
                        local,
                        placement.localClickedFace(),
                        face + " / " + local
                );
            }
        }
    }

    @Test
    void localHitOffsetRoundTripsOnAllSixFaces() {
        Vec3 expectedLocal =
                new Vec3(0.2D, 0.8D, 0.35D);

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            PlanetFrameVector localFromCenter =
                    new PlanetFrameVector(
                            expectedLocal.x - 0.5D,
                            expectedLocal.y - 0.5D,
                            expectedLocal.z - 0.5D
                    );
            PlanetFrameVector worldFromCenter =
                    frame.localToWorld(
                            localFromCenter
                    );
            Vec3 center = Vec3.atCenterOf(target);
            Vec3 worldClick =
                    center.add(
                            worldFromCenter.x(),
                            worldFromCenter.y(),
                            worldFromCenter.z()
                    );

            PlanetBlockPlacementFrame placement =
                    PlanetBlockPlacementFrame.resolve(
                            FIELD,
                            target,
                            stateFrame(target)
                                .localToWorld(Direction.UP),
                            worldClick
                    ).orElseThrow();

            assertEquals(
                    expectedLocal.x,
                    placement.localHitOffset().x,
                    EPS,
                    face.toString()
            );
            assertEquals(
                    expectedLocal.y,
                    placement.localHitOffset().y,
                    EPS,
                    face.toString()
            );
            assertEquals(
                    expectedLocal.z,
                    placement.localHitOffset().z,
                    EPS,
                    face.toString()
            );
            assertEquals(true, placement.localHitUpperHalf());
        }
    }

    @Test
    void exactEdgeUsesTargetCanonicalFrameNotSourceTraversalChart() {
        BlockPos target =
                new BlockPos(
                        CORE.x() + R,
                        CORE.y() + R,
                        CORE.z()
                );

        PlanetBlockPlacementFrame placement =
                PlanetBlockPlacementFrame.resolve(
                        FIELD,
                        target,
                        Direction.UP,
                        Vec3.atCenterOf(target)
                ).orElseThrow();

        assertEquals(
                PlanetFace.POS_X,
                placement.targetStateFrame().face()
        );
        assertEquals(
                Direction.WEST,
                placement.localClickedFace()
        );
    }

    private static PlanetBlockStateFrame stateFrame(
            BlockPos pos
    ) {
        return PlanetBlockStateFrame.resolve(
                FIELD,
                pos
        ).orElseThrow();
    }

    private static BlockPos faceCenter(
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up =
                frame.worldUp();

        return new BlockPos(
                CORE.x() + up.x() * R,
                CORE.y() + up.y() * R,
                CORE.z() + up.z() * R
        );
    }
}
