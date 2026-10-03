package dev.planetary.debug;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Runtime acceptance for StandingAndWallBlockItem variant selection.
 *
 * <p>The target is deliberately inside the solid central area, so standing and
 * multiple wall supports can all be valid at once. The clicked local face must
 * therefore determine which variant wins, matching real "place inside a hole"
 * behavior without modifying the world.</p>
 */
public final class PlanetStandingWallPlacementDiagnostics {
    private static final ProbeTorchItem PROBE =
            new ProbeTorchItem();

    private PlanetStandingWallPlacementDiagnostics() {
    }

    public static Result verify(
            ServerPlayer player
    ) {
        Objects.requireNonNull(player, "player");

        PlanetGravityField field =
                PlanetGravityRuntime.find(
                        player.serverLevel()
                ).orElseThrow();

        int standingChecks = 0;
        int wallChecks = 0;

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target =
                    twoBlocksFromCore(
                            field,
                            face
                    );

            BlockState standing =
                    PROBE.select(
                            contextFor(
                                    player,
                                    target,
                                    Direction.UP
                            )
                    );

            if (standing == null
                    || !standing.is(Blocks.TORCH)) {
                throw new IllegalStateException(
                        "Standing/wall probe expected standing torch on local floor at "
                                + face
                                + ", got "
                                + standing
                );
            }
            standingChecks++;

            for (Direction localClickedFace :
                    new Direction[] {
                            Direction.NORTH,
                            Direction.SOUTH,
                            Direction.WEST,
                            Direction.EAST
                    }) {
                BlockState wall =
                        PROBE.select(
                                contextFor(
                                        player,
                                        target,
                                        localClickedFace
                                )
                        );

                if (wall == null
                        || !wall.is(Blocks.WALL_TORCH)) {
                    throw new IllegalStateException(
                            "Standing/wall probe expected wall torch at "
                                    + face
                                    + " / clicked "
                                    + localClickedFace
                                    + ", got "
                                    + wall
                    );
                }

                Direction expectedFacing =
                        localClickedFace;
                Direction actualFacing =
                        wall.getValue(
                                net.minecraft.world.level.block.WallTorchBlock.FACING
                        );

                if (actualFacing != expectedFacing) {
                    throw new IllegalStateException(
                            "Wall torch local facing mismatch at "
                                    + face
                                    + " / clicked "
                                    + localClickedFace
                                    + ": expected "
                                    + expectedFacing
                                    + ", got "
                                    + actualFacing
                    );
                }

                wallChecks++;
            }
        }

        return new Result(
                standingChecks,
                wallChecks
        );
    }

    private static BlockPlaceContext contextFor(
            ServerPlayer player,
            BlockPos target,
            Direction localClickedFace
    ) {
        Direction physicalClickedFace =
                PlanetFrameApi.localSideToPhysical(
                        player.serverLevel(),
                        target,
                        localClickedFace
                ).orElseThrow();

        BlockPos support =
                target.relative(
                        physicalClickedFace.getOpposite()
                );

        BlockHitResult hit =
                new BlockHitResult(
                        Vec3.atCenterOf(target),
                        physicalClickedFace,
                        support,
                        false
                );

        BlockPlaceContext context =
                new BlockPlaceContext(
                        player,
                        InteractionHand.MAIN_HAND,
                        new ItemStack(Items.TORCH),
                        hit
                );

        if (!context.getClickedPos().equals(target)) {
            throw new IllegalStateException(
                    "Standing/wall probe target mismatch: expected "
                            + target
                            + ", got "
                            + context.getClickedPos()
            );
        }

        return context;
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

    private static final class ProbeTorchItem
            extends StandingAndWallBlockItem {
        private ProbeTorchItem() {
            super(
                    Blocks.TORCH,
                    Blocks.WALL_TORCH,
                    new Item.Properties(),
                    Direction.DOWN
            );
        }

        private BlockState select(
                BlockPlaceContext context
        ) {
            return super.getPlacementState(
                    context
            );
        }
    }

    public record Result(
            int standingChecks,
            int wallChecks
    ) {
    }
}
