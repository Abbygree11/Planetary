package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Reorients standing/wall torch flame and smoke emitter positions.
 *
 * <p>Vanilla has already calculated the complete emitter coordinates when this
 * hook runs. Their delta from the block center is semantic LOCAL geometry, so
 * preserve it exactly and only rotate that delta into physical world XYZ.</p>
 */
@Mixin({
        TorchBlock.class,
        WallTorchBlock.class
})
public abstract class TorchParticleGravityMixin {
    @ModifyArgs(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void planetary$localTorchEmitter(
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
