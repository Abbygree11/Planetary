package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleMoveRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Local-frame adapter for the BASE vanilla Particle.move implementation.
 *
 * <p>Any particle that dispatches to Particle.move inherits the same vanilla
 * Y-vs-XZ collision semantics, so this is the stable engine boundary. Classes
 * overriding move() bypass this method naturally and remain under their own
 * audit.</p>
 */
@Mixin(Particle.class)
public abstract class LocalGravityParticleMoveMixin {
    @Unique
    private BlockPos.MutableBlockPos planetary$nearBlockPos;

    @Inject(
            method = "move(DDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$moveInLocalFrame(
            double requestedX,
            double requestedY,
            double requestedZ,
            CallbackInfo ci
    ) {
        ParticleGravityAccessor state =
                (ParticleGravityAccessor) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        state.planetary$getLevel(),
                        state.planetary$getX(),
                        state.planetary$getY(),
                        state.planetary$getZ()
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        ci.cancel();

        if (this.planetary$nearBlockPos == null) {
            this.planetary$nearBlockPos =
                    new BlockPos.MutableBlockPos();
        }

        PlanetParticleMoveRuntime.move(
                (Particle) (Object) this,
                state,
                frameOptional.get(),
                requestedX,
                requestedY,
                requestedZ,
                this.planetary$nearBlockPos
        );
    }
}
