package dev.planetary.world;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Ordered neighbor-update queue for the future Level compatibility layer.
 *
 * <p>The queue stores canonical PlanetBlockPos values, so updates can cross
 * cube edges without losing the target gravity face.</p>
 */
public final class PlanetNeighborUpdateQueue {
    private final ArrayDeque<PlanetNeighborUpdate> queue = new ArrayDeque<>();

    public void enqueueAllNeighbors(
            PlanetWorldAccess world,
            PlanetBlockPos source,
            Block sourceBlock
    ) {
        enqueueNeighborsExcept(world, source, sourceBlock, null);
    }

    public void enqueueNeighborsExcept(
            PlanetWorldAccess world,
            PlanetBlockPos source,
            Block sourceBlock,
            Direction exceptDirection
    ) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sourceBlock, "sourceBlock");

        for (Direction direction : Direction.values()) {
            if (direction == exceptDirection) {
                continue;
            }

            PlanetNeighborRef neighbor = world.neighbor(source, direction);
            queue.addLast(new PlanetNeighborUpdate(
                    neighbor.position(),
                    source,
                    sourceBlock,
                    neighbor.directionBackToSource()
            ));
        }
    }

    public void enqueue(PlanetNeighborUpdate update) {
        queue.addLast(Objects.requireNonNull(update, "update"));
    }

    public PlanetNeighborUpdate poll() {
        return queue.pollFirst();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public List<PlanetNeighborUpdate> drain() {
        ArrayList<PlanetNeighborUpdate> result = new ArrayList<>(queue.size());
        while (!queue.isEmpty()) {
            result.add(queue.removeFirst());
        }
        return List.copyOf(result);
    }

    public int drainTo(Consumer<PlanetNeighborUpdate> consumer, int limit) {
        Objects.requireNonNull(consumer, "consumer");
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }

        int count = 0;
        while (count < limit && !queue.isEmpty()) {
            consumer.accept(queue.removeFirst());
            count++;
        }
        return count;
    }

    public void clear() {
        queue.clear();
    }
}
