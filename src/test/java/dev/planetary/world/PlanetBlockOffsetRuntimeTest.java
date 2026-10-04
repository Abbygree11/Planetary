package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlanetBlockOffsetRuntimeTest {
    @Test
    void canonicalXZSeedIsInvariantAlongLocalVerticalOnEveryFace() {
        BlockPos origin =
                new BlockPos(7, 11, 13);

        for (PlanetFace face : PlanetFace.values()) {
            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            Direction worldUp =
                    PlanetVanillaDirection.localToWorld(
                            frame,
                            Direction.UP
                    );
            Direction worldEast =
                    PlanetVanillaDirection.localToWorld(
                            frame,
                            Direction.EAST
                    );
            Direction worldSouth =
                    PlanetVanillaDirection.localToWorld(
                            frame,
                            Direction.SOUTH
                    );

            BlockPos seed =
                    PlanetBlockOffsetRuntime.canonicalSeedPos(
                            origin,
                            face
                    );
            BlockPos verticalSeed =
                    PlanetBlockOffsetRuntime.canonicalSeedPos(
                            origin.relative(worldUp, 5),
                            face
                    );
            BlockPos eastSeed =
                    PlanetBlockOffsetRuntime.canonicalSeedPos(
                            origin.relative(worldEast),
                            face
                    );
            BlockPos southSeed =
                    PlanetBlockOffsetRuntime.canonicalSeedPos(
                            origin.relative(worldSouth),
                            face
                    );

            assertEquals(seed.getX(), verticalSeed.getX(), face + " local X");
            assertEquals(seed.getZ(), verticalSeed.getZ(), face + " local Z");
            assertEquals(seed.getY() + 5, verticalSeed.getY(), face + " local Y");

            assertEquals(seed.getX() + 1, eastSeed.getX(), face + " east X");
            assertEquals(seed.getZ(), eastSeed.getZ(), face + " east Z");

            assertEquals(seed.getX(), southSeed.getX(), face + " south X");
            assertEquals(seed.getZ() + 1, southSeed.getZ(), face + " south Z");
        }
    }
}
