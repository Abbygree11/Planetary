package dev.planetary.api;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.world.PlanetBlockNeighborQuery;
import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.world.PlanetVanillaDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import java.util.Objects;
import java.util.Optional;

/**
 * Stable integration surface for Planet-local block directions.
 */
public final class PlanetFrameApi {
    private PlanetFrameApi() {
    }

    public static Optional<PlanetFace> canonicalBlockFace(
            Level level,
            BlockPos pos
    ) {
        return PlanetBlockRuntime.stateFrameAt(level, pos)
                .map(frame -> frame.face());
    }

    public static Optional<Direction> localSideToPhysical(
            Level level,
            BlockPos pos,
            Direction localSide
    ) {
        return PlanetBlockRuntime.localSideToPhysical(
                level,
                pos,
                localSide
        );
    }

    public static Optional<Direction> physicalSideToLocal(
            Level level,
            BlockPos pos,
            Direction physicalSide
    ) {
        return PlanetBlockRuntime.physicalSideToLocal(
                level,
                pos,
                physicalSide
        );
    }

    public static Optional<PlacementFrame> placementFrame(
            Level level,
            BlockPos targetPos,
            Direction physicalClickedFace,
            Vec3 worldClickLocation
    ) {
        return PlanetBlockRuntime.placementFrame(
                level,
                targetPos,
                physicalClickedFace,
                worldClickLocation
        ).map(frame ->
                new PlacementFrame(
                        frame.targetPos(),
                        frame.targetStateFrame().face(),
                        frame.physicalClickedFace(),
                        frame.localClickedFace(),
                        frame.worldClickLocation(),
                        frame.localHitOffset()
                )
        );
    }

    public static Optional<PlacementFrame> placementFrame(
            BlockPlaceContext context
    ) {
        Objects.requireNonNull(context, "context");

        return placementFrame(
                context.getLevel(),
                context.getClickedPos(),
                context.getClickedFace(),
                context.getClickLocation()
        );
    }

    /**
     * Planet-local counterpart of BlockPlaceContext.getNearestLookingDirections
     * for interactive player placement.
     *
     * <p>The player's yaw/pitch are body-local in Planet runtime, so
     * Direction.orderedByNearest already gives local orientation order. The
     * vanilla non-replacing reorder must however use the LOCAL clicked face,
     * not the physical BlockHitResult face.</p>
     *
     * <p>Returns empty for player-less contexts such as dispenser/falling-block
     * DirectionalPlaceContext. Those call paths have their own physical/local
     * contract and are intentionally not guessed here.</p>
     */
    public static Optional<List<Direction>> localNearestLookingDirections(
            BlockPlaceContext context
    ) {
        Objects.requireNonNull(context, "context");

        Player player = context.getPlayer();
        if (player == null) {
            return Optional.empty();
        }

        Optional<PlacementFrame> placementOptional =
                placementFrame(context);
        if (placementOptional.isEmpty()) {
            return Optional.empty();
        }

        Direction[] directions =
                Direction.orderedByNearest(player);

        /*
         * orderedByNearest is expressed in the player's BODY-local frame.
         * Placement properties belong to the TARGET block's canonical frame.
         * They normally match on a face interior but can differ at an exact
         * gravity edge, so re-express every candidate through physical world
         * Direction before applying target-local placement rules.
         */
        if (!(player instanceof PlanetGravityEntity gravityEntity)) {
            return Optional.empty();
        }

        Optional<PlanetGravityFrame> playerFrameOptional =
                gravityEntity.planetary$gravityFrame();
        if (playerFrameOptional.isEmpty()) {
            return Optional.empty();
        }

        PlanetGravityFrame playerFrame =
                playerFrameOptional.get();
        PlanetGravityFrame targetFrame =
                new PlanetGravityFrame(
                        placementOptional.get().targetFace()
                );

        for (int i = 0; i < directions.length; i++) {
            Direction physical =
                    PlanetVanillaDirection.localToWorld(
                            playerFrame,
                            directions[i]
                    );
            directions[i] =
                    PlanetVanillaDirection.worldToLocal(
                            targetFrame,
                            physical
                    );
        }

        if (!context.replacingClickedOnBlock()) {
            Direction preferred =
                    placementOptional.get()
                            .localClickedFace()
                            .getOpposite();

            int index = 0;
            while (index < directions.length
                    && directions[index] != preferred) {
                index++;
            }

            if (index > 0 && index < directions.length) {
                System.arraycopy(
                        directions,
                        0,
                        directions,
                        1,
                        index
                );
                directions[0] = preferred;
            }
        }

        return Optional.of(List.of(directions));
    }

    public static Optional<BlockNeighbor> localNeighbor(
            Level level,
            BlockPos sourcePos,
            Direction sourceLocalDirection
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(sourceLocalDirection, "sourceLocalDirection");

        return PlanetBlockRuntime.neighbor(
                level,
                sourcePos,
                sourceLocalDirection
        ).map(PlanetFrameApi::toApiNeighbor);
    }

    private static BlockNeighbor toApiNeighbor(
            PlanetBlockNeighborQuery query
    ) {
        return new BlockNeighbor(
                query.sourcePos(),
                query.sourceStateFrame().face(),
                query.sourceLocalDirection(),
                query.physicalDirection(),
                query.targetPos(),
                query.targetStateFrame().face(),
                query.targetLocalSideTowardSource(),
                query.crossedTraversalBoundary()
        );
    }

    public record PlacementFrame(
            BlockPos targetPos,
            PlanetFace targetFace,
            Direction physicalClickedFace,
            Direction localClickedFace,
            Vec3 worldClickLocation,
            Vec3 localHitOffset
    ) {
        public PlacementFrame {
            Objects.requireNonNull(targetPos, "targetPos");
            Objects.requireNonNull(targetFace, "targetFace");
            Objects.requireNonNull(
                    physicalClickedFace,
                    "physicalClickedFace"
            );
            Objects.requireNonNull(
                    localClickedFace,
                    "localClickedFace"
            );
            Objects.requireNonNull(
                    worldClickLocation,
                    "worldClickLocation"
            );
            Objects.requireNonNull(
                    localHitOffset,
                    "localHitOffset"
            );
        }

        public boolean localHitUpperHalf() {
            return localHitOffset.y > 0.5D;
        }
    }

    public record BlockNeighbor(
            BlockPos sourcePos,
            PlanetFace sourceFace,
            Direction sourceLocalDirection,
            Direction physicalDirection,
            BlockPos targetPos,
            PlanetFace targetFace,
            Direction targetLocalSideTowardSource,
            boolean crossedGravityBoundary
    ) {
        public BlockNeighbor {
            Objects.requireNonNull(sourcePos, "sourcePos");
            Objects.requireNonNull(sourceFace, "sourceFace");
            Objects.requireNonNull(sourceLocalDirection, "sourceLocalDirection");
            Objects.requireNonNull(physicalDirection, "physicalDirection");
            Objects.requireNonNull(targetPos, "targetPos");
            Objects.requireNonNull(targetFace, "targetFace");
            Objects.requireNonNull(
                    targetLocalSideTowardSource,
                    "targetLocalSideTowardSource"
            );
        }
    }
}
