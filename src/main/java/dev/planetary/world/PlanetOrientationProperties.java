package dev.planetary.world;

import dev.planetary.topology.PlanetFrameVector;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

/**
 * Explicitly opt-in, target-canonical BlockState property semantics.
 *
 * <p>Physical positions, hit normals and world headings remain unchanged.
 * These methods return EMPTY outside an active target Planet frame or if a
 * source-specific orientation cannot be represented by the target property.
 * They NEVER rewrite generic Minecraft/NeoForge BlockState methods, mod
 * properties, physical adjacency, structures or world placement callbacks.</p>
 */
public final class PlanetOrientationProperties {
    private static final double MIN_TANGENT_LENGTH_SQUARED = 1.0E-12D;

    private PlanetOrientationProperties() {
    }

    /**
     * ROTATION_16 is a tangent-plane angle, not a Direction. Different owners
     * choose different zero orientations: sign/banner initial state uses
     * actor yaw+180; skull uses actor yaw. The caller must select its OWNER'S
     * reference orientation, not a global assumption about ROTATION_16.
     */
    public enum Rotation16Zero {
        NORTH_CLOCKWISE,
        SOUTH_CLOCKWISE
    }

    public static Optional<Direction> facing(
            PlanetPlacementResolver.Resolution resolution,
            Direction physicalDirection
    ) {
        Objects.requireNonNull(resolution, "resolution");
        Objects.requireNonNull(physicalDirection, "physicalDirection");
        return resolution.targetPlacementFrame()
                .map(frame -> frame.targetStateFrame()
                        .worldToLocal(physicalDirection));
    }

    /** Only a genuine local horizontal result can populate a 4-way property. */
    public static Optional<Direction> horizontalFacing(
            PlanetPlacementResolver.Resolution resolution,
            Direction physicalDirection
    ) {
        return facing(resolution, physicalDirection)
                .filter(dir -> dir.getAxis().isHorizontal());
    }

    /** Physical X/Y/Z axis -> canonical target BlockState X/Y/Z axis. */
    public static Optional<Direction.Axis> axis(
            PlanetPlacementResolver.Resolution resolution,
            Direction.Axis physicalAxis
    ) {
        Objects.requireNonNull(resolution, "resolution");
        Objects.requireNonNull(physicalAxis, "physicalAxis");
        return resolution.targetPlacementFrame().map(frame ->
                frame.targetStateFrame().worldAxisToLocal(physicalAxis));
    }

    /**
     * Standard stair-like TOP/BOTTOM preference, distinct from SLAB_TYPE and
     * DOUBLE_BLOCK_HALF; concrete owners still decide replacement/stacking.
     * Clicked face is already re-expressed into TARGET canonical coordinates.
     */
    public static Optional<Half> halfFromClick(
            PlanetPlacementResolver.Resolution resolution
    ) {
        Objects.requireNonNull(resolution, "resolution");
        return resolution.targetPlacementFrame().map(frame -> {
            Direction clicked = frame.localClickedFace();
            if (clicked == Direction.DOWN) {
                return Half.TOP;
            }
            if (clicked == Direction.UP) {
                return Half.BOTTOM;
            }
            return frame.localHitOffset().y > 0.5D ? Half.TOP : Half.BOTTOM;
        });
    }

    /**
     * The argument is the PHYSICAL direction FROM TARGET TO SUPPORT,
     * not the clicked surface normal. FaceAttachedHorizontalDirectionalBlock
     * chooses CEILING for support UP, FLOOR for support DOWN, otherwise WALL.
     * It has a separate four-way FACING and its own canSurvive logic.
     */
    public static Optional<AttachFace> attachToSupport(
            PlanetPlacementResolver.Resolution resolution,
            Direction physicalDirectionToSupport
    ) {
        return facing(resolution, physicalDirectionToSupport).map(local ->
                switch (local) {
                    case UP -> AttachFace.CEILING;
                    case DOWN -> AttachFace.FLOOR;
                    default -> AttachFace.WALL;
                });
    }

    /**
     * FrontAndTop has 12 legal enum states, NOT every orthogonal pair.
     * Return empty on an unsupported source-authored pair rather than
     * replacing it by an arbitrary closest orientation.
     */
    public static Optional<FrontAndTop> frontAndTop(
            PlanetPlacementResolver.Resolution resolution,
            Direction physicalFront,
            Direction physicalTop
    ) {
        Objects.requireNonNull(resolution, "resolution");
        Objects.requireNonNull(physicalFront, "physicalFront");
        Objects.requireNonNull(physicalTop, "physicalTop");
        return facing(resolution, physicalFront).flatMap(front ->
                facing(resolution, physicalTop).flatMap(top ->
                        Optional.ofNullable(FrontAndTop.fromFrontAndTop(front, top))));
    }

    /**
     * Converts a PHYSICAL world-space heading into the TARGET tangent plane.
     * For a heading parallel to local UP/DOWN there is no unambiguous 16-way
     * angle, so the algorithm returns empty rather than fabricating one.
     *
     * <p>Returns the 0..15 index; it does NOT apply the owner-specific
     * yaw offset, attachment rules, support tests, neighbor inheritance or
     * rotate/mirror lifecycle. The caller chooses NORTH/SOUTH 0 by its
     * already-audited concrete author algorithm.</p>
     */
    public static Optional<Integer> tangentRotation16(
            PlanetPlacementResolver.Resolution resolution,
            Vec3 physicalHeading,
            Rotation16Zero zero
    ) {
        Objects.requireNonNull(resolution, "resolution");
        Objects.requireNonNull(physicalHeading, "physicalHeading");
        Objects.requireNonNull(zero, "zero");
        if (!Double.isFinite(physicalHeading.x)
                || !Double.isFinite(physicalHeading.y)
                || !Double.isFinite(physicalHeading.z)) {
            return Optional.empty();
        }

        return resolution.targetPlacementFrame().flatMap(frame -> {
            PlanetFrameVector local = frame.targetStateFrame().frame()
                    .worldToLocal(new PlanetFrameVector(
                            physicalHeading.x, physicalHeading.y, physicalHeading.z));

            double tangentLen2 = local.x() * local.x() + local.z() * local.z();
            if (!Double.isFinite(tangentLen2)
                    || tangentLen2 <= MIN_TANGENT_LENGTH_SQUARED) {
                return Optional.empty();
            }

            double yaw = switch (zero) {
                case NORTH_CLOCKWISE -> Math.toDegrees(
                        Math.atan2(local.x(), -local.z()));
                case SOUTH_CLOCKWISE -> Math.toDegrees(
                        Math.atan2(-local.x(), local.z()));
            };
            return Optional.of(RotationSegment.convertToSegment((float) yaw));
        });
    }

    /**
     * Explicit property write for an owner which opted into target-local
     * semantics. Preserves the concrete BlockState type and ALL unrelated
     * properties, including mod-specific values and NeoForge extensions.
     * No mutation occurs for an illegal value or absent property.
     */
    public static <T extends Comparable<T>> Optional<BlockState> withProperty(
            PlanetPlacementResolver.Resolution resolution,
            BlockState original,
            Property<T> property,
            T localValue
    ) {
        Objects.requireNonNull(resolution, "resolution");
        Objects.requireNonNull(original, "original");
        Objects.requireNonNull(property, "property");
        Objects.requireNonNull(localValue, "localValue");
        if (!resolution.isPlanetTarget()
                || !original.hasProperty(property)
                || !property.getPossibleValues().contains(localValue)) {
            return Optional.empty();
        }
        return Optional.of(original.setValue(property, localValue));
    }
}
