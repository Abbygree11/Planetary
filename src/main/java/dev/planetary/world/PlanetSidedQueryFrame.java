package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Legacy virtual-atlas canonicalization for sided block queries.
 *
 * <p>At a planet seam, a mod commonly computes the target as
 * {@code source.relative(direction)} and passes {@code direction.getOpposite()}
 * as the side from which the target is queried. The target position then lives
 * in the source face's guard frame, so the raw opposite direction is not
 * necessarily the target block's local side.</p>
 *
 * <p>The dedicated physical Planet world no longer uses this class to decide
 * BlockState side semantics. Runtime physical queries go through
 * {@link PlanetBlockRuntime} / {@link PlanetBlockStateFrame}; this class is
 * retained for the old virtual-atlas compatibility path and its tests.</p>
 */
public final class PlanetSidedQueryFrame {
    private static final ThreadLocal<Integer> REENTRY_PERMITS =
            new ThreadLocal<>();

    private PlanetSidedQueryFrame() {
    }

    public static Optional<SidedFrame> resolve(
            Level level,
            BlockPos queriedPos,
            Direction queriedSide
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(queriedPos, "queriedPos");
        Objects.requireNonNull(queriedSide, "queriedSide");

        Optional<PlanetLevelBridge.ResolvedPosition> queried =
                PlanetLevelBridge.resolve(level, queriedPos);
        if (queried.isEmpty()) {
            return Optional.empty();
        }

        // A sided query describes the face pointing toward the caller. Walking
        // one block through that face reconstructs the caller/reference block
        // in the query's current vanilla frame.
        BlockPos referenceAlias = queriedPos.relative(queriedSide);
        Optional<PlanetLevelBridge.ResolvedPosition> reference =
                PlanetLevelBridge.resolve(level, referenceAlias);
        if (reference.isEmpty()) {
            return Optional.empty();
        }

        PlanetLevelBridge.ResolvedPosition target = queried.get();
        PlanetLevelBridge.ResolvedPosition source = reference.get();
        if (target.world() != source.world()) {
            return Optional.empty();
        }

        return PlanetVanillaUpdateFrame.between(
                target.world(),
                target.position(),
                source.position()
        ).map(frame -> new SidedFrame(
                frame.world(),
                frame.target(),
                frame.source(),
                frame.targetPos(),
                frame.directionToSource()
        ));
    }

    public static Optional<BlockPos> canonicalize(
            Level level,
            BlockPos pos
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");

        return PlanetLevelBridge.resolve(level, pos)
                .map(resolved ->
                        resolved.world()
                                .vanillaPosCodec()
                                .encode(resolved.position())
                );
    }

    public static boolean consumeReentryPermit() {
        Integer permits = REENTRY_PERMITS.get();
        if (permits == null || permits == 0) {
            return false;
        }

        if (permits == 1) {
            REENTRY_PERMITS.remove();
        } else {
            REENTRY_PERMITS.set(permits - 1);
        }
        return true;
    }

    public static <T> T callWithReentryPermit(Supplier<T> action) {
        Objects.requireNonNull(action, "action");

        int before = permitCount();
        REENTRY_PERMITS.set(before + 1);
        try {
            return action.get();
        } finally {
            int after = permitCount();
            if (after > before) {
                restorePermitCount(before);
            }
        }
    }

    private static int permitCount() {
        Integer permits = REENTRY_PERMITS.get();
        return permits == null ? 0 : permits;
    }

    private static void restorePermitCount(int permits) {
        if (permits == 0) {
            REENTRY_PERMITS.remove();
        } else {
            REENTRY_PERMITS.set(permits);
        }
    }

    public record SidedFrame(
            PlanetWorldAccess world,
            PlanetBlockPos target,
            PlanetBlockPos reference,
            BlockPos targetPos,
            Direction targetSide
    ) {
        public SidedFrame {
            Objects.requireNonNull(world, "world");
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(reference, "reference");
            Objects.requireNonNull(targetPos, "targetPos");
            Objects.requireNonNull(targetSide, "targetSide");
        }
    }
}
