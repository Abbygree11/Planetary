package dev.planetary.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3R-2g, research-only. Discover compiled NeoForge 21.1.215
 * Minecraft and NeoForge caller CLASSFILES from their actual runtime classpath,
 * rather than selecting only registered Block ancestry. This is a named-method
 * INVOKE census, not a dynamic-dispatch closure or installed-mod audit.
 */
final class Phase3R2gEngineShapeCallerCensusTest {
    private static final Set<String> TARGETS = Set.of(
            "getShape", "getCollisionShape", "getVisualShape",
            "getInteractionShape", "getOcclusionShape",
            "getBlockSupportShape", "getFaceOcclusionShape",
            "isFaceSturdy", "isSupporting", "getFaceShape",
            "isFaceFull", "isShapeFullBlock", "mergedFaceOccludes",
            "skipRendering", "hidesNeighborFace",
            "supportsExternalFaceHiding", "getBlockFloorHeight",
            "getBlockShape", "joinIsNotEmpty", "hasDynamicShape",
            "getOffset", "hasOffsetFunction", "isAbove",
            "isDescending", "getBlockEntity", "getModelData");

    private static final String[] DISCOVERY_RESOURCES = {
            "net/minecraft/world/level/block/Block.class",
            "net/neoforged/neoforge/common/extensions/IBlockExtension.class",
            "net/neoforged/neoforge/client/model/IDynamicBakedModel.class"
    };

    private record Edge(String source, String name, String desc,
                        String target, String called, String targetDesc,
                        int opcode, String scope, String area) {
        String row() {
            return String.join("\t", source, name, desc, target,
                    called, targetDesc, Integer.toString(opcode), scope, area);
        }
    }

