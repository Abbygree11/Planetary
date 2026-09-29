package dev.planetary.worldgen;

import com.mojang.serialization.MapCodec;
import dev.planetary.PlanetaryMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class PlanetWorldgenRegistries {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>>
            CHUNK_GENERATORS =
            DeferredRegister.create(
                    BuiltInRegistries.CHUNK_GENERATOR,
                    PlanetaryMod.MOD_ID
            );

    public static final Supplier<MapCodec<? extends ChunkGenerator>>
            PLANET_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register(
                    "planet",
                    () -> PlanetChunkGenerator.CODEC
            );

    private PlanetWorldgenRegistries() {
    }

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
