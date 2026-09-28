package dev.planetary.world;

import net.minecraft.world.ticks.TickPriority;

import java.util.Objects;

/**
 * Scheduled tick keyed by a canonical planet position instead of vanilla's
 * global BlockPos.
 */
public record PlanetScheduledTick<T>(
        T type,
        PlanetBlockPos position,
        long triggerTick,
        TickPriority priority,
        long subTickOrder
) {
    public PlanetScheduledTick {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(priority, "priority");
    }
}
