package dev.planetary.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Research-only Phase 3R-2c, exact NeoForge 21.1.215 JVM reflection.
 * No production changes. All registered blocks, all four remaining
 * contextual/shape APIs, including intermediate unregistered ancestors.
 *
 * Runtime class-reflection evidence != applied Mixin or gameplay acceptance.
 */
final class Phase3R2cOriginalShapeOwnerCensusTest {
    private static final Path REPORT_DIR = Path.of(System.getProperty(
            "planetary.phase2.censusReportDir", "build/reports/planetary"));

    private record Signature(String name, Class<?>[] parameters) {
        String signature() {
            return name + "(" + String.join(",",
                    java.util.Arrays.stream(parameters).map(Class::getName).toList()) + ")";
        }
    }

    private static final List<Signature> APIS = List.of(
            new Signature("getVisualShape", new Class<?>[]{
                    BlockState.class, BlockGetter.class, BlockPos.class,
                    CollisionContext.class}),
            new Signature("getInteractionShape", new Class<?>[]{
                    BlockState.class, BlockGetter.class, BlockPos.class}),
            new Signature("getOcclusionShape", new Class<?>[]{
                    BlockState.class, BlockGetter.class, BlockPos.class}),
            new Signature("getBlockSupportShape", new Class<?>[]{
                    BlockState.class, BlockGetter.class, BlockPos.class})
    );

    private static Method declaredExact(Class<?> type, Signature sig) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equals(sig.name())
                    && java.util.Arrays.equals(m.getParameterTypes(), sig.parameters())
                    && !m.isSynthetic() && !m.isBridge()) {
                assertEquals(VoxelShape.class, m.getReturnType(),
                        type.getName() + "#" + sig.name());
                assertFalse(Modifier.isStatic(m.getModifiers()));
                return m;
            }
        }
        return null;
    }

    private static String hierarchy(Class<?> type) {
        List<String> chain = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            chain.add(current.getSimpleName());
        }
        return String.join(">", chain);
    }

    private static String exactOwner(Class<?> type, Signature sig) {
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            Method found = declaredExact(current, sig);
            if (found != null) {
                return current.getName();
            }
        }
        fail("No exact original method " + sig.signature() + " in " + type);
        return "MISSING";
    }

    @Test
    void actualNeoForgeRegisteredFullFourShapeApiEffectiveOwners()
            throws IOException {
        Bootstrap.bootStrap();
        Map<String, Class<?>> classes = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        int ids = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            ids++;
            String name = block.getClass().getName();
            classes.put(name, block.getClass());
            counts.merge(name, 1, Integer::sum);
        }
        assertEquals(1060, ids, "Pinned original 21.1.215 vanilla registry");
        assertEquals(241, classes.size(), "Pinned original 21.1.215 class families");

        List<String> report = new ArrayList<>();
        report.add("java_class\tregistered_block_ids\toriginal_exact_hierarchy"
                + "\tgetVisualShape_exact_owner\tgetInteractionShape_exact_owner"
                + "\tgetOcclusionShape_exact_owner\tgetBlockSupportShape_exact_owner"
                + "\toriginal_compiled_method_signatures\tevidence");
        List<Class<?>> sorted = classes.values().stream()
                .sorted(Comparator.comparing(Class::getName)).toList();
        for (Class<?> type : sorted) {
            List<String> owners = new ArrayList<>();
            for (Signature api : APIS) {
                owners.add(exactOwner(type, api));
            }
            String signatures = String.join(";", APIS.stream()
                    .map(Signature::signature).toList());
            report.add(String.join("\t", type.getName(),
                    Integer.toString(counts.get(type.getName())), hierarchy(type),
                    owners.get(0), owners.get(1), owners.get(2), owners.get(3),
                    signatures, "ORIGINAL_NEOFORGE_21_1_215_JVM_REFLECTION"));
        }
        Files.createDirectories(REPORT_DIR);
        Files.write(REPORT_DIR.resolve(
                "phase3-neo1211-four-shape-effective-owners.tsv"),
                report, StandardCharsets.UTF_8);
        Files.writeString(REPORT_DIR.resolve(
                "phase3-neo1211-four-shape-owner-summary.txt"),
                "Original NeoForge 21.1.215 registry class families="
                + classes.size() + ", block IDs=" + ids
                + ", exact method signatures=4, generated rows="
                + (report.size() - 1) + System.lineSeparator(),
                StandardCharsets.UTF_8);
        assertEquals(242, report.size());
        assertEquals(Set.of("getVisualShape", "getInteractionShape",
                "getOcclusionShape", "getBlockSupportShape"),
                APIS.stream().map(Signature::name).collect(
                        java.util.stream.Collectors.toSet()));
    }
}
