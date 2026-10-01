package dev.planetary.topology;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Piecewise-axis gravity field of a cubic Minecraft planet.
 *
 * <p>The world itself remains one ordinary XYZ block grid. The six gravity
 * regions are the six infinite square pyramids obtained by asking which
 * absolute coordinate relative to the core is largest. Their boundaries are
 * the diagonal planes such as |x| == |y|.</p>
 *
 * <p>The natural shell coordinate is the Chebyshev/L-infinity radius:
 * {@code max(|dx|, |dy|, |dz|)}. Constant shell radius therefore forms a cube,
 * which gives an odd-diameter cubic planet a single central core block.</p>
 */
public final class PlanetGravityField {
    private static final double EPSILON = 1.0e-12;

    private final PlanetCore core;

    public PlanetGravityField(PlanetCore core) {
        this.core = Objects.requireNonNull(core, "core");
    }

    public PlanetCore core() {
        return core;
    }

    /**
     * Returns the cubic shell radius of a block center relative to the core
     * block center. The core block itself is shell 0.
     */
    public long shellRadius(
            int blockX,
            int blockY,
            int blockZ
    ) {
        long dx = (long) blockX - core.x();
        long dy = (long) blockY - core.y();
        long dz = (long) blockZ - core.z();

        return Math.max(
                Math.abs(dx),
                Math.max(Math.abs(dy), Math.abs(dz))
        );
    }

    /**
     * Signed distance in whole block shells from the base cube surface.
     *
     * <ul>
     *     <li>0: base surface shell</li>
     *     <li>negative: inside the base planet</li>
     *     <li>positive: outside the base planet</li>
     * </ul>
     */
    public long signedShellOffset(
            int blockX,
            int blockY,
            int blockZ
    ) {
        return shellRadius(blockX, blockY, blockZ)
                - core.radius();
    }

    public boolean isInsideBasePlanet(
            int blockX,
            int blockY,
            int blockZ
    ) {
        return shellRadius(blockX, blockY, blockZ)
                <= core.radius();
    }

    /**
     * Returns every gravity face that is mathematically valid at a block
     * center. Ordinary points have one candidate, edge-boundary points have
     * two, cube-corner boundaries have three, and the core itself has none.
     *
     * <p>Keeping ties explicit is important: an edge block is physically one
     * ordinary Minecraft block and can be approached from either neighboring
     * gravity region.</p>
     */
    public Set<PlanetFace> candidateFaces(
            int blockX,
            int blockY,
            int blockZ
    ) {
        long dx = (long) blockX - core.x();
        long dy = (long) blockY - core.y();
        long dz = (long) blockZ - core.z();

        long ax = Math.abs(dx);
        long ay = Math.abs(dy);
        long az = Math.abs(dz);
        long best = Math.max(ax, Math.max(ay, az));

        if (best == 0L) {
            return Set.of();
        }

        EnumSet<PlanetFace> faces =
                EnumSet.noneOf(PlanetFace.class);

        if (ax == best) {
            faces.add(dx > 0
                    ? PlanetFace.POS_X
                    : PlanetFace.NEG_X);
        }
        if (ay == best) {
            faces.add(dy > 0
                    ? PlanetFace.POS_Y
                    : PlanetFace.NEG_Y);
        }
        if (az == best) {
            faces.add(dz > 0
                    ? PlanetFace.POS_Z
                    : PlanetFace.NEG_Z);
        }

        return Collections.unmodifiableSet(faces);
    }

    /**
     * Chooses the canonical frame of one physical block from position alone.
     *
     * <p>Exact gravity ties use the same explicit axis priority as
     * {@link #selectEntityFace(double, double, double, PlanetFace, double)}
     * with no preferred face and zero hysteresis: X, then Y, then Z. Sign only
     * selects the positive/negative face of that winning axis.</p>
     *
     * <p>This canonical choice is for physical BlockState orientation,
     * collision/outline shapes and other properties that must not depend on
     * the traversal path used to reach the block.</p>
     */
    public Optional<PlanetFace> selectCanonicalBlockFace(
            int blockX,
            int blockY,
            int blockZ
    ) {
        long dx = (long) blockX - core.x();
        long dy = (long) blockY - core.y();
        long dz = (long) blockZ - core.z();

        long ax = Math.abs(dx);
        long ay = Math.abs(dy);
        long az = Math.abs(dz);
        long best = Math.max(
                ax,
                Math.max(ay, az)
        );

        if (best == 0L) {
            return Optional.empty();
        }

        if (ax >= ay && ax >= az) {
            return Optional.of(
                    dx >= 0
                            ? PlanetFace.POS_X
                            : PlanetFace.NEG_X
            );
        }
        if (ay >= az) {
            return Optional.of(
                    dy >= 0
                            ? PlanetFace.POS_Y
                            : PlanetFace.NEG_Y
            );
        }

        return Optional.of(
                dz >= 0
                        ? PlanetFace.POS_Z
                        : PlanetFace.NEG_Z
        );
    }

