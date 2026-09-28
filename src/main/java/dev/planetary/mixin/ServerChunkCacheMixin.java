package dev.planetary.mixin;

import dev.planetary.world.PlanetChunkAvailabilityBridge;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ServerChunkCache overrides ChunkSource#hasChunk, so the server needs its own
 * interception point in addition to ChunkSourceMixin.
 */
@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {

    @Inject(method = "hasChunk", at = @At("HEAD"), cancellable = true)
    private void planetary$hasChunk(
            int chunkX,
            int chunkZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerChunkCache chunkSource =
                (ServerChunkCache) (Object) this;

        if (PlanetChunkAvailabilityBridge.isPlanetaryChunk(
                chunkSource,
                chunkX,
                chunkZ
        )) {
            cir.setReturnValue(true);
        }
    }
}
