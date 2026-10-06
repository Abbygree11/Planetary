package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CherryLeavesBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Replaces only the cherry-petal branch of CherryLeavesBlock.animateTick after
 * LeavesBlock.animateTick has already run.
 */
@Mixin(CherryLeavesBlock.class)
public abstract class CherryLeavesParticleGravityMixin {
    @Inject(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/LeavesBlock;animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void planetary$emitCherryPetalBelowLocalFace(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        level,
                        pos
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        // LeavesBlock.animateTick has already executed. Skip only vanilla's
        // CherryLeavesBlock world-Y particle branch and reproduce it locally.
        ci.cancel();

        if (random.nextInt(10) != 0) {
            return;
        }

        PlanetGravityFrame frame =
                frameOptional.get();
        Direction physicalDown =
                PlanetBlockGravity.toDirection(
                        frame.worldDown()
                );
        BlockPos below =
                pos.relative(
                        physicalDown
                );
        BlockState belowState =
                level.getBlockState(
                        below
                );
        VoxelShape belowShape =
                belowState.getCollisionShape(
                        level,
                        below
                );

        // Vanilla asks whether the UP face of pos.below() is full. In the
        // rotated physical shape this is the face pointing back toward leaves.
        Direction faceTowardLeaves =
                physicalDown.getOpposite();

        if (Block.isFaceFull(
                belowShape,
                faceTowardLeaves
        )) {
            return;
        }

        // Preserve vanilla RNG order: X sample first, then Z sample.
        double localXSample =
                random.nextDouble();
        double localZSample =
                random.nextDouble();

        Vec3 origin =
                PlanetParticleEmitter.belowBlock(
                        pos,
                        frame,
                        localXSample,
                        localZSample
                );

        level.addParticle(
                ParticleTypes.CHERRY_LEAVES,
                origin.x,
                origin.y,
                origin.z,
                0.0D,
                0.0D,
                0.0D
        );
    }
}
