package dev.planetary.debug;

import dev.planetary.topology.PlanetFace;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetDebugWorldTest {

    @Test
    void debugPlanetHasOneCoreAndOddDiameter() {
        assertEquals(13, PlanetDebugWorld.DIAMETER);
        assertEquals(
                13,
                PlanetDebugWorld.core().diameter()
        );

        assertEquals(
                Blocks.BEDROCK,
                PlanetDebugWorld.blockStateAtOffset(
                        0,
                        0,
                        0
                ).getBlock()
        );
    }

    @Test
    void realEdgesAndCornersAreSinglePhysicalBlocks() {
        assertEquals(
                Blocks.IRON_BLOCK,
                PlanetDebugWorld.blockStateAtOffset(
                        PlanetDebugWorld.RADIUS,
                        PlanetDebugWorld.RADIUS,
                        0
                ).getBlock()
        );

        assertEquals(
                Blocks.GOLD_BLOCK,
                PlanetDebugWorld.blockStateAtOffset(
                        PlanetDebugWorld.RADIUS,
                        PlanetDebugWorld.RADIUS,
                        PlanetDebugWorld.RADIUS
                ).getBlock()
        );
    }

    @Test
    void sixFaceInteriorsUseDiagnosticMaterials() {
        int r = PlanetDebugWorld.RADIUS;

        assertEquals(
                Blocks.RED_CONCRETE,
                PlanetDebugWorld.blockStateAtOffset(
                        r, 1, 1
                ).getBlock()
        );
        assertEquals(
                Blocks.BLUE_CONCRETE,
                PlanetDebugWorld.blockStateAtOffset(
                        -r, 1, 1
                ).getBlock()
        );
        assertEquals(
                Blocks.GRASS_BLOCK,
                PlanetDebugWorld.blockStateAtOffset(
                        1, r, 1
                ).getBlock()
        );
        assertEquals(
                Blocks.DEEPSLATE,
                PlanetDebugWorld.blockStateAtOffset(
                        1, -r, 1
                ).getBlock()
        );
        assertEquals(
                Blocks.LIME_CONCRETE,
                PlanetDebugWorld.blockStateAtOffset(
                        1, 1, r
                ).getBlock()
        );
        assertEquals(
                Blocks.YELLOW_CONCRETE,
                PlanetDebugWorld.blockStateAtOffset(
                        1, 1, -r
                ).getBlock()
        );
    }

    @Test
    void spawnStartsInPositiveYGravityRegion() {
        var spawn = PlanetDebugWorld.playerSpawn();

        assertEquals(
                PlanetFace.POS_Y,
                PlanetDebugWorld.gravityField()
                        .selectEntityFace(
                                spawn.x,
                                spawn.y,
                                spawn.z,
                                null,
                                0.0D
                        )
                        .orElseThrow()
        );
    }

    @Test
    void outsideTheCubeIsAirInTheRecipe() {
        assertTrue(
                PlanetDebugWorld.blockStateAtOffset(
                        PlanetDebugWorld.RADIUS + 1,
                        0,
                        0
                ).isAir()
        );
    }
}
