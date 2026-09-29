package dev.planetary.client;

import dev.planetary.PlanetaryMod;
import dev.planetary.debug.PlanetDebugWorld;
import dev.planetary.gravity.PlanetGravityRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Client half of the temporary real-block debug planet harness.
 */
@EventBusSubscriber(
        modid = PlanetaryMod.MOD_ID,
        value = Dist.CLIENT
)
public final class PlanetaryClientEvents {
    private static ClientLevel level;

    private PlanetaryClientEvents() {
    }

    @SubscribeEvent
    public static void onLogin(
            ClientPlayerNetworkEvent.LoggingIn event
    ) {
        detach();

        LocalPlayer player = event.getPlayer();
        level = (ClientLevel) player.level();

        PlanetGravityRuntime.bind(
                level,
                PlanetDebugWorld.gravityField(),
                PlanetDebugWorld::isInGravityTestArea
        );

        player.displayClientMessage(
                Component.literal(
                        "[Planetary] Client gravity field attached; "
                                + "waiting for server debug planet."
                ),
                false
        );
    }

    @SubscribeEvent
    public static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {
        detach();
    }

    private static void detach() {
        if (level != null) {
            PlanetGravityRuntime.unbind(
                    level,
                    PlanetDebugWorld.gravityField()
            );
        }
        level = null;
    }
}