    /**
     * Enumerate only class files physically co-located with the known target
     * NeoForge runtime resource. Unsupported URL types fail rather than
     * inventing a complete-corpus result.
     */
    private static void discover(String resource, Set<String> names) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            return; // optional NeoForge API root; disclosed by summary
        }
        if ("jar".equals(url.getProtocol())) {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            try (JarFile jar = new JarFile(Path.of(connection.getJarFileURL().toURI()).toFile())) {
                var entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!entry.isDirectory() && name.endsWith(".class")
                            && (name.startsWith("net/minecraft/")
                            || name.startsWith("net/neoforged/neoforge/"))) {
                        names.add(name.substring(0, name.length() - 6));
                    }
                }
            }
        } else if ("file".equals(url.getProtocol())) {
            Path classFile = Path.of(url.toURI());
            Path root = classFile;
            for (int i = 0; i < resource.split("/").length; i++) {
                root = root.getParent();
            }
            for (String namespace : List.of("net/minecraft", "net/neoforged/neoforge")) {
                Path subtree = root.resolve(namespace);
                if (!Files.isDirectory(subtree)) continue;
                try (var files = Files.walk(subtree)) {
                    files.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".class"))
                            .forEach(p -> {
                                String name = root.relativize(p).toString().replace('\\', '/');
                                names.add(name.substring(0, name.length() - 6));
                            });
                }
            }
        } else {
            fail("Unsupported NeoForge runtime class resource protocol "
                    + url.getProtocol() + " for " + resource);
        }
    }

    private static List<Edge> calls(String owner, String scope) throws IOException {
        List<Edge> rows = new ArrayList<>();
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(input, "Discovered class not readable: " + owner);
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name,
                        String descriptor, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String target,
                                String called, String targetDesc, boolean itf) {
                            if (TARGETS.contains(called)) {
                                rows.add(new Edge(owner, name, descriptor, target,
                                        called, targetDesc, opcode,
                                        scope, categorize(owner)));
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return rows;
    }

    private static String categorize(String owner) {
        if (owner.startsWith("net/neoforged/")) return "neoforge";
        if (owner.startsWith("net/minecraft/client/")) return "client";
        if (owner.startsWith("net/minecraft/server/")) return "server";
        if (owner.startsWith("net/minecraft/world/entity/")) return "entity";
        if (owner.startsWith("net/minecraft/world/item/")) return "item";
        if (owner.startsWith("net/minecraft/world/level/block/")) return "block";
        if (owner.startsWith("net/minecraft/world/level/")) return "world_level";
        if (owner.startsWith("net/minecraft/world/phys/")) return "physics";
        return "other";
    }

    @Test
    void enumerateUnregisteredEngineGeometryAndSupportConsumers() throws Exception {
        Bootstrap.bootStrap();
        Set<String> registeredAncestry = new TreeSet<>();
        int blocks = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            blocks++;
            for (Class<?> c = block.getClass(); c != null && c != Object.class;
                    c = c.getSuperclass()) {
                if (c.getName().startsWith("net.minecraft.")) {
                    registeredAncestry.add(c.getName().replace('.', '/'));
                }
            }
        }
        assertEquals(1060, blocks);
        assertEquals(269, registeredAncestry.size());

        Set<String> names = new TreeSet<>();
        Set<String> availableRoots = new TreeSet<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String resource : DISCOVERY_RESOURCES) {
            if (loader.getResource(resource) != null) {
                availableRoots.add(resource);
                discover(resource, names);
            }
        }
        assertTrue(availableRoots.contains(DISCOVERY_RESOURCES[0]));
        assertTrue(names.contains("net/minecraft/world/level/block/Block"));
        assertTrue(names.size() > registeredAncestry.size(),
                "Engine corpus must extend beyond registered block class ancestry");

        List<Edge> edges = new ArrayList<>();
        Set<String> scanned = new HashSet<>();
        for (String name : names) {
            if (!scanned.add(name)) continue;
            String scope = registeredAncestry.contains(name)
                    ? "registered_block_ancestry" : "outside_block_ancestry";
            edges.addAll(calls(name, scope));
        }
        edges.sort(Comparator.comparing(Edge::source)
                .thenComparing(Edge::name).thenComparing(Edge::desc)
                .thenComparing(Edge::target).thenComparing(Edge::called)
                .thenComparing(Edge::targetDesc).thenComparingInt(Edge::opcode));

        long outside = edges.stream().filter(e ->
                e.scope.equals("outside_block_ancestry")
                        && !e.called.equals("getBlockEntity")).count();
        assertTrue(outside > 0, "No non-block shape-related JVM callsites discovered");

        Path output = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(output);
        List<String> all = new ArrayList<>();
        all.add("source_owner\tsource_method\tsource_jvm_descriptor"
                + "\ttarget_owner\ttarget_method\ttarget_jvm_descriptor"
                + "\tinvoke_opcode\tsource_scope\tsource_area");
        edges.stream().map(Edge::row).forEach(all::add);
        Files.write(output.resolve("phase3-neo1211-engine-shape-consumer-edges.tsv"),
                all, StandardCharsets.UTF_8);

        TreeMap<String, Integer> byArea = new TreeMap<>();
        TreeMap<String, Integer> byMethod = new TreeMap<>();
        Set<String> newCallers = new HashSet<>();
        for (Edge e : edges) {
            byArea.merge(e.area(), 1, Integer::sum);
            byMethod.merge(e.called(), 1, Integer::sum);
            if (e.scope.equals("outside_block_ancestry")) {
                newCallers.add(e.source());
            }
        }
        List<String> summary = new ArrayList<>(List.of(
                "minecraftVersion=1.21.1",
                "neoforgeVersion=21.1.215",
                "availableDiscoveryRoots=" + String.join(",", availableRoots),
                "discoveredClassFiles=" + names.size(),
                "registeredBlockAncestry=" + registeredAncestry.size(),
                "selectedInvocationEdges=" + edges.size(),
                "nonBlockAncestrySourceOwnersWithCalls=" + newCallers.size(),
                "nonBlockAncestryNonGenericBECalls=" + outside,
                "genericBlockEntityInvocations=" + byMethod.getOrDefault("getBlockEntity", 0),
                "coverage=discoverable_jar_or_directory_classfiles_on_target_test_classpath",
                "notCoverage=all_loaded_mods,invokedynamic,reflection,complete_virtual_dispatch,applied_mixins,gameplay"));
        byArea.forEach((area, count) -> summary.add("area." + area + "=" + count));
        byMethod.forEach((method, count) -> summary.add("method." + method + "=" + count));
        Files.write(output.resolve("phase3-neo1211-engine-shape-consumer-summary.txt"),
                summary, StandardCharsets.UTF_8);
    }
}
