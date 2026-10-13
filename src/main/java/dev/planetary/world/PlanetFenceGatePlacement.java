package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Entire inherited FenceGateBlock semantic owner, not a wood-ID patch.
 * Actual item, fluid, signal, neighboring physical BlockPos, sounds and
 * game events are left to Minecraft/NeoForge; only canonical FACING/IN_WALL
 * and the player heading used to choose opening direction are reframed.
 */
public final class PlanetFenceGatePlacement {
    private PlanetFenceGatePlacement() {
    }

    /**
     * Vanilla 1.21.1 IN_WALL owner: lateral WALLS on either side of a gate.
     * Query is in the gate's canonical local chart; lookups perform their
     * own physical seam traversal rather than rewriting Level adjacency.
     */
    public static boolean inWall(
            Direction gateFacing, Predicate<Direction> isWallOnLocalSide
    ) {
        Objects.requireNonNull(gateFacing, "gateFacing");
        Objects.requireNonNull(isWallOnLocalSide, "isWallOnLocalSide");
        if (!gateFacing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Fence gate FACING must be local horizontal");
        }
        Direction perpendicular = gateFacing.getClockWise();
        return isWallOnLocalSide.test(perpendicular)
                || isWallOnLocalSide.test(perpendicular.getOpposite());
    }

    /** Read only the two canonical tangent neighbors, retaining their actual physical positions. */
    public static Optional<Boolean> inWallAt(Level level, BlockPos pos, Direction facing) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");
        return PlanetBlockRuntime.stateFrameAt(level, pos).map(frame ->
                inWall(facing, side -> PlanetBlockRuntime.neighbor(level, pos, side)
                        .map(neighbor -> level.getBlockState(neighbor.targetPos())
                                .is(BlockTags.WALLS))
                        .orElse(false)));
    }

    /**
     * Preserve the state returned by the original getStateForPlacement,
     * especially POWERED and OPEN which are based on PHYSICAL redstone.
     */
    public static Optional<BlockState> afterPlacement(BlockPlaceContext context, BlockState original) {
        Objects.requireNonNull(context, "context");
        if (original == null || !original.hasProperty(FenceGateBlock.FACING)
                || !original.hasProperty(FenceGateBlock.IN_WALL)) {
            return Optional.empty();
        }
        return PlanetBlockRuntime.stateFrameAt(context.getLevel(), context.getClickedPos())
                .flatMap(target -> {
                    Optional<Direction> heading = Optional.empty();
                    if (context.getPlayer() != null) {
                        heading = PlanetFrameApi.localHorizontalDirection(context);
                    } else if (context instanceof DirectionalPlaceContext) {
                        Direction physicalHeading = context.getHorizontalDirection();
                        if (physicalHeading != null) {
                            Direction transformed = target.worldToLocal(physicalHeading);
                            if (transformed.getAxis().isHorizontal()) {
                                heading = Optional.of(transformed);
                            }
                        }
                    }
                    return heading.flatMap(facing ->
                            inWallAt(context.getLevel(), context.getClickedPos(), facing)
                                    .map(wall -> original
                                            .setValue(FenceGateBlock.FACING, facing)
                                            .setValue(FenceGateBlock.IN_WALL, wall)));
                });
    }

    /**
     * updateShape may arrive from physical world UP/DOWN; that could be a
     * lateral wall direction on a rotated Planet face. Recompute at RETURN
     * for any physical neighbor, leaving all other update effects intact.
     */
    public static Optional<BlockState> afterNeighborUpdate(
            LevelAccessor reader, BlockPos pos, BlockState original
    ) {
        if (!(reader instanceof Level level) || original == null
                || !original.hasProperty(FenceGateBlock.FACING)
                || !original.hasProperty(FenceGateBlock.IN_WALL)) {
            return Optional.empty();
        }
        return inWallAt(level, pos, original.getValue(FenceGateBlock.FACING))
                .map(wall -> original.setValue(FenceGateBlock.IN_WALL, wall));
    }

    /**
     * Cross-face player-body-to-target-BlockState semantic heading.
     * The result must remain horizontal in the gate's canonical frame.
     * Reject ambiguous vertical projections instead of inventing yaw.
     */
    public static Optional<Direction> headingInGateFrame(
            PlanetGravityFrame playerFrame,
            PlanetGravityFrame targetFrame,
            Direction playerBodyLocalHeading
    ) {
        Objects.requireNonNull(playerFrame, "playerFrame");
        Objects.requireNonNull(targetFrame, "targetFrame");
        Objects.requireNonNull(playerBodyLocalHeading, "playerBodyLocalHeading");
        Direction physical = PlanetVanillaDirection.localToWorld(
                playerFrame, playerBodyLocalHeading);
        Direction local = PlanetVanillaDirection.worldToLocal(
                targetFrame, physical);
        return local.getAxis().isHorizontal() ? Optional.of(local) : Optional.empty();
    }

    /**
     * Called ONLY instead of the single Player#getDirection invocation
     * inside FenceGateBlock.useWithoutItem. Preserve vanilla mechanics,
     * sound/event dispatch and physical player direction outside Planet.
     *
     * An unavailable actor frame or a non-horizontal target-local result
     * is NOT guessed: vanilla input is returned unchanged.
     */
    public static Direction interactingPlayerDirection(
            Player player, Level level, BlockPos gatePos
    ) {
        Objects.requireNonNull(player, "player");
        Direction vanilla = player.getDirection();
        if (!(player instanceof PlanetGravityEntity entity)) {
            return vanilla;
        }
        Optional<PlanetGravityFrame> playerFrame = entity.planetary$gravityFrame();
        if (playerFrame.isEmpty()) {
            return vanilla;
        }
        return PlanetBlockRuntime.stateFrameAt(level, gatePos)
                .flatMap(target -> headingInGateFrame(
                        playerFrame.get(), new PlanetGravityFrame(target.face()), vanilla))
                .orElse(vanilla);
    }
}
