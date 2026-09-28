package dev.planetary.world;

import dev.planetary.topology.PlanetDirection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Objects;

/**
 * First world-access bridge between cube-planet coordinates and vanilla block
 * data.
 *
 * <p>This deliberately is not a fake ServerLevel yet. It gives the later
 * Level/BlockGetter compatibility layer one canonical place for topology-aware
 * block, fluid and neighbor access.</p>
 */
public final class PlanetWorldAccess {
    private final int faceSizeBlocks;
    private final PlanetBlockStateStore blocks;

    public PlanetWorldAccess(int faceSizeBlocks) {
        this(faceSizeBlocks, new PlanetBlockStateStore());
    }

    public PlanetWorldAccess(int faceSizeBlocks, PlanetBlockStateStore blocks) {
        if (faceSizeBlocks <= 0) {
            throw new IllegalArgumentException("faceSizeBlocks must be > 0");
        }
        this.faceSizeBlocks = faceSizeBlocks;
        this.blocks = Objects.requireNonNull(blocks, "blocks");
    }

    public int faceSizeBlocks() {
        return faceSizeBlocks;
    }

    public PlanetBlockStateStore blocks() {
        return blocks;
    }

    public PlanetBlockPos relative(PlanetBlockPos pos, Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return PlanetBlockTopology.step(
                Objects.requireNonNull(pos, "pos"),
                PlanetVanillaDirection.fromVanilla(direction),
                faceSizeBlocks
        );
    }

    public PlanetBlockPos relative(PlanetBlockPos pos, PlanetDirection direction) {
        return PlanetBlockTopology.step(
                Objects.requireNonNull(pos, "pos"),
                Objects.requireNonNull(direction, "direction"),
                faceSizeBlocks
        );
    }

    public BlockState getBlockState(PlanetBlockPos pos) {
        return blocks.getBlockState(pos);
    }

    public FluidState getFluidState(PlanetBlockPos pos) {
        return blocks.getFluidState(pos);
    }

    /**
     * Stores the exact vanilla/modded BlockState and returns the previous one.
     */
    public BlockState setBlockState(PlanetBlockPos pos, BlockState state) {
        return blocks.setBlockState(pos, state);
    }

    public BlockState getNeighborState(PlanetBlockPos pos, Direction direction) {
        return getBlockState(relative(pos, direction));
    }

    public FluidState getNeighborFluidState(PlanetBlockPos pos, Direction direction) {
        return getFluidState(relative(pos, direction));
    }
}
