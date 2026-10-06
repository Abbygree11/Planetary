package dev.planetary.mixin;

import net.minecraft.client.particle.DragonBreathParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DragonBreathParticle.class)
public interface DragonBreathParticleAccessor {
    @Accessor("hasHitGround")
    boolean planetary$hasHitGround();
}
