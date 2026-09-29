package dev.planetary.gravity;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Gravity-frame helpers for vanilla systems that operate on blocks rather
 * than entities.
 */
public final class PlanetBlockGravity {
    private PlanetBlockGravity() {
    }

    public static Optional<PlanetGravityFrame> frameAt(
            Level level,
            BlockPos pos
    ) {
        return frameAt(level, pos, null);
    }

    public static Optional<PlanetGravityFrame> frameAt(
            Level level,
            BlockPos pos,
            PlanetFace preferred
    ) {
        return PlanetGravityRuntime.findAt(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        ).flatMap(
                field -> field.selectBlockFrame(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        preferred
                )
        );
    }

    public static Optional<PlanetGravityFrame> frameAt(
            Level level,
            double worldX,
            double worldY,
            double worldZ
    ) {
        return frameAt(
                level,
                worldX,
                worldY,
                worldZ,
                null
        );
    }

    public static Optional<PlanetGravityFrame> frameAt(
            Level level,
            double worldX,
            double worldY,
            double worldZ,
            PlanetFace preferred
    ) {
        return PlanetGravityRuntime.findAt(
                level,
                worldX,
                worldY,
                worldZ
        ).flatMap(
                field -> field.selectEntityFrame(
                        worldX,
                        worldY,
                        worldZ,
                        preferred,
                        0.0D
                )
        );
    }

    public static Direction localDown(
            Level level,
            BlockPos pos
    ) {
        return localDown(level, pos, null);
    }

    public static Direction localDown(
            Level level,
            BlockPos pos,
            PlanetFace preferred
    ) {
        return frameAt(level, pos, preferred)
                .map(frame -> toDirection(
                        frame.worldAxis(
                                PlanetDirection.DOWN
                        )
                ))
                .orElse(Direction.DOWN);
    }

    public static Direction localUp(
            Level level,
            BlockPos pos
    ) {
        return localUp(level, pos, null);
    }

    public static Direction localUp(
            Level level,
            BlockPos pos,
            PlanetFace preferred
    ) {
        return frameAt(level, pos, preferred)
                .map(frame -> toDirection(
                        frame.worldAxis(
                                PlanetDirection.UP
                        )
                ))
                .orElse(Direction.UP);
    }

    public static Direction toDirection(
            PlanetVector vector
    ) {
        if (vector.x() == 1) {
            return Direction.EAST;
        }
        if (vector.x() == -1) {
            return Direction.WEST;
        }
        if (vector.y() == 1) {
            return Direction.UP;
        }
        if (vector.y() == -1) {
            return Direction.DOWN;
        }
        if (vector.z() == 1) {
            return Direction.SOUTH;
        }
        if (vector.z() == -1) {
            return Direction.NORTH;
        }

        throw new IllegalArgumentException(
                "Expected unit axis vector, got " + vector
        );
    }
}
