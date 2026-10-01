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
     * Side of the target block that physically faces the source, expressed in
     * the target TRAVERSAL chart.
     *
     * <p>Do not use this as a canonical BlockState support/capability side on
     * an exact edge or corner. Those queries must reframe the physical side
     * through {@link PlanetBlockStateFrame} because traversal charts are
     * path-dependent.</p>
     */
    public Direction targetTraversalSideTowardSource() {
        return target.worldToLocal(
                physicalDirection.getOpposite()
        );
    }
}
