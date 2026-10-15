package dev.planetary.debug;

import dev.planetary.PlanetaryMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Temporary dev-only visual aid for the six-face Planet world.
 * This is vanilla night vision, NOT real sky/block lighting or local AO.
 * Distributed jars never enable the system property by default.
 */
@EventBusSubscriber(modid = PlanetaryMod.MOD_ID)
public final class PlanetDevVisibility {
    public static final String PROPERTY = "planetary.debug.visibility.nightVision";
    private static final int DURATION_TICKS = 240;
    private static final int RENEW_BELOW_TICKS = 100;
    private static final int CHECK_EVERY_TICKS = 40;

    // Store the actual instance identity; never intentionally remove somebody
    // else's night-vision potion just because it shares the effect type.
    private static final Map<UUID, MobEffectInstance> OWNED = new HashMap<>();
    private static final Set<UUID> OPTED_OUT = new HashSet<>();

    private PlanetDevVisibility() {
    }

    public static boolean isActive(ServerPlayer player) {
        return Boolean.getBoolean(PROPERTY)
                && !OPTED_OUT.contains(player.getUUID())
                && PlanetTestFixtures.isPlanet(player.serverLevel());
    }

    /** Per-player opt-out for examining actual light/AO in the dev client. */
    public static boolean setEnabled(ServerPlayer player, boolean enabled) {
        if (!Boolean.getBoolean(PROPERTY)) {
            return false;
        }
        if (enabled) {
            OPTED_OUT.remove(player.getUUID());
        } else {
            OPTED_OUT.add(player.getUUID());
        }
        reconcile(player, true);
        return true;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            reconcile(player, false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removeOurEffect(player);
            OPTED_OUT.remove(player.getUUID());
        }
    }

    static boolean needsRefresh(boolean present, boolean owned, int remainingTicks) {
        return !present || (owned && remainingTicks <= RENEW_BELOW_TICKS);
    }

    static boolean hasDevSignature(MobEffectInstance instance) {
        return instance != null
                && instance.getAmplifier() == 0
                && instance.isAmbient()
                && !instance.isVisible()
                && !instance.showIcon();
    }

    private static void reconcile(ServerPlayer player, boolean force) {
        UUID id = player.getUUID();
        if (!isActive(player)) {
            if (OWNED.containsKey(id)) {
                removeOurEffect(player);
            }
            return;
        }
        if (!force && player.tickCount % CHECK_EVERY_TICKS != 0) {
            return;
        }

        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        MobEffectInstance previous = OWNED.get(id);
        boolean owned = current != null && current == previous && hasDevSignature(current);
        if (current != null && !owned) {
            OWNED.remove(id);
            return; // A natural potion or another mod owns night vision.
        }
        if (!needsRefresh(current != null, owned,
                current == null ? 0 : current.getDuration())) {
            return;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, DURATION_TICKS, 0, true, false, false));
        MobEffectInstance applied = player.getEffect(MobEffects.NIGHT_VISION);
        if (hasDevSignature(applied)) {
            OWNED.put(id, applied);
        }
    }

    private static void removeOurEffect(ServerPlayer player) {
        UUID id = player.getUUID();
        MobEffectInstance previous = OWNED.remove(id);
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && current == previous && hasDevSignature(current)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
