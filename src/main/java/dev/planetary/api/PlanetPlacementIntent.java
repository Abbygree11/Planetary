package dev.planetary.api;

import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable, author-supplied placement request. Neither a hit normal nor
 * any block position is implicitly transformed into local gravity space.
 *
 * <p>Hit and target coordinates belong to the SAME physical Level or the
 * SAME named foreign coordinate system. A foreign/virtual space must be
 * explicitly projected to a real Level by its integration before Planet
 * canonical block state semantics can be requested.</p>
 *
 * <p>The hit block and final target are separate because BlockPlaceContext
 * may choose a replaceable hit or a different target, and specialized items
 * can retarget the placement. Never reconstruct the hit from the target.</p>
 */
public record PlanetPlacementIntent(
        Author author,
        PlanetCoordinateContext hitBlock,
        PlanetCoordinateContext targetBlock,
        Direction hitFace,
        Vec3 hitLocation,
        boolean replacingHitBlock,
        Optional<PlanetGravityFrame> actorBodyFrame
) {
    /**
     * Author provenance, NOT permission to convert an unknown mod's
     * physical Direction into a canonical BlockState property.
     */
    public enum Author {
        PLAYER_BLOCK_ITEM,
        AUTOMATED_ITEM,
        FALLING_BLOCK,
        SYNTHETIC,
        STRUCTURE,
        WORLDGEN,
        MOD_CUSTOM,
        UNKNOWN
    }

    public PlanetPlacementIntent {
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(hitBlock, "hitBlock");
        Objects.requireNonNull(targetBlock, "targetBlock");
        Objects.requireNonNull(hitFace, "hitFace");
        Objects.requireNonNull(hitLocation, "hitLocation");
        Objects.requireNonNull(actorBodyFrame, "actorBodyFrame");

        if (hitBlock instanceof PlanetCoordinateContext.PhysicalBlock hit
                && targetBlock instanceof PlanetCoordinateContext.PhysicalBlock target) {
            if (hit.level() != target.level()) {
                throw new IllegalArgumentException("hit and target must use the same physical Level");
            }
        } else if (hitBlock instanceof PlanetCoordinateContext.ForeignBlock hit
                && targetBlock instanceof PlanetCoordinateContext.ForeignBlock target) {
            if (!hit.spaceId().equals(target.spaceId())) {
                throw new IllegalArgumentException("hit and target must use the same foreign space");
            }
        } else {
            throw new IllegalArgumentException("mixed physical/foreign coordinates require an explicit adapter");
        }

        if (actorBodyFrame.isPresent() && author != Author.PLAYER_BLOCK_ITEM) {
            throw new IllegalArgumentException("player body frame requires PLAYER_BLOCK_ITEM provenance");
        }
    }

    /**
     * Explicitly asserts real physical Minecraft coordinates; do NOT pass a
     * virtual/contraption Level here without an integration-level projection.
     */
    public static PlanetPlacementIntent physical(
            Author author,
            Level level,
            BlockPos physicalHitBlock,
            BlockPos physicalTargetBlock,
            Direction physicalHitFace,
            Vec3 physicalHitLocation,
            boolean replacingHitBlock,
            Optional<PlanetGravityFrame> playerBodyFrame
    ) {
        return new PlanetPlacementIntent(
                author,
                PlanetCoordinateContext.physical(level, physicalHitBlock),
                PlanetCoordinateContext.physical(level, physicalTargetBlock),
                physicalHitFace,
                physicalHitLocation,
                replacingHitBlock,
                playerBodyFrame
        );
    }

    /**
     * Foreign coordinates are deliberately NOT treated as physical geometry.
     * hitFace and hitLocation belong to the foreign chart until projected.
     */
    public static PlanetPlacementIntent foreign(
            Author author,
            ResourceLocation spaceId,
            BlockPos hitBlock,
            BlockPos targetBlock,
            Direction hitFace,
            Vec3 hitLocation,
            boolean replacingHitBlock
    ) {
        return new PlanetPlacementIntent(
                author,
                PlanetCoordinateContext.foreign(spaceId, hitBlock),
                PlanetCoordinateContext.foreign(spaceId, targetBlock),
                hitFace,
                hitLocation,
                replacingHitBlock,
                Optional.empty()
        );
    }

    public boolean hasPhysicalCoordinates() {
        return targetBlock instanceof PlanetCoordinateContext.PhysicalBlock;
    }
}
