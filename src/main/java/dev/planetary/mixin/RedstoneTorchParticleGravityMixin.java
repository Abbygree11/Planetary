package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Reorients lit redstone-torch particle emitter positions without touching
 * redstone signal semantics.
 *
 * <p>Both vanilla redstone torch variants calculate their complete randomised
 * coordinates before Level.addParticle. Treat that final delta from block
 * center as LOCAL coordinates, preserving the exact random sample and wall
 * offset while rotating only the frame.</p>
 */
@Mixin({
        RedstoneTorchBlock.class,
        RedstoneWallTorchBlock.class
})
public abstract class RedstoneTorchParticleGravityMixin {
    @ModifyArgs(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void planetary$localRedstoneTorchEmitter(
            Args args,
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
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

        Vec3 emitter =
                PlanetParticleEmitter.transformVanillaLocalEmitter(
                        pos,
                        frameOptional.get(),
                        (double) args.get(1),
                        (double) args.get(2),
                        (double) args.get(3)
                );

        args.set(1, emitter.x);
        args.set(2, emitter.y);
        args.set(3, emitter.z);
    }
}
