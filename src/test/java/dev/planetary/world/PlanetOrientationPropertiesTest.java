package dev.planetary.world;

import dev.planetary.api.PlanetPlacementIntent;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

final class PlanetOrientationPropertiesTest {
    private static final PlanetCore CORE = new PlanetCore(3, 100, -7, 20);
    private static final PlanetGravityField FIELD = new PlanetGravityField(CORE);
    private static final Level LEVEL = mock(Level.class);

    @Test
    void sixWayFourWayAndThreeAxesPreservePhysicalValueOnAllFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            PlanetBlockStateFrame frame = PlanetBlockStateFrame.resolve(FIELD, target).orElseThrow();
            var resolution = resolved(target, Direction.UP, Vec3.atCenterOf(target));

            for (Direction local : Direction.values()) {
                Direction physical = frame.localToWorld(local);
                assertEquals(local,
                        PlanetOrientationProperties.facing(resolution, physical).orElseThrow(),
                        face + "/" + local);
                assertEquals(local.getAxis().isHorizontal(),
                        PlanetOrientationProperties.horizontalFacing(resolution, physical).isPresent());
            }
            for (Direction.Axis localAxis : Direction.Axis.values()) {
                Direction.Axis physicalAxis = frame.localAxisToWorld(localAxis);
                assertEquals(localAxis, PlanetOrientationProperties.axis(resolution, physicalAxis)
                        .orElseThrow(), face + "/" + localAxis);
            }
        }
    }

    @Test
    void halfAndAttachmentUseLocalTargetHitAndExplicitSupportDirection() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            PlanetGravityFrame orientation = new PlanetGravityFrame(face);
            PlanetBlockStateFrame canonical = PlanetBlockStateFrame.resolve(FIELD, target).orElseThrow();
            Vec3 low = hitWithLocalOffset(target, orientation, new Vec3(.2, .3, .4));
            Vec3 high = hitWithLocalOffset(target, orientation, new Vec3(.2, .8, .4));

            assertEquals(Half.BOTTOM, PlanetOrientationProperties.halfFromClick(
                    resolved(target, canonical.localToWorld(Direction.UP), high)).orElseThrow());
            assertEquals(Half.TOP, PlanetOrientationProperties.halfFromClick(
                    resolved(target, canonical.localToWorld(Direction.DOWN), low)).orElseThrow());
            assertEquals(Half.BOTTOM, PlanetOrientationProperties.halfFromClick(
                    resolved(target, canonical.localToWorld(Direction.EAST), low)).orElseThrow());
            assertEquals(Half.TOP, PlanetOrientationProperties.halfFromClick(
                    resolved(target, canonical.localToWorld(Direction.EAST), high)).orElseThrow());

            assertEquals(AttachFace.CEILING, PlanetOrientationProperties.attachToSupport(
                    resolved(target, Direction.NORTH, low),
                    canonical.localToWorld(Direction.UP)).orElseThrow());
            assertEquals(AttachFace.FLOOR, PlanetOrientationProperties.attachToSupport(
                    resolved(target, Direction.NORTH, low),
                    canonical.localToWorld(Direction.DOWN)).orElseThrow());
            for (Direction local : Direction.Plane.HORIZONTAL) {
                assertEquals(AttachFace.WALL, PlanetOrientationProperties.attachToSupport(
                        resolved(target, Direction.NORTH, low),
                        canonical.localToWorld(local)).orElseThrow());
            }
        }
    }

    @Test
    void frontTopOnlyAllowsTwelveRealPropertyStatesOnEveryCanonicalFace() {
        assertEquals(12, FrontAndTop.values().length);
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            var resolved = resolved(target, Direction.UP, Vec3.atCenterOf(target));
            PlanetBlockStateFrame frame = PlanetBlockStateFrame.resolve(FIELD, target).orElseThrow();

            for (FrontAndTop value : FrontAndTop.values()) {
                assertEquals(value, PlanetOrientationProperties.frontAndTop(
                        resolved,
                        frame.localToWorld(value.front()),
                        frame.localToWorld(value.top())).orElseThrow());
            }
            assertTrue(PlanetOrientationProperties.frontAndTop(
                    resolved, frame.localToWorld(Direction.NORTH),
                    frame.localToWorld(Direction.SOUTH)).isEmpty());
            // Orthogonal does NOT imply a supported FrontAndTop pair.
            assertTrue(PlanetOrientationProperties.frontAndTop(
                    resolved, frame.localToWorld(Direction.NORTH),
                    frame.localToWorld(Direction.EAST)).isEmpty());
        }
    }

    @Test
    void sixteenRotationSegmentsRespectOwnerSelectedZeroAndRejectVertical() {
        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target = faceCenter(face);
            PlanetGravityFrame frame = new PlanetGravityFrame(face);
            var resolved = resolved(target, Direction.UP, Vec3.atCenterOf(target));

            for (int segment = 0; segment < 16; segment++) {
                double radians = Math.toRadians(segment * 22.5D);
                // NORTH->EAST clockwise, with vanilla local x=EAST, z=SOUTH.
                var localNorth = new PlanetFrameVector(
                        Math.sin(radians), 0, -Math.cos(radians));
                PlanetFrameVector worldNorth = frame.localToWorld(localNorth);
                assertEquals(segment, PlanetOrientationProperties.tangentRotation16(
                        resolved,
                        new Vec3(worldNorth.x(), worldNorth.y(), worldNorth.z()),
                        PlanetOrientationProperties.Rotation16Zero.NORTH_CLOCKWISE
                ).orElseThrow(), face + "/NORTH_0/" + segment);
                assertEquals((segment + 8) & 15, PlanetOrientationProperties.tangentRotation16(
                        resolved,
                        new Vec3(worldNorth.x(), worldNorth.y(), worldNorth.z()),
                        PlanetOrientationProperties.Rotation16Zero.SOUTH_CLOCKWISE
                ).orElseThrow(), face + "/SOUTH_0/" + segment);
            }
            PlanetFrameVector up = frame.localToWorld(new PlanetFrameVector(0, 1, 0));
            assertTrue(PlanetOrientationProperties.tangentRotation16(
                    resolved, new Vec3(up.x(), up.y(), up.z()),
                    PlanetOrientationProperties.Rotation16Zero.NORTH_CLOCKWISE).isEmpty());
        }
    }

    @Test
    void exactEdgesAndCornersAreTargetCanonicalRegardlessOfSourceSide() {
        int[] signs = {-20, 20};
        int count = 0;
        for (int a = 0; a < 3; a++) {
            for (int b = a + 1; b < 3; b++) {
                for (int da : signs) {
                    for (int db : signs) {
                        int[] d = new int[3];
                        d[a] = da; d[b] = db;
                        checkBoundary(new BlockPos(CORE.x()+d[0], CORE.y()+d[1], CORE.z()+d[2]));
                        count++;
                    }
                }
            }
        }
        assertEquals(12, count);
        count = 0;
        for (int x : signs) for (int y : signs) for (int z : signs) {
            checkBoundary(new BlockPos(CORE.x()+x, CORE.y()+y, CORE.z()+z));
            count++;
        }
        assertEquals(8, count);
    }

    @Test
    void explicitPropertyWritePreservesUnrelatedStateAndRejectsIllegalDomain() {
        BlockPos target = faceCenter(PlanetFace.POS_X);
        var resolved = resolved(target, Direction.UP, Vec3.atCenterOf(target));
        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        assertEquals(Direction.Axis.Z,
                PlanetOrientationProperties.withProperty(resolved, log,
                        RotatedPillarBlock.AXIS, Direction.Axis.Z)
                        .orElseThrow().getValue(RotatedPillarBlock.AXIS));
        assertTrue(PlanetOrientationProperties.withProperty(resolved, log,
                net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16, 5)
                .isEmpty());
        assertEquals(Direction.Axis.Y, log.getValue(RotatedPillarBlock.AXIS));
    }

    @Test
    void foreignOrNoPlanetDefaultToNoImplicitPropertyRewrite() {
        BlockPos target = faceCenter(PlanetFace.POS_X);
        var intent = PlanetPlacementIntent.physical(
                PlanetPlacementIntent.Author.MOD_CUSTOM, LEVEL,
                target, target, Direction.NORTH, Vec3.atCenterOf(target),
                true, Optional.empty());
        var noPlanet = PlanetPlacementResolver.resolve(intent, ignored -> Optional.empty());
        assertTrue(PlanetOrientationProperties.facing(noPlanet, Direction.NORTH).isEmpty());
        assertTrue(PlanetOrientationProperties.axis(noPlanet, Direction.Axis.X).isEmpty());
        assertTrue(PlanetOrientationProperties.halfFromClick(noPlanet).isEmpty());
        assertTrue(PlanetOrientationProperties.withProperty(noPlanet,
                Blocks.OAK_LOG.defaultBlockState(), RotatedPillarBlock.AXIS,
                Direction.Axis.X).isEmpty());

        var foreign = PlanetPlacementIntent.foreign(PlanetPlacementIntent.Author.MOD_CUSTOM,
                ResourceLocation.parse("test:contraption"), target, target,
                Direction.NORTH, Vec3.atCenterOf(target), false);
        AtomicInteger count = new AtomicInteger();
        var virtual = PlanetPlacementResolver.resolve(foreign, ignored -> {
            count.incrementAndGet(); return Optional.of(FIELD);
        });
        assertEquals(0, count.get());
        assertTrue(PlanetOrientationProperties.tangentRotation16(virtual,
                new Vec3(1, 0, 0),
                PlanetOrientationProperties.Rotation16Zero.NORTH_CLOCKWISE).isEmpty());
    }

    @Test
    void stairFullCornerGraphMatchesVanillaAcrossAllFourDirectionsAndHalves() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                var frontLeft = new PlanetStairPlacement.StairNeighbor(
                        Blocks.OAK_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF, half),
                        facing.getCounterClockWise());
                var frontRight = new PlanetStairPlacement.StairNeighbor(
                        Blocks.QUARTZ_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF, half),
                        facing.getClockWise());
                var backLeft = frontLeft;
                var backRight = frontRight;
                assertEquals(net.minecraft.world.level.block.state.properties.StairsShape.OUTER_LEFT,
                        PlanetStairPlacement.shape(facing, half,
                                direction -> direction == facing
                                        ? Optional.of(frontLeft) : Optional.empty()));
                assertEquals(net.minecraft.world.level.block.state.properties.StairsShape.OUTER_RIGHT,
                        PlanetStairPlacement.shape(facing, half,
                                direction -> direction == facing
                                        ? Optional.of(frontRight) : Optional.empty()));
                assertEquals(net.minecraft.world.level.block.state.properties.StairsShape.INNER_LEFT,
                        PlanetStairPlacement.shape(facing, half,
                                direction -> direction == facing.getOpposite()
                                        ? Optional.of(backLeft) : Optional.empty()));
                assertEquals(net.minecraft.world.level.block.state.properties.StairsShape.INNER_RIGHT,
                        PlanetStairPlacement.shape(facing, half,
                                direction -> direction == facing.getOpposite()
                                        ? Optional.of(backRight) : Optional.empty()));
                assertEquals(net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT,
                        PlanetStairPlacement.shape(facing, half,
                                direction -> Optional.empty()));

                var blocker = new PlanetStairPlacement.StairNeighbor(
                        Blocks.COPPER_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF, half),
                        facing);
                assertEquals(net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT,
                        PlanetStairPlacement.shape(facing, half, direction ->
                                direction == facing ? Optional.of(frontLeft)
                                        : direction == facing.getClockWise()
                                        ? Optional.of(blocker) : Optional.empty()));
            }
        }
    }

    @Test
    void stairCrossSeamHeadingTransportIsInvertibleForSixFaces() {
        for (PlanetFace face : PlanetFace.values()) {
            for (Direction localCross : Direction.Plane.HORIZONTAL) {
                var transition = dev.planetary.topology.PlanetTopology.edgeTransform(
                        face, PlanetVanillaDirection.fromVanilla(localCross));
                for (Direction heading : Direction.Plane.HORIZONTAL) {
                    Direction transported = PlanetVanillaDirection.toVanilla(
                            transition.transformDirection(
                                    PlanetVanillaDirection.fromVanilla(heading)));
                    assertEquals(heading, PlanetStairPlacement.facingInSource(
                            face, transition.targetFace(), localCross, transported).orElseThrow());
                }
            }
        }
    }

    private static void checkBoundary(BlockPos target) {
        PlanetBlockStateFrame canonical = PlanetBlockStateFrame.resolve(FIELD, target).orElseThrow();
        var resolved = resolved(target, Direction.UP, Vec3.atCenterOf(target));
        assertEquals(canonical.face(), resolved.targetPlacementFrame()
                .orElseThrow().targetStateFrame().face());
        for (Direction local : Direction.values()) {
            Direction physical = canonical.localToWorld(local);
            assertEquals(local, PlanetOrientationProperties.facing(resolved, physical).orElseThrow());
            assertEquals(physical, canonical.localToWorld(
                    PlanetOrientationProperties.facing(resolved, physical).orElseThrow()));
        }
    }

    private static PlanetPlacementResolver.Resolution resolved(
            BlockPos target, Direction physicalHitFace, Vec3 worldClick
    ) {
        PlanetPlacementIntent intent = PlanetPlacementIntent.physical(
                PlanetPlacementIntent.Author.UNKNOWN, LEVEL,
                target.relative(physicalHitFace.getOpposite()), target,
                physicalHitFace, worldClick, false, Optional.empty());
        return PlanetPlacementResolver.resolve(intent, ignored -> Optional.of(FIELD));
    }

    private static Vec3 hitWithLocalOffset(
            BlockPos target, PlanetGravityFrame frame, Vec3 localOffset
    ) {
        PlanetFrameVector rotated = frame.localToWorld(new PlanetFrameVector(
                localOffset.x - .5, localOffset.y - .5, localOffset.z - .5));
        return Vec3.atCenterOf(target).add(rotated.x(), rotated.y(), rotated.z());
    }

    private static BlockPos faceCenter(PlanetFace face) {
        PlanetVector up = new PlanetGravityFrame(face).worldUp();
        return new BlockPos(CORE.x() + up.x()*20,
                CORE.y() + up.y()*20, CORE.z() + up.z()*20);
    }
}
