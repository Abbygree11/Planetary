package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

import java.util.Objects;
import java.util.Optional;

/**
 * Local-frame equivalent of PointedDripstoneBlock.spawnFallingStalactite.
 */
public final class PlanetDripstoneFalling {
    private static final int VANILLA_MAX_DAMAGE = 40;
    private static final int VANILLA_MIN_DAMAGE_SIZE = 6;

    private PlanetDripstoneFalling() {
    }

    public static void spawnFallingStalactite(
            BlockState startState,
            ServerLevel level,
            BlockPos startPos
    ) {
        Objects.requireNonNull(startState, "startState");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(startPos, "startPos");

        BlockPos currentPos =
                startPos;
        BlockState currentState =
                startState;
        int localDistance = 0;

        while (isStalactite(currentState)) {
            FallingBlockEntity falling =
                    FallingBlockEntity.fall(
                            level,
                            currentPos,
                            currentState
                    );

            if (isTip(currentState, true)) {
                int size =
                        Math.max(
                                1 + localDistance,
                                VANILLA_MIN_DAMAGE_SIZE
                        );
                falling.setHurtsEntities(
                        (float) size,
                        VANILLA_MAX_DAMAGE
                );

                break;
            }

            Optional<PlanetBlockNeighborQuery> next =
                    PlanetBlockRuntime.neighbor(
                            level,
                            currentPos,
                            Direction.DOWN
                    );
            if (next.isEmpty()) {
                break;
            }

            currentPos =
                    next.get().targetPos();
            currentState =
                    level.getBlockState(
                            currentPos
                    );
            localDistance++;
        }
    }

    private static boolean isStalactite(
            BlockState state
    ) {
        return state.is(
                Blocks.POINTED_DRIPSTONE
        ) && state.getValue(
                PointedDripstoneBlock.TIP_DIRECTION
        ) == Direction.DOWN;
    }

    private static boolean isTip(
            BlockState state,
            boolean allowMerged
    ) {
        if (!state.is(
                Blocks.POINTED_DRIPSTONE
        )) {
            return false;
        }

        DripstoneThickness thickness =
                state.getValue(
                        PointedDripstoneBlock.THICKNESS
                );

        return thickness == DripstoneThickness.TIP
                || allowMerged
                && thickness
                == DripstoneThickness.TIP_MERGE;
    }
}
