package dev.planetary.mixin;

import dev.planetary.world.PlanetChunkAvailabilityBridge;
import net.minecraft.world.level.chunk.ChunkSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Covers ChunkSource implementations that inherit the base hasChunk method,
 * including the client-side chunk cache path.
 */
@Mixin(ChunkSource.class)
public abstract class ChunkSourceMixin {

    @Inject(method = "hasChunk", at = @At("HEAD"), cancellable = true)
    private void planetary$hasChunk(
            int chunkX,
            int chunkZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ChunkSource chunkSource = (ChunkSource) (Object) this;
        if (PlanetChunkAvailabilityBridge.isPlanetaryChunk(
                chunkSource,
                chunkX,
                chunkZ
        )) {
            cir.setReturnValue(true);
        }
    }
}
