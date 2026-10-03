package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

/**
 * Local-frame topology used by natural spreading blocks such as grass.
 *
 * <p>Vanilla picks one Cartesian offset (x,y,z). A curved cube surface has no
 * order-independent equivalent once that displacement crosses a gravity seam.
 * For growth only, Planetary defines an explicit ordered path:</p>
 *
 * <ol>
 *     <li>local vertical Y (UP/DOWN),</li>
 *     <li>local Z (NORTH/SOUTH),</li>
 *     <li>local X (WEST/EAST).</li>
 * </ol>
 *
 * <p>Every segment uses the current transported traversal chart. This is a
 * subsystem policy, not a generic replacement for BlockPos.offset.</p>
 */
public final class PlanetGrowthTopology {
    private PlanetGrowthTopology() {
    }

    public static Optional<BlockPos> spreadTarget(
            Level level,
            BlockPos sourcePos,
            int localX,
            int localY,
            int localZ
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(sourcePos, "sourcePos");

        return PlanetBlockRuntime.traversalAt(
                level,
                sourcePos
        ).map(context -> {
            PlanetBlockFrameContext current =
                    context;

            current = walkSegment(
                    current,
                    localY < 0
                            ? Direction.DOWN
                            : Direction.UP,
                    Math.abs(localY)
            );
            current = walkSegment(
                    current,
                    localZ < 0
                            ? Direction.NORTH
                            : Direction.SOUTH,
                    Math.abs(localZ)
            );
            current = walkSegment(
                    current,
                    localX < 0
                            ? Direction.WEST
                            : Direction.EAST,
                    Math.abs(localX)
            );

            return current.pos();
        });
    }

    private static PlanetBlockFrameContext walkSegment(
            PlanetBlockFrameContext start,
            Direction direction,
            int steps
    ) {
        PlanetBlockFrameContext current =
                start;
        Direction transported =
                direction;

        for (int i = 0; i < steps; i++) {
            PlanetBlockStep step =
                    current.step(
                            transported
                    );
            current =
                    step.target();
            transported =
                    step.transportedDirection();
        }

        return current;
    }
}
