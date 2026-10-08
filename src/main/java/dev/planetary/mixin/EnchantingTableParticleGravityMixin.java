package dev.planetary.mixin;

import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Complements the common SERVER+CLIENT bookshelf physical neighbor query:
 * vanilla retains its chosen bookshelf and all nextInt/nextFloat sampling.
 * ENCHANT origin and its authored LOCAL bookshelf-offset velocity are reframed
 * once at the existing Level.addParticle boundary.
 */
@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableParticleGravityMixin {
    @ModifyArgs(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void planetary$reframeBookshelfEnchantParticle(
            Args args,
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {
        Optional<PlanetGravityFrame> optional =
                PlanetBlockRuntime.stateFrameAt(level, pos)
                        .map(frame -> frame.frame());

        if (optional.isEmpty() || optional.get().face() == PlanetFace.POS_Y) {
            return;
        }

        PlanetGravityFrame frame = optional.get();
        Vec3 origin = PlanetParticleEmitter.transformVanillaLocalEmitter(
                pos, frame,
                (double) args.get(1),
                (double) args.get(2),
                (double) args.get(3)
        );
        Vec3 physicalVector = PlanetParticleMotion.localVelocityToWorld(
                new Vec3(
                        (double) args.get(4),
                        (double) args.get(5),
                        (double) args.get(6)
                ),
                frame
        );

        args.set(1, origin.x);
        args.set(2, origin.y);
        args.set(3, origin.z);
        args.set(4, physicalVector.x);
        args.set(5, physicalVector.y);
        args.set(6, physicalVector.z);
    }
}
