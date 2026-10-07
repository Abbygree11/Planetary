package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Common block-local emission boundary for audited 1.21.1 animateTick blocks.
 *
 * <p>Vanilla performs every random sample and emits the normal number and
 * types of particles. Only the completed position arguments are reinterpreted
 * in the block's canonical LOCAL frame. The chest/anchor also author
 * direction-sensitive LOCAL velocity; other types preserve physical
 * (zero or isotropic Gaussian) velocity.</p>
 *
 * <p>Do not add blocks to this list without auditing the emitter: fluid,
 * weather, portal plane and candle-lambda sources have different ownership.</p>
 */
@Mixin({
        FurnaceBlock.class,
        BlastFurnaceBlock.class,
        SmokerBlock.class,
        BrewingStandBlock.class,
        EndRodBlock.class,
        RespawnAnchorBlock.class,
        EnderChestBlock.class
})
public abstract class BlockLocalParticleEmitterGravityMixin {
    @ModifyArgs(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void planetary$reframeBlockLocalEmitter(
            Args args,
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(level, pos);

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        PlanetGravityFrame frame = frameOptional.get();
        Vec3 origin = PlanetParticleEmitter.transformVanillaLocalEmitter(
                pos,
                frame,
                (double) args.get(1),
                (double) args.get(2),
                (double) args.get(3)
        );

        args.set(1, origin.x);
        args.set(2, origin.y);
        args.set(3, origin.z);

        // These two sources author a local vertical/tangent drift.
        // End-rod isotropic Gaussian and other sources' zero velocity must
        // remain unmodified; generic Level.addParticle accepts PHYSICAL XYZ.
        if (state.getBlock() instanceof RespawnAnchorBlock
                || state.getBlock() instanceof EnderChestBlock) {
            Vec3 motion = PlanetParticleMotion.localVelocityToWorld(
                    new Vec3(
                            (double) args.get(4),
                            (double) args.get(5),
                            (double) args.get(6)
                    ),
                    frame
            );

            args.set(4, motion.x);
            args.set(5, motion.y);
            args.set(6, motion.z);
        }
    }
}
