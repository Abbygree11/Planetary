package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * DripParticle overrides Particle.tick(), so the generic particle-gravity
 * mixin never sees water/lava/honey/dripstone drops. Reorient its own
 * hard-coded world -Y acceleration into Planet local DOWN.
 */
@Mixin(DripParticle.class)
public abstract class DripParticleGravityMixin {
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
    private void planetary$rotateDripGravity(
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

        double acceleration = (double) this.gravity;

        // DripParticle.tick() is about to do: yd -= gravity.
        // Cancel that term, then apply the same acceleration along local DOWN.
        this.yd += acceleration;

        PlanetVector down = frame.get().worldDown();

        this.xd += down.x() * acceleration;
        this.yd += down.y() * acceleration;
        this.zd += down.z() * acceleration;
    }
}
