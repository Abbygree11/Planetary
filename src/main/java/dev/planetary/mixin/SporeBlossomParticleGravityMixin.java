package dev.planetary.mixin;

import dev.planetary.gravity.PlanetSporeBlossomSourceRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Unlike basic block emitter positions, the 14 spore candidates are queried
 * for COLLISION before emitting. The complete local sample algorithm must
 * own candidate position and emitted sub-cell coordinates as one unit.
 *
 * <p>Vanilla animateTick stays entirely unchanged on ordinary/+Y worlds.
 * The very short vanilla source is isolated for 1.21.1 review in
 * PlanetSporeBlossomSourceRuntime rather than duplicated inside this Mixin.</p>
 */
@Mixin(SporeBlossomBlock.class)
public abstract class SporeBlossomParticleGravityMixin {
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void planetary$emitLocalSporeVolume(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo callback
    ) {
        PlanetBlockRuntime.traversalAt(level, pos).ifPresent(source -> {
            if (source.face() == PlanetFace.POS_Y) {
                return;
            }
            PlanetSporeBlossomSourceRuntime.emit(level, source, random);
            callback.cancel();
        });
    }
}
