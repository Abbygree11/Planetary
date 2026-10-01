package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;

/**
 * One physical block step interpreted in a Planet local block frame.
 *
 * @param source source block/frame context
 * @param localDirection direction requested by vanilla/local block logic
 * @param physicalDirection actual ordinary-world axis crossed by this step
 * @param target target block/frame context
 * @param transportedDirection requested direction parallel-transported into
 *                             the target local frame for continued traversal
 * @param crossedGravityBoundary whether the local frame changed
 */
public record PlanetBlockStep(
        PlanetBlockFrameContext source,
        Direction localDirection,
        Direction physicalDirection,
        PlanetBlockFrameContext target,
        Direction transportedDirection,
        boolean crossedGravityBoundary
) {
    public PlanetBlockStep {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(localDirection, "localDirection");
        Objects.requireNonNull(physicalDirection, "physicalDirection");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(
                transportedDirection,
                "transportedDirection"
        );

        BlockPos expected =
                source.pos().relative(physicalDirection);
        if (!expected.equals(target.pos())) {
            throw new IllegalArgumentException(
                    "physicalDirection does not lead from "
                            + source.pos()
                            + " to "
                            + target.pos()
                            + ": "
                            + physicalDirection
            );
        }
    }

    /**
     * Local side of the target block that physically faces back toward the
     * source block. This is the side to use for target-side support,
     * capability and survival queries after the physical neighbor step.
     */
    public Direction targetLocalSideTowardSource() {
        return target.worldToLocal(
                physicalDirection.getOpposite()
        );
    }
}
