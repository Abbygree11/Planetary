package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Orthogonally rotates block-local voxel shapes into a Planet gravity frame.
 *
 * <p>Vanilla block shapes are authored in the canonical local frame where
 * +Y is local UP. Planet worlds keep that semantic shape unchanged and rotate
 * the result around the center of the block only when a physical shape is
 * required.</p>
 *
 * <p>The local-to-world path is cached by canonical shape instance and
 * {@link PlanetFace}. Weak keys keep temporary/modded query shapes from being
 * retained forever. POS_Y is the canonical identity frame and returns the
 * original shape directly.</p>
 */
public final class PlanetVoxelShapeRotation {
    private static final double CENTER = 0.5D;

    private static final Map<
            VoxelShape,
            EnumMap<PlanetFace, VoxelShape>
    > LOCAL_TO_WORLD_CACHE = Collections.synchronizedMap(
            new WeakHashMap<>()
    );

    private PlanetVoxelShapeRotation() {
    }

    /**
     * Rotates a canonical local-Y-up shape into physical world axes.
     */
    public static VoxelShape localToWorld(
            VoxelShape localShape,
            PlanetFace face
    ) {
        Objects.requireNonNull(localShape, "localShape");
        Objects.requireNonNull(face, "face");

        if (face == PlanetFace.POS_Y) {
            return localShape;
        }

        synchronized (LOCAL_TO_WORLD_CACHE) {
            EnumMap<PlanetFace, VoxelShape> byFace =
                    LOCAL_TO_WORLD_CACHE.computeIfAbsent(
                            localShape,
                            ignored -> new EnumMap<>(
                                    PlanetFace.class
                            )
                    );

            return byFace.computeIfAbsent(
                    face,
                    targetFace -> transform(
                            localShape,
                            new PlanetGravityFrame(targetFace),
                            false
                    )
            );
        }
    }

    /**
     * Converts a physical shape in the supplied frame back to canonical local
     * block axes. This path is intentionally not globally cached because
     * physical query shapes are not necessarily stable canonical instances.
     */
    public static VoxelShape worldToLocal(
            VoxelShape worldShape,
            PlanetFace face
    ) {
        Objects.requireNonNull(worldShape, "worldShape");
        Objects.requireNonNull(face, "face");

        if (face == PlanetFace.POS_Y) {
            return worldShape;
        }

        return transform(
                worldShape,
                new PlanetGravityFrame(face),
                true
        );
    }

    private static VoxelShape transform(
            VoxelShape source,
            PlanetGravityFrame frame,
            boolean inverse
    ) {
        VoxelShape result = Shapes.empty();

        for (AABB box : source.toAabbs()) {
            result = Shapes.or(
                    result,
                    Shapes.create(
                            transformBox(
                                    box,
                                    frame,
                                    inverse
                            )
                    )
            );
        }

        return result.optimize();
    }

    private static AABB transformBox(
            AABB source,
            PlanetGravityFrame frame,
            boolean inverse
    ) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        double[] xs = {source.minX, source.maxX};
        double[] ys = {source.minY, source.maxY};
        double[] zs = {source.minZ, source.maxZ};

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    PlanetFrameVector centered =
                            new PlanetFrameVector(
                                    x - CENTER,
                                    y - CENTER,
                                    z - CENTER
                            );

                    PlanetFrameVector transformed =
                            inverse
                                    ? frame.worldToLocal(centered)
                                    : frame.localToWorld(centered);

                    double worldX =
                            transformed.x() + CENTER;
                    double worldY =
                            transformed.y() + CENTER;
                    double worldZ =
                            transformed.z() + CENTER;

                    minX = Math.min(minX, worldX);
                    minY = Math.min(minY, worldY);
                    minZ = Math.min(minZ, worldZ);
                    maxX = Math.max(maxX, worldX);
                    maxY = Math.max(maxY, worldY);
                    maxZ = Math.max(maxZ, worldZ);
                }
            }
        }

        return new AABB(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }
}
