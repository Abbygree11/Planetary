package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class PlanetVoxelShapeRotationTest {
    private static final double EPSILON = 1.0E-12;

    @Test
    void rotatesOneAsymmetricBoxCorrectlyOnAllSixFaces() {
        VoxelShape source = Shapes.box(
                1.0D / 16.0D,
                2.0D / 16.0D,
                3.0D / 16.0D,
                5.0D / 16.0D,
                7.0D / 16.0D,
                11.0D / 16.0D
        );

        assertSingleBox(
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.POS_Y
                ),
                box(
                        1, 2, 3,
                        5, 7, 11
                )
        );

        assertSingleBox(
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.NEG_Y
                ),
                box(
                        1, 9, 5,
                        5, 14, 13
                )
        );

        assertSingleBox(
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.POS_X
                ),
                box(
                        2, 11, 3,
                        7, 15, 11
                )
        );

        assertSingleBox(
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.NEG_X
                ),
                box(
                        9, 1, 3,
                        14, 5, 11
                )
        );

        assertSingleBox(
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.POS_Z
                ),
                box(
                        1, 5, 2,
                        5, 13, 7
                )
        );

        assertSingleBox(
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.NEG_Z
                ),
                box(
                        11, 5, 9,
                        15, 13, 14
                )
        );
    }

    @Test
    void localToWorldAndBackPreservesMultiBoxShapeIncludingOutsideBlock() {
        VoxelShape source = Shapes.or(
                Shapes.create(
                        new AABB(
                                -0.25D,
                                0.125D,
                                0.25D,
                                0.25D,
                                0.5D,
                                0.75D
                        )
                ),
                Shapes.create(
                        new AABB(
                                0.625D,
                                0.75D,
                                -0.125D,
                                1.125D,
                                1.25D,
                                0.25D
                        )
                )
        );

        for (PlanetFace face : PlanetFace.values()) {
            VoxelShape world =
                    PlanetVoxelShapeRotation.localToWorld(
                            source,
                            face
                    );
            VoxelShape roundTrip =
                    PlanetVoxelShapeRotation.worldToLocal(
                            world,
                            face
                    );

            assertBoxesEqual(
                    source.toAabbs(),
                    roundTrip.toAabbs(),
                    face.toString()
            );
        }
    }

    @Test
    void invariantSingletonShapesKeepVanillaIdentityOnEveryFace() {
        VoxelShape full = Shapes.block();
        VoxelShape empty = Shapes.empty();

        for (PlanetFace face : PlanetFace.values()) {
            assertSame(
                    full,
                    PlanetVoxelShapeRotation.localToWorld(
                            full,
                            face
                    ),
                    face + " full"
            );
            assertSame(
                    empty,
                    PlanetVoxelShapeRotation.localToWorld(
                            empty,
                            face
                    ),
                    face + " empty"
            );
            assertSame(
                    full,
                    PlanetVoxelShapeRotation.worldToLocal(
                            full,
                            face
                    ),
                    face + " inverse full"
            );
            assertSame(
                    empty,
                    PlanetVoxelShapeRotation.worldToLocal(
                            empty,
                            face
                    ),
                    face + " inverse empty"
            );
        }
    }

    @Test
    void repeatedLocalToWorldRotationReusesCachedInstance() {
        VoxelShape source = Shapes.or(
                Shapes.box(
                        0.0D,
                        0.0D,
                        0.0D,
                        0.25D,
                        0.5D,
                        0.75D
                ),
                Shapes.box(
                        0.5D,
                        0.625D,
                        0.125D,
                        1.0D,
                        1.0D,
                        0.375D
                )
        );

        VoxelShape first =
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.POS_X
                );
        VoxelShape second =
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.POS_X
                );

        assertSame(first, second);
        assertSame(
                source,
                PlanetVoxelShapeRotation.localToWorld(
                        source,
                        PlanetFace.POS_Y
                )
        );
    }

    private static AABB box(
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        return new AABB(
                minX / 16.0D,
                minY / 16.0D,
                minZ / 16.0D,
                maxX / 16.0D,
                maxY / 16.0D,
                maxZ / 16.0D
        );
    }

    private static void assertSingleBox(
            VoxelShape actual,
            AABB expected
    ) {
        List<AABB> boxes = actual.toAabbs();
        assertEquals(1, boxes.size());
        assertBoxEquals(expected, boxes.getFirst(), "");
    }

    private static void assertBoxesEqual(
            List<AABB> expected,
            List<AABB> actual,
            String message
    ) {
        List<AABB> expectedSorted =
                sortedCopy(expected);
        List<AABB> actualSorted =
                sortedCopy(actual);

        assertEquals(
                expectedSorted.size(),
                actualSorted.size(),
                message
        );

        for (int i = 0; i < expectedSorted.size(); i++) {
            assertBoxEquals(
                    expectedSorted.get(i),
                    actualSorted.get(i),
                    message + " box " + i
            );
        }
    }

    private static List<AABB> sortedCopy(
            List<AABB> boxes
    ) {
        List<AABB> copy = new ArrayList<>(boxes);
        copy.sort(
                Comparator.comparingDouble(
                                (AABB box) -> box.minX
                        )
                        .thenComparingDouble(
                                box -> box.minY
                        )
                        .thenComparingDouble(
                                box -> box.minZ
                        )
                        .thenComparingDouble(
                                box -> box.maxX
                        )
                        .thenComparingDouble(
                                box -> box.maxY
                        )
                        .thenComparingDouble(
                                box -> box.maxZ
                        )
        );
        return copy;
    }

    private static void assertBoxEquals(
            AABB expected,
            AABB actual,
            String message
    ) {
        assertEquals(
                expected.minX,
                actual.minX,
                EPSILON,
                message + " minX"
        );
        assertEquals(
                expected.minY,
                actual.minY,
                EPSILON,
                message + " minY"
        );
        assertEquals(
                expected.minZ,
                actual.minZ,
                EPSILON,
                message + " minZ"
        );
        assertEquals(
                expected.maxX,
                actual.maxX,
                EPSILON,
                message + " maxX"
        );
        assertEquals(
                expected.maxY,
                actual.maxY,
                EPSILON,
                message + " maxY"
        );
        assertEquals(
                expected.maxZ,
                actual.maxZ,
                EPSILON,
                message + " maxZ"
        );
    }
}
