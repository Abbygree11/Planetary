package dev.planetary.world;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared runtime entry point for Planet block-frame semantics.
 *
 * <p>Physical Minecraft coordinates remain authoritative. This class only
 * resolves the Planet gravity field that is active at a physical BlockPos and
 * then exposes canonical BlockState and traversal views over that position.</p>
 *
 * <p>Runtime mixins and compatibility integrations should use this class
 * instead of independently reading PlanetGravityRuntime and selecting gravity
 * faces themselves.</p>
 */
public final class PlanetBlockRuntime {
    private PlanetBlockRuntime() {
    }

    public static Optional<PlanetGravityField> fieldAt(
            Level level,
            BlockPos pos
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");

        return PlanetGravityRuntime.findAt(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );
    }

    public static Optional<PlanetBlockStateFrame> stateFrameAt(
            Level level,
            BlockPos pos
    ) {
        return fieldAt(level, pos)
                .flatMap(field ->
                        PlanetBlockStateFrame.resolve(
                                field,
                                pos
                        )
                );
    }

    public static Optional<PlanetBlockFrameContext> traversalAt(
            Level level,
            BlockPos pos,
            PlanetFace preferredFace
    ) {
        return fieldAt(level, pos)
                .flatMap(field ->
                        PlanetBlockFrameContext.resolve(
                                field,
                                pos,
                                preferredFace
                        )
                );
    }

    public static Optional<PlanetBlockFrameContext> traversalAt(
            Level level,
            BlockPos pos
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");

        return stateFrameAt(level, pos)
                .flatMap(stateFrame ->
                        PlanetBlockFrameContext.resolve(
                                stateFrame.field(),
                                pos,
                                stateFrame.face()
                        )
                );
    }

    public static Optional<PlanetBlockPlacementFrame> placementFrame(
            Level level,
            BlockPos targetPos,
            Direction physicalClickedFace,
            net.minecraft.world.phys.Vec3 worldClickLocation
    ) {
        Objects.requireNonNull(
                physicalClickedFace,
                "physicalClickedFace"
        );
        Objects.requireNonNull(
                worldClickLocation,
                "worldClickLocation"
        );

        return fieldAt(level, targetPos)
                .flatMap(field ->
                        PlanetBlockPlacementFrame.resolve(
                                field,
                                targetPos,
                                physicalClickedFace,
                                worldClickLocation
                        )
                );
    }

    public static Optional<PlanetBlockNeighborQuery> neighbor(
            Level level,
            BlockPos sourcePos,
            Direction sourceLocalDirection
    ) {
        Objects.requireNonNull(
                sourceLocalDirection,
                "sourceLocalDirection"
        );

        return fieldAt(level, sourcePos)
                .flatMap(field ->
                        PlanetBlockNeighborQuery.resolve(
                                field,
                                sourcePos,
                                sourceLocalDirection
                        )
                );
    }

    public static Optional<PlanetBlockSupportQuery> supportQuery(
            Level level,
            BlockPos sourcePos,
            Direction sourceLocalDirectionToSupport
    ) {
        Objects.requireNonNull(
                sourceLocalDirectionToSupport,
                "sourceLocalDirectionToSupport"
        );

        return fieldAt(level, sourcePos)
                .flatMap(field ->
                        PlanetBlockSupportQuery.resolve(
                                field,
                                sourcePos,
                                sourceLocalDirectionToSupport
                        )
                );
    }

    /**
     * Converts one PHYSICAL world side of a block into that physical block's
     * canonical LOCAL BlockState side.
     */
    public static Optional<Direction> physicalSideToLocal(
            Level level,
            BlockPos pos,
            Direction physicalSide
    ) {
        Objects.requireNonNull(
                physicalSide,
                "physicalSide"
        );

        return stateFrameAt(level, pos)
                .map(frame ->
                        frame.worldToLocal(
                                physicalSide
                        )
                );
    }

    /**
     * Converts one canonical LOCAL BlockState side into the corresponding
     * PHYSICAL world side at this block.
     */
    public static Optional<Direction> localSideToPhysical(
            Level level,
            BlockPos pos,
            Direction localSide
    ) {
        Objects.requireNonNull(
                localSide,
                "localSide"
        );

        return stateFrameAt(level, pos)
                .map(frame ->
                        frame.localToWorld(
                                localSide
                        )
                );
    }
}
