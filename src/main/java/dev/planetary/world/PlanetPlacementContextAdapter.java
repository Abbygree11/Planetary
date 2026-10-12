package dev.planetary.world;

import dev.planetary.api.PlanetPlacementIntent;
import dev.planetary.api.PlanetOriginalHitSource;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Objects;
import java.util.Optional;

/**
 * Minecraft 1.21.1 BlockPlaceContext -> Planet semantic intent adapter.
 *
 * <p>The caller must establish that this is a REAL physical Level and supply
 * the original hit BlockPos explicitly. UseOnContext.getHitResult() is protected,
 * while BlockPlaceContext.getClickedPos() is the FINAL target, not necessarily
 * the original hit. In particular, do not undo retargeting by a modded item.</p>
 *
 * <p>This adapter does NOT replace vanilla BlockItem.place, canPlace,
 * getStateForPlacement, setPlacedBy, NeoForge events or item components.</p>
 */
public final class PlanetPlacementContextAdapter {
    private PlanetPlacementContextAdapter() {
    }

    /**
     * Original physical hit for THIS BlockPlaceContext via a woven read-only
     * UseOnContext bridge. Synthetic contexts use their own synthetic hit.
     * Do not infer source geometry from getClickedPos() on unwoven contexts.
     */
    public static Optional<PlanetPlacementIntent> fromPhysicalBlockPlaceContext(
            BlockPlaceContext context,
            PlanetPlacementIntent.Author author
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(author, "author");
        if (!(context instanceof PlanetOriginalHitSource source)) {
            return Optional.empty();
        }
        BlockHitResult hit = Objects.requireNonNull(source.planetary$originalHit(), "hit");
        return Optional.of(PlanetPlacementIntent.physical(
                author, context.getLevel(), hit.getBlockPos(), context.getClickedPos(),
                hit.getDirection(), hit.getLocation(), context.replacingClickedOnBlock(),
                playerFrame(context, author)
        ));
    }

    public static Optional<PlanetPlacementResolver.Resolution> resolvePhysical(
            BlockPlaceContext context, PlanetPlacementIntent.Author author
    ) {
        return fromPhysicalBlockPlaceContext(context, author)
                .map(PlanetPlacementResolver::resolve);
    }

    private static Optional<PlanetGravityFrame> playerFrame(
            BlockPlaceContext context, PlanetPlacementIntent.Author author
    ) {
        if (author != PlanetPlacementIntent.Author.PLAYER_BLOCK_ITEM) {
            return Optional.empty();
        }
        Player player = context.getPlayer();
        return player instanceof PlanetGravityEntity gravity
                ? gravity.planetary$gravityFrame() : Optional.empty();
    }

    public static PlanetPlacementIntent fromPhysicalBlockPlaceContext(
            BlockPlaceContext context,
            BlockPos originalPhysicalHitBlock,
            PlanetPlacementIntent.Author author
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(originalPhysicalHitBlock, "originalPhysicalHitBlock");
        Objects.requireNonNull(author, "author");

        Optional<PlanetGravityFrame> playerFrame = playerFrame(context, author);

        return PlanetPlacementIntent.physical(
                author,
                context.getLevel(),
                originalPhysicalHitBlock,
                context.getClickedPos(),
                context.getClickedFace(),
                context.getClickLocation(),
                context.replacingClickedOnBlock(),
                playerFrame
        );
    }
}
