package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetTopology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Position-aware local block frame for the physical cubic Planet world.
 *
 * <p>This is deliberately different from the older virtual face-atlas
 * topology. A physical edge block is one ordinary BlockPos. When traversal
 * enters that shared edge block, the local frame is already transported to
 * the adjacent gravity face. Continued traversal therefore bends around the
 * cube using ordinary one-block physical neighbors without alias cells.</p>
 */
public final class PlanetBlockFrameContext {
    private final PlanetGravityField field;
    private final BlockPos pos;
    private final PlanetGravityFrame frame;

    private PlanetBlockFrameContext(
            PlanetGravityField field,
            BlockPos pos,
            PlanetFace face
    ) {
        this.field = Objects.requireNonNull(field, "field");
        this.pos = Objects.requireNonNull(pos, "pos");
        this.frame = new PlanetGravityFrame(
                Objects.requireNonNull(face, "face")
        );
    }

    /**
     * Resolves a frame at a physical block. On an exact gravity boundary the
     * preferred face wins when it is one of the valid candidates.
     */
    public static Optional<PlanetBlockFrameContext> resolve(
            PlanetGravityField field,
            BlockPos pos,
            PlanetFace preferredFace
    ) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(pos, "pos");

        return field.selectBlockFace(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                preferredFace
        ).map(face -> new PlanetBlockFrameContext(
                field,
                pos.immutable(),
                face
        ));
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

    /**
     * Rotates a canonical local-Y-up block shape into this physical frame.
     */
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

    /**
     * Takes one local block step in the physical world.
     *
     * <p>Horizontal traversal rotates at a gravity seam. Entering a shared
     * edge block transports the target frame immediately. If the source
     * context itself is already on that seam, the physical step is folded
     * around the edge into the adjacent face.</p>
     */
    public PlanetBlockStep step(
            Direction localDirection
    ) {
        Objects.requireNonNull(
                localDirection,
                "localDirection"
        );

        PlanetDirection local =
                PlanetVanillaDirection.fromVanilla(
                        localDirection
                );

        if (local.isHorizontal()) {
            return stepHorizontal(
                    localDirection,
                    local
            );
        }

        return stepVertical(
                localDirection
        );
    }

    private PlanetBlockStep stepHorizontal(
            Direction vanillaLocal,
            PlanetDirection local
    ) {
        PlanetFace sourceFace = face();
        FaceTransform transform =
                PlanetTopology.edgeTransform(
                        sourceFace,
                        local
                );
        PlanetFace adjacentFace =
                transform.targetFace();

        Set<PlanetFace> sourceCandidates =
                field.candidateFaces(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()
                );

        Direction transported =
                PlanetVanillaDirection.toVanilla(
                        transform.transformDirection(local)
                );

        /*
         * We are already standing on the seam in the source chart. Advancing
         * "outward" must fold immediately around the cube rather than moving
         * into the empty radial shell.
         */
        if (sourceCandidates.contains(adjacentFace)) {
            PlanetBlockFrameContext targetFrame =
                    explicitTargetContext(
                            pos,
                            adjacentFace
                    );
            Direction physical =
                    targetFrame.localToWorld(
                            transported
                    );
            BlockPos targetPos =
                    pos.relative(physical);

            return new PlanetBlockStep(
                    this,
                    vanillaLocal,
                    physical,
                    explicitTargetContext(
                            targetPos,
                            adjacentFace
                    ),
                    transported,
                    true
            );
        }

        Direction physical =
                localToWorld(vanillaLocal);
        BlockPos rawTarget =
                pos.relative(physical);

        Set<PlanetFace> targetCandidates =
                field.candidateFaces(
                        rawTarget.getX(),
                        rawTarget.getY(),
                        rawTarget.getZ()
                );

        /*
         * Entering the shared seam block transports the chart now. This makes
         * the next local step continue around the adjacent face instead of
         * requiring a duplicate/alias boundary cell.
         */
        if (targetCandidates.contains(adjacentFace)) {
            return new PlanetBlockStep(
                    this,
                    vanillaLocal,
                    physical,
                    explicitTargetContext(
                            rawTarget,
                            adjacentFace
                    ),
                    transported,
                    true
            );
        }

        PlanetFace targetFace =
                field.selectBlockFace(
                        rawTarget.getX(),
                        rawTarget.getY(),
                        rawTarget.getZ(),
                        sourceFace
                ).orElse(sourceFace);

        if (targetFace != sourceFace) {
            throw new IllegalStateException(
                    "Horizontal step changed from "
                            + sourceFace
                            + " to unexpected face "
                            + targetFace
                            + " while moving "
                            + vanillaLocal
                            + " from "
                            + pos
            );
        }

        return new PlanetBlockStep(
                this,
                vanillaLocal,
                physical,
                explicitTargetContext(
                        rawTarget,
                        sourceFace
                ),
                vanillaLocal,
                false
        );
    }

    private PlanetBlockStep stepVertical(
            Direction localDirection
    ) {
        Direction physical =
                localToWorld(localDirection);
        BlockPos targetPos =
                pos.relative(physical);

        PlanetFace targetFace =
                field.selectBlockFace(
                        targetPos.getX(),
                        targetPos.getY(),
                        targetPos.getZ(),
                        face()
                ).orElse(face());

        return new PlanetBlockStep(
                this,
                localDirection,
                physical,
                explicitTargetContext(
                        targetPos,
                        targetFace
                ),
                localDirection,
                targetFace != face()
        );
    }

    private PlanetBlockFrameContext explicitTargetContext(
            BlockPos targetPos,
            PlanetFace targetFace
    ) {
        if (!field.core().isCoreBlock(
                targetPos.getX(),
                targetPos.getY(),
                targetPos.getZ()
        )) {
            Set<PlanetFace> candidates =
                    field.candidateFaces(
                            targetPos.getX(),
                            targetPos.getY(),
                            targetPos.getZ()
                    );
            if (!candidates.contains(targetFace)) {
                throw new IllegalStateException(
                        "Target face "
                                + targetFace
                                + " is not valid at "
                                + targetPos
                                + "; candidates="
                                + candidates
                );
            }
        }

        return new PlanetBlockFrameContext(
                field,
                targetPos.immutable(),
                targetFace
        );
    }
}
