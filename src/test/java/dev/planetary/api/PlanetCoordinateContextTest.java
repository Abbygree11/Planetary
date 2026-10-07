package dev.planetary.api;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetCoordinateContextTest {
    @Test
    void foreignCoordinatesCannotAccidentallyEnterPhysicalFrameApi() {
        Level physicalLevel = mock(Level.class);
        BlockPos pos = new BlockPos(20, 100, 0);
        PlanetGravityField field =
                new PlanetGravityField(new PlanetCore(0, 100, 0, 20));

        PlanetGravityRuntime.bind(physicalLevel, field);
        try {
            PlanetCoordinateContext physical =
                    PlanetCoordinateContext.physical(physicalLevel, pos);
            PlanetCoordinateContext foreign =
                    PlanetCoordinateContext.foreign(
                            ResourceLocation.fromNamespaceAndPath(
                                    "planetary", "test_virtual_contraption"
                            ),
                            pos
                    );

            assertEquals(
                    PlanetFace.POS_X,
                    PlanetFrameApi.canonicalBlockFace(physical).orElseThrow()
            );
            assertEquals(
                    pos.east(),
                    PlanetFrameApi.localNeighbor(physical, Direction.UP)
                            .orElseThrow()
                            .targetPos()
            );

            // Same numeric coordinates do not grant virtual-space access
            // to the physical Level or its Planet gravity field.
            assertTrue(PlanetFrameApi.canonicalBlockFace(foreign).isEmpty());
            assertTrue(
                    PlanetFrameApi.localNeighbor(foreign, Direction.UP)
                            .isEmpty()
            );
        } finally {
            PlanetGravityRuntime.unbind(physicalLevel, field);
        }
    }
}
