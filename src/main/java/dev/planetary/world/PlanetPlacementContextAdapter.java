package dev.planetary.world;

import dev.planetary.api.PlanetPlacementIntent;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

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

    public static PlanetPlacementIntent fromPhysicalBlockPlaceContext(
            BlockPlaceContext context,
            BlockPos originalPhysicalHitBlock,
            PlanetPlacementIntent.Author author
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(originalPhysicalHitBlock, "originalPhysicalHitBlock");
        Objects.requireNonNull(author, "author");

        Optional<PlanetGravityFrame> playerFrame = Optional.empty();
        Player player = context.getPlayer();
        if (author == PlanetPlacementIntent.Author.PLAYER_BLOCK_ITEM
                && player instanceof PlanetGravityEntity gravityEntity) {
            playerFrame = gravityEntity.planetary$gravityFrame();
        }

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
