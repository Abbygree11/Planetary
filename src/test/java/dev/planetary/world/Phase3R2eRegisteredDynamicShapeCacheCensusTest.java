package dev.planetary.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 3R-2e research-only NeoForge 21.1.215 runtime registry census.
 * Measures real per-ID BlockState cache eligibility and population, NOT
 * the presence or absence of dynamic modded VoxelShape callbacks.
 *
 * No production, mixin or gameplay changes.
 */
final class Phase3R2eRegisteredDynamicShapeCacheCensusTest {
    private static final Path REPORT_DIR = Path.of(System.getProperty(
            "planetary.phase2.censusReportDir", "build/reports/planetary"));

    private record Counts(int ids, int states, int cachedStates, int uncachedStates,
                          int dynamicIds, int offsetStates) {
    }

    @Test
    void censusAllRegisteredBlocksAndAllPossibleStates() throws IOException {
        Bootstrap.bootStrap();

        Field cache;
        try {
            cache = BlockBehaviour.BlockStateBase.class.getDeclaredField("cache");
            cache.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("NeoForge 21.1.215 BlockStateBase cache ABI missing", e);
        }

        List<String> perId = new ArrayList<>();
        perId.add("block_id\tjava_class\tstate_count\thasDynamicShape"
                + "\tcached_state_count\tuncached_state_count"
                + "\tstates_with_offset_function\tevidence");

        Map<String, long[]> families = new HashMap<>();
        int ids = 0;
        int states = 0;
        int cached = 0;
        int dynamicIds = 0;
        int offsetStates = 0;
        List<String> idsOutOfInvariant = new ArrayList<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            ids++;
            String id = BuiltInRegistries.BLOCK.getKey(block).toString();
            String owner = block.getClass().getName();
            boolean dynamic = block.hasDynamicShape();
            int blockStates = 0;
            int blockCached = 0;
            int blockOffset = 0;
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                blockStates++;
                Object present;
                try {
                    present = cache.get(state);
                } catch (IllegalAccessException e) {
                    throw new AssertionError("Cannot reflect original BlockState cache", e);
                }
                boolean cachePresent = present != null;
                if (cachePresent) blockCached++;
                if (state.hasOffsetFunction()) blockOffset++;
                if (dynamic == cachePresent) {
                    idsOutOfInvariant.add(id + ":" + state + ":" + dynamic);
                }
            }
            assertTrue(blockStates > 0, "No valid BlockStates for " + id);
            if (dynamic) dynamicIds++;
            states += blockStates;
            cached += blockCached;
            offsetStates += blockOffset;

            long[] family = families.computeIfAbsent(owner, k -> new long[7]);
            family[0]++; // IDs
            family[1] += blockStates;
            family[2] += blockCached;
            family[3] += blockStates - blockCached;
            family[4] += dynamic ? 1 : 0;
            family[5] += blockOffset;
            family[6] += !dynamic ? 1 : 0;

            perId.add(String.join("\t", id, owner,
                    Integer.toString(blockStates), Boolean.toString(dynamic),
                    Integer.toString(blockCached), Integer.toString(blockStates - blockCached),
                    Integer.toString(blockOffset),
                    "ORIGINAL_NEOFORGE_21_1_215_BOOTSTRAPPED_JVM_REFLECTION"));
        }

        assertEquals(1060, ids, "Pinned vanilla NeoForge registry");
        assertEquals(241, families.size(), "Pinned vanilla declaring class families");
        assertTrue(idsOutOfInvariant.isEmpty(),
                "BlockState cache eligibility differs from block.dynamicShape: "
                        + idsOutOfInvariant.stream().limit(5).toList());
        assertEquals(states, cached + families.values().stream().mapToInt(a -> (int)a[3]).sum());
        assertTrue(states >= ids);
        assertTrue(dynamicIds > 0, "Expect dynamic-shaped vanilla blocks");

        List<String> perClass = new ArrayList<>();
        perClass.add("java_class\tregistered_block_ids\tblockstate_variants"
                + "\tcached_states\tuncached_states\tdynamic_block_ids"
                + "\tstates_with_offset_function\tstatic_block_ids\tevidence");
        families.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            long[] a = e.getValue();
            perClass.add(String.join("\t", e.getKey(),
                    Long.toString(a[0]), Long.toString(a[1]), Long.toString(a[2]),
                    Long.toString(a[3]), Long.toString(a[4]), Long.toString(a[5]),
                    Long.toString(a[6]),
                    "ORIGINAL_NEOFORGE_21_1_215_BOOTSTRAPPED_JVM_REFLECTION"));
        });

        perId.subList(1, perId.size()).sort(Comparator.naturalOrder());
        Files.createDirectories(REPORT_DIR);
        Files.write(REPORT_DIR.resolve("phase3-neo1211-block-dynamic-cache.tsv"),
                perId, StandardCharsets.UTF_8);
        Files.write(REPORT_DIR.resolve("phase3-neo1211-class-dynamic-cache.tsv"),
                perClass, StandardCharsets.UTF_8);
        Files.writeString(REPORT_DIR.resolve("phase3-neo1211-dynamic-cache-summary.txt"),
                "Original NeoForge 21.1.215 blocks=" + ids
                + " registered classes=" + families.size()
                + " total BlockState variants=" + states
                + " cached BlockState variants=" + cached
                + " uncached variants=" + (states - cached)
                + " dynamicBlockIds=" + dynamicIds
                + " offsetStates=" + offsetStates + System.lineSeparator(),
                StandardCharsets.UTF_8);
        assertEquals(1061, perId.size());
        assertEquals(242, perClass.size());
    }
}
