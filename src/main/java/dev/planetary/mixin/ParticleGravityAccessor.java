package dev.planetary.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses fields declared on Particle itself for subclass mixins such as
 * DripParticleGravityMixin. Shadowing inherited fields directly on a
 * DripParticle-targeted mixin is invalid.
 */
@Mixin(Particle.class)
public interface ParticleGravityAccessor {
    @Accessor("level")
    ClientLevel planetary$getLevel();

    @Accessor("x")
    double planetary$getX();

    @Accessor("y")
    double planetary$getY();

    @Accessor("z")
    double planetary$getZ();

    @Accessor("xd")
    double planetary$getXd();

    @Accessor("xd")
    void planetary$setXd(double value);

    @Accessor("yd")
    double planetary$getYd();

    @Accessor("yd")
    void planetary$setYd(double value);

    @Accessor("zd")
    double planetary$getZd();

    @Accessor("zd")
    void planetary$setZd(double value);

    @Accessor("gravity")
    float planetary$getGravity();
}
