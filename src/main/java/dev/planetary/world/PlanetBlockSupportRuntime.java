package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;

/**
 * Runtime bridge from canonical local support semantics to vanilla support
 * queries on the actual physical neighboring block.
 */
public final class PlanetBlockSupportRuntime {
    private PlanetBlockSupportRuntime() {
    }

    public static Optional<PlanetBlockSupportQuery> query(
            LevelReader level,
            BlockPos sourcePos,
            Direction sourceLocalDirectionToSupport
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(
                sourceLocalDirectionToSupport,
                "sourceLocalDirectionToSupport"
        );

        if (!(level instanceof Level physicalLevel)) {
            return Optional.empty();
        }

        return PlanetBlockRuntime.supportQuery(
                physicalLevel,
                sourcePos,
                sourceLocalDirectionToSupport
        );
    }

    public static boolean canSupportCenter(
            LevelReader level,
            PlanetBlockSupportQuery query
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(query, "query");

        return Block.canSupportCenter(
                level,
                query.supportPos(),
                query.supportLocalSideTowardSource()
        );
    }

    public static boolean isFaceSturdy(
            LevelReader level,
            PlanetBlockSupportQuery query
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(query, "query");

        BlockState support =
                level.getBlockState(
                        query.supportPos()
                );

        return support.isFaceSturdy(
                level,
                query.supportPos(),
                query.supportLocalSideTowardSource()
        );
    }

    public static boolean isSupportNeighbor(
            PlanetBlockSupportQuery query,
            BlockPos physicalNeighborPos
    ) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(
                physicalNeighborPos,
                "physicalNeighborPos"
        );

        return query.supportPos()
                .equals(physicalNeighborPos);
    }
}
