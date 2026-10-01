package dev.planetary.world;

import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;
import java.util.Optional;

/**
 * Pure seam-aware resolution of one BlockState support-neighbor query.
 *
 * <p>The requested direction is interpreted in the source block's canonical
 * local BlockState frame. Physical adjacency is resolved through
 * {@link PlanetBlockFrameContext}, then the physical face of the support block
 * that points back to the source is converted into the support block's own
 * canonical {@link PlanetBlockStateFrame}.</p>
 *
 * <p>This intentionally does not depend on BlockState or SupportType. Runtime
 * adapters can pass {@link #supportLocalSideTowardSource()} directly to
 * vanilla isFaceSturdy/canSupportCenter logic while preserving the canonical
 * vanilla BlockState cache.</p>
 */
public record PlanetBlockSupportQuery(
        PlanetBlockStateFrame sourceStateFrame,
        Direction sourceLocalDirectionToSupport,
        Direction physicalDirectionToSupport,
        PlanetBlockStateFrame supportStateFrame,
        Direction supportLocalSideTowardSource,
        boolean crossedTraversalBoundary
) {
    public PlanetBlockSupportQuery {
        Objects.requireNonNull(
                sourceStateFrame,
                "sourceStateFrame"
        );
        Objects.requireNonNull(
                sourceLocalDirectionToSupport,
                "sourceLocalDirectionToSupport"
        );
        Objects.requireNonNull(
                physicalDirectionToSupport,
                "physicalDirectionToSupport"
        );
        Objects.requireNonNull(
                supportStateFrame,
                "supportStateFrame"
        );
        Objects.requireNonNull(
                supportLocalSideTowardSource,
                "supportLocalSideTowardSource"
        );

        if (sourceStateFrame.field()
                != supportStateFrame.field()) {
            throw new IllegalArgumentException(
                    "source/support frames use different gravity fields"
            );
        }

        BlockPos expectedSupport =
                sourceStateFrame.pos().relative(
                        physicalDirectionToSupport
                );
        if (!expectedSupport.equals(
                supportStateFrame.pos()
        )) {
            throw new IllegalArgumentException(
                    "physicalDirectionToSupport does not lead from "
                            + sourceStateFrame.pos()
                            + " to "
                            + supportStateFrame.pos()
            );
        }

        Direction physicalBackToSource =
                supportStateFrame.localToWorld(
                        supportLocalSideTowardSource
                );
        if (physicalBackToSource
                != physicalDirectionToSupport.getOpposite()) {
            throw new IllegalArgumentException(
                    "supportLocalSideTowardSource does not face source"
            );
        }
    }

    public static Optional<PlanetBlockSupportQuery> resolve(
            PlanetGravityField field,
            BlockPos sourcePos,
            Direction sourceLocalDirectionToSupport
    ) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(
                sourceLocalDirectionToSupport,
                "sourceLocalDirectionToSupport"
        );

        Optional<PlanetBlockStateFrame> sourceState =
                PlanetBlockStateFrame.resolve(
                        field,
                        sourcePos
                );
        if (sourceState.isEmpty()) {
            return Optional.empty();
        }

        PlanetBlockStateFrame source =
                sourceState.get();

        Optional<PlanetBlockFrameContext> traversal =
                PlanetBlockFrameContext.resolve(
                        field,
                        sourcePos,
                        source.face()
                );
        if (traversal.isEmpty()) {
            /*
             * The core has a canonical BlockState frame but no gravity/traversal
             * chart. A local support direction is therefore undefined there.
             */
            return Optional.empty();
        }

        PlanetBlockStep step =
                traversal.get().step(
                        sourceLocalDirectionToSupport
                );

        Optional<PlanetBlockStateFrame> supportState =
                PlanetBlockStateFrame.resolve(
                        field,
                        step.target().pos()
                );
        if (supportState.isEmpty()) {
            return Optional.empty();
        }

        PlanetBlockStateFrame support =
                supportState.get();

        Direction supportSide =
                support.worldToLocal(
                        step.physicalDirection()
                                .getOpposite()
                );

        return Optional.of(
                new PlanetBlockSupportQuery(
                        source,
                        sourceLocalDirectionToSupport,
                        step.physicalDirection(),
                        support,
                        supportSide,
                        step.crossedGravityBoundary()
                )
        );
    }

    public BlockPos sourcePos() {
        return sourceStateFrame.pos();
    }

    public BlockPos supportPos() {
        return supportStateFrame.pos();
    }
}
