package dev.planetary.debug;

import dev.planetary.PlanetaryMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Temporary server-side harness for the first playable real-block planet.
 */
@EventBusSubscriber(modid = PlanetaryMod.MOD_ID)
public final class PlanetDebugServerEvents {
    private PlanetDebugServerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) {
            player.sendSystemMessage(
                    Component.literal(
                            "[Planetary] Debug planet is currently enabled "
                                    + "only in the Overworld."
                    )
            );
            return;
        }

        PlanetDebugWorld.attach(level);

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        Vec3 spawn = PlanetDebugWorld.playerSpawn();
        player.teleportTo(
                level,
                spawn.x,
                spawn.y,
                spawn.z,
                0.0F,
                0.0F
        );
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;

        player.sendSystemMessage(
                Component.literal(
                        "[Planetary] Real debug planet enabled. Core: "
                                + PlanetDebugWorld.CORE_POS.toShortString()
                                + ", diameter: "
                                + PlanetDebugWorld.DIAMETER
                                + ". Walk to an edge to test gravity."
                )
        );
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PlanetDebugWorld.detach(level);
        }
    }
}
