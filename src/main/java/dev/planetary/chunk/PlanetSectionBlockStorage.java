package dev.planetary.chunk;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.material.FluidState;

import java.util.Objects;

/**
 * One Minecraft-sized 16x16x16 block-state section.
 *
 * <p>The payload is Minecraft's own {@link PalettedContainer} of real
 * {@link BlockState} objects. Planetary does not introduce its own material
 * enum or copy hardness/tool/fluid properties.</p>
 */
public final class PlanetSectionBlockStorage {
    private final PalettedContainer<BlockState> states;
    private int nonAirBlockCount;

    public PlanetSectionBlockStorage() {
        this.states = new PalettedContainer<>(
                Block.BLOCK_STATE_REGISTRY,
                Blocks.AIR.defaultBlockState(),
                PalettedContainer.Strategy.SECTION_STATES
        );
    }

    public BlockState getBlockState(int localX, int localY, int localZ) {
        validateLocal(localX, localY, localZ);
        return states.get(localX, localY, localZ);
    }

    public FluidState getFluidState(int localX, int localY, int localZ) {
        return getBlockState(localX, localY, localZ).getFluidState();
    }

    public BlockState setBlockState(
            int localX,
            int localY,
            int localZ,
            BlockState newState
    ) {
        validateLocal(localX, localY, localZ);
        Objects.requireNonNull(newState, "newState");

        BlockState previous = states.get(localX, localY, localZ);
        if (previous == newState) {
            return previous;
        }

        boolean wasAir = previous.isAir();
        boolean isAir = newState.isAir();

        states.set(localX, localY, localZ, newState);

        if (wasAir && !isAir) {
            nonAirBlockCount++;
        } else if (!wasAir && isAir) {
            nonAirBlockCount--;
        }

        return previous;
    }

    public int nonAirBlockCount() {
        return nonAirBlockCount;
    }

    public boolean hasOnlyAir() {
        return nonAirBlockCount == 0;
    }

    /**
     * Exposes the vanilla palette container for future packet/save adapters.
     */
    public PalettedContainer<BlockState> states() {
        return states;
    }

    private static void validateLocal(int x, int y, int z) {
        if (x < 0 || x >= PlanetSection.SIZE
                || y < 0 || y >= PlanetSection.SIZE
                || z < 0 || z >= PlanetSection.SIZE) {
            throw new IllegalArgumentException(
                    "Section-local coordinates must be in [0, 15]: "
                            + x + ", " + y + ", " + z
            );
        }
    }
}
