package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

/**
 * Dispatches Planetary neighbor changes through Minecraft's real
 * {@link Level#neighborChanged(BlockPos, Block, BlockPos)} pipeline.
 *
 * <p>Every target is passed to vanilla in its own canonical face-local virtual
 * frame. The source position is encoded as a one-block alias adjacent to that
 * target. This matters at cube seams: the target block therefore sees normal
 * vanilla-adjacent BlockPos values and its Direction arithmetic is relative to
 * the target face, not to the face the update came from.</p>
 */
public final class PlanetVanillaNeighborUpdates {
    private PlanetVanillaNeighborUpdates() {
    }

    public static void updateAll(
            Level level,
            PlanetWorldAccess world,
            PlanetBlockPos source,
            Block sourceBlock
    ) {
        updateExcept(level, world, source, sourceBlock, null);
    }

    public static void updateExcept(
            Level level,
            PlanetWorldAccess world,
            PlanetBlockPos source,
            Block sourceBlock,
            Direction exceptDirection
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sourceBlock, "sourceBlock");

        PlanetWorldAccess boundWorld = PlanetLevelBridge.get(level);
        if (boundWorld != world) {
            throw new IllegalStateException(
                    "Level is not bound to the supplied PlanetWorldAccess"
            );
        }

        for (Direction direction : Direction.values()) {
            if (direction == exceptDirection) {
                continue;
            }

            PlanetNeighborRef neighbor = world.neighbor(source, direction);

            BlockPos targetPos = world.vanillaPosCodec().encode(
                    neighbor.position()
            );

            BlockPos sourceAlias = targetPos.relative(
                    neighbor.directionBackToSource()
            );

            PlanetBlockPos decodedSource = world.vanillaPosCodec().decode(
                    sourceAlias
            );
            if (!decodedSource.equals(source)) {
                throw new IllegalStateException(
                        "Topology produced a non-adjacent neighbor update: "
                                + source + " -> " + neighbor.position()
                );
            }

            level.neighborChanged(
                    targetPos,
                    sourceBlock,
                    sourceAlias
            );
        }
    }
}
