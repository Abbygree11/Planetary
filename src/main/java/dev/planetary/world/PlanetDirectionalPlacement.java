package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Semantic directional state placement while keeping hit geometry and
 * the physical clicked-neighbor lookup vanilla.
 *
 * <p>FACING is stored in the TARGET canonical BlockState frame.
 * A neighbor's stored FACING must be translated through that neighbor's
 * OWN canonical frame before comparing with the physical clicked face.</p>
 */
public final class PlanetDirectionalPlacement {
    private PlanetDirectionalPlacement() {
    }

    public static Optional<BlockState> endRod(BlockPlaceContext context,
                                                EndRodBlock block) {
        return PlanetFrameApi.placementFrame(context)
                .map(placement -> {
                    Direction clickedPhysical = context.getClickedFace();
                    Direction targetLocal = placement.localClickedFace();

                    // Vanilla's neighbor inspection is physical geometry,
                    // NOT a local-direction topological step.
                    BlockPos adjacent = context.getClickedPos()
                            .relative(clickedPhysical.getOpposite());
                    BlockState neighbor = context.getLevel()
                            .getBlockState(adjacent);

                    if (neighbor.is(block)) {
                        Direction neighborLocal = neighbor.getValue(
                                EndRodBlock.FACING);
                        Direction neighborPhysical =
                                PlanetBlockRuntime.stateFrameAt(
                                                context.getLevel(), adjacent)
                                        .map(frame ->
                                                frame.localToWorld(neighborLocal))
                                        .orElse(neighborLocal);

                        if (neighborPhysical == clickedPhysical) {
                            targetLocal = targetLocal.getOpposite();
                        }
                    }

                    return block.defaultBlockState()
                            .setValue(EndRodBlock.FACING, targetLocal);
                });
    }

    /**
     * The vanilla Ender Chest uses player horizontal facing opposite. This
     * helper reframes the player body-local horizontal direction to the
     * target's canonical local direction; waterlogging remains vanilla.
     */
    public static Optional<Direction> horizontalFacing(BlockPlaceContext context) {
        return PlanetFrameApi.localHorizontalDirection(context)
                .map(Direction::getOpposite);
    }
}
