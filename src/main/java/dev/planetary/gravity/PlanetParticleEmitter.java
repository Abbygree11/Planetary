package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Pure local-frame particle emitter geometry for block-bound visual effects.
 *
 * <p>Particle motion stays in physical world coordinates. This helper only
 * converts emitter offsets whose meaning is local to a block, such as
 * "0.2 above a standing torch" or "0.27 toward the wall and 0.22 up".</p>
 */
public final class PlanetParticleEmitter {
    private static final double STANDING_TORCH_UP = 0.2D;
    private static final double WALL_TORCH_UP = 0.22D;
    private static final double WALL_TORCH_SUPPORT = 0.27D;

    private PlanetParticleEmitter() {
    }

    public static Vec3 standingTorch(
            BlockPos pos,
            PlanetGravityFrame frame
    ) {
        return localOffsetFromCenter(
                pos,
                frame,
                0.0D,
                STANDING_TORCH_UP,
                0.0D
        );
    }

    /**
     * WallTorchBlock.FACING is canonical LOCAL orientation. Vanilla places its
     * flame/smoke toward FACING.opposite(), i.e. back toward the supporting
     * wall, while also offsetting upward.
     */
    public static Vec3 wallTorch(
            BlockPos pos,
            PlanetGravityFrame frame,
            Direction localFacing
    ) {
        Objects.requireNonNull(localFacing, "localFacing");

        if (localFacing.getAxis().isVertical()) {
            throw new IllegalArgumentException(
                    "Wall torch facing must be horizontal: "
                            + localFacing
            );
        }

        Direction towardSupport =
                localFacing.getOpposite();

        return localOffsetFromCenter(
                pos,
                frame,
                towardSupport.getStepX()
                        * WALL_TORCH_SUPPORT,
                WALL_TORCH_UP,
                towardSupport.getStepZ()
                        * WALL_TORCH_SUPPORT
        );
    }

    public static Vec3 localOffsetFromCenter(
            BlockPos pos,
            PlanetGravityFrame frame,
            double localX,
            double localY,
            double localZ
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector worldOffset =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localX,
                                localY,
                                localZ
                        )
                );

        return Vec3.atCenterOf(pos).add(
                worldOffset.x(),
                worldOffset.y(),
                worldOffset.z()
        );
    }
}
