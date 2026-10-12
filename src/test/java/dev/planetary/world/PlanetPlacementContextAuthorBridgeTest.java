package dev.planetary.world;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.api.PlanetOriginalHitSource;
import dev.planetary.api.PlanetPlacementIntent;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class PlanetPlacementContextAuthorBridgeTest {
    private static final PlanetGravityField FIELD =
            new PlanetGravityField(new PlanetCore(0, 0, 0, 20));

    @Test
    void originalPhysicalHitAndRetargetedBlockHaveIndependentFramesAtSeam() {
        Level level = mock(Level.class);
        BlockPlaceContext ctx = mock(BlockPlaceContext.class,
                withSettings().extraInterfaces(PlanetOriginalHitSource.class));
        BlockPos hitPos = new BlockPos(20, 21, 0);
        BlockPos target = new BlockPos(20, 20, 0);
        BlockHitResult hit = new BlockHitResult(
                new Vec3(20.5, 21.0, 0.5), Direction.UP, hitPos, false);
        when(((PlanetOriginalHitSource) ctx).planetary$originalHit()).thenReturn(hit);
        when(ctx.getLevel()).thenReturn(level);
        when(ctx.getClickedPos()).thenReturn(target);
        when(ctx.getClickedFace()).thenReturn(Direction.DOWN);
        when(ctx.getClickLocation()).thenReturn(new Vec3(100, 100, 100));
        when(ctx.replacingClickedOnBlock()).thenReturn(false);
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            var intent = PlanetPlacementContextAdapter.fromPhysicalBlockPlaceContext(
                    ctx, PlanetPlacementIntent.Author.MOD_CUSTOM).orElseThrow();
            assertEquals(hitPos, intent.hitBlock().pos());
            assertEquals(target, intent.targetBlock().pos());
            assertEquals(Direction.UP, intent.hitFace());
            assertEquals(hit.getLocation(), intent.hitLocation());
            assertFalse(intent.replacingHitBlock());
            assertEquals(PlanetPlacementIntent.Author.MOD_CUSTOM, intent.author());

            var resolved = PlanetPlacementContextAdapter.resolvePhysical(
                    ctx, PlanetPlacementIntent.Author.MOD_CUSTOM).orElseThrow();
            assertEquals(PlanetFace.POS_Y, resolved.hitStateFrame().orElseThrow().face());
            assertEquals(PlanetFace.POS_X,
                    resolved.targetPlacementFrame().orElseThrow().targetStateFrame().face());
            assertEquals(Direction.WEST, resolved.targetLocalHitFace().orElseThrow());
            var shared = PlanetFrameApi.placementFrame(ctx).orElseThrow();
            assertEquals(Direction.WEST, shared.localClickedFace());
            assertEquals(hit.getLocation(), shared.worldClickLocation());
            assertEquals(target, shared.targetPos());
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }

    @Test
    void absenceOfMixinNeverFabricatesHitFromFinalTarget() {
        BlockPlaceContext ctx = mock(BlockPlaceContext.class);
        assertTrue(PlanetPlacementContextAdapter.fromPhysicalBlockPlaceContext(
                ctx, PlanetPlacementIntent.Author.UNKNOWN).isEmpty());
        assertTrue(PlanetPlacementContextAdapter.resolvePhysical(
                ctx, PlanetPlacementIntent.Author.UNKNOWN).isEmpty());
    }

    @Test
    void ordinaryWorldHasNoLocalFrameAndNoInferredPlayerBody() {
        Level level = mock(Level.class);
        BlockPlaceContext ctx = mock(BlockPlaceContext.class,
                withSettings().extraInterfaces(PlanetOriginalHitSource.class));
        BlockPos pos = new BlockPos(20, 0, 0);
        when(((PlanetOriginalHitSource) ctx).planetary$originalHit()).thenReturn(
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.EAST, pos, false));
        when(ctx.getLevel()).thenReturn(level);
        when(ctx.getClickedPos()).thenReturn(pos);
        var intent = PlanetPlacementContextAdapter.fromPhysicalBlockPlaceContext(
                ctx, PlanetPlacementIntent.Author.AUTOMATED_ITEM).orElseThrow();
        assertTrue(intent.actorBodyFrame().isEmpty());
        assertFalse(PlanetPlacementContextAdapter.resolvePhysical(
                ctx, PlanetPlacementIntent.Author.AUTOMATED_ITEM).orElseThrow().isPlanetTarget());
        assertTrue(PlanetFrameApi.placementFrame(ctx).isEmpty());
    }

    @Test
    void unwovenUnitTestContextsPreservePreviousCanonicalPlacementFrame() {
        Level level = mock(Level.class);
        BlockPlaceContext ctx = mock(BlockPlaceContext.class);
        BlockPos target = new BlockPos(20, 0, 0);
        when(ctx.getLevel()).thenReturn(level);
        when(ctx.getClickedPos()).thenReturn(target);
        when(ctx.getClickedFace()).thenReturn(Direction.EAST);
        when(ctx.getClickLocation()).thenReturn(Vec3.atCenterOf(target));
        PlanetGravityRuntime.bind(level, FIELD);
        try {
            var frame = PlanetFrameApi.placementFrame(ctx).orElseThrow();
            assertEquals(PlanetFace.POS_X, frame.targetFace());
            assertEquals(Direction.UP, frame.localClickedFace());
        } finally {
            PlanetGravityRuntime.unbind(level, FIELD);
        }
    }
}
