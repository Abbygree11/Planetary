package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;

import java.util.Objects;
import java.util.Optional;

/**
 * Explicit P10 16-angle author adapter. A standing sign/banner, standing
 * skull, and ceiling-hanging sign have separate ORIGINAL yaw-zero and
 * support/attachment semantics; this shared helper transports actor yaw
 * through real physical XYZ to the target canonical tangent plane.
 *
 * No global BlockState/Rotation rewriting, item variant choice, fluid or
 * BE/BannerPattern/SignText transform. Invalid author/chart leaves original.
 */
public final class PlanetRotation16Placement {
    public enum Owner {
        STANDING_SIGN_OR_BANNER,
        SKULL,
        CEILING_HANGING_SIGN
    }

    /** All owner angles, including ceiling's snapped rigid support mode. */
    public record Rotation(int southZero, int northZero, int snappedCeiling) {}

    private PlanetRotation16Placement() {
    }

    /** Pure source-body-yaw to physical-world to target-local projection. */
    public static Optional<Rotation> rotation(
            PlanetGravityFrame actorFrame,
            PlanetGravityFrame targetFrame,
            float actorBodyYaw
    ) {
        Objects.requireNonNull(actorFrame, "actorFrame");
        Objects.requireNonNull(targetFrame, "targetFrame");
        if (!Float.isFinite(actorBodyYaw)) {
            return Optional.empty();
        }
        double radians = Math.toRadians(actorBodyYaw);
        // Vanilla yaw 0 = SOUTH, 90 = WEST in local body coordinates.
        PlanetFrameVector localActor = new PlanetFrameVector(
                -Math.sin(radians), 0, Math.cos(radians));
        PlanetFrameVector physical = actorFrame.localToWorld(localActor);
        PlanetFrameVector localTarget = targetFrame.worldToLocal(physical);
        Optional<Integer> southZero = PlanetOrientationProperties
                .targetTangentRotation16(localTarget,
                        PlanetOrientationProperties.Rotation16Zero.SOUTH_CLOCKWISE);
        Optional<Integer> northZero = PlanetOrientationProperties
                .targetTangentRotation16(localTarget,
                        PlanetOrientationProperties.Rotation16Zero.NORTH_CLOCKWISE);
        if (southZero.isEmpty() || northZero.isEmpty()) {
            // A body heading parallel to target local UP has no projected
            // tangent yaw: do not make up a new canonical orientation.
            return Optional.empty();
        }
        double targetYaw = Math.toDegrees(Math.atan2(
                -localTarget.x(), localTarget.z()));
        Direction snappedActorHeading = Direction.fromYRot(targetYaw);
        int snapped = RotationSegment.convertToSegment(
                snappedActorHeading.getOpposite());
        return Optional.of(new Rotation(
                southZero.get(), northZero.get(), snapped));
    }

    /** Whole declaring-owner family hook AFTER original NeoForge placement. */
    public static Optional<BlockState> afterPlacement(
            BlockPlaceContext context, BlockState original, Owner owner
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(owner, "owner");
        if (original == null
                || !original.hasProperty(BlockStateProperties.ROTATION_16)) {
            return Optional.empty();
        }
        Player player = context.getPlayer();
        if (!(player instanceof PlanetGravityEntity gravityEntity)) {
            return Optional.empty();
        }
        return PlanetFrameApi.placementFrame(context).flatMap(placement ->
                gravityEntity.planetary$gravityFrame().flatMap(actor ->
                        rotation(actor, new PlanetGravityFrame(
                                        placement.targetFace()),
                                context.getRotation()))).map(angles -> {
            int value;
            if (owner == Owner.SKULL) {
                value = angles.southZero();
            } else if (owner == Owner.CEILING_HANGING_SIGN
                    && original.hasProperty(CeilingHangingSignBlock.ATTACHED)) {
                boolean attached = attachedAtCanonicalSupport(
                        context, original, angles);
                value = attached ? angles.northZero()
                        : angles.snappedCeiling();
                return original.setValue(CeilingHangingSignBlock.ATTACHED, attached)
                        .setValue(BlockStateProperties.ROTATION_16, value);
            } else {
                value = angles.northZero();
            }
            return original.setValue(BlockStateProperties.ROTATION_16, value);
        });
    }

    /**
     * Only for the explicitly opted-in ceiling owner. Preserve the vanilla
     * chain, non-full collision face, sneak and existing hanging-sign axis
     * policy while converting support and heading into the target chart.
     * Wall variants and structures are independent authors.
     */
    private static boolean attachedAtCanonicalSupport(
            BlockPlaceContext context, BlockState original, Rotation angles
    ) {
        if (!(context.getLevel() instanceof Level level)) {
            return original.getValue(CeilingHangingSignBlock.ATTACHED);
        }
        BlockPos pos = context.getClickedPos();
        var support = PlanetBlockSupportRuntime.query(
                level, pos, Direction.UP);
        if (support.isEmpty()) {
            return original.getValue(CeilingHangingSignBlock.ATTACHED);
        }
        var query = support.get();
        BlockState upper = level.getBlockState(query.supportPos());
        boolean sneaking = context.isSecondaryUseActive();
        boolean attached = !Block.isFaceFull(
                upper.getCollisionShape(level, query.supportPos()),
                query.supportLocalSideTowardSource()) || sneaking;
        if (attached && !sneaking && upper.is(BlockTags.ALL_HANGING_SIGNS)) {
            Direction localHeading = RotationSegment
                    .convertToDirection(angles.snappedCeiling())
                    .map(Direction::getOpposite).orElse(Direction.SOUTH);
            if (upper.hasProperty(WallHangingSignBlock.FACING)) {
                attached = !upper.getValue(WallHangingSignBlock.FACING)
                        .getAxis().test(localHeading);
            } else if (upper.hasProperty(BlockStateProperties.ROTATION_16)) {
                attached = RotationSegment.convertToDirection(
                        upper.getValue(BlockStateProperties.ROTATION_16))
                        .filter(other -> other.getAxis().test(localHeading))
                        .isEmpty();
            }
        }
        return attached;
    }
}
