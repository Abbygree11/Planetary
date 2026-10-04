package dev.planetary.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Render-space geometry for a FallingBlockEntity whose Entity.position is the
 * center of its local-DOWN face.
 */
public final class PlanetFallingBlockRender {
    private PlanetFallingBlockRender() {
    }

    public static Vec3 cellCenter(
            Vec3 entityAnchor,
            Direction physicalLocalDown
    ) {
        Objects.requireNonNull(entityAnchor, "entityAnchor");
        Objects.requireNonNull(
                physicalLocalDown,
                "physicalLocalDown"
        );

        return entityAnchor.add(
                physicalLocalDown.getStepX() * -0.5D,
                physicalLocalDown.getStepY() * -0.5D,
                physicalLocalDown.getStepZ() * -0.5D
        );
    }

    public static BlockPos renderBlockPos(
            Vec3 entityAnchor,
            Direction physicalLocalDown
    ) {
        Vec3 center =
                cellCenter(
                        entityAnchor,
                        physicalLocalDown
                );

        return BlockPos.containing(
                center.x,
                center.y,
                center.z
        );
    }

    public static Vec3 modelTranslation(
            Direction physicalLocalDown
    ) {
        Objects.requireNonNull(
                physicalLocalDown,
                "physicalLocalDown"
        );

        return new Vec3(
                -0.5D
                        - physicalLocalDown.getStepX()
                        * 0.5D,
                -0.5D
                        - physicalLocalDown.getStepY()
                        * 0.5D,
                -0.5D
                        - physicalLocalDown.getStepZ()
                        * 0.5D
        );
    }
}
