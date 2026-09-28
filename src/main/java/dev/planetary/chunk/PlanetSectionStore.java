package dev.planetary.chunk;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Sparse in-memory section storage.
 *
 * <p>Completely empty sections are represented implicitly as air and do not
 * allocate a palette container. This is important for the large amount of
 * empty space around a finite planet.</p>
 */
public final class PlanetSectionStore {
    private final Map<PlanetSectionAddress, PlanetSectionBlockStorage> sections = new HashMap<>();

    public Optional<PlanetSectionBlockStorage> findSection(PlanetSectionAddress address) {
        return Optional.ofNullable(sections.get(address));
    }

    public PlanetSectionBlockStorage getOrCreateSection(PlanetSectionAddress address) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        return sections.computeIfAbsent(address, ignored -> new PlanetSectionBlockStorage());
    }

    public BlockState getBlockState(
            PlanetSectionAddress address,
            int localX,
            int localY,
            int localZ
    ) {
        PlanetSectionBlockStorage section = sections.get(address);
        if (section == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return section.getBlockState(localX, localY, localZ);
    }

    public BlockState setBlockState(
            PlanetSectionAddress address,
            int localX,
            int localY,
            int localZ,
            BlockState state
    ) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        if (state == null) {
            throw new NullPointerException("state");
        }

        PlanetSectionBlockStorage existing = sections.get(address);
        if (existing == null && state.isAir()) {
            return Blocks.AIR.defaultBlockState();
        }

        PlanetSectionBlockStorage section = existing != null
                ? existing
                : getOrCreateSection(address);

        BlockState previous = section.setBlockState(localX, localY, localZ, state);

        if (section.hasOnlyAir()) {
            sections.remove(address);
        }

        return previous;
    }

    public int allocatedSectionCount() {
        return sections.size();
    }

    public void clear() {
        sections.clear();
    }
}
