package dev.planetary;

import dev.planetary.worldgen.PlanetWorldgenRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(PlanetaryMod.MOD_ID)
public final class PlanetaryMod {
    public static final String MOD_ID = "planetary";

    public PlanetaryMod(IEventBus modBus) {
        PlanetWorldgenRegistries.register(modBus);
        System.out.println("[Planetary] 2.0 planet worldgen loaded");
    }
}
