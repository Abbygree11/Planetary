package dev.planetary.debug;

import dev.planetary.PlanetaryMod;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.worldgen.PlanetChunkGenerator;
import dev.planetary.worldgen.PlanetWorldSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Dedicated Planet world lifecycle.
 *
 * <p>The old automatic Overworld debug harness is intentionally disabled.
 * Planet gravity must never be attached to an ordinary Overworld merely
 * because the mod is present.</p>
 */
@EventBusSubscriber(modid = PlanetaryMod.MOD_ID)
public final class PlanetDebugServerEvents {
    private PlanetDebugServerEvents() {
    }

    @SubscribeEvent
    public static void onLevelLoad(
            LevelEvent.Load event
    ) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (level.getChunkSource().getGenerator()
                instanceof PlanetChunkGenerator) {
            PlanetGravityRuntime.bind(
                    level,
                    PlanetWorldSettings.gravityField()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!(level.getChunkSource().getGenerator()
                instanceof PlanetChunkGenerator)) {
            return;
        }

        PlanetGravityRuntime.bind(
                level,
                PlanetWorldSettings.gravityField()
        );

        PlanetCapabilityDiagnostics.Result capabilityProbe =
                PlanetCapabilityDiagnostics.verify(
                        level
                );

        PlanetPlacementDiagnostics.Result placementProbe =
                PlanetPlacementDiagnostics.verify(
                        player
                );

        PlanetSupportDiagnostics.Result supportProbe =
                PlanetSupportDiagnostics.verify(
                        level
                );

        PlanetCompatibilityDiagnostics.Result compatibilityProbe =
                PlanetCompatibilityDiagnostics.verify(level);

        player.sendSystemMessage(
                Component.literal(
                        "[Planetary] Support probe passed: "
                                + supportProbe.survivalChecks()
                                + " survival checks, "
                                + supportProbe.updateChecks()
                                + " updateShape checks."
                )
        );

        player.sendSystemMessage(
                Component.literal(
                        "[Planetary] Placement probe passed: "
                                + placementProbe.stateChecks()
                                + " vanilla state checks."
                )
        );

        player.sendSystemMessage(
                Component.literal(
                        "[Planetary] Frame API probe passed: "
                                + compatibilityProbe.neighborChecks()
                                + " local-neighbor checks, "
                                + compatibilityProbe.rawRelativeMismatches()
                                + " raw BlockPos.relative mismatches detected."
                )
        );

        player.sendSystemMessage(
                Component.literal(
                        "[Planetary] Capability probe passed: "
                                + capabilityProbe.sideChecks()
                                + " generic side mappings, "
                                + capabilityProbe.standardCapabilityChecks()
                                + " standard item/fluid/energy checks, "
                                + capabilityProbe.cacheInvalidations()
                                + " cache invalidation."
                )
        );

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        player.sendSystemMessage(
                Component.literal(
                        "[Planetary] Dedicated Planet world active. "
                                + "Core: "
                                + PlanetWorldSettings.CORE_X
                                + ", "
                                + PlanetWorldSettings.CORE_Y
                                + ", "
                                + PlanetWorldSettings.CORE_Z
                                + "; diameter: "
                                + PlanetWorldSettings.DIAMETER
                )
        );
    }

    @SubscribeEvent
    public static void onLevelUnload(
            LevelEvent.Unload event
    ) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (level.getChunkSource().getGenerator()
                instanceof PlanetChunkGenerator) {
            PlanetGravityRuntime.unbind(
                    level,
                    PlanetWorldSettings.gravityField()
            );
        }
    }
}
