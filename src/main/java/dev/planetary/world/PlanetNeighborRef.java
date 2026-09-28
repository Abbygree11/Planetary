package dev.planetary.world;

import net.minecraft.core.Direction;

import java.util.Objects;

/**
 * A topology-aware adjacent block.
 *
 * @param position canonical neighbor position
 * @param directionFromSource direction used to leave the source block
 * @param directionBackToSource direction that points from the neighbor back to the source
 */
public record PlanetNeighborRef(
        PlanetBlockPos position,
        Direction directionFromSource,
        Direction directionBackToSource
) {
    public PlanetNeighborRef {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(directionFromSource, "directionFromSource");
        Objects.requireNonNull(directionBackToSource, "directionBackToSource");
    }
}
