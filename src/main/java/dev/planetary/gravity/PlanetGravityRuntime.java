package dev.planetary.gravity;

import dev.planetary.topology.PlanetGravityField;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Associates a Minecraft level with the planet gravity field active there.
 *
 * <p>A real Planet dimension normally uses an unbounded binding. Temporary
 * development harnesses can provide an activation predicate so ordinary
 * Overworld entities outside the test area are completely unaffected.</p>
 */
public final class PlanetGravityRuntime {
    private static final Map<Level, Binding> BINDINGS =
            new IdentityHashMap<>();

    private PlanetGravityRuntime() {
    }

    public static synchronized void bind(
            Level level,
            PlanetGravityField field
    ) {
        bind(level, field, Activation.ALWAYS);
    }

    public static synchronized void bind(
            Level level,
            PlanetGravityField field,
            Activation activation
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(activation, "activation");

        Binding next = new Binding(field, activation);
        Binding previous = BINDINGS.put(level, next);

        if (previous != null
                && previous.field() != field) {
            BINDINGS.put(level, previous);
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

        Binding binding = BINDINGS.get(level);
        if (binding != null && binding.field() == field) {
            BINDINGS.remove(level);
        }
    }

    /**
     * Returns the level binding without applying a positional activation
     * predicate. Useful for lifecycle/tests; entity physics should use findAt.
     */
    public static synchronized Optional<PlanetGravityField> find(
            Level level
    ) {
        Objects.requireNonNull(level, "level");
        Binding binding = BINDINGS.get(level);
        return binding == null
                ? Optional.empty()
                : Optional.of(binding.field());
    }

    public static synchronized Optional<PlanetGravityField> findAt(
            Level level,
            double x,
            double y,
            double z
    ) {
        Objects.requireNonNull(level, "level");

        Binding binding = BINDINGS.get(level);
        if (binding == null
                || !binding.activation().contains(x, y, z)) {
            return Optional.empty();
        }

        return Optional.of(binding.field());
    }

    @FunctionalInterface
    public interface Activation {
        Activation ALWAYS = (x, y, z) -> true;

        boolean contains(double x, double y, double z);
    }

    private record Binding(
            PlanetGravityField field,
            Activation activation
    ) {
    }
}
