package dev.planetary.world;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

/**
 * Canonical placement-orientation view over a physical Minecraft block hit.
 *
 * <p>The original hit face/location remain physical world geometry. This class
 * derives the LOCAL values that placement/state code needs for the target
 * block's canonical BlockState frame.</p>
 */
public record PlanetBlockPlacementFrame(
        PlanetBlockStateFrame targetStateFrame,
        Direction physicalClickedFace,
        Direction localClickedFace,
        Vec3 worldClickLocation,
        Vec3 localHitOffset
) {
    public PlanetBlockPlacementFrame {
        Objects.requireNonNull(
                targetStateFrame,
                "targetStateFrame"
        );
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

        if (targetStateFrame.localToWorld(
                localClickedFace
        ) != physicalClickedFace) {
            throw new IllegalArgumentException(
                    "local clicked face does not map to physical hit face"
            );
        }
    }

    public static Optional<PlanetBlockPlacementFrame> resolve(
            PlanetGravityField field,
            BlockPos targetPos,
            Direction physicalClickedFace,
            Vec3 worldClickLocation
    ) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(targetPos, "targetPos");
        Objects.requireNonNull(
                physicalClickedFace,
                "physicalClickedFace"
        );
        Objects.requireNonNull(
                worldClickLocation,
                "worldClickLocation"
        );

        return PlanetBlockStateFrame.resolve(
                field,
                targetPos
        ).map(targetFrame -> {
            Vec3 center =
                    Vec3.atCenterOf(targetPos);
            Vec3 worldFromCenter =
                    worldClickLocation.subtract(center);

            PlanetFrameVector localFromCenter =
                    targetFrame.frame().worldToLocal(
                            new PlanetFrameVector(
                                    worldFromCenter.x,
                                    worldFromCenter.y,
                                    worldFromCenter.z
                            )
                    );

            Vec3 localHitOffset =
                    new Vec3(
                            localFromCenter.x() + 0.5D,
                            localFromCenter.y() + 0.5D,
                            localFromCenter.z() + 0.5D
                    );

            return new PlanetBlockPlacementFrame(
                    targetFrame,
                    physicalClickedFace,
                    targetFrame.worldToLocal(
                            physicalClickedFace
                    ),
                    worldClickLocation,
                    localHitOffset
            );
        });
    }

    public BlockPos targetPos() {
        return targetStateFrame.pos();
    }

    public boolean localHitUpperHalf() {
        return localHitOffset.y > 0.5D;
    }
}
