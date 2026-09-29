package dev.planetary.gravity;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Minimal land-node evaluator whose horizontal plane follows Planet local
 * gravity instead of global X/Z.
 *
 * <p>This first version deliberately supports the geometry needed by ordinary
 * land mobs on cube faces: four tangent neighbors, one-block step-up/down and
 * edge crossing. Vanilla's richer hazard/door/rail semantics can be layered
 * on after the local geometry is validated in game.</p>
 */
public final class PlanetWalkNodeEvaluator extends WalkNodeEvaluator {
    private static final PlanetDirection[] HORIZONTAL = {
            PlanetDirection.NORTH,
            PlanetDirection.SOUTH,
            PlanetDirection.WEST,
            PlanetDirection.EAST
    };

    @Override
    public Node getStart() {
        BlockPos start = this.mob.blockPosition();

        Node node = accepted(start);
        if (node != null) {
            return node;
        }

        Direction up = PlanetBlockGravity.localUp(
                this.mob.level(),
                start,
                preferredFace()
        );
        node = accepted(start.relative(up));
        if (node != null) {
            return node;
        }

        Direction down = PlanetBlockGravity.localDown(
                this.mob.level(),
                start,
                preferredFace()
        );
        node = accepted(start.relative(down));
        if (node != null) {
            return node;
        }

        Node fallback = this.getNode(start);
        fallback.type = pathType(start);
        fallback.costMalus =
                this.mob.getPathfindingMalus(fallback.type);
        return fallback;
    }

    @Override
    public Target getTarget(
            double x,
            double y,
            double z
    ) {
        BlockPos requested =
                BlockPos.containing(x, y, z);

        BlockPos target = nearestWalkable(requested);
        return this.getTargetNodeAt(
                target.getX(),
                target.getY(),
                target.getZ()
        );
    }

    @Override
    public int getNeighbors(
            Node[] output,
            Node node
    ) {
        int count = 0;
        BlockPos origin =
                new BlockPos(node.x, node.y, node.z);

        Optional<PlanetGravityFrame> frame =
                PlanetBlockGravity.frameAt(
                        this.mob.level(),
                        origin,
                        preferredFace()
                );

        if (frame.isEmpty()) {
            return super.getNeighbors(output, node);
        }

        for (PlanetDirection localDirection : HORIZONTAL) {
            Direction worldDirection =
                    PlanetBlockGravity.toDirection(
                            frame.get().worldAxis(localDirection)
                    );

            Node next = tangentNeighbor(
                    origin.relative(worldDirection)
            );

            if (next != null
                    && !next.closed
                    && count < output.length) {
                output[count++] = next;
            }
        }

        return count;
    }

    @Override
    public PathType getPathTypeOfMob(
            PathfindingContext context,
            int x,
            int y,
            int z,
            Mob mob
    ) {
        return pathType(new BlockPos(x, y, z));
    }

    @Override
    public PathType getPathType(
            PathfindingContext context,
            int x,
            int y,
            int z
    ) {
        return pathType(new BlockPos(x, y, z));
    }

    @Nullable
    private Node tangentNeighbor(
            BlockPos candidate
    ) {
        Node same = accepted(candidate);
        if (same != null) {
            return same;
        }

        if (this.mob.maxUpStep() >= 1.0F) {
            Direction up =
                    PlanetBlockGravity.localUp(
                            this.mob.level(),
                            candidate,
                            preferredFace()
                    );

            Node steppedUp =
                    accepted(candidate.relative(up));
            if (steppedUp != null) {
                return steppedUp;
            }
        }

        Direction down =
                PlanetBlockGravity.localDown(
                        this.mob.level(),
                        candidate,
                        preferredFace()
                );

        BlockPos lower = candidate.relative(down);
        if (this.mob.getMaxFallDistance() >= 1) {
            Node steppedDown = accepted(lower);
            if (steppedDown != null) {
                return steppedDown;
            }
        }

        return null;
    }

    private BlockPos nearestWalkable(
            BlockPos requested
    ) {
        if (accepted(requested) != null) {
            return requested;
        }

        Direction up =
                PlanetBlockGravity.localUp(
                        this.mob.level(),
                        requested,
                        preferredFace()
                );
        BlockPos above = requested.relative(up);
        if (accepted(above) != null) {
            return above;
        }

        Direction down =
                PlanetBlockGravity.localDown(
                        this.mob.level(),
                        requested,
                        preferredFace()
                );
        BlockPos below = requested.relative(down);
        if (accepted(below) != null) {
            return below;
        }

        return requested;
    }

    @Nullable
    private Node accepted(
            BlockPos pos
    ) {
        PathType type = pathType(pos);
        float malus =
                this.mob.getPathfindingMalus(type);

        if (!isAcceptedType(type) || malus < 0.0F) {
            return null;
        }

        Node node = this.getNode(pos);
        node.type = type;
        node.costMalus =
                Math.max(node.costMalus, malus);
        return node;
    }

    private boolean isAcceptedType(
            PathType type
    ) {
        if (type == PathType.WALKABLE
                || type == PathType.WALKABLE_DOOR) {
            return true;
        }

        return this.canFloat()
                && type == PathType.WATER;
    }

    private PlanetFace preferredFace() {
        return ((PlanetGravityEntity) this.mob)
                .planetary$gravityFace()
                .orElse(null);
    }

    private PathType pathType(
            BlockPos pos
    ) {
        PathType occupied =
                getPathTypeFromState(
                        this.currentContext.level(),
                        pos
                );

        if (occupied != PathType.OPEN) {
            if (occupied == PathType.DOOR_OPEN
                    && this.canPassDoors()) {
                return PathType.WALKABLE_DOOR;
            }
            return occupied;
        }

        Direction down =
                PlanetBlockGravity.localDown(
                        this.mob.level(),
                        pos,
                        preferredFace()
                );
        BlockPos supportPos =
                pos.relative(down);

        BlockState support =
                this.currentContext.getBlockState(
                        supportPos
                );

        if (!support.getCollisionShape(
                this.currentContext.level(),
                supportPos
        ).isEmpty()) {
            return PathType.WALKABLE;
        }

        if (this.canFloat()
                && !support.getFluidState().isEmpty()) {
            return PathType.WATER;
        }

        return PathType.OPEN;
    }
}
