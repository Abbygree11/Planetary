package dev.planetary.world;

import net.minecraft.world.ticks.TickPriority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final Set<TickKey<T>> toRunThisTick = new HashSet<>();
    private long nextSubTickOrder;

    public boolean schedule(
            T type,
            PlanetBlockPos position,
            long triggerTick,
            TickPriority priority
    ) {
        return schedule(
                type,
                position,
                triggerTick,
                priority,
                nextSubTickOrder++
        );
    }

    /**
     * Schedules a tick while preserving vanilla's exact sub-tick ordering.
     */
    public boolean schedule(
            T type,
            PlanetBlockPos position,
            long triggerTick,
            TickPriority priority,
            long subTickOrder
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(priority, "priority");

        if (subTickOrder >= nextSubTickOrder && subTickOrder < Long.MAX_VALUE) {
            nextSubTickOrder = subTickOrder + 1L;
        }

        TickKey<T> key = new TickKey<>(type, position);
        if (scheduled.containsKey(key)) {
            return false;
        }

        PlanetScheduledTick<T> tick = new PlanetScheduledTick<>(
                type,
                position,
                triggerTick,
                priority,
                subTickOrder
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
        return drainDue(gameTime, maxTicks);
    }

    /**
     * Collects the fixed batch that vanilla would run this game tick.
     *
     * <p>Ticks scheduled by a callback after this collection are deliberately
     * left for the next collection, matching LevelTicks' collect-then-run
     * behavior.</p>
     */
    public List<PlanetScheduledTick<T>> collectDueForExecution(
            long gameTime,
            int maxTicks
    ) {
        if (!toRunThisTick.isEmpty()) {
            throw new IllegalStateException(
                    "Previous Planetary tick batch has not been finished"
            );
        }

        List<PlanetScheduledTick<T>> due = drainDue(gameTime, maxTicks);
        for (PlanetScheduledTick<T> tick : due) {
            toRunThisTick.add(new TickKey<>(tick.type(), tick.position()));
        }
        return due;
    }

    public boolean willTickThisTick(PlanetBlockPos position, T type) {
        return toRunThisTick.contains(new TickKey<>(type, position));
    }

    /**
     * Must be called immediately before the callback for this tick is invoked.
     * Vanilla removes the current entry from its to-run set before invoking
     * the block/fluid callback, so queries made by that callback see false for
     * the currently executing tick.
     */
    public void markRunning(PlanetScheduledTick<T> tick) {
        Objects.requireNonNull(tick, "tick");
        toRunThisTick.remove(new TickKey<>(tick.type(), tick.position()));
    }

    public void finishExecutionBatch() {
        toRunThisTick.clear();
    }

    private List<PlanetScheduledTick<T>> drainDue(
            long gameTime,
            int maxTicks
    ) {
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
        toRunThisTick.clear();
    }

    private record TickKey<T>(T type, PlanetBlockPos position) {
        private TickKey {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(position, "position");
        }
    }
}
