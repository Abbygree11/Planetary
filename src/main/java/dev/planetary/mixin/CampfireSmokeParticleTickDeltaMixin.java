package dev.planetary.mixin;

import net.minecraft.client.particle.CampfireSmokeParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Exact 1.21.1 tick() bridge for CampfireSmokeParticle.
 *
 * <p>Java compiles this.move(...) to an INVOKEVIRTUAL owned by CampfireSmokeParticle,
 * even though Particle declares the inherited move method. The old
 * multi-target injection incorrectly matched Particle.move and prevented
 * the entire client from starting. Never suppress missing targets.</p>
 */
@Mixin(CampfireSmokeParticle.class)
public abstract class CampfireSmokeParticleTickDeltaMixin {
    @Unique
    private PlanetSemanticTickDeltaState planetary$tickDelta;

    @Inject(method = "tick", at = @At("HEAD"))
    private void planetary$captureTickVelocity(CallbackInfo ci) {
        planetary$tickDelta = PlanetSemanticTickDeltaState.begin(
                planetary$tickDelta,
                (ParticleGravityAccessor) (Object) this
        );
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/CampfireSmokeParticle;move(DDD)V"
            )
    )
    private void planetary$reinterpretTickDeltaBeforeMove(CallbackInfo ci) {
        PlanetSemanticTickDeltaState.beforeMove(
                planetary$tickDelta,
                (ParticleGravityAccessor) (Object) this
        );
    }
}
