package dev.planetary.gravity;

import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Geometry helpers for LocalPlayer code that vanilla hard-codes to world XZ.
 */
public final class PlanetLocalPlayerGeometry {
    private PlanetLocalPlayerGeometry() {
    }

    public static Vec3 samplePoint(
            Vec3 playerPosition,
            double localXOffset,
            double localZOffset,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(playerPosition, "playerPosition");
        Objects.requireNonNull(frame, "frame");

        PlanetFrameVector world =
                frame.localToWorld(
                        new PlanetFrameVector(
                                localXOffset,
                                0.0D,
                                localZOffset
                        )
                );

        return playerPosition.add(
                world.x(),
                world.y(),
                world.z()
        );
    }

    /**
     * Builds the one-block local XZ column used by LocalPlayer.suffocatesAt,
     * while preserving the current player's extent along local Y.
     */
    public static AABB suffocationColumn(
            AABB playerBox,
            BlockPos blockPos,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(playerBox, "playerBox");
        Objects.requireNonNull(blockPos, "blockPos");
        Objects.requireNonNull(frame, "frame");

        AABB blockBox = new AABB(blockPos);
        PlanetVector up = frame.worldUp();

        int verticalX = Math.abs(up.x());
        int verticalY = Math.abs(up.y());
        int verticalZ = Math.abs(up.z());

        int floorX = 1 - verticalX;
        int floorY = 1 - verticalY;
        int floorZ = 1 - verticalZ;

        return new AABB(
                verticalX * playerBox.minX
                        + floorX * blockBox.minX,
                verticalY * playerBox.minY
                        + floorY * blockBox.minY,
                verticalZ * playerBox.minZ
                        + floorZ * blockBox.minZ,
                verticalX * playerBox.maxX
                        + floorX * blockBox.maxX,
                verticalY * playerBox.maxY
                        + floorY * blockBox.maxY,
                verticalZ * playerBox.maxZ
                        + floorZ * blockBox.maxZ
        );
    }
}
