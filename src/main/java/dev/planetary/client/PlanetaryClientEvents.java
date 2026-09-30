package dev.planetary.client;

import dev.planetary.PlanetaryMod;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.worldgen.PlanetWorldKeys;
import dev.planetary.worldgen.PlanetWorldSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Client binding for the dedicated Planet world.
 *
 * <p>The legacy automatic Overworld debug gravity binding is disabled.</p>
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

        boolean planetWorld =
                level.dimensionTypeRegistration()
                        .unwrapKey()
                        .filter(
                                PlanetWorldKeys.PLANET_DIMENSION_TYPE::equals
                        )
                        .isPresent();

        if (!planetWorld) {
            return;
        }

        PlanetGravityRuntime.bind(
                level,
                PlanetWorldSettings.gravityField()
        );

        player.displayClientMessage(
                Component.literal(
                        "[Planetary] Dedicated Planet gravity attached."
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
            PlanetGravityRuntime.find(level)
                    .ifPresent(
                            field -> PlanetGravityRuntime.unbind(
                                    level,
                                    field
                            )
                    );
        }
        level = null;
    }
}
