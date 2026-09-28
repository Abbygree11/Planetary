package dev.planetary.debug;

import dev.planetary.chunk.MinecraftTestBootstrap;
import dev.planetary.topology.PlanetFace;
import dev.planetary.world.PlanetBlockPos;
import dev.planetary.world.PlanetWorldAccess;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class PlanetDebugPlanetoidTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.ensureBootstrapped();
    }

    @Test
    void generatorBuildsOneCompleteSurfacePerFace() {
        PlanetWorldAccess world = PlanetDebugPlanetoid.create();

        int expectedBlocks = PlanetDebugPlanetoid.FACE_SIZE
                * PlanetDebugPlanetoid.FACE_SIZE
                * PlanetFace.values().length;

        assertEquals(
                expectedBlocks,
                world.blocks()
                        .sections()
                        .findSection(
                                new PlanetBlockPos(
                                        PlanetFace.POS_Y,
                                        0,
                                        0,
                                        0
                                ).sectionAddress()
                        )
                        .map(ignored -> countSurfaceBlocks(world))
                        .orElse(0)
        );
    }

    @Test
    void cornersEdgesCentersAndFaceMaterialsAreDeterministic() {
        PlanetWorldAccess world = PlanetDebugPlanetoid.create();
        int last = PlanetDebugPlanetoid.FACE_SIZE - 1;
        int center = PlanetDebugPlanetoid.FACE_SIZE / 2;

        assertSame(
                Blocks.GOLD_BLOCK.defaultBlockState(),
                world.getBlockState(
                        new PlanetBlockPos(
                                PlanetFace.POS_X,
                                0,
                                0,
                                0
                        )
                )
        );
        assertSame(
                Blocks.IRON_BLOCK.defaultBlockState(),
                world.getBlockState(
                        new PlanetBlockPos(
                                PlanetFace.POS_X,
                                0,
                                0,
                                center
                        )
                )
        );
        assertSame(
                Blocks.SEA_LANTERN.defaultBlockState(),
                world.getBlockState(
                        new PlanetBlockPos(
                                PlanetFace.POS_X,
                                center,
                                0,
                                center
                        )
                )
        );
        assertSame(
                Blocks.RED_CONCRETE.defaultBlockState(),
                world.getBlockState(
                        new PlanetBlockPos(
                                PlanetFace.POS_X,
                                2,
                                0,
                                2
                        )
                )
        );
        assertSame(
                Blocks.BLUE_CONCRETE.defaultBlockState(),
                world.getBlockState(
                        new PlanetBlockPos(
                                PlanetFace.NEG_X,
                                last - 2,
                                0,
                                last - 2
                        )
                )
        );
    }

    private static int countSurfaceBlocks(PlanetWorldAccess world) {
        int count = 0;
        for (PlanetFace face : PlanetFace.values()) {
            for (int x = 0;
                 x < PlanetDebugPlanetoid.FACE_SIZE;
                 x++) {
                for (int z = 0;
                     z < PlanetDebugPlanetoid.FACE_SIZE;
                     z++) {
                    if (!world.getBlockState(
                            new PlanetBlockPos(face, x, 0, z)
                    ).isAir()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
