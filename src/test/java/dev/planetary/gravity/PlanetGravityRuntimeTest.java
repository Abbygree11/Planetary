package dev.planetary.gravity;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetGravityRuntimeTest {

    @Test
    void fieldsAreBoundByLevelIdentity() {
        Level first = mock(Level.class);
        Level second = mock(Level.class);
        PlanetGravityField field =
                new PlanetGravityField(
                        new PlanetCore(0, 0, 0, 500)
                );

        PlanetGravityRuntime.bind(first, field);
        try {
            assertSame(
                    field,
                    PlanetGravityRuntime.find(first).orElseThrow()
            );
            assertTrue(
                    PlanetGravityRuntime.find(second).isEmpty()
            );
        } finally {
            PlanetGravityRuntime.unbind(first, field);
        }

        assertTrue(
                PlanetGravityRuntime.find(first).isEmpty()
        );
    }
}
