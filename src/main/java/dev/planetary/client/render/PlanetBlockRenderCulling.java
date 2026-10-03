package dev.planetary.client.render;

import dev.planetary.world.PlanetBlockStateFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Frame-aware counterpart of Block.shouldRenderFace.
 *
 * <p>Physical neighbor positions stay physical. State-side semantics and
 * canonical occlusion shapes use each block's own local frame.</p>
 */
public final class PlanetBlockRenderCulling {
    private static final int CACHE_SIZE = 2048;

    private static final ThreadLocal<Map<CacheKey, Boolean>>
            OCCLUSION_CACHE =
            ThreadLocal.withInitial(
                    () -> new LinkedHashMap<>(
                            256,
                            0.75F,
                            true
                    ) {
                        @Override
                        protected boolean removeEldestEntry(
                                Map.Entry<CacheKey, Boolean> eldest
                        ) {
                            return size() > CACHE_SIZE;
                        }
                    }
            );

    private PlanetBlockRenderCulling() {
    }

    public static Optional<Boolean> shouldRenderFace(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction physicalDirection,
            BlockPos neighborPos
    ) {
        Optional<PlanetBlockStateFrame> sourceOptional =
                PlanetClientRenderFrame.stateFrameAt(
                        level,
                        pos
                );
        Optional<PlanetBlockStateFrame> targetOptional =
                PlanetClientRenderFrame.stateFrameAt(
                        level,
                        neighborPos
                );

        if (sourceOptional.isEmpty()
                || targetOptional.isEmpty()) {
            return Optional.empty();
        }

        PlanetBlockStateFrame source =
                sourceOptional.get();
        PlanetBlockStateFrame target =
                targetOptional.get();

        Direction sourceLocalSide =
                source.worldToLocal(
                        physicalDirection
                );
        Direction targetLocalSide =
                target.worldToLocal(
                        physicalDirection
                                .getOpposite()
                );

        BlockState neighborState =
                level.getBlockState(
                        neighborPos
                );

        if (state.skipRendering(
                neighborState,
                sourceLocalSide
        )) {
            return Optional.of(false);
        }

        if (neighborState.hidesNeighborFace(
                level,
                neighborPos,
                state,
                targetLocalSide
        ) && state.supportsExternalFaceHiding()) {
            return Optional.of(false);
        }

        if (!neighborState.canOcclude()) {
            return Optional.of(true);
        }

        CacheKey key =
                new CacheKey(
                        state,
                        neighborState,
                        sourceLocalSide,
                        targetLocalSide
                );

        Map<CacheKey, Boolean> cache =
                OCCLUSION_CACHE.get();
        Boolean cached =
                cache.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }

        VoxelShape sourceShape =
                state.getFaceOcclusionShape(
                        level,
                        pos,
                        sourceLocalSide
                );

        boolean result;
        if (sourceShape.isEmpty()) {
            result = true;
        } else {
            VoxelShape targetShape =
                    neighborState
                            .getFaceOcclusionShape(
                                    level,
                                    neighborPos,
                                    targetLocalSide
                            );

            result =
                    Shapes.joinIsNotEmpty(
                            sourceShape,
                            targetShape,
                            BooleanOp.ONLY_FIRST
                    );
        }

        cache.put(
                key,
                result
        );
        return Optional.of(result);
    }

    private record CacheKey(
            BlockState state,
            BlockState neighborState,
            Direction sourceLocalSide,
            Direction targetLocalSide
    ) {
    }
}
