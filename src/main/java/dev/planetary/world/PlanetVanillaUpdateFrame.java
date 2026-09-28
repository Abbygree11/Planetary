package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

/**
 * Converts a vanilla adjacent-block callback into the canonical virtual frame
 * of the block that is being updated.
 *
 * <p>This is essential at cube seams. Vanilla code is free to derive neighbor
 * positions with {@link BlockPos#relative(Direction)}, which leaves a position
 * in the source face's guard space. That position decodes to the correct
 * PlanetBlockPos, but continuing Direction arithmetic in that old virtual
 * frame would use the wrong face orientation.</p>
 *
 * <p>The canonical target position is therefore re-encoded on the target face
 * and the source is represented by a one-block alias adjacent to it. The
 * direction is consequently target-local as vanilla expects.</p>
 */
public final class PlanetVanillaUpdateFrame {
    private static final ThreadLocal<Integer> REENTRY_PERMITS = new ThreadLocal<>();

    private PlanetVanillaUpdateFrame() {
    }

    public static Optional<AdjacentFrame> resolve(
            Level level,
            BlockPos targetPos,
            BlockPos sourcePos
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(targetPos, "targetPos");
        Objects.requireNonNull(sourcePos, "sourcePos");

        Optional<PlanetLevelBridge.ResolvedPosition> target =
                PlanetLevelBridge.resolve(level, targetPos);
        if (target.isEmpty()) {
            return Optional.empty();
        }

        Optional<PlanetLevelBridge.ResolvedPosition> source =
                PlanetLevelBridge.resolve(level, sourcePos);
        if (source.isEmpty()) {
            return Optional.empty();
        }

        PlanetLevelBridge.ResolvedPosition resolvedTarget = target.get();
        PlanetLevelBridge.ResolvedPosition resolvedSource = source.get();

        if (resolvedTarget.world() != resolvedSource.world()) {
            return Optional.empty();
        }

        return between(
                resolvedTarget.world(),
                resolvedTarget.position(),
                resolvedSource.position()
        );
    }

    public static Optional<AdjacentFrame> between(
            PlanetWorldAccess world,
            PlanetBlockPos target,
            PlanetBlockPos source
    ) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");

        Direction directionToSource = null;
        for (Direction direction : Direction.values()) {
            if (world.relative(target, direction).equals(source)) {
                directionToSource = direction;
                break;
            }
        }

        if (directionToSource == null) {
            return Optional.empty();
        }

        BlockPos canonicalTarget = world.vanillaPosCodec().encode(target);
        BlockPos sourceAlias = canonicalTarget.relative(directionToSource);

        PlanetBlockPos decodedSource =
                world.vanillaPosCodec().decode(sourceAlias);
        if (!decodedSource.equals(source)) {
            throw new IllegalStateException(
                    "Canonical update frame does not preserve adjacency: "
                            + target + " -> " + source
            );
        }

        return Optional.of(new AdjacentFrame(
                world,
                target,
                source,
                directionToSource,
                canonicalTarget,
                sourceAlias
        ));
    }

    /**
     * The reframed callback has to re-enter the same vanilla method once.
     * A one-shot permit skips only that immediate mixin interception. It is
     * consumed at method entry, so nested neighbor updates produced by vanilla
     * during the callback are intercepted normally.
     */
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

    public static void runWithReentryPermit(Runnable action) {
        Objects.requireNonNull(action, "action");

        int before = permitCount();
        REENTRY_PERMITS.set(before + 1);
        try {
            action.run();
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

    public record AdjacentFrame(
            PlanetWorldAccess world,
            PlanetBlockPos target,
            PlanetBlockPos source,
            Direction directionToSource,
            BlockPos targetPos,
            BlockPos sourceAliasPos
    ) {
        public AdjacentFrame {
            Objects.requireNonNull(world, "world");
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(directionToSource, "directionToSource");
            Objects.requireNonNull(targetPos, "targetPos");
            Objects.requireNonNull(sourceAliasPos, "sourceAliasPos");
        }
    }
}
