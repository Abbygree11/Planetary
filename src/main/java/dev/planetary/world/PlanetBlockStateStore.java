package dev.planetary.world;

import dev.planetary.chunk.PlanetSectionStore;
import dev.planetary.topology.PlanetDirection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Objects;

/**
 * BlockState access over sparse 16^3 planet sections.
 *
 * <p>All values are real vanilla/modded BlockState instances. This layer only
 * translates a canonical PlanetBlockPos into section address + local
 * coordinates.</p>
 */
public final class PlanetBlockStateStore {
    private final PlanetSectionStore sections;

    public PlanetBlockStateStore() {
        this(new PlanetSectionStore());
    }

    public PlanetBlockStateStore(PlanetSectionStore sections) {
        this.sections = Objects.requireNonNull(sections, "sections");
    }

    public BlockState getBlockState(PlanetBlockPos pos) {
        Objects.requireNonNull(pos, "pos");
        return sections.getBlockState(
                pos.sectionAddress(),
                pos.localX(),
                pos.localY(),
                pos.localZ()
        );
    }

    public FluidState getFluidState(PlanetBlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    public BlockState setBlockState(PlanetBlockPos pos, BlockState state) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(state, "state");

        return sections.setBlockState(
                pos.sectionAddress(),
                pos.localX(),
                pos.localY(),
                pos.localZ(),
                state
        );
    }

    public BlockState getNeighborState(
            PlanetBlockPos pos,
            PlanetDirection direction,
            int faceSizeBlocks
    ) {
        return getBlockState(
                PlanetBlockTopology.step(pos, direction, faceSizeBlocks)
        );
    }

    public PlanetSectionStore sections() {
        return sections;
    }
}
