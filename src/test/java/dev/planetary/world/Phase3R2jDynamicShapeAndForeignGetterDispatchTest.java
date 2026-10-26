package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Planetary 3R-2j RESEARCH ONLY. A synthetic Block implementing the original
 * NeoForge1.21.1 Block override ABI demonstrates actual JVM virtual dispatch
 * with identical BlockState under distinct BlockGetter, BlockPos and mutable
 * context (analogue of BlockEntity/tick-driven data).
 *
 * This does not prove any actual third-party mod's behavior, actual client
 * Mixin application or fake-world frame provenance.
 */
final class Phase3R2jDynamicShapeAndForeignGetterDispatchTest {
    private static final BlockPos EVEN = new BlockPos(2, 70, 4);
    private static final BlockPos ODD = new BlockPos(3, 70, 4);
    private static final Direction FACE = Direction.UP;

    private static final class SyntheticGetter implements BlockGetter {
        private final String name;
        private final AtomicBoolean active = new AtomicBoolean(true);
        private BlockState blockState;

        SyntheticGetter(String name) {
            this.name = name;
        }

        @Override public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }
        @Override public BlockState getBlockState(BlockPos pos) {
            return blockState;
        }
        @Override public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.defaultFluidState();
        }
        @Override public int getHeight() {
            return 384;
        }
        @Override public int getMinBuildHeight() {
            return -64;
        }
    }

    private static final class SyntheticContextualBlock extends Block {
        private final AtomicInteger collisionCalls = new AtomicInteger();
        private final AtomicInteger occlusionCalls = new AtomicInteger();
        private final AtomicInteger supportCalls = new AtomicInteger();
        private final AtomicInteger hideCalls = new AtomicInteger();
        private final AtomicInteger externalHideCalls = new AtomicInteger();

        SyntheticContextualBlock() {
            super(BlockBehaviour.Properties.of().dynamicShape());
        }

        private static boolean full(BlockGetter getter, BlockPos pos) {
            return getter instanceof SyntheticGetter s
                    && s.active.get()
                    && "planet".equals(s.name)
                    && (pos.getX() & 1) == 0;
        }

        private static VoxelShape shape(BlockGetter getter, BlockPos pos) {
            return full(getter, pos) ? Shapes.block() : Shapes.empty();
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter getter,
                                            BlockPos pos, CollisionContext context) {
            collisionCalls.incrementAndGet();
            return shape(getter, pos);
        }

        @Override
        public VoxelShape getOcclusionShape(BlockState state, BlockGetter getter,
                                            BlockPos pos) {
            occlusionCalls.incrementAndGet();
            return shape(getter, pos);
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter getter,
                                                BlockPos pos) {
            supportCalls.incrementAndGet();
            return shape(getter, pos);
        }

        @Override
        public boolean hidesNeighborFace(BlockState state, BlockGetter getter,
                                         BlockPos pos, BlockState neighborState,
                                         Direction direction) {
            hideCalls.incrementAndGet();
            return full(getter, pos);
        }

        @Override
        public boolean supportsExternalFaceHiding(BlockState state) {
            externalHideCalls.incrementAndGet();
            return true;
        }
    }

    @Test
    void originalNeoForgeBlockStateVirtualDispatchRetainsGetterPositionAndMutableContext()
            throws Exception {
        Bootstrap.bootStrap();
        var block = new SyntheticContextualBlock();
        BlockState identicalState = block.defaultBlockState();
        SyntheticGetter planet = new SyntheticGetter("planet");
        SyntheticGetter foreign = new SyntheticGetter("foreign");
        planet.blockState = identicalState;
        foreign.blockState = identicalState;

        assertSame(planet.getBlockState(EVEN), foreign.getBlockState(EVEN));
        assertSame(planet.getBlockState(EVEN), planet.getBlockState(ODD));

        List<String> result = new ArrayList<>();
        result.add("case\tgetter\tposition\ttoggle\tcollision_full\tocclusion_full"
                + "\tsupport_full\tface_occlusion_full\thides_neighbor_face");

        record Probe(String label, SyntheticGetter getter, BlockPos pos,
                     boolean active, boolean expected) {}
        List<Probe> probes = List.of(
                new Probe("planet_even_on", planet, EVEN, true, true),
                new Probe("planet_odd_on", planet, ODD, true, false),
                new Probe("foreign_even_on", foreign, EVEN, true, false),
                new Probe("planet_even_off", planet, EVEN, false, false),
                new Probe("planet_even_restored", planet, EVEN, true, true)
        );
        for (Probe sample : probes) {
            sample.getter().active.set(sample.active());
            boolean collision = Shapes.block().equals(
                    identicalState.getCollisionShape(sample.getter(), sample.pos()));
            boolean occlusion = Shapes.block().equals(
                    identicalState.getOcclusionShape(sample.getter(), sample.pos()));
            boolean support = Shapes.block().equals(
                    identicalState.getBlockSupportShape(sample.getter(), sample.pos()));
            boolean faceOcclusion = Shapes.block().equals(
                    identicalState.getFaceOcclusionShape(sample.getter(), sample.pos(), FACE));
            boolean hide = identicalState.hidesNeighborFace(
                    sample.getter(), sample.pos(), identicalState, FACE);
            assertEquals(sample.expected(), collision, sample.label() + ": collision");
            assertEquals(sample.expected(), occlusion, sample.label() + ": occlusion");
            assertEquals(sample.expected(), support, sample.label() + ": support");
            assertEquals(sample.expected(), faceOcclusion,
                    sample.label() + ": face occlusion");
            assertEquals(sample.expected(), hide, sample.label() + ": mod face hook");
            assertTrue(identicalState.supportsExternalFaceHiding(),
                    "Synthetic Block virtual override lost by IBlockStateExtension");
            result.add(String.join("\t", sample.label(), sample.getter().name,
                    sample.pos().toShortString(), Boolean.toString(sample.active()),
                    Boolean.toString(collision), Boolean.toString(occlusion),
                    Boolean.toString(support), Boolean.toString(faceOcclusion),
                    Boolean.toString(hide)));
        }

        assertTrue(block.collisionCalls.get() >= probes.size());
        assertTrue(block.occlusionCalls.get() >= probes.size());
        assertTrue(block.supportCalls.get() >= probes.size());
        assertTrue(block.hideCalls.get() >= probes.size());
        assertTrue(block.externalHideCalls.get() >= probes.size());
        Path out = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(out);
        Files.write(out.resolve("phase3-r2j-synthetic-dynamic-context-dispatch.tsv"),
                result, StandardCharsets.UTF_8);
        Files.writeString(out.resolve("phase3-r2j-synthetic-dynamic-context-summary.txt"),
                "originalNeoForge=21.1.215\n"
                + "uniqueBlockStates=1\ngetterIdentities=2\npositions=2\n"
                + "selectedContextSamples=" + probes.size() + "\n"
                + "mutableSameStateToggle=true\n"
                + "dynamicBlock=true\n"
                + "collisionOverrides=" + block.collisionCalls.get() + "\n"
                + "occlusionOverrides=" + block.occlusionCalls.get() + "\n"
                + "supportOverrides=" + block.supportCalls.get() + "\n"
                + "hideOverrides=" + block.hideCalls.get() + "\n"
                + "externalHideOverrides=" + block.externalHideCalls.get() + "\n"
                + "limitation=SYNTHETIC_BLOCK_NOT_MODDED_CLIENT_OR_BLOCKENTITY\n",
                StandardCharsets.UTF_8);
    }
}
