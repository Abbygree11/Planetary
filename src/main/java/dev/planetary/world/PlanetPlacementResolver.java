package dev.planetary.world;

import dev.planetary.api.PlanetCoordinateContext;
import dev.planetary.api.PlanetPlacementIntent;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Read-only bridge between author provenance, PHYSICAL hit/target geometry,
 * and the target block's canonical Planet BlockState semantics.
 *
 * <p>It does not write BlockStates, call vanilla/NeoForge placement hooks,
 * reinterpret foreign coordinates, or infer a traversal chart from a hit.</p>
 */
public final class PlanetPlacementResolver {
    private PlanetPlacementResolver() {
    }

    /** Runtime entry point. In an ordinary non-Planet level this is pass-through. */
    public static Resolution resolve(PlanetPlacementIntent intent) {
        return resolve(
                intent,
                physical -> PlanetBlockRuntime.fieldAt(
                        physical.level(),
                        physical.pos()
                )
        );
    }

    /**
     * Explicit field lookup seam for pure tests and opt-in integrations.
     * The lookup is NEVER called for foreign coordinates.
     */
    public static Resolution resolve(
            PlanetPlacementIntent intent,
            Function<PlanetCoordinateContext.PhysicalBlock,
                    Optional<PlanetGravityField>> fieldLookup
    ) {
        Objects.requireNonNull(intent, "intent");
        Objects.requireNonNull(fieldLookup, "fieldLookup");

        if (!(intent.hitBlock() instanceof PlanetCoordinateContext.PhysicalBlock hit)
                || !(intent.targetBlock() instanceof PlanetCoordinateContext.PhysicalBlock target)) {
            return new Resolution(intent, Optional.empty(), Optional.empty());
        }

        Optional<PlanetBlockStateFrame> sourceFrame =
                Objects.requireNonNull(fieldLookup.apply(hit), "hit field lookup")
                        .flatMap(field -> PlanetBlockStateFrame.resolve(field, hit.pos()));

        Optional<PlanetBlockPlacementFrame> targetFrame =
                Objects.requireNonNull(fieldLookup.apply(target), "target field lookup")
                        .flatMap(field -> PlanetBlockPlacementFrame.resolve(
                                field,
                                target.pos(),
                                intent.hitFace(),
                                intent.hitLocation()
                        ));

        return new Resolution(intent, sourceFrame, targetFrame);
    }

    /**
     * A missing target frame means PHYSICAL vanilla semantics, not identity
     * transformation or permission to reinterpret unknown mod directions.
     */
    public record Resolution(
            PlanetPlacementIntent intent,
            Optional<PlanetBlockStateFrame> hitStateFrame,
            Optional<PlanetBlockPlacementFrame> targetPlacementFrame
    ) {
        public Resolution {
            Objects.requireNonNull(intent, "intent");
            Objects.requireNonNull(hitStateFrame, "hitStateFrame");
            Objects.requireNonNull(targetPlacementFrame, "targetPlacementFrame");
        }

        public boolean isPlanetTarget() {
            return targetPlacementFrame.isPresent();
        }

        public Optional<Direction> targetLocalHitFace() {
            return targetPlacementFrame.map(PlanetBlockPlacementFrame::localClickedFace);
        }

        public Optional<Vec3> targetLocalHitOffset() {
            return targetPlacementFrame.map(PlanetBlockPlacementFrame::localHitOffset);
        }

        /**
         * Only an explicitly supplied player BODY frame can be converted.
         * Player-less sources have no inferred nearest-looking direction.
         */
        public Optional<Direction> playerBodyToTargetLocal(Direction bodyLocal) {
            Objects.requireNonNull(bodyLocal, "bodyLocal");
            if (targetPlacementFrame.isEmpty()) {
                return Optional.empty();
            }
            return intent.actorBodyFrame().map(body -> {
                Direction physical = PlanetVanillaDirection.localToWorld(body, bodyLocal);
                return targetPlacementFrame.get().targetStateFrame().worldToLocal(physical);
            });
        }
    }
}
