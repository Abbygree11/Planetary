package dev.planetary.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Spawn geometry for FallingBlockEntity in an arbitrary local gravity frame.
 *
 * <p>Vanilla anchors a falling block at the center of the block's world-DOWN
 * face: (x + 0.5, y, z + 0.5). On a rotated Planet face that leaves the entity
 * half a block off-center along a local tangent axis. The correct generalized
 * anchor is the center of the source block's LOCAL-DOWN face.</p>
 */
public final class PlanetFallingBlockSpawn {
    private PlanetFallingBlockSpawn() {
    }

    public static Vec3 anchor(
            BlockPos sourcePos,
            Direction physicalLocalDown
    ) {
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(
                physicalLocalDown,
                "physicalLocalDown"
        );

        Vec3 center =
                Vec3.atCenterOf(sourcePos);

        return center.add(
                physicalLocalDown.getStepX() * 0.5D,
                physicalLocalDown.getStepY() * 0.5D,
                physicalLocalDown.getStepZ() * 0.5D
        );
    }
}
