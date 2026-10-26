package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Planetary 3R-2j RESEARCH ONLY. Execute the original registered vanilla
 * MOVING_PISTON BlockState -> Block -> PistonMovingBlockEntity virtual path.
 * Never create new Block instances: the target NeoForge test bootstrap freezes
 * its intrusive Block registry.
 *
 * Identical BlockState can have different collision shapes depending on
 * BlockGetter identity, BlockPos and the block entity obtained from that getter.
 * This demonstrates dynamic collision source context, NOT proof of a modded
 * face-occlusion-cache bug nor a fix to piston movement.
 */
final class Phase3R2jDynamicShapeAndForeignGetterDispatchTest {
    private static final BlockPos EVEN = new BlockPos(2, 70, 4);
    private static final BlockPos ODD = new BlockPos(3, 70, 4);

    private static final class SyntheticGetter implements BlockGetter {
        private final String name;
        private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
        private BlockState state;

        SyntheticGetter(String name) {
            this.name = name;
        }
        void put(BlockPos pos, BlockEntity entity) {
            blockEntities.put(pos, entity);
        }

        @Override public BlockEntity getBlockEntity(BlockPos pos) {
            return blockEntities.get(pos);
        }
        @Override public BlockState getBlockState(BlockPos pos) {
            return state;
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

    private static PistonMovingBlockEntity moving(BlockPos pos, BlockState movingState,
                                                   BlockState movedState) {
        return new PistonMovingBlockEntity(
                pos, movingState, movedState, Direction.UP, true, false);
    }

    @Test
    void originalRegisteredMovingPistonVirtualDispatchDependsOnGetterPositionAndBlockEntity()
            throws Exception {
        Bootstrap.bootStrap();
        BlockState identicalState = Blocks.MOVING_PISTON.defaultBlockState();
        SyntheticGetter planet = new SyntheticGetter("planet");
        SyntheticGetter foreign = new SyntheticGetter("foreign");
        planet.state = identicalState;
        foreign.state = identicalState;

        planet.put(EVEN, moving(EVEN, identicalState, Blocks.STONE.defaultBlockState()));
        planet.put(ODD, moving(ODD, identicalState, Blocks.AIR.defaultBlockState()));
        foreign.put(EVEN, moving(EVEN, identicalState, Blocks.AIR.defaultBlockState()));

        assertSame(planet.getBlockState(EVEN), foreign.getBlockState(EVEN));
        assertSame(planet.getBlockState(EVEN), planet.getBlockState(ODD));

        List<String> rows = new ArrayList<>();
        rows.add("case\tgetter\tposition\tmoved_state\tcollision_nonempty");
        record Probe(String id, SyntheticGetter getter, BlockPos pos, boolean expected) {}
        List<Probe> initial = List.of(
                new Probe("planet_even_stone", planet, EVEN, true),
                new Probe("planet_odd_air", planet, ODD, false),
                new Probe("foreign_even_air", foreign, EVEN, false));
        for (Probe probe : initial) {
            boolean collision = !identicalState.getCollisionShape(
                    probe.getter(), probe.pos()).isEmpty();
            assertEquals(probe.expected(), collision, probe.id());
            rows.add(String.join("\t", probe.id(), probe.getter().name,
                    probe.pos().toShortString(),
                    probe.expected() ? "stone" : "air", Boolean.toString(collision)));
        }

        // SAME BlockState, BlockGetter, BlockPos and direction; only BlockEntity
        // content changed, simulating a moving piston transition/replacement.
        planet.put(EVEN, moving(EVEN, identicalState, Blocks.AIR.defaultBlockState()));
        boolean changed = !identicalState.getCollisionShape(planet, EVEN).isEmpty();
        assertFalse(changed, "After BE swap the moving block shape must change");
        rows.add(String.join("\t", "planet_even_air_after_be_change", "planet",
                EVEN.toShortString(), "air", Boolean.toString(changed)));

        planet.put(EVEN, moving(EVEN, identicalState, Blocks.STONE.defaultBlockState()));
        boolean restored = !identicalState.getCollisionShape(planet, EVEN).isEmpty();
        assertTrue(restored, "Moving block shape must update after BE restoration");
        rows.add(String.join("\t", "planet_even_stone_restored", "planet",
                EVEN.toShortString(), "stone", Boolean.toString(restored)));

        assertEquals(6, rows.size()); // header + five context samples
        Path out = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(out);
        Files.write(out.resolve("phase3-r2j-original-moving-be-context-dispatch.tsv"),
                rows, StandardCharsets.UTF_8);
        Files.writeString(out.resolve("phase3-r2j-original-moving-be-context-summary.txt"),
                "originalNeoForge=21.1.215\n"
                + "registeredBlock=minecraft:moving_piston\n"
                + "uniqueBlockStates=1\ngetterIdentities=2\npositions=2\n"
                + "selectedContextSamples=5\n"
                + "sameStateSameGetterSamePositionBlockEntitySwap=true\n"
                + "source=registeredVanillaBlockState_to_Block_to_PistonMovingBlockEntity\n"
                + "limitation=ORIGINAL_COLLISION_ONLY_NOT_FACE_OCCLUSION_OR_LOADED_MOD\n",
                StandardCharsets.UTF_8);
    }
}
