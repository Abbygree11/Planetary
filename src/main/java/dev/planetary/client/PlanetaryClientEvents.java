package dev.planetary.client;

import dev.planetary.PlanetaryMod;
import dev.planetary.debug.PlanetDebugPlanetoid;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Temporary client harness for seeing the real six-face Planetary topology in
 * game before entity gravity and custom collision are wired in.
 */
@EventBusSubscriber(
        modid = PlanetaryMod.MOD_ID,
        value = Dist.CLIENT
)
public final class PlanetaryClientEvents {
    private static ClientLevel level;
    private static PlanetWorldAccess world;
    private static BlockPos renderCenter;

    private PlanetaryClientEvents() {
    }

    @SubscribeEvent
    public static void onLogin(
            ClientPlayerNetworkEvent.LoggingIn event
    ) {
        detach();

        LocalPlayer player = event.getPlayer();
        level = (ClientLevel) player.level();
        world = PlanetDebugPlanetoid.create();
        world.bindLevel(level);

        renderCenter = player.blockPosition()
                .relative(player.getDirection(), 20)
                .above(5);

        player.displayClientMessage(
                Component.literal(
                        "[Planetary] Diagnostic planetoid at "
                                + renderCenter.getX() + ", "
                                + renderCenter.getY() + ", "
                                + renderCenter.getZ()
                                + " (visual prototype; fly to inspect)"
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

    @SubscribeEvent
    public static void onRender(
            RenderLevelStageEvent event
    ) {
        if (event.getStage()
                != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (world == null
                || level == null
                || renderCenter == null
                || Minecraft.getInstance().level != level) {
            return;
        }

        PlanetDebugPlanetoidRenderer.render(
                event,
                world,
                renderCenter
        );
    }

    private static void detach() {
        if (world != null) {
            world.unbindLevel();
        }
        world = null;
        level = null;
        renderCenter = null;
    }
}
