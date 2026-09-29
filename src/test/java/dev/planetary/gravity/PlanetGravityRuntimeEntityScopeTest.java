package dev.planetary.gravity;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class PlanetGravityRuntimeEntityScopeTest {

    @Test
    void entityActivationCanBeInfiniteButPlayerScoped() {
        Level level = mock(Level.class);
        Entity allowed = mock(Entity.class);
        Entity denied = mock(Entity.class);

        when(allowed.level()).thenReturn(level);
        when(denied.level()).thenReturn(level);
        when(allowed.getX()).thenReturn(10000.0D);
        when(allowed.getY()).thenReturn(-20000.0D);
        when(allowed.getZ()).thenReturn(30000.0D);

        PlanetGravityField field =
                new PlanetGravityField(
                        new PlanetCore(0, 0, 0, 1)
                );

        PlanetGravityRuntime.bind(
                level,
                field,
                PlanetGravityRuntime.Activation.ALWAYS,
                entity -> entity == allowed
        );
        try {
            assertSame(
                    field,
                    PlanetGravityRuntime.findFor(allowed)
                            .orElseThrow()
            );
            assertTrue(
                    PlanetGravityRuntime.findFor(denied)
                            .isEmpty()
            );
        } finally {
            PlanetGravityRuntime.unbind(level, field);
        }
    }
}
