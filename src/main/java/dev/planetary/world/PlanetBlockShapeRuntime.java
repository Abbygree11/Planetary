package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

/**
 * Shared runtime boundary for canonical-local BlockState VoxelShapes.
 *
 * <p>Vanilla shape methods can call one another. A thread-local query depth
 * ensures only the outermost physical shape query is rotated exactly once.
 * Canonical-only queries (support/occlusion for now) enter the same scope but
 * deliberately do not rotate their final result.</p>
 */
public final class PlanetBlockShapeRuntime {
    private static final ThreadLocal<Integer> QUERY_DEPTH =
            ThreadLocal.withInitial(() -> 0);

    private PlanetBlockShapeRuntime() {
    }

    public static void enterQuery() {
        QUERY_DEPTH.set(
                QUERY_DEPTH.get() + 1
        );
    }

    public static VoxelShape finishPhysical(
            BlockGetter getter,
            BlockPos pos,
            VoxelShape canonicalShape
    ) {
        Objects.requireNonNull(getter, "getter");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(
                canonicalShape,
                "canonicalShape"
        );

        boolean outermost =
                leaveQuery();

        if (!outermost
                || !(getter instanceof Level level)) {
            return canonicalShape;
        }

        return PlanetBlockRuntime.stateFrameAt(
                level,
                pos
        ).map(frame ->
                frame.rotateShape(
                        canonicalShape
                )
        ).orElse(canonicalShape);
    }

    public static VoxelShape finishCanonical(
            VoxelShape canonicalShape
    ) {
        Objects.requireNonNull(
                canonicalShape,
                "canonicalShape"
        );

        leaveQuery();
        return canonicalShape;
    }

    static int queryDepthForTests() {
        return QUERY_DEPTH.get();
    }

    private static boolean leaveQuery() {
        int depth = QUERY_DEPTH.get();

        if (depth <= 0) {
            throw new IllegalStateException(
                    "Planet block shape query scope underflow"
            );
        }

        int next = depth - 1;
        if (next == 0) {
            QUERY_DEPTH.remove();
            return true;
        }

        QUERY_DEPTH.set(next);
        return false;
    }
}
