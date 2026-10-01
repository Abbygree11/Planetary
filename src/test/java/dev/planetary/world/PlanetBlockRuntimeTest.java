package dev.planetary.world;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

final class PlanetBlockRuntimeTest {
    private static final PlanetCore CORE =
            new PlanetCore(10, 20, 30, 20);
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(CORE);

    @Test
    void canonicalStateFrameUsesBoundRuntimeField() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos corner =
                    new BlockPos(
                            CORE.x() + 20,
                            CORE.y() + 20,
                            CORE.z() + 20
                    );

            assertEquals(
                    FIELD,
                    PlanetBlockRuntime.fieldAt(
                            level,
                            corner
                    ).orElseThrow()
            );
            assertEquals(
                    PlanetFace.POS_X,
                    PlanetBlockRuntime.stateFrameAt(
                            level,
                            corner
                    ).orElseThrow().face()
            );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    @Test
    void blockActivationIsEvaluatedAtBlockCenter() {
        Level level = mock(Level.class);

        PlanetGravityRuntime.bind(
                level,
                FIELD,
                (x, y, z) -> x < 11.0D
        );

        try {
            assertTrue(
                    PlanetBlockRuntime.fieldAt(
                            level,
                            new BlockPos(10, 999, 999)
                    ).isPresent()
            );
            assertTrue(
                    PlanetBlockRuntime.fieldAt(
                            level,
                            new BlockPos(11, 999, 999)
                    ).isEmpty()
            );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    @Test
    void physicalAndCanonicalLocalSidesRoundTripOnAllSixFaces() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            for (PlanetFace face : PlanetFace.values()) {
                BlockPos pos =
                        faceCenter(face);

                for (Direction local :
                        Direction.values()) {
                    Direction physical =
                            PlanetBlockRuntime.localSideToPhysical(
                                    level,
                                    pos,
                                    local
                            ).orElseThrow();

                    assertEquals(
                            local,
                            PlanetBlockRuntime.physicalSideToLocal(
                                    level,
                                    pos,
                                    physical
                            ).orElseThrow(),
                            face + " / " + local
                    );
                }
            }
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    @Test
    void traversalCanKeepBoundaryChartWhileStateFrameStaysCanonical() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos edge =
                    new BlockPos(
                            CORE.x() + 20,
                            CORE.y() + 20,
                            CORE.z()
                    );

            assertEquals(
                    PlanetFace.POS_X,
                    PlanetBlockRuntime.stateFrameAt(
                            level,
                            edge
                    ).orElseThrow().face()
            );
            assertEquals(
                    PlanetFace.POS_Y,
                    PlanetBlockRuntime.traversalAt(
                            level,
                            edge,
                            PlanetFace.POS_Y
                    ).orElseThrow().face()
            );
            assertEquals(
                    PlanetFace.POS_X,
                    PlanetBlockRuntime.traversalAt(
                            level,
                            edge
                    ).orElseThrow().face()
            );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    @Test
    void supportQueryUsesSameRuntimeFieldResolver() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);

        try {
            BlockPos source =
                    new BlockPos(
                            CORE.x() + 20,
                            CORE.y(),
                            CORE.z()
                    );

            PlanetBlockSupportQuery query =
                    PlanetBlockRuntime.supportQuery(
                            level,
                            source,
                            Direction.DOWN
                    ).orElseThrow();

            assertEquals(
                    PlanetFace.POS_X,
                    query.sourceStateFrame().face()
            );
            assertEquals(
                    Direction.WEST,
                    query.physicalDirectionToSupport()
            );
            assertEquals(
                    source.west(),
                    query.supportPos()
            );
        } finally {
            PlanetGravityRuntime.unbind(
                    level,
                    FIELD
            );
        }
    }

    private static BlockPos faceCenter(
            PlanetFace face
    ) {
        return switch (face) {
            case POS_X -> new BlockPos(
                    CORE.x() + 20,
                    CORE.y(),
                    CORE.z()
            );
            case NEG_X -> new BlockPos(
                    CORE.x() - 20,
                    CORE.y(),
                    CORE.z()
            );
            case POS_Y -> new BlockPos(
                    CORE.x(),
                    CORE.y() + 20,
                    CORE.z()
            );
            case NEG_Y -> new BlockPos(
                    CORE.x(),
                    CORE.y() - 20,
                    CORE.z()
            );
            case POS_Z -> new BlockPos(
                    CORE.x(),
                    CORE.y(),
                    CORE.z() + 20
            );
            case NEG_Z -> new BlockPos(
                    CORE.x(),
                    CORE.y(),
                    CORE.z() - 20
            );
        };
    }
}
