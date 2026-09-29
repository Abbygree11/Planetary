package dev.planetary.client;

import dev.planetary.topology.PlanetFace;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetCameraTransitionTest {
    private static final double EPSILON = 1.0E-5D;

    @Test
    void firstFrameSnapsToCurrentFaceWithoutAnimation() {
        PlanetCameraTransition transition =
                new PlanetCameraTransition(6.0D);

        Quaternionf target =
                new Quaternionf()
                        .rotateX((float) Math.PI / 2.0F);

        var snapshot = transition.update(
                PlanetFace.POS_X,
                target,
                target,
                100.0D
        );

        assertEquals(1.0F, snapshot.progress());
        assertDirection(
                snapshot.cameraRotation(),
                target
        );
    }

    @Test
    void faceChangeInterpolatesAndFinishesAfterDuration() {
        PlanetCameraTransition transition =
                new PlanetCameraTransition(6.0D);

        Quaternionf top = new Quaternionf();
        Quaternionf side =
                new Quaternionf()
                        .rotateZ((float) -Math.PI / 2.0F);

        transition.update(
                PlanetFace.POS_Y,
                top,
                top,
                10.0D
        );

        var start = transition.update(
                PlanetFace.POS_X,
                side,
                side,
                11.0D
        );
        assertEquals(0.0F, start.progress(), EPSILON);
        assertDirection(start.cameraRotation(), top);

        var middle = transition.update(
                PlanetFace.POS_X,
                side,
                side,
                14.0D
        );
        assertTrue(
                middle.progress() > 0.0F
                        && middle.progress() < 1.0F
        );

        var end = transition.update(
                PlanetFace.POS_X,
                side,
                side,
                17.0D
        );
        assertEquals(1.0F, end.progress(), EPSILON);
        assertDirection(end.cameraRotation(), side);
    }

    @Test
    void secondFaceChangeStartsFromCurrentVisualRotation() {
        PlanetCameraTransition transition =
                new PlanetCameraTransition(6.0D);

        Quaternionf top = new Quaternionf();
        Quaternionf side =
                new Quaternionf()
                        .rotateZ((float) -Math.PI / 2.0F);
        Quaternionf bottom =
                new Quaternionf()
                        .rotateZ((float) Math.PI);

        transition.update(
                PlanetFace.POS_Y,
                top,
                top,
                0.0D
        );
        var halfway = transition.update(
                PlanetFace.POS_X,
                side,
                side,
                3.0D
        );
        halfway = transition.update(
                PlanetFace.POS_X,
                side,
                side,
                6.0D
        );

        Quaternionf beforeSecondChange =
                new Quaternionf(
                        halfway.cameraRotation()
                );

        var secondStart = transition.update(
                PlanetFace.NEG_Y,
                bottom,
                bottom,
                6.0D
        );

        assertDirection(
                secondStart.cameraRotation(),
                beforeSecondChange
        );
    }

    private static void assertDirection(
            Quaternionf actual,
            Quaternionf expected
    ) {
        Vector3f actualForward =
                actual.transform(
                        new Vector3f(0.0F, 0.0F, -1.0F)
                );
        Vector3f expectedForward =
                expected.transform(
                        new Vector3f(0.0F, 0.0F, -1.0F)
                );

        assertEquals(
                expectedForward.x,
                actualForward.x,
                EPSILON
        );
        assertEquals(
                expectedForward.y,
                actualForward.y,
                EPSILON
        );
        assertEquals(
                expectedForward.z,
                actualForward.z,
                EPSILON
        );
    }
}
