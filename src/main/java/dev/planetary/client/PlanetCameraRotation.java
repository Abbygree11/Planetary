package dev.planetary.client;

import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

import java.util.Objects;

/**
 * Builds the exact camera/world rotation for one local gravity frame.
 *
 * <p>The frame quaternion maps local Minecraft axes into ordinary world XYZ:
 * local +X=EAST, local +Y=UP and local +Z=SOUTH. Vanilla yaw/pitch/roll are
 * evaluated inside that local frame and only then rotated into the world.</p>
 */
public final class PlanetCameraRotation {
    private PlanetCameraRotation() {
    }

    public static Quaternionf frameQuaternion(
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(frame, "frame");

        PlanetVector east = frame.worldEast();
        PlanetVector up = frame.worldUp();
        PlanetVector south = frame.worldSouth();

        // JOML's m00/m01/m02 are column 0, m10/m11/m12 column 1,
        // m20/m21/m22 column 2. Therefore the three local basis vectors are
        // supplied as matrix columns.
        Matrix3f matrix = new Matrix3f(
                east.x(), east.y(), east.z(),
                up.x(), up.y(), up.z(),
                south.x(), south.y(), south.z()
        );

        return new Quaternionf().setFromNormalized(matrix);
    }

    /**
     * Rotation that maps a vanilla entity model from its local gravity frame
     * into world space. Kept separate from cameraQuaternion because camera
     * composition additionally includes local yaw/pitch/roll.
     */
    public static Quaternionf entityQuaternion(
            PlanetGravityFrame frame
    ) {
        return frameQuaternion(frame);
    }

    public static Quaternionf cameraQuaternion(
            float yawDegrees,
            float pitchDegrees,
            float rollDegrees,
            PlanetGravityFrame frame
    ) {
        Objects.requireNonNull(frame, "frame");

        Quaternionf vanillaLocal = new Quaternionf()
                .rotationYXZ(
                        (float) Math.PI
                                - (float) Math.toRadians(
                                        yawDegrees
                                ),
                        -(float) Math.toRadians(
                                pitchDegrees
                        ),
                        -(float) Math.toRadians(
                                rollDegrees
                        )
                );

        return frameQuaternion(frame).mul(vanillaLocal);
    }
}