    /**
     * Chooses a traversal chart for one block while preserving a preferred
     * valid face on an exact boundary. If the preferred chart is not valid,
     * the position-only canonical block face is used as the deterministic
     * fallback.
     */
    public Optional<PlanetFace> selectBlockFace(
            int blockX,
            int blockY,
            int blockZ,
            PlanetFace preferred
    ) {
        Set<PlanetFace> candidates =
                candidateFaces(blockX, blockY, blockZ);

        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        if (preferred != null && candidates.contains(preferred)) {
            return Optional.of(preferred);
        }

        return selectCanonicalBlockFace(
                blockX,
                blockY,
                blockZ
        );
    }

    /**
     * Chooses gravity for a continuous point such as an entity.
     *
     * <p>The point is measured relative to the center of the core block. With
     * hysteresis 0 the face changes immediately after the entity center crosses
     * a diagonal gravity boundary. A small positive hysteresis can later be
     * used purely to prevent camera/physics chatter at floating-point ties.</p>
     */
    public Optional<PlanetFace> selectEntityFace(
            double worldX,
            double worldY,
            double worldZ,
            PlanetFace preferred,
            double hysteresis
    ) {
        if (hysteresis < 0.0) {
            throw new IllegalArgumentException(
                    "hysteresis must be >= 0"
            );
        }

        double dx = worldX - core.centerX();
        double dy = worldY - core.centerY();
        double dz = worldZ - core.centerZ();

        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);
        double best = Math.max(ax, Math.max(ay, az));

        if (best <= EPSILON) {
            return Optional.empty();
        }

        if (preferred != null) {
            double preferredScore = faceScore(
                    preferred,
                    dx,
                    dy,
                    dz
            );
            if (preferredScore >= best - hysteresis) {
                return Optional.of(preferred);
            }
        }

        if (ax >= ay && ax >= az) {
            return Optional.of(
                    dx >= 0.0
                            ? PlanetFace.POS_X
                            : PlanetFace.NEG_X
            );
        }
        if (ay >= az) {
            return Optional.of(
                    dy >= 0.0
                            ? PlanetFace.POS_Y
                            : PlanetFace.NEG_Y
            );
        }

        return Optional.of(
                dz >= 0.0
                        ? PlanetFace.POS_Z
                        : PlanetFace.NEG_Z
        );
    }

    public Optional<PlanetGravityFrame> selectBlockFrame(
            int blockX,
            int blockY,
            int blockZ,
            PlanetFace preferred
    ) {
        return selectBlockFace(
                blockX,
                blockY,
                blockZ,
                preferred
        ).map(PlanetGravityFrame::new);
    }

    public Optional<PlanetGravityFrame> selectEntityFrame(
            double worldX,
            double worldY,
            double worldZ,
            PlanetFace preferred,
            double hysteresis
    ) {
        return selectEntityFace(
                worldX,
                worldY,
                worldZ,
                preferred,
                hysteresis
        ).map(PlanetGravityFrame::new);
    }

    public PlanetVector gravityDirection(PlanetFace face) {
        Objects.requireNonNull(face, "face");
        return face.worldVector(PlanetDirection.DOWN);
    }

    public PlanetVector localUp(PlanetFace face) {
        Objects.requireNonNull(face, "face");
        return face.worldVector(PlanetDirection.UP);
    }

    private static double faceScore(
            PlanetFace face,
            double dx,
            double dy,
            double dz
    ) {
        PlanetVector outward =
                face.worldVector(PlanetDirection.UP);

        return outward.x() * dx
                + outward.y() * dy
                + outward.z() * dz;
    }
}
