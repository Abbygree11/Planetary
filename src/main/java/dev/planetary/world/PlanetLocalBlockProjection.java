package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Resolves an authored LOCAL three-axis block displacement to the one
 * authoritative PHYSICAL block position. Ordinary levels use exact vanilla
 * BlockPos.offset; Planet levels use the ordered surface traversal kernel.
 *
 * <p>Use for block-source sample/neighbor queries, not as a global replacement
 * for physical BlockPos.offset, and never for foreign/virtual coordinates.</p>
 */
public final class PlanetLocalBlockProjection {
    private PlanetLocalBlockProjection() {
    }

    public static BlockPos physicalOffset(
            Level level,
            BlockPos source,
            int localX,
            int localY,
            int localZ
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(source, "source");

        return PlanetBlockRuntime.traversalAt(level, source)
                .map(context -> PlanetLocalBlockOffset.traverse(
                        context,
                        localX,
                        localY,
                        localZ
                ).pos())
                .orElseGet(() -> source.offset(localX, localY, localZ));
    }
}
