package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetTopology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Canonical target-local StairBlock placement and corner shape maintenance.
 * Handles the inherited StairBlock algorithm (including non-vanilla materials
 * which inherit it), NOT independently-authored mod algorithms.
 *
 * <p>All actual neighboring cells remain physical Minecraft BlockPos. When
 * source/target have different local gravity frames, horizontal headings are
 * parallel-transported across the specific shared cube edge before comparing
 * orientation, rather than comparing unrelated canonical FACING enum values.</p>
 */
public final class PlanetStairPlacement {
    private PlanetStairPlacement() {
    }

    public record StairNeighbor(BlockState state, Direction facingInSource) {
        public StairNeighbor {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(facingInSource, "facingInSource");
        }

        public boolean matchingHalf(Half half) {
            return StairBlock.isStairs(state) && state.getValue(StairBlock.HALF) == half;
        }
    }

    /**
     * Vanilla-equivalent 5-way StairsShape decision in one LOCAL chart.
     * A lookup returns no neighbor for air/nonstairs or an unavailable chart.
     * Evaluates the same front/back/perpendicular cases as 1.21.1.
     */
    public static StairsShape shape(
            Direction facing,
            Half half,
            Function<Direction, Optional<StairNeighbor>> neighbors
    ) {
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(half, "half");
        Objects.requireNonNull(neighbors, "neighbors");
        if (!facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("stairs FACING must be local horizontal");
        }

        Optional<StairNeighbor> front = neighbors.apply(facing);
        if (front.isPresent() && front.get().matchingHalf(half)) {
            Direction other = front.get().facingInSource();
            if (other.getAxis() != facing.getAxis()
                    && canTakeShape(facing, half, other.getOpposite(), neighbors)) {
                return other == facing.getCounterClockWise()
                        ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }

        Optional<StairNeighbor> back = neighbors.apply(facing.getOpposite());
        if (back.isPresent() && back.get().matchingHalf(half)) {
            Direction other = back.get().facingInSource();
            if (other.getAxis() != facing.getAxis()
                    && canTakeShape(facing, half, other, neighbors)) {
                return other == facing.getCounterClockWise()
                        ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(
            Direction sourceFacing,
            Half sourceHalf,
            Direction localSide,
            Function<Direction, Optional<StairNeighbor>> neighbors
    ) {
        return neighbors.apply(localSide)
                .map(other -> !other.matchingHalf(sourceHalf)
                        || other.facingInSource() != sourceFacing)
                .orElse(true);
    }

    /**
     * Explicitly transport a target BlockState canonical heading back into
     * the source BlockState chart. At exact corners, don't guess an unrelated
     * frame transformation. Unknown mappings are not rewritten.
     */
    public static Optional<Direction> facingInSource(
            PlanetFace sourceFace,
            PlanetFace targetFace,
            Direction localStepFromSource,
            Direction targetCanonicalFacing
    ) {
        Objects.requireNonNull(sourceFace, "sourceFace");
        Objects.requireNonNull(targetFace, "targetFace");
        Objects.requireNonNull(localStepFromSource, "localStepFromSource");
        Objects.requireNonNull(targetCanonicalFacing, "targetCanonicalFacing");
        if (!localStepFromSource.getAxis().isHorizontal()
                || !targetCanonicalFacing.getAxis().isHorizontal()) {
            return Optional.empty();
        }
        if (sourceFace == targetFace) {
            return Optional.of(targetCanonicalFacing);
        }
        FaceTransform seam = PlanetTopology.edgeTransform(sourceFace,
                PlanetVanillaDirection.fromVanilla(localStepFromSource));
        if (seam.targetFace() != targetFace) {
            return Optional.empty();
        }
        return Optional.of(PlanetVanillaDirection.toVanilla(
                seam.inverse().transformDirection(
                        PlanetVanillaDirection.fromVanilla(targetCanonicalFacing))));
    }

    /** Reads real physical neighbors selected by PlanetBlockNeighborQuery. */
    public static Optional<StairsShape> shapeAt(
            Level level,
            BlockPos target,
            BlockState state
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(state, "state");
        if (!state.hasProperty(StairBlock.FACING)
                || !state.hasProperty(StairBlock.HALF)
                || !state.hasProperty(StairBlock.SHAPE)) {
            return Optional.empty();
        }
        return PlanetBlockRuntime.stateFrameAt(level, target).map(frame ->
                shape(state.getValue(StairBlock.FACING),
                        state.getValue(StairBlock.HALF),
                        side -> PlanetBlockRuntime.neighbor(level, target, side)
                                .flatMap(query -> {
                                    BlockState neighbor = level.getBlockState(query.targetPos());
                                    if (!StairBlock.isStairs(neighbor)) {
                                        return Optional.empty();
                                    }
                                    return facingInSource(frame.face(),
                                            query.targetStateFrame().face(), side,
                                            neighbor.getValue(StairBlock.FACING))
                                            .map(heading -> new StairNeighbor(neighbor, heading));
                                })));
    }

    /**
     * The vanilla getStateForPlacement already resolved waterlogging and ran
     * the concrete owner/NeoForge dispatch. Rewrite only orientation + corner
     * shape after resolving a proven target-local actor heading.
     */
    public static Optional<BlockState> afterPlacement(
            BlockPlaceContext context,
            BlockState vanilla
    ) {
        Objects.requireNonNull(context, "context");
        if (vanilla == null || !vanilla.hasProperty(StairBlock.FACING)
                || !vanilla.hasProperty(StairBlock.HALF)
                || !vanilla.hasProperty(StairBlock.SHAPE)) {
            return Optional.empty();
        }
        // The shared typed context separates the original hit from the
        // retargeted final BlockPlaceContext cell. Resolve it only once.
        var physicalHit = PlanetFrameApi.placementFrame(context);
        Direction physicalClicked = physicalHit
                .map(PlanetFrameApi.PlacementFrame::physicalClickedFace)
                .orElseGet(context::getClickedFace);
        var hitLocation = physicalHit
                .map(PlanetFrameApi.PlacementFrame::worldClickLocation)
                .orElseGet(context::getClickLocation);
        return PlanetBlockRuntime.placementFrame(
                context.getLevel(), context.getClickedPos(), physicalClicked, hitLocation)
                .flatMap(frame -> {
                    Optional<Direction> horizontal = Optional.empty();
                    if (context.getPlayer() != null) {
                        horizontal = PlanetFrameApi.localHorizontalDirection(context);
                    } else if (context instanceof DirectionalPlaceContext) {
                        Direction physical = context.getHorizontalDirection();
                        if (physical != null) {
                            Direction transformed = frame.targetStateFrame().worldToLocal(physical);
                            if (transformed.getAxis().isHorizontal()) {
                                horizontal = Optional.of(transformed);
                            }
                        }
                    }
                    if (horizontal.isEmpty()) {
                        return Optional.empty();
                    }
                    Direction clicked = frame.localClickedFace();
                    Half half = clicked != Direction.DOWN
                            && (clicked == Direction.UP || !frame.localHitUpperHalf())
                            ? Half.BOTTOM : Half.TOP;
                    BlockState adjusted = vanilla.setValue(StairBlock.FACING, horizontal.get())
                            .setValue(StairBlock.HALF, half);
                    return shapeAt(context.getLevel(), context.getClickedPos(), adjusted)
                            .map(shape -> adjusted.setValue(StairBlock.SHAPE, shape));
                });
    }

    /**
     * A real physical neighbor callback may have a world-vertical direction
     * which is LOCAL horizontal on a side face. Re-evaluate at RETURN for all
     * physical sides, preserving vanilla water tick and other update effects.
     * Only real Level-backed world updates are supported here; WorldGenRegion
     * and foreign LevelAccessor remain vanilla until an explicit author adapter.
     */
    public static Optional<BlockState> afterNeighborUpdate(
            LevelAccessor accessor, BlockPos target, BlockState vanilla
    ) {
        if (!(accessor instanceof Level level) || vanilla == null) {
            return Optional.empty();
        }
        return shapeAt(level, target, vanilla)
                .map(shape -> vanilla.setValue(StairBlock.SHAPE, shape));
    }
}
