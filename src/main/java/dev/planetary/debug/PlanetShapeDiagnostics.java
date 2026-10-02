package dev.planetary.debug;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.world.PlanetBlockStateFrame;
import dev.planetary.world.PlanetVoxelShapeRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Runtime acceptance for canonical-local -> physical VoxelShape boundaries.
 */
public final class PlanetShapeDiagnostics {
    private static final double EPS = 1.0E-9D;
    private static final VoxelShape BOTTOM_SLAB_CANONICAL =
            Shapes.box(
                    0.0D,
                    0.0D,
                    0.0D,
                    1.0D,
                    0.5D,
                    1.0D
            );

    private PlanetShapeDiagnostics() {
    }

    public static Result verify(
            ServerLevel level
    ) {
        Objects.requireNonNull(level, "level");

        PlanetGravityField field =
                PlanetGravityRuntime.find(level)
                        .orElseThrow();

        int physicalChecks = 0;
        int canonicalChecks = 0;
        int identityChecks = 0;

        BlockState slab =
                Blocks.STONE_SLAB
                        .defaultBlockState()
                        .setValue(
                                SlabBlock.TYPE,
                                SlabType.BOTTOM
                        );

        BlockState hopper =
                Blocks.HOPPER.defaultBlockState();

        VoxelShape canonicalHopperInteraction =
                hopper.getInteractionShape(
                        EmptyBlockGetter.INSTANCE,
                        BlockPos.ZERO
                );

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos pos =
                    twoBlocksFromCore(
                            field,
                            face
                    );

            PlanetBlockStateFrame frame =
                    PlanetBlockStateFrame.resolve(
                            field,
                            pos
                    ).orElseThrow();

            if (frame.face() != face) {
                throw new IllegalStateException(
                        "Shape probe frame mismatch: expected "
                                + face
                                + ", got "
                                + frame.face()
                );
            }

            VoxelShape expectedSlab =
                    frame.rotateShape(
                            BOTTOM_SLAB_CANONICAL
                    );

            assertShape(
                    expectedSlab,
                    slab.getShape(level, pos),
                    face + " outline/simple"
            );
            physicalChecks++;

            assertShape(
                    expectedSlab,
                    slab.getShape(
                            level,
                            pos,
                            CollisionContext.empty()
                    ),
                    face + " outline/context"
            );
            physicalChecks++;

            assertShape(
                    expectedSlab,
                    slab.getCollisionShape(level, pos),
                    face + " collision/simple"
            );
            physicalChecks++;

            assertShape(
                    expectedSlab,
                    slab.getCollisionShape(
                            level,
                            pos,
                            CollisionContext.empty()
                    ),
                    face + " collision/context"
            );
            physicalChecks++;

            assertShape(
                    expectedSlab,
                    slab.getVisualShape(
                            level,
                            pos,
                            CollisionContext.empty()
                    ),
                    face + " visual"
            );
            physicalChecks++;

            assertShape(
                    BOTTOM_SLAB_CANONICAL,
                    slab.getBlockSupportShape(
                            level,
                            pos
                    ),
                    face + " support canonical"
            );
            canonicalChecks++;

            assertShape(
                    BOTTOM_SLAB_CANONICAL,
                    slab.getOcclusionShape(
                            level,
                            pos
                    ),
                    face + " occlusion canonical"
            );
            canonicalChecks++;

            assertShape(
                    PlanetVoxelShapeRotation.localToWorld(
                            canonicalHopperInteraction,
                            face
                    ),
                    hopper.getInteractionShape(
                            level,
                            pos
                    ),
                    face + " interaction"
            );
            physicalChecks++;

            VoxelShape fullCollision =
                    Blocks.STONE.defaultBlockState()
                            .getCollisionShape(
                                    level,
                                    pos,
                                    CollisionContext.empty()
                            );
            if (fullCollision != Shapes.block()) {
                throw new IllegalStateException(
                        "Full-block collision lost Shapes.block identity on "
                                + face
                );
            }
            identityChecks++;
        }

        return new Result(
                physicalChecks,
                canonicalChecks,
                identityChecks
        );
    }

    private static BlockPos twoBlocksFromCore(
            PlanetGravityField field,
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up =
                frame.worldUp();

        return new BlockPos(
                field.core().x() + up.x() * 2,
                field.core().y() + up.y() * 2,
                field.core().z() + up.z() * 2
        );
    }

    private static void assertShape(
            VoxelShape expected,
            VoxelShape actual,
            String label
    ) {
        List<AABB> expectedBoxes =
                sorted(expected.toAabbs());
        List<AABB> actualBoxes =
                sorted(actual.toAabbs());

        if (expectedBoxes.size()
                != actualBoxes.size()) {
            throw new IllegalStateException(
                    label
                            + ": expected "
                            + expectedBoxes.size()
                            + " boxes, got "
                            + actualBoxes.size()
            );
        }

        for (int i = 0; i < expectedBoxes.size(); i++) {
            assertBox(
                    expectedBoxes.get(i),
                    actualBoxes.get(i),
                    label + " box " + i
            );
        }
    }

    private static List<AABB> sorted(
            List<AABB> boxes
    ) {
        List<AABB> copy =
                new ArrayList<>(boxes);
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

    private static void assertBox(
            AABB expected,
            AABB actual,
            String label
    ) {
        if (Math.abs(expected.minX - actual.minX) > EPS
                || Math.abs(expected.minY - actual.minY) > EPS
                || Math.abs(expected.minZ - actual.minZ) > EPS
                || Math.abs(expected.maxX - actual.maxX) > EPS
                || Math.abs(expected.maxY - actual.maxY) > EPS
                || Math.abs(expected.maxZ - actual.maxZ) > EPS) {
            throw new IllegalStateException(
                    label
                            + ": expected "
                            + expected
                            + ", got "
                            + actual
            );
        }
    }

    public record Result(
            int physicalChecks,
            int canonicalChecks,
            int identityChecks
    ) {
    }
}
