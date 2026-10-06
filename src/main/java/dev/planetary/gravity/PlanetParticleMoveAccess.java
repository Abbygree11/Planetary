package dev.planetary.gravity;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Minecraft-facing mutable state required by the shared Particle.move adapter.
 *
 * <p>This interface isolates Planet's move algorithm from Mixin field-access
 * plumbing. A future Minecraft port can replace the accessor implementation
 * without rewriting the local-frame move algorithm.</p>
 */
public interface PlanetParticleMoveAccess {
    ClientLevel planetary$getLevel();

    double planetary$getX();
    void planetary$setX(double value);

    double planetary$getY();
    void planetary$setY(double value);

    double planetary$getZ();
    void planetary$setZ(double value);

    double planetary$getXd();
    void planetary$setXd(double value);

    double planetary$getYd();
    void planetary$setYd(double value);

    double planetary$getZd();
    void planetary$setZd(double value);

    boolean planetary$isOnGround();
    void planetary$setOnGround(boolean value);

    boolean planetary$hasPhysics();

    float planetary$getBbWidth();
    float planetary$getBbHeight();

    boolean planetary$isStoppedByCollision();
    void planetary$setStoppedByCollision(boolean value);
}
