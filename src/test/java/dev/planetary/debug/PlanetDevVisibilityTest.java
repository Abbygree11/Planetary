package dev.planetary.debug;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PlanetDevVisibilityTest {
    @Test
    void renewsOnlyMissingOrShortDeveloperOwnedEffect() {
        assertTrue(PlanetDevVisibility.needsRefresh(false, false, 0));
        assertFalse(PlanetDevVisibility.needsRefresh(true, false, 0),
                "Do not override natural potions or other mods");
        assertFalse(PlanetDevVisibility.needsRefresh(true, true, 220));
        assertTrue(PlanetDevVisibility.needsRefresh(true, true, 100));
    }

    @Test
    void developerEffectHasAnUnobtrusiveSignature() {
        assertTrue(PlanetDevVisibility.hasDevSignature(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0,
                        true, false, false)));
        assertFalse(PlanetDevVisibility.hasDevSignature(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0,
                        false, true, true)));
        assertFalse(PlanetDevVisibility.hasDevSignature(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 1,
                        true, false, false)));
        assertFalse(PlanetDevVisibility.hasDevSignature(null));
    }
}
