package dev.planetary.api;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class PlanetCapabilityAdaptersTest {
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(new PlanetCore(0, 128, 0, 20));

    @Test
    void optInAndUnwrappedProvidersUseIndependentSideContractsOnAllFaces() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            IBlockCapabilityProvider<Direction, Direction> physical =
                    (l, pos, state, be, side) -> side;
            IBlockCapabilityProvider<Direction, Direction> local =
                    PlanetCapabilityAdapters.canonicalLocalBlock(
                            (l, pos, state, be, side) -> side
                    );

            for (PlanetFace face : PlanetFace.values()) {
                BlockPos pos = faceCenter(face);
                for (Direction physicalSide : Direction.values()) {
                    Direction expectedLocal =
                            PlanetBlockRuntime.physicalSideToLocal(
                                    level, pos, physicalSide
                            ).orElseThrow();
                    assertEquals(
                            physicalSide,
                            physical.getCapability(level, pos, null, null, physicalSide),
                            face + " / " + physicalSide + " unwrapped"
                    );
                    assertEquals(
                            expectedLocal,
                            local.getCapability(level, pos, null, null, physicalSide),
                            face + " / " + physicalSide + " opted-in"
                    );
                }
            }
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void localProviderUsesCanonicalBlockFaceNotAnIncomingTraversalChart() {
        Level level = mock(Level.class);
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            BlockPos corner = new BlockPos(20, 148, 20);
            IBlockCapabilityProvider<Direction, Direction> local =
                    PlanetCapabilityAdapters.canonicalLocalBlock(
                            (l, pos, state, be, side) -> side
                    );
            for (Direction physicalSide : Direction.values()) {
                assertEquals(
                        PlanetBlockRuntime.physicalSideToLocal(
                                level, corner, physicalSide
                        ).orElseThrow(),
                        local.getCapability(
                                level, corner, null, null, physicalSide
                        )
                );
            }
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void nullSideAndUnboundWorldArePassedThrough() {
        Level level = mock(Level.class);
        BlockPos pos = faceCenter(PlanetFace.POS_X);
        IBlockCapabilityProvider<Direction, Direction> local =
                PlanetCapabilityAdapters.canonicalLocalBlock(
                        (l, p, state, be, side) -> side
                );

        assertEquals(
                Direction.EAST,
                local.getCapability(level, pos, null, null, Direction.EAST)
        );
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            assertNull(local.getCapability(level, pos, null, null, null));
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void entityProviderDoesNotInventAFrameBeforeLevelAttachment() {
        Level level = mock(Level.class);
        BlockEntity blockEntity = mock(BlockEntity.class);
        ICapabilityProvider<BlockEntity, Direction, Direction> local =
                PlanetCapabilityAdapters.canonicalLocalBlockEntity(
                        (be, side) -> side
                );

        assertEquals(
                Direction.EAST,
                local.getCapability(blockEntity, Direction.EAST)
        );
        when(blockEntity.getLevel()).thenReturn(level);
        when(blockEntity.getBlockPos())
                .thenReturn(faceCenter(PlanetFace.POS_X));
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            assertEquals(
                    Direction.UP,
                    local.getCapability(blockEntity, Direction.EAST)
            );
            assertNull(local.getCapability(blockEntity, null));
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    private static BlockPos faceCenter(PlanetFace face) {
        PlanetGravityFrame frame = new PlanetGravityFrame(face);
        return new BlockPos(
                FIELD.core().x() + 20 * frame.worldUp().x(),
                FIELD.core().y() + 20 * frame.worldUp().y(),
                FIELD.core().z() + 20 * frame.worldUp().z()
        );
    }
}
