package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.Optional;

/**
 * Canonical local frame of one physical BlockPos for BlockState semantics.
 *
 * <p>Unlike {@link PlanetBlockFrameContext}, this frame is NOT a traversal
 * chart. It depends only on the physical position, so the same block has one
 * stable orientation for state properties, shapes and support queries no
 * matter which path or neighboring gravity chart reached it.</p>
 */
public final class PlanetBlockStateFrame {
    private final PlanetGravityField field;
    private final BlockPos pos;
    private final PlanetGravityFrame frame;

    private PlanetBlockStateFrame(
            PlanetGravityField field,
            BlockPos pos,
            PlanetFace face
    ) {
        this.field = Objects.requireNonNull(field, "field");
        this.pos = Objects.requireNonNull(pos, "pos").immutable();
        this.frame = new PlanetGravityFrame(
                Objects.requireNonNull(face, "face")
        );
    }

    public static Optional<PlanetBlockStateFrame> resolve(
            PlanetGravityField field,
            BlockPos pos
    ) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(pos, "pos");

        return field.selectCanonicalBlockFace(
                pos.getX(),
                pos.getY(),
                pos.getZ()
        ).map(face ->
                new PlanetBlockStateFrame(
                        field,
                        pos,
                        face
                )
        );
    }

    public PlanetGravityField field() {
        return field;
    }

    public BlockPos pos() {
        return pos;
    }

    public PlanetGravityFrame frame() {
        return frame;
    }

    public PlanetFace face() {
        return frame.face();
    }

    public Direction localToWorld(
            Direction localDirection
    ) {
        return PlanetVanillaDirection.localToWorld(
                frame,
                Objects.requireNonNull(
                        localDirection,
                        "localDirection"
                )
        );
    }

    public Direction worldToLocal(
            Direction worldDirection
    ) {
        return PlanetVanillaDirection.worldToLocal(
                frame,
                Objects.requireNonNull(
                        worldDirection,
                        "worldDirection"
                )
        );
    }

    public Direction.Axis localAxisToWorld(
            Direction.Axis localAxis
    ) {
        return PlanetVanillaDirection.localAxisToWorld(
                frame,
                Objects.requireNonNull(
                        localAxis,
                        "localAxis"
                )
        );
    }

    public Direction.Axis worldAxisToLocal(
            Direction.Axis worldAxis
    ) {
        return PlanetVanillaDirection.worldAxisToLocal(
                frame,
                Objects.requireNonNull(
                        worldAxis,
                        "worldAxis"
                )
        );
    }

    public VoxelShape rotateShape(
            VoxelShape localShape
    ) {
        return PlanetVoxelShapeRotation.localToWorld(
                Objects.requireNonNull(
                        localShape,
                        "localShape"
                ),
                face()
        );
    }
}
