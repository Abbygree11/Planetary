package dev.planetary.client;

import dev.planetary.PlanetaryMod;
import dev.planetary.debug.PlanetDebugWorld;
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

        boolean planetWorld =
                level.dimensionTypeRegistration()
                        .unwrapKey()
                        .filter(
                                PlanetWorldKeys.PLANET_DIMENSION_TYPE::equals
                        )
                        .isPresent();

        if (planetWorld) {
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
        } else {
            PlanetGravityRuntime.bind(
                    level,
                    PlanetDebugWorld.gravityField(),
                    PlanetGravityRuntime.Activation.ALWAYS,
                    PlanetDebugWorld::isGravityTestEntity
            );

            player.displayClientMessage(
                    Component.literal(
                            "[Planetary] Client debug gravity attached."
                    ),
                    false
            );
        }
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
