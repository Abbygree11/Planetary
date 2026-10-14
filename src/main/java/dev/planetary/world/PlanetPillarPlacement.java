package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

/**
 * Whole axis-pillar placement algorithm in the TARGET block's canonical frame.
 * RotatedPillarBlock subclasses inherit its placement semantics; independent
 * InfestedRotatedPillarBlock authors the same rule from a different base class
 * and delegates through this explicit opt-in helper.
 *
 * <p>Only AXIS is rewritten on the original return state. ChainBlock's
 * WATERLOGGED state, mod-authored properties, identity, item callbacks,
 * structures, worldgen and post-placement state components stay intact.</p>
 */
public final class PlanetPillarPlacement {
    private PlanetPillarPlacement() {
    }

    public static Optional<Direction.Axis> canonicalAxis(BlockPlaceContext context) {
        Objects.requireNonNull(context, "context");
        return PlanetFrameApi.placementFrame(context)
                .map(frame -> frame.localClickedFace().getAxis());
    }

    public static Optional<BlockState> afterPlacement(
            BlockPlaceContext context, BlockState original
    ) {
        Objects.requireNonNull(context, "context");
        if (original == null || !original.hasProperty(RotatedPillarBlock.AXIS)) {
            return Optional.empty();
        }
        return canonicalAxis(context)
                .map(axis -> original.setValue(RotatedPillarBlock.AXIS, axis));
    }
}
