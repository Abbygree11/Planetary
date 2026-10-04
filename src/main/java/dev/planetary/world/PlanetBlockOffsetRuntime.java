package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Canonical-local semantics for vanilla position-randomized block offsets.
 */
public final class PlanetBlockOffsetRuntime {
    private PlanetBlockOffsetRuntime() {
    }

    public static BlockPos canonicalSeedPos(
            BlockPos physicalPos,
            PlanetFace face
    ) {
        Objects.requireNonNull(physicalPos, "physicalPos");
        Objects.requireNonNull(face, "face");

        PlanetFrameVector local =
                new PlanetGravityFrame(face)
                        .worldToLocal(
                                new PlanetFrameVector(
                                        physicalPos.getX(),
                                        physicalPos.getY(),
                                        physicalPos.getZ()
                                )
                        );

        return new BlockPos(
                (int) Math.round(local.x()),
                (int) Math.round(local.y()),
                (int) Math.round(local.z())
        );
    }

    public static Vec3 canonicalOffset(
            BlockState state,
            BlockGetter getter,
            BlockPos physicalPos,
            PlanetFace face
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(getter, "getter");

        return state.getOffset(
                getter,
                canonicalSeedPos(
                        physicalPos,
                        face
                )
        );
    }
}
