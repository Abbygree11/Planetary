package dev.planetary.world;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

/**
 * Canonical neighbor-change event independent from a concrete Level adapter.
 */
public record PlanetNeighborUpdate(
        PlanetBlockPos target,
        PlanetBlockPos source,
        Block sourceBlock,
        Direction directionToSource
) {
    public PlanetNeighborUpdate {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sourceBlock, "sourceBlock");
        Objects.requireNonNull(directionToSource, "directionToSource");
    }
}
