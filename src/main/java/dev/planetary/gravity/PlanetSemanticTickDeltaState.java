package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Shared, per-particle tick state for custom Particle.tick implementations.
 *
 * <p>Only the delta authored by the subclass tick before the move call is
 * interpreted as local. The incoming velocity is already physical and must
 * never be rotated a second time. Vanilla tick/RNG/control flow remain in
 * the original subclass. This helper has no injected target methods:
 * each subclass bridge owns its exact-version INVOKE anchor.</p>
 */
public final class PlanetSemanticTickDeltaState {
    private PlanetGravityFrame frame;
    private double startXd;
    private double startYd;
    private double startZd;

    private PlanetSemanticTickDeltaState() {
    }

    /**
     * Return existing state (possibly null) without allocating anything on
     * non-Planet and +Y frames. Inactive states are always cleared.
     */
    public static PlanetSemanticTickDeltaState begin(
            PlanetSemanticTickDeltaState existing,
            PlanetParticleMoveAccess particle
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        particle.planetary$getLevel(),
                        particle.planetary$getX(),
                        particle.planetary$getY(),
                        particle.planetary$getZ()
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            if (existing != null) {
                existing.frame = null;
            }
            return existing;
        }

        PlanetSemanticTickDeltaState state =
                existing == null
                        ? new PlanetSemanticTickDeltaState()
                        : existing;
        state.frame = frameOptional.get();
        state.startXd = particle.planetary$getXd();
        state.startYd = particle.planetary$getYd();
        state.startZd = particle.planetary$getZd();
        return state;
    }

    public static void beforeMove(
            PlanetSemanticTickDeltaState state,
            PlanetParticleMoveAccess particle
    ) {
        if (state == null || state.frame == null) {
            return;
        }

        Vec3 corrected =
                PlanetParticleMotion.reinterpretTickDeltaAsLocal(
                        new Vec3(
                                state.startXd,
                                state.startYd,
                                state.startZd
                        ),
                        new Vec3(
                                particle.planetary$getXd(),
                                particle.planetary$getYd(),
                                particle.planetary$getZd()
                        ),
                        state.frame
                );

        particle.planetary$setXd(corrected.x);
        particle.planetary$setYd(corrected.y);
        particle.planetary$setZd(corrected.z);

        // No stale state if the same particle moves independently later.
        state.frame = null;
    }
}
