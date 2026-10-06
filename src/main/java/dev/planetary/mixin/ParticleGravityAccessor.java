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

    @Accessor("xo")
    void planetary$setXo(double value);

    @Accessor("yo")
    void planetary$setYo(double value);

    @Accessor("zo")
    void planetary$setZo(double value);

    @Accessor("lifetime")
    int planetary$getLifetime();

    @Accessor("lifetime")
    void planetary$setLifetime(int value);

    @Accessor("friction")
    float planetary$getFriction();

    @Accessor("onGround")
    boolean planetary$isOnGround();

    @Accessor("roll")
    float planetary$getRoll();

    @Accessor("roll")
    void planetary$setRoll(float value);

    @Accessor("oRoll")
    void planetary$setORoll(float value);
}
