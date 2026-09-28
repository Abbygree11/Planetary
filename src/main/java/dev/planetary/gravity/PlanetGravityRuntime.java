package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityField;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Associates a Minecraft level with the single cubic planet that defines its
 * gravity field.
 *
 * <p>The final Planet world will bind this during level creation. Keeping the
 * association outside Entity means entity mixins remain dormant in ordinary
 * vanilla levels and test worlds until a planet is explicitly attached.</p>
 */
public final class PlanetGravityRuntime {
    private static final Map<Level, PlanetGravityField> FIELDS =
            new IdentityHashMap<>();

    private PlanetGravityRuntime() {
    }

    public static synchronized void bind(
            Level level,
            PlanetGravityField field
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(field, "field");

        PlanetGravityField previous = FIELDS.put(level, field);
        if (previous != null && previous != field) {
            FIELDS.put(level, previous);
            throw new IllegalStateException(
                    "Level is already bound to another PlanetGravityField"
            );
        }
    }

    public static synchronized void unbind(
            Level level,
            PlanetGravityField field
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(field, "field");

        if (FIELDS.get(level) == field) {
            FIELDS.remove(level);
        }
    }

    public static synchronized Optional<PlanetGravityField> find(
            Level level
    ) {
        Objects.requireNonNull(level, "level");
        return Optional.ofNullable(FIELDS.get(level));
    }
}
