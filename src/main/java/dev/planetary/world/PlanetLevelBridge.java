package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Associates a real Minecraft Level with one PlanetWorldAccess instance.
 *
 * <p>The registry is intentionally explicit: only Levels bound by Planetary
 * participate in virtual-position interception. Ordinary vanilla dimensions
 * are therefore untouched.</p>
 */
public final class PlanetLevelBridge {
    private static final Map<Level, PlanetWorldAccess> BOUND_LEVELS =
            new IdentityHashMap<>();

    private PlanetLevelBridge() {
    }

    public static synchronized void bind(Level level, PlanetWorldAccess world) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(world, "world");

        PlanetWorldAccess previous = BOUND_LEVELS.put(level, world);
        if (previous != null && previous != world) {
            BOUND_LEVELS.put(level, previous);
            throw new IllegalStateException(
                    "Level is already bound to a different PlanetWorldAccess"
            );
        }
    }

    public static synchronized void unbind(Level level, PlanetWorldAccess world) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(world, "world");

        if (BOUND_LEVELS.get(level) == world) {
            BOUND_LEVELS.remove(level);
        }
    }

    public static synchronized PlanetWorldAccess get(Level level) {
        return BOUND_LEVELS.get(Objects.requireNonNull(level, "level"));
    }

    public static Optional<ResolvedPosition> resolve(
            Level level,
            BlockPos vanillaPos
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(vanillaPos, "vanillaPos");

        PlanetWorldAccess world;
        synchronized (PlanetLevelBridge.class) {
            world = BOUND_LEVELS.get(level);
        }
        if (world == null) {
            return Optional.empty();
        }

        return world.vanillaPosCodec()
                .tryDecode(vanillaPos)
                .map(pos -> new ResolvedPosition(world, pos));
    }

    public record ResolvedPosition(
            PlanetWorldAccess world,
            PlanetBlockPos position
    ) {
        public ResolvedPosition {
            Objects.requireNonNull(world, "world");
            Objects.requireNonNull(position, "position");
        }
    }
}
