package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Local-frame particle emitters owned by CampfireBlock.
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireParticleEmitterGravityMixin {
    @Redirect(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void planetary$emitCampfireEmberInLocalFrame(
            Level invocationLevel,
            ParticleOptions options,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        level,
                        pos
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            invocationLevel.addParticle(
                    options,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed
            );
            return;
        }

        Vec3 velocity =
                PlanetParticleMotion.localVelocityToWorld(
                        new Vec3(
                                xSpeed,
                                ySpeed,
                                zSpeed
                        ),
                        frameOptional.get()
                );

        invocationLevel.addParticle(
                options,
                x,
                y,
                z,
                velocity.x,
                velocity.y,
                velocity.z
        );
    }

    @Inject(
            method = "makeParticles",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void planetary$makeParticlesInLocalFrame(
            Level level,
            BlockPos pos,
            boolean signalFire,
            boolean spawnExtraSmoke,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        level,
                        pos
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        ci.cancel();

        PlanetGravityFrame frame =
                frameOptional.get();
        RandomSource random =
                level.getRandom();
        SimpleParticleType smokeType =
                signalFire
                        ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
                        : ParticleTypes.CAMPFIRE_COSY_SMOKE;

        double localX =
                random.nextDouble()
                        / 3.0D
                        * (random.nextBoolean()
                        ? 1.0D
                        : -1.0D);
        double localY =
                random.nextDouble()
                        + random.nextDouble()
                        - 0.5D;
        double localZ =
                random.nextDouble()
                        / 3.0D
                        * (random.nextBoolean()
                        ? 1.0D
                        : -1.0D);

        Vec3 origin =
                PlanetParticleEmitter.localOffsetFromCenter(
                        pos,
                        frame,
                        localX,
                        localY,
                        localZ
                );
        Vec3 velocity =
                PlanetParticleMotion.localVelocityToWorld(
                        new Vec3(
                                0.0D,
                                0.07D,
                                0.0D
                        ),
                        frame
                );

        level.addAlwaysVisibleParticle(
                smokeType,
                true,
                origin.x,
                origin.y,
                origin.z,
                velocity.x,
                velocity.y,
                velocity.z
        );

        if (!spawnExtraSmoke) {
            return;
        }

        double extraLocalX =
                random.nextDouble()
                        / 4.0D
                        * (random.nextBoolean()
                        ? 1.0D
                        : -1.0D);
        double extraLocalZ =
                random.nextDouble()
                        / 4.0D
                        * (random.nextBoolean()
                        ? 1.0D
                        : -1.0D);

        Vec3 extraOrigin =
                PlanetParticleEmitter.localOffsetFromCenter(
                        pos,
                        frame,
                        extraLocalX,
                        -0.1D,
                        extraLocalZ
                );
        Vec3 extraVelocity =
                PlanetParticleMotion.localVelocityToWorld(
                        new Vec3(
                                0.0D,
                                0.005D,
                                0.0D
                        ),
                        frame
                );

        level.addParticle(
                ParticleTypes.SMOKE,
                extraOrigin.x,
                extraOrigin.y,
                extraOrigin.z,
                extraVelocity.x,
                extraVelocity.y,
                extraVelocity.z
        );
    }
}
