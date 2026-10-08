package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleEmitter;
import dev.planetary.gravity.PlanetParticleMotion;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Candles and candle cakes share AbstractCandleBlock's two distinct
 * iterable-based particle source paths.
 *
 * <p>For lit-candle animateTick, wrap only the original forEach consumer:
 * the original lambda still chooses smoke/flame/sound and consumes exactly
 * the vanilla RNG samples. For extinguish, the one vanilla smoke lambda
 * has hardcoded world +Y velocity and must be reproduced narrowly on
 * rotated Planet worlds to rotate that velocity as well.</p>
 */
@Mixin(AbstractCandleBlock.class)
public abstract class AbstractCandleParticleEmitterGravityMixin {
    @ModifyArg(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Iterable;forEach(Ljava/util/function/Consumer;)V"
            ),
            index = 0
    )
    private Consumer<Vec3> planetary$reframeLitCandleOffsets(
            Consumer<Vec3> vanillaConsumer,
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(level, pos);

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            return vanillaConsumer;
        }

        PlanetGravityFrame frame = frameOptional.get();
        return originalOffset -> vanillaConsumer.accept(
                PlanetParticleEmitter.rotateUnitBlockEmitterOffset(
                        originalOffset, frame
                )
        );
    }

    @Redirect(
            method = "extinguish",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Iterable;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private static void planetary$reframeExtinguishSmoke(
            Iterable<Vec3> offsets,
            Consumer<Vec3> vanillaConsumer,
            Player player,
            BlockState state,
            LevelAccessor level,
            BlockPos pos
    ) {
        // LevelAccessor also represents virtual/worldgen spaces with no
        // physical Planet gravity binding. Preserve their vanilla behavior.
        if (!(level instanceof Level physicalLevel)) {
            offsets.forEach(vanillaConsumer);
            return;
        }

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(physicalLevel, pos);

        if (frameOptional.isEmpty()
                || frameOptional.get().face() == PlanetFace.POS_Y) {
            offsets.forEach(vanillaConsumer);
            return;
        }

        PlanetGravityFrame frame = frameOptional.get();
        Vec3 motion = PlanetParticleMotion.localVelocityToWorld(
                new Vec3(0.0D, (double) 0.1F, 0.0D),
                frame
        );

        for (Vec3 originalOffset : offsets) {
            Vec3 physicalOffset =
                    PlanetParticleEmitter.rotateUnitBlockEmitterOffset(
                            originalOffset, frame
                    );

            // Reproduce only AbstractCandleBlock's tiny smoke lambda.
            // setLit, extinguish sound and GameEvent remain vanilla.
            level.addParticle(
                    ParticleTypes.SMOKE,
                    pos.getX() + physicalOffset.x,
                    pos.getY() + physicalOffset.y,
                    pos.getZ() + physicalOffset.z,
                    motion.x,
                    motion.y,
                    motion.z
            );
        }
    }
}
