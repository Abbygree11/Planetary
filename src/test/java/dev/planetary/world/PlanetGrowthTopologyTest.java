package dev.planetary.world;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

final class PlanetGrowthTopologyTest {
    private static final int SHELL = 20;
    private static final PlanetCore CORE =
            new PlanetCore(
                    3,
                    100,
                    -7,
                    SHELL
            );
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void interiorSpreadOffsetMatchesLocalCartesianAxesOnAllFaces() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(
                level,
                FIELD
        );

        try {
            for (PlanetFace face :
                    PlanetFace.values()) {
                BlockPos source =
                        physical(
                                face,
                                SHELL,
                                2,
                                -3
                        );

                int localX = 1;
                int localY = -2;
                int localZ = 1;

                BlockPos actual =
                        PlanetGrowthTopology
                                .spreadTarget(
                                        level,
                                        source,
                                        localX,
                                        localY,
                                        localZ
                                )
                                .orElseThrow();

                PlanetGravityFrame frame =
                        new PlanetGravityFrame(
                                face
                        );
                PlanetVector east =
                        frame.worldEast();
                PlanetVector up =
                        frame.worldUp();
                PlanetVector south =
                        frame.worldSouth();

                BlockPos expected =
                        new BlockPos(
                                source.getX()
                                        + east.x() * localX
                                        + up.x() * localY
                                        + south.x() * localZ,
                                source.getY()
                                        + east.y() * localX
                                        + up.y() * localY
                                        + south.y() * localZ,
                                source.getZ()
                                        + east.z() * localX
                                        + up.z() * localY
                                        + south.z() * localZ
                        );

                assertEquals(
                        expected,
                        actual,
                        face.toString()
                );
            }
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    @Test
    void seamSpreadUsesDocumentedVerticalThenZThenXPath() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(
                level,
                FIELD
        );

        try {
            BlockPos source =
                    physical(
                            PlanetFace.POS_Y,
                            SHELL,
                            SHELL - 1,
                            0
                    );

            BlockPos actual =
                    PlanetGrowthTopology
                            .spreadTarget(
                                    level,
                                    source,
                                    2,
                                    0,
                                    1
                            )
                            .orElseThrow();

            PlanetBlockFrameContext current =
                    PlanetBlockRuntime.traversalAt(
                            level,
                            source
                    ).orElseThrow();

            // local Z segment first
            PlanetBlockStep z =
                    current.step(
                            Direction.SOUTH
                    );
            current = z.target();

            // then local X segment, transporting through the seam
            Direction east =
                    Direction.EAST;
            for (int i = 0; i < 2; i++) {
                PlanetBlockStep step =
                        current.step(east);
                current =
                        step.target();
                east =
                        step.transportedDirection();
            }

            assertEquals(
                    current.pos(),
                    actual
            );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    private static BlockPos physical(
            PlanetFace face,
            int upScore,
            int eastScore,
            int southScore
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up =
                frame.worldUp();
        PlanetVector east =
                frame.worldEast();
        PlanetVector south =
                frame.worldSouth();

        return new BlockPos(
                CORE.x()
                        + up.x() * upScore
                        + east.x() * eastScore
                        + south.x() * southScore,
                CORE.y()
                        + up.y() * upScore
                        + east.y() * eastScore
                        + south.y() * southScore,
                CORE.z()
                        + up.z() * upScore
                        + east.z() * eastScore
                        + south.z() * southScore
        );
    }
}
