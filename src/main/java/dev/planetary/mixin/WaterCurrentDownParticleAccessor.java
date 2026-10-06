package dev.planetary.mixin;

import net.minecraft.client.particle.WaterCurrentDownParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WaterCurrentDownParticle.class)
public interface WaterCurrentDownParticleAccessor {
    @Accessor("angle")
    float planetary$getAngle();
}
