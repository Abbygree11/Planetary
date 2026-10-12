package dev.planetary.world;

import dev.planetary.api.PlanetCoordinateContext;
import dev.planetary.api.PlanetPlacementIntent;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class PlanetPlacementIntentTest {
    private static final PlanetCore CORE = new PlanetCore(3, 100, -7, 20);
    private static final PlanetGravityField FIELD = new PlanetGravityField(CORE);

    @Test
    void allSixFacesAllSixHitDirectionsPreservePhysicalHitAndMapTargetLocally() {
        Level level = mock(Level.class);
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            BlockPos hit = target.relative(Direction.NORTH);
            Vec3 worldHit = Vec3.atCenterOf(target);
            PlanetBlockStateFrame canonical =
                    PlanetBlockStateFrame.resolve(FIELD, target).orElseThrow();

            for (Direction local : Direction.values()) {
                Direction physical = canonical.localToWorld(local);
                PlanetPlacementIntent intent = PlanetPlacementIntent.physical(
                        PlanetPlacementIntent.Author.PLAYER_BLOCK_ITEM,
                        level, hit, target, physical, worldHit, false,
                        Optional.of(new PlanetGravityFrame(face))
                );
                PlanetPlacementResolver.Resolution resolved =
                        PlanetPlacementResolver.resolve(intent, ignored -> Optional.of(FIELD));

                assertTrue(resolved.isPlanetTarget(), face.toString());
                assertEquals(face, resolved.targetPlacementFrame().orElseThrow()
                        .targetStateFrame().face());
                assertEquals(local, resolved.targetLocalHitFace().orElseThrow());
                assertEquals(worldHit, resolved.targetPlacementFrame().orElseThrow()
                        .worldClickLocation());
                assertEquals(hit, intent.hitBlock().pos());
                assertEquals(target, intent.targetBlock().pos());
                assertEquals(physical, intent.hitFace());
                assertEquals(local, resolved.playerBodyToTargetLocal(local).orElseThrow());
            }
        }
    }

    @Test
    void sourceAndTargetHaveIndependentCanonicalFramesOnExactEdge() {
        Level level = mock(Level.class);
        BlockPos hit = new BlockPos(CORE.x() + 20, CORE.y() + 21, CORE.z());
        BlockPos target = new BlockPos(CORE.x() + 20, CORE.y() + 20, CORE.z());
        PlanetPlacementIntent intent = PlanetPlacementIntent.physical(
                PlanetPlacementIntent.Author.PLAYER_BLOCK_ITEM,
                level, hit, target, Direction.UP, Vec3.atCenterOf(target), true,
                Optional.of(new PlanetGravityFrame(PlanetFace.POS_Y))
        );

        PlanetPlacementResolver.Resolution resolved =
                PlanetPlacementResolver.resolve(intent, ignored -> Optional.of(FIELD));

        assertEquals(PlanetFace.POS_Y, resolved.hitStateFrame().orElseThrow().face());
        assertEquals(PlanetFace.POS_X, resolved.targetPlacementFrame().orElseThrow()
                .targetStateFrame().face());
        assertEquals(Direction.WEST, resolved.targetLocalHitFace().orElseThrow());
        assertEquals(Direction.WEST, resolved.playerBodyToTargetLocal(Direction.UP).orElseThrow());
    }

    @Test
    void foreignCoordinatesNeverInvokePhysicalFieldResolver() {
        AtomicInteger calls = new AtomicInteger();
        PlanetPlacementIntent intent = PlanetPlacementIntent.foreign(
                PlanetPlacementIntent.Author.MOD_CUSTOM,
                ResourceLocation.parse("test:contraption"),
                new BlockPos(2, 5, 8), new BlockPos(2, 6, 8),
                Direction.UP, new Vec3(2.5, 5.9, 8.5), false
        );

        PlanetPlacementResolver.Resolution resolved = PlanetPlacementResolver.resolve(intent, physical -> {
            calls.incrementAndGet();
            return Optional.of(FIELD);
        });

        assertEquals(0, calls.get());
        assertFalse(intent.hasPhysicalCoordinates());
        assertFalse(resolved.isPlanetTarget());
        assertTrue(resolved.targetLocalHitFace().isEmpty());
        assertTrue(resolved.playerBodyToTargetLocal(Direction.NORTH).isEmpty());
        assertEquals(new BlockPos(2, 6, 8), intent.targetBlock().pos());
    }

    @Test
    void unboundPhysicalWorldFallsBackWithoutFabricatingLocalDirections() {
        Level level = mock(Level.class);
        PlanetPlacementIntent intent = PlanetPlacementIntent.physical(
                PlanetPlacementIntent.Author.AUTOMATED_ITEM, level,
                BlockPos.ZERO, BlockPos.ZERO, Direction.NORTH,
                Vec3.ZERO, true, Optional.empty()
        );
        PlanetPlacementResolver.Resolution resolved =
                PlanetPlacementResolver.resolve(intent, ignored -> Optional.empty());

        assertTrue(intent.hasPhysicalCoordinates());
        assertFalse(resolved.isPlanetTarget());
        assertTrue(resolved.targetLocalHitFace().isEmpty());
        assertTrue(resolved.targetLocalHitOffset().isEmpty());
        assertTrue(resolved.playerBodyToTargetLocal(Direction.NORTH).isEmpty());
        assertEquals(Direction.NORTH, intent.hitFace());
    }

    @Test
    void rejectsMixedSpacesDifferentLevelsAndInferredNonPlayerBody() {
        Level a = mock(Level.class);
        Level b = mock(Level.class);
        PlanetCoordinateContext.PhysicalBlock physical =
                PlanetCoordinateContext.physical(a, BlockPos.ZERO);
        PlanetCoordinateContext.PhysicalBlock differentLevel =
                PlanetCoordinateContext.physical(b, BlockPos.ZERO);
        PlanetCoordinateContext.ForeignBlock foreign =
                PlanetCoordinateContext.foreign(ResourceLocation.parse("test:virtual"), BlockPos.ZERO);

        assertThrows(IllegalArgumentException.class, () -> new PlanetPlacementIntent(
                PlanetPlacementIntent.Author.UNKNOWN, physical, foreign,
                Direction.UP, Vec3.ZERO, true, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new PlanetPlacementIntent(
                PlanetPlacementIntent.Author.UNKNOWN, physical, differentLevel,
                Direction.UP, Vec3.ZERO, true, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new PlanetPlacementIntent(
                PlanetPlacementIntent.Author.SYNTHETIC, physical, physical,
                Direction.UP, Vec3.ZERO, true,
                Optional.of(new PlanetGravityFrame(PlanetFace.POS_Y))));
        assertThrows(IllegalArgumentException.class, () -> PlanetPlacementIntent.foreign(
                PlanetPlacementIntent.Author.MOD_CUSTOM,
                ResourceLocation.parse("test:virtual"), BlockPos.ZERO, BlockPos.ZERO,
                null, Vec3.ZERO, true));
    }

    @Test
    void minecraftAdapterDoesNotReconstructOriginalHitFromRetargetedPosition() {
        Level level = mock(Level.class);
        BlockPlaceContext context = mock(BlockPlaceContext.class);
        BlockPos originalHit = new BlockPos(10, 9, 8);
        BlockPos actualTarget = new BlockPos(30, 31, 32);
        Vec3 hitLocation = new Vec3(10.1, 9.9, 8.1);
        when(context.getLevel()).thenReturn(level);
        when(context.getClickedPos()).thenReturn(actualTarget);
        when(context.getClickedFace()).thenReturn(Direction.DOWN);
        when(context.getClickLocation()).thenReturn(hitLocation);
        when(context.replacingClickedOnBlock()).thenReturn(false);

        PlanetPlacementIntent intent = PlanetPlacementContextAdapter.fromPhysicalBlockPlaceContext(
                context, originalHit, PlanetPlacementIntent.Author.MOD_CUSTOM);

        assertEquals(originalHit, intent.hitBlock().pos());
        assertEquals(actualTarget, intent.targetBlock().pos());
        assertEquals(Direction.DOWN, intent.hitFace());
        assertEquals(hitLocation, intent.hitLocation());
        assertFalse(intent.replacingHitBlock());
        assertTrue(intent.actorBodyFrame().isEmpty());
    }

    @Test
    void fullRuntimeLookupDoesNotActivateInAnUnboundLevel() {
        Level level = mock(Level.class);
        PlanetPlacementIntent intent = PlanetPlacementIntent.physical(
                PlanetPlacementIntent.Author.UNKNOWN, level,
                BlockPos.ZERO, new BlockPos(10, 10, 10),
                Direction.SOUTH, Vec3.atCenterOf(new BlockPos(10, 10, 10)),
                false, Optional.empty());
        assertFalse(PlanetPlacementResolver.resolve(intent).isPlanetTarget());
    }

    private static BlockPos faceCenter(PlanetFace face) {
        PlanetVector up = new PlanetGravityFrame(face).worldUp();
        return new BlockPos(
                CORE.x() + up.x() * 20,
                CORE.y() + up.y() * 20,
                CORE.z() + up.z() * 20
        );
    }
}
