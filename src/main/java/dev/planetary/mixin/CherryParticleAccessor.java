package dev.planetary.mixin;

import net.minecraft.client.particle.CherryParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CherryParticle.class)
public interface CherryParticleAccessor {
    @Accessor("rotSpeed")
    float planetary$getRotSpeed();

    @Accessor("rotSpeed")
    void planetary$setRotSpeed(float value);

    @Accessor("particleRandom")
    float planetary$getParticleRandom();

    @Accessor("spinAcceleration")
    float planetary$getSpinAcceleration();
}
