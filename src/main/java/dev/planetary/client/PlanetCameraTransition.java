package dev.planetary.client;

import dev.planetary.topology.PlanetFace;
import org.joml.Quaternionf;

import java.util.Objects;

/**
 * Visual-only smoothing for gravity-face camera changes.
 *
 * <p>Physics can switch gravity immediately while the rendered camera rotates
 * over a few ticks. The full camera quaternion is interpolated because entity
 * yaw is parallel-transported immediately at the same face transition.</p>
 */
public final class PlanetCameraTransition {
    public static final double DEFAULT_DURATION_TICKS = 6.0D;

    private final double durationTicks;

    private boolean initialized;
    private boolean transitioning;
    private PlanetFace face;
    private double transitionStartTime;

    private final Quaternionf lastCamera =
            new Quaternionf();
    private final Quaternionf lastFrame =
            new Quaternionf();
    private final Quaternionf startCamera =
            new Quaternionf();
    private final Quaternionf startFrame =
            new Quaternionf();

    public PlanetCameraTransition() {
        this(DEFAULT_DURATION_TICKS);
    }

    public PlanetCameraTransition(double durationTicks) {
        if (durationTicks <= 0.0D) {
            throw new IllegalArgumentException(
                    "durationTicks must be > 0"
            );
        }
        this.durationTicks = durationTicks;
    }

    public Snapshot update(
            PlanetFace nextFace,
            Quaternionf targetCamera,
            Quaternionf targetFrame,
            double timeTicks
    ) {
        Objects.requireNonNull(nextFace, "nextFace");
        Objects.requireNonNull(targetCamera, "targetCamera");
        Objects.requireNonNull(targetFrame, "targetFrame");

        if (!initialized) {
            initialized = true;
            face = nextFace;
            lastCamera.set(targetCamera);
            lastFrame.set(targetFrame);
            return snapshot(1.0F);
        }

        if (face != nextFace) {
            face = nextFace;
            transitionStartTime = timeTicks;
            startCamera.set(lastCamera);
            startFrame.set(lastFrame);
            transitioning = true;
        }

        if (!transitioning) {
            lastCamera.set(targetCamera);
            lastFrame.set(targetFrame);
            return snapshot(1.0F);
        }

        double rawProgress =
                (timeTicks - transitionStartTime)
                        / durationTicks;
        float progress = (float) Math.max(
                0.0D,
                Math.min(1.0D, rawProgress)
        );
        float smooth =
                progress * progress
                        * (3.0F - 2.0F * progress);

        lastCamera.set(startCamera)
                .slerp(targetCamera, smooth)
                .normalize();
        lastFrame.set(startFrame)
                .slerp(targetFrame, smooth)
                .normalize();

        if (progress >= 1.0F) {
            transitioning = false;
            lastCamera.set(targetCamera);
            lastFrame.set(targetFrame);
        }

        return snapshot(progress);
    }

    public void reset() {
        initialized = false;
        transitioning = false;
        face = null;
        transitionStartTime = 0.0D;
        lastCamera.identity();
        lastFrame.identity();
        startCamera.identity();
        startFrame.identity();
    }

    private Snapshot snapshot(float progress) {
        return new Snapshot(
                new Quaternionf(lastCamera),
                new Quaternionf(lastFrame),
                progress
        );
    }

    public record Snapshot(
            Quaternionf cameraRotation,
            Quaternionf frameRotation,
            float progress
    ) {
    }
}
