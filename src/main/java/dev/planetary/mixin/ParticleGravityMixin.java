package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reorients vanilla particle gravity into the local Planet gravity frame.
 *
 * <p>Vanilla Particle.tick always applies {@code -Y} acceleration. At HEAD we
 * add the opposite +Y acceleration to cancel that upcoming term, then add the
 * same acceleration magnitude along Planet local DOWN. The original tick is
 * otherwise left intact.</p>
 */
@Mixin(Particle.class)
public abstract class ParticleGravityMixin {
    @Shadow
    @Final
    protected ClientLevel level;

    @Shadow
    protected double x;

    @Shadow
    protected double y;

    @Shadow
    protected double z;

    @Shadow
    protected double xd;

    @Shadow
    protected double yd;

    @Shadow
    protected double zd;

    @Shadow
    protected float gravity;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void planetary$rotateGravity(
            CallbackInfo ci
    ) {
        if (this.gravity == 0.0F) {
            return;
        }

        Optional<PlanetGravityFrame> frame =
                PlanetBlockGravity.frameAt(
                        this.level,
                        this.x,
                        this.y,
                        this.z
                );

        if (frame.isEmpty()) {
            return;
        }

        double acceleration =
                0.04D * (double) this.gravity;

        // Cancel the hard-coded vanilla -Y acceleration that tick() is about
        // to apply.
        this.yd += acceleration;

        PlanetVector down =
                frame.get().worldDown();

        this.xd += down.x() * acceleration;
        this.yd += down.y() * acceleration;
        this.zd += down.z() * acceleration;
    }
}
