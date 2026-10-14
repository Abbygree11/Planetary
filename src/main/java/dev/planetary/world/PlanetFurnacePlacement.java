package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

/**
 * The canonical horizontal placement rule of the inherited
 * AbstractFurnaceBlock family (furnace, smoker, blast furnace and subclasses
 * which do not override the placement method).
 *
 * Original BlockState is retained: LIT, registry identity, NeoForge
 * extensions, custom properties and block-entity lifecycle stay vanilla.
 */
public final class PlanetFurnacePlacement {
    private PlanetFurnacePlacement() {
    }

    /** The furnace faces the opposite side of the player's local heading. */
    public static Optional<Direction> frontFromAuthor(
            Optional<Direction> localHorizontalAuthor
    ) {
        Objects.requireNonNull(localHorizontalAuthor, "localHorizontalAuthor");
        return localHorizontalAuthor
                .filter(direction -> direction.getAxis().isHorizontal())
                .map(Direction::getOpposite);
    }

    public static Optional<BlockState> afterPlacement(
            BlockPlaceContext context, BlockState original
    ) {
        Objects.requireNonNull(context, "context");
        if (original == null || !original.hasProperty(AbstractFurnaceBlock.FACING)) {
            return Optional.empty();
        }

        // Only resolve the target's canonical chart after the original
        // NeoForge/BlockItem placement path has executed.
        return PlanetBlockRuntime.stateFrameAt(context.getLevel(),
                        context.getClickedPos())
                .flatMap(target -> {
                    Optional<Direction> horizontal = Optional.empty();
                    if (context.getPlayer() != null) {
                        horizontal = PlanetFrameApi.localHorizontalDirection(context);
                    } else if (context instanceof DirectionalPlaceContext) {
                        // Synthetic directional placement explicitly carries
                        // a PHYSICAL direction, not a player's body yaw.
                        Direction physical = context.getHorizontalDirection();
                        if (physical != null) {
                            Direction local = target.worldToLocal(physical);
                            if (local.getAxis().isHorizontal()) {
                                horizontal = Optional.of(local);
                            }
                        }
                    }
                    return frontFromAuthor(horizontal)
                            .map(front -> original.setValue(AbstractFurnaceBlock.FACING, front));
                });
    }
}
