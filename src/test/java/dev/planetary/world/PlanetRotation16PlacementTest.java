package dev.planetary.world;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

final class PlanetRotation16PlacementTest {
    @Test
    void sixteenActorYawPositionsPreserveAllFourOriginalOwnerRulesOnSixFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            var frame = new PlanetGravityFrame(face);
            for (int i = 0; i < 16; i++) {
                float yaw = i * 22.5f;
                var output = PlanetRotation16Placement.rotation(
                        frame, frame, yaw).orElseThrow();
                assertEquals(RotationSegment.convertToSegment(yaw),
                        output.southZero(), "Skull " + face + "/" + yaw);
                assertEquals(RotationSegment.convertToSegment(yaw + 180f),
                        output.northZero(), "Standing sign/banner " + face + "/" + yaw);
                assertEquals(RotationSegment.convertToSegment(
                                Direction.fromYRot(yaw).getOpposite()),
                        output.snappedCeiling(), "Rigid ceiling " + face + "/" + yaw);
            }
        }
    }

    @Test
    void sourceToTargetChartTransportIsNotActorLocalIdentity() {
        var actor = new PlanetGravityFrame(PlanetFace.POS_Y);
        var target = new PlanetGravityFrame(PlanetFace.POS_X);
        var rotation = PlanetRotation16Placement.rotation(
                actor, target, -90f).orElseThrow();
        assertNotEquals(PlanetRotation16Placement.rotation(
                actor, actor, -90f).orElseThrow(), rotation);
        // Actor +Y WEST/EAST vector remains physical, then target projection.
        var localActor = new dev.planetary.topology.PlanetFrameVector(
                -Math.sin(Math.toRadians(-90)), 0,
                Math.cos(Math.toRadians(-90)));
        var localTarget = target.worldToLocal(actor.localToWorld(localActor));
        assertEquals(PlanetOrientationProperties.targetTangentRotation16(
                localTarget,
                PlanetOrientationProperties.Rotation16Zero.NORTH_CLOCKWISE
        ).orElseThrow(), rotation.northZero());
    }

    @Test
    void headingParallelToTargetLocalUpIsNotInvented() {
        var actor = new PlanetGravityFrame(PlanetFace.POS_Y);
        var target = new PlanetGravityFrame(PlanetFace.POS_Z);
        assertEquals(Optional.empty(), PlanetRotation16Placement.rotation(
                actor, target, 0));
        assertEquals(Optional.empty(), PlanetRotation16Placement.rotation(
                actor, target, Float.NaN));
        assertEquals(Optional.empty(), PlanetRotation16Placement.rotation(
                actor, target, Float.POSITIVE_INFINITY));
    }

    @Test
    void missingPlayerAndForeignContextsArePurePassthrough() {
        var context = org.mockito.Mockito.mock(
                net.minecraft.world.item.context.BlockPlaceContext.class);
        var original = net.minecraft.world.level.block.Blocks.OAK_SIGN.defaultBlockState();
        assertTrue(PlanetRotation16Placement.afterPlacement(
                context, original,
                PlanetRotation16Placement.Owner.STANDING_SIGN_OR_BANNER).isEmpty());
        assertTrue(PlanetRotation16Placement.afterPlacement(
                context, null, PlanetRotation16Placement.Owner.SKULL).isEmpty());
        assertTrue(PlanetRotation16Placement.afterPlacement(
                context, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
                PlanetRotation16Placement.Owner.SKULL).isEmpty());
    }
}
