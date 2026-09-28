package dev.planetary.world;

import net.minecraft.world.ticks.TickPriority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Minimal canonical tick scheduler for blocks and fluids.
 *
 * <p>Like vanilla scheduled ticks, a type/position pair is unique while it is
 * queued. The concrete Level adapter will later bridge these entries into the
 * exact vanilla execution callbacks.</p>
 */
public final class PlanetTickScheduler<T> {
    private static final Comparator<PlanetScheduledTick<?>> ORDER =
            Comparator.<PlanetScheduledTick<?>>comparingLong(PlanetScheduledTick::triggerTick)
                    .thenComparingInt(tick -> tick.priority().ordinal())
                    .thenComparingLong(PlanetScheduledTick::subTickOrder);

    private final PriorityQueue<PlanetScheduledTick<T>> queue =
            new PriorityQueue<>((left, right) -> ORDER.compare(left, right));
    private final Map<TickKey<T>, PlanetScheduledTick<T>> scheduled = new HashMap<>();
    private long nextSubTickOrder;

    public boolean schedule(
            T type,
            PlanetBlockPos position,
            long triggerTick,
            TickPriority priority
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(priority, "priority");

        TickKey<T> key = new TickKey<>(type, position);
        if (scheduled.containsKey(key)) {
            return false;
        }

        PlanetScheduledTick<T> tick = new PlanetScheduledTick<>(
                type,
                position,
                triggerTick,
                priority,
                nextSubTickOrder++
        );

        scheduled.put(key, tick);
        queue.add(tick);
        return true;
    }

    public boolean hasScheduledTick(PlanetBlockPos position, T type) {
        return scheduled.containsKey(new TickKey<>(type, position));
    }

    public int size() {
        return scheduled.size();
    }

    public boolean isEmpty() {
        return scheduled.isEmpty();
    }

    public List<PlanetScheduledTick<T>> pollDue(long gameTime, int maxTicks) {
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must be >= 0");
        }

        ArrayList<PlanetScheduledTick<T>> due = new ArrayList<>();
        while (due.size() < maxTicks) {
            PlanetScheduledTick<T> next = queue.peek();
            if (next == null || next.triggerTick() > gameTime) {
                break;
            }

            queue.remove();
            scheduled.remove(new TickKey<>(next.type(), next.position()));
            due.add(next);
        }

        return List.copyOf(due);
    }

    public void clear() {
        queue.clear();
        scheduled.clear();
    }

    private record TickKey<T>(T type, PlanetBlockPos position) {
        private TickKey {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(position, "position");
        }
    }
}
