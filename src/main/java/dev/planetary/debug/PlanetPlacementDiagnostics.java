package dev.planetary.debug;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Runtime acceptance probe for the first canonical-local placement adapters.
 *
 * <p>No block is placed. The probe builds real BlockPlaceContext instances and
 * invokes vanilla getStateForPlacement methods so Mixin dispatch is exercised
 * without mutating the world.</p>
 */
public final class PlanetPlacementDiagnostics {
    private PlanetPlacementDiagnostics() {
    }

    public static Result verify(
            ServerPlayer player
    ) {
        Objects.requireNonNull(player, "player");

        PlanetGravityField field =
                PlanetGravityRuntime.find(
                        player.serverLevel()
                ).orElseThrow();

        int checks = 0;

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target =
                    oneBlockFromCore(
                            field,
                            face
                    );

            for (Direction localClicked :
                    Direction.values()) {
                double localY =
                        (localClicked.ordinal() & 1) == 0
                                ? 0.25D
                                : 0.75D;

                BlockPlaceContext context =
                        contextFor(
                                player,
                                field,
                                target,
                                localClicked,
                                localY
                        );

                if (!context.getClickedPos().equals(target)) {
                    throw new IllegalStateException(
                            "Placement probe target mismatch at "
                                    + face + " / "
                                    + localClicked
                                    + ": got "
                                    + context.getClickedPos()
                    );
                }

                BlockState logState =
                        Blocks.OAK_LOG
                                .getStateForPlacement(
                                        context
                                );
                if (logState == null
                        || logState.getValue(
                                RotatedPillarBlock.AXIS
                        ) != localClicked.getAxis()) {
                    throw new IllegalStateException(
                            "Rotated pillar placement mismatch at "
                                    + face + " / "
                                    + localClicked
                    );
                }
                checks++;

                Direction expectedHopper =
                        localClicked.getOpposite();
                if (expectedHopper.getAxis()
                        == Direction.Axis.Y) {
                    expectedHopper = Direction.DOWN;
                }

                BlockState hopperState =
                        Blocks.HOPPER
                                .getStateForPlacement(
                                        context
                                );
                if (hopperState == null
                        || hopperState.getValue(
                                HopperBlock.FACING
                        ) != expectedHopper) {
                    throw new IllegalStateException(
                            "Hopper placement mismatch at "
                                    + face + " / "
                                    + localClicked
                                    + ": expected "
                                    + expectedHopper
                                    + ", got "
                                    + (hopperState == null
                                    ? null
                                    : hopperState.getValue(
                                            HopperBlock.FACING
                                    ))
                    );
                }
                checks++;

                BlockState slabState =
                        Blocks.STONE_SLAB
                                .getStateForPlacement(
                                        context
                                );
                boolean expectedBottom =
                        localClicked != Direction.DOWN
                                && (localClicked == Direction.UP
                                || localY <= 0.5D);
                SlabType expectedSlab =
                        expectedBottom
                                ? SlabType.BOTTOM
                                : SlabType.TOP;

                if (slabState == null
                        || slabState.getValue(
                                SlabBlock.TYPE
                        ) != expectedSlab) {
                    throw new IllegalStateException(
                            "Slab placement mismatch at "
                                    + face + " / "
                                    + localClicked
                                    + " localY="
                                    + localY
                                    + ": expected "
                                    + expectedSlab
                                    + ", got "
                                    + (slabState == null
                                    ? null
                                    : slabState.getValue(
                                            SlabBlock.TYPE
                                    ))
                    );
                }
                checks++;
            }
        }

        return new Result(checks);
    }

    private static BlockPlaceContext contextFor(
            ServerPlayer player,
            PlanetGravityField field,
            BlockPos target,
            Direction localClicked,
            double localY
    ) {
        Direction physicalClicked =
                PlanetFrameApi.localSideToPhysical(
                        player.serverLevel(),
                        target,
                        localClicked
                ).orElseThrow();

        BlockPos physicalClickedBlock =
                target.relative(
                        physicalClicked.getOpposite()
                );

        PlanetFace targetFace =
                PlanetFrameApi.canonicalBlockFace(
                        player.serverLevel(),
                        target
                ).orElseThrow();
        PlanetGravityFrame frame =
                new PlanetGravityFrame(
                        targetFace
                );

        PlanetFrameVector localFromCenter =
                new PlanetFrameVector(
                        0.0D,
                        localY - 0.5D,
                        0.0D
                );
        PlanetFrameVector worldFromCenter =
                frame.localToWorld(
                        localFromCenter
                );
        Vec3 worldClick =
                Vec3.atCenterOf(target)
                        .add(
                                worldFromCenter.x(),
                                worldFromCenter.y(),
                                worldFromCenter.z()
                        );

        BlockHitResult hit =
                new BlockHitResult(
                        worldClick,
                        physicalClicked,
                        physicalClickedBlock,
                        false
                );

        return new BlockPlaceContext(
                player,
                InteractionHand.MAIN_HAND,
                new ItemStack(
                        Blocks.STONE_SLAB
                ),
                hit
        );
    }

    private static BlockPos oneBlockFromCore(
            PlanetGravityField field,
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up =
                frame.worldUp();

        return new BlockPos(
                field.core().x() + up.x(),
                field.core().y() + up.y(),
                field.core().z() + up.z()
        );
    }

    public record Result(
            int stateChecks
    ) {
    }
}
