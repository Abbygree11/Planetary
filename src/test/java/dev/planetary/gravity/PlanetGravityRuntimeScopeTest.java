package dev.planetary.gravity;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetGravityRuntimeScopeTest {

    @Test
    void scopedBindingOnlyActivatesInsidePredicate() {
        Level level = mock(Level.class);
        PlanetGravityField field =
                new PlanetGravityField(
                        new PlanetCore(0, 0, 0, 10)
                );

        PlanetGravityRuntime.bind(
                level,
                field,
                (x, y, z) -> Math.abs(x) <= 5.0D
        );
        try {
            assertSame(
                    field,
                    PlanetGravityRuntime.findAt(
                            level,
                            1.0D,
                            1000.0D,
                            1000.0D
                    ).orElseThrow()
            );
            assertTrue(
                    PlanetGravityRuntime.findAt(
                            level,
                            6.0D,
                            0.0D,
                            0.0D
                    ).isEmpty()
            );
        } finally {
            PlanetGravityRuntime.unbind(level, field);
        }
    }
}
