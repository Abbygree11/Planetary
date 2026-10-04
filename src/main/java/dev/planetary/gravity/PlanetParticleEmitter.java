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
 * converts emitter offsets whose meaning is local to a block.</p>
 */
public final class PlanetParticleEmitter {
    private static final double STANDING_TORCH_UP = 0.2D;

    /**
     * Vanilla WallTorchBlock starts from y + 0.7 and then adds another +0.22.
     * Relative to the block center (y + 0.5), the true local-UP offset is 0.42.
     */
    private static final double WALL_TORCH_UP = 0.42D;
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
     * wall, and 0.42 blocks above the block center.
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

    /**
     * Reinterprets coordinates already calculated by a vanilla block emitter as
     * LOCAL block-frame offsets.
     *
     * <p>This is intentionally based on the final vanilla coordinates rather
     * than duplicating their constants/random sampling. For example, wall
     * torches already include y+0.7 and +0.22, while redstone torches also add
     * random +/-0.1 jitter. We preserve those exact sampled values and only
     * rotate the resulting offset into the active physical frame.</p>
     */
    public static Vec3 transformVanillaLocalEmitter(
            BlockPos pos,
            PlanetGravityFrame frame,
            double vanillaX,
            double vanillaY,
            double vanillaZ
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(frame, "frame");

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        return localOffsetFromCenter(
                pos,
                frame,
                vanillaX - centerX,
                vanillaY - centerY,
                vanillaZ - centerZ
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
