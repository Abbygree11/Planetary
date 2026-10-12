package dev.planetary.api;

import net.minecraft.world.phys.BlockHitResult;

/** Read-only original physical hit of THIS UseOnContext, not the final placement target. */
public interface PlanetOriginalHitSource {
    BlockHitResult planetary$originalHit();
}
