package dev.planetary.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import dev.planetary.gravity.PlanetParticleMoveAccess;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses fields declared on Particle itself for subclass mixins such as
 * DripParticleGravityMixin. Shadowing inherited fields directly on a
 * DripParticle-targeted mixin is invalid.
 */
@Mixin(Particle.class)
public interface ParticleGravityAccessor
        extends PlanetParticleMoveAccess {
    @Accessor("level")
    ClientLevel planetary$getLevel();

    @Accessor("x")
    double planetary$getX();

    @Accessor("x")
    void planetary$setX(double value);

    @Accessor("y")
    double planetary$getY();

    @Accessor("y")
    void planetary$setY(double value);

    @Accessor("z")
    double planetary$getZ();

    @Accessor("z")
    void planetary$setZ(double value);

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

    @Accessor("onGround")
    void planetary$setOnGround(boolean value);

    @Accessor("hasPhysics")
    boolean planetary$hasPhysics();

    @Accessor("bbWidth")
    float planetary$getBbWidth();

    @Accessor("bbHeight")
    float planetary$getBbHeight();

    @Accessor("stoppedByCollision")
    boolean planetary$isStoppedByCollision();

    @Accessor("stoppedByCollision")
    void planetary$setStoppedByCollision(boolean value);

    @Accessor("roll")
    float planetary$getRoll();

    @Accessor("roll")
    void planetary$setRoll(float value);

    @Accessor("oRoll")
    void planetary$setORoll(float value);
}
