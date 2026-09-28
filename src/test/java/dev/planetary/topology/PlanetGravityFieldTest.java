package dev.planetary.topology;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlanetGravityFieldTest {
    private static final PlanetCore CORE =
            new PlanetCore(10, -20, 30, 500);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void oddDiameterHasExactlyOneCoreBlock() {
        assertEquals(1001, CORE.diameter());
        assertTrue(CORE.isCoreBlock(10, -20, 30));
        assertFalse(CORE.isCoreBlock(11, -20, 30));

        assertEquals(0, FIELD.shellRadius(10, -20, 30));
        assertTrue(
                FIELD.candidateFaces(10, -20, 30).isEmpty()
        );
    }

    @Test
    void shellRadiusUsesCubicChebyshevDistance() {
        assertEquals(500, FIELD.shellRadius(510, -20, 30));
        assertEquals(500, FIELD.shellRadius(-490, -20, 30));
        assertEquals(500, FIELD.shellRadius(10, 480, 30));
        assertEquals(500, FIELD.shellRadius(10, -20, -470));

        assertEquals(
                500,
                FIELD.shellRadius(510, 480, 530)
        );

        assertEquals(0, FIELD.signedShellOffset(510, -20, 30));
        assertEquals(-499, FIELD.signedShellOffset(11, -20, 30));
        assertEquals(9_500, FIELD.signedShellOffset(10_010, -20, 30));
    }

    @Test
    void sixPyramidsContinueIndefinitelyOutward() {
        assertEquals(
                Set.of(PlanetFace.POS_X),
                FIELD.candidateFaces(1_000_010, -20, 30)
        );
        assertEquals(
                Set.of(PlanetFace.NEG_X),
                FIELD.candidateFaces(-999_990, -20, 30)
        );
        assertEquals(
                Set.of(PlanetFace.POS_Y),
                FIELD.candidateFaces(10, 999_980, 30)
        );
        assertEquals(
                Set.of(PlanetFace.NEG_Z),
                FIELD.candidateFaces(10, -20, -999_970)
        );
    }

    @Test
    void exactGravityBoundariesExposeBothOrAllThreeFaces() {
        assertEquals(
                Set.of(
                        PlanetFace.POS_X,
                        PlanetFace.POS_Y
                ),
                FIELD.candidateFaces(110, 80, 30)
        );

        assertEquals(
                Set.of(
                        PlanetFace.NEG_X,
                        PlanetFace.POS_Y
                ),
                FIELD.candidateFaces(-90, 80, 30)
        );

        assertEquals(
                Set.of(
                        PlanetFace.POS_X,
                        PlanetFace.POS_Y,
                        PlanetFace.NEG_Z
                ),
                FIELD.candidateFaces(110, 80, -70)
        );
    }

    @Test
    void preferredFaceIsKeptOnAnExactBlockBoundary() {
        assertEquals(
                PlanetFace.POS_Y,
                FIELD.selectBlockFace(
                        110,
                        80,
                        30,
                        PlanetFace.POS_Y
                ).orElseThrow()
        );
        assertEquals(
                PlanetFace.POS_X,
                FIELD.selectBlockFace(
                        110,
                        80,
                        30,
                        PlanetFace.POS_X
                ).orElseThrow()
        );
    }

    @Test
    void entityGravitySwitchesAsItsCenterCrossesTheDiagonalPlane() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        assertEquals(
                PlanetFace.POS_Y,
                FIELD.selectEntityFace(
                        cx + 100.0,
                        cy + 101.0,
                        cz,
                        PlanetFace.POS_Y,
                        0.0
                ).orElseThrow()
        );

        assertEquals(
                PlanetFace.POS_Y,
                FIELD.selectEntityFace(
                        cx + 101.0,
                        cy + 101.0,
                        cz,
                        PlanetFace.POS_Y,
                        0.0
                ).orElseThrow()
        );

        assertEquals(
                PlanetFace.POS_X,
                FIELD.selectEntityFace(
                        cx + 102.0,
                        cy + 101.0,
                        cz,
                        PlanetFace.POS_Y,
                        0.0
                ).orElseThrow()
        );
    }

    @Test
    void optionalHysteresisOnlyDelaysTinyBoundaryFlips() {
        double cx = CORE.centerX();
        double cy = CORE.centerY();
        double cz = CORE.centerZ();

        assertEquals(
                PlanetFace.POS_Y,
                FIELD.selectEntityFace(
                        cx + 101.04,
                        cy + 101.0,
                        cz,
                        PlanetFace.POS_Y,
                        0.05
                ).orElseThrow()
        );

        assertEquals(
                PlanetFace.POS_X,
                FIELD.selectEntityFace(
                        cx + 101.06,
                        cy + 101.0,
                        cz,
                        PlanetFace.POS_Y,
                        0.05
                ).orElseThrow()
        );
    }

    @Test
    void gravityAlwaysPointsTowardCoreAlongSelectedAxis() {
        assertEquals(
                new PlanetVector(-1, 0, 0),
                FIELD.gravityDirection(PlanetFace.POS_X)
        );
        assertEquals(
                new PlanetVector(1, 0, 0),
                FIELD.gravityDirection(PlanetFace.NEG_X)
        );
        assertEquals(
                new PlanetVector(0, -1, 0),
                FIELD.gravityDirection(PlanetFace.POS_Y)
        );
        assertEquals(
                new PlanetVector(0, 0, 1),
                FIELD.gravityDirection(PlanetFace.NEG_Z)
        );
    }
}
