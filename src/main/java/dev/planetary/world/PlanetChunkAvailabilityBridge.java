package dev.planetary.world;

import net.minecraft.world.level.chunk.ChunkSource;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Makes LevelAccessor#hasChunk / hasChunkAt see Planetary's virtual atlas as
 * loaded without teaching vanilla ChunkSource how to materialize fake chunks.
 *
 * <p>Only explicitly bound ChunkSource instances participate. Outside the
 * atlas the original vanilla hasChunk implementation is left untouched.</p>
 */
public final class PlanetChunkAvailabilityBridge {
    private static final Map<ChunkSource, PlanetWorldAccess> BINDINGS =
            new IdentityHashMap<>();

    private PlanetChunkAvailabilityBridge() {
    }

    public static synchronized void bind(
            ChunkSource chunkSource,
            PlanetWorldAccess world
    ) {
        Objects.requireNonNull(chunkSource, "chunkSource");
        Objects.requireNonNull(world, "world");

        PlanetWorldAccess previous = BINDINGS.put(chunkSource, world);
        if (previous != null && previous != world) {
            BINDINGS.put(chunkSource, previous);
            throw new IllegalStateException(
                    "ChunkSource is already bound to another PlanetWorldAccess"
            );
        }
    }

    public static synchronized void unbind(
            ChunkSource chunkSource,
            PlanetWorldAccess world
    ) {
        Objects.requireNonNull(chunkSource, "chunkSource");
        Objects.requireNonNull(world, "world");

        if (BINDINGS.get(chunkSource) == world) {
            BINDINGS.remove(chunkSource);
        }
    }

    public static synchronized boolean isPlanetaryChunk(
            ChunkSource chunkSource,
            int chunkX,
            int chunkZ
    ) {
        Objects.requireNonNull(chunkSource, "chunkSource");

        PlanetWorldAccess world = BINDINGS.get(chunkSource);
        return world != null
                && world.vanillaPosCodec().containsChunk(
                        chunkX,
                        chunkZ
                );
    }
}
