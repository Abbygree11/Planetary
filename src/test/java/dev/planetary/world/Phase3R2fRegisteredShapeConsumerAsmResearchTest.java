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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3R-2f RESEARCH ONLY. Enumerate actual compiled original
 * NeoForge 21.1.215 shape/support/occlusion method invocation callsites
 * within ALL registered vanilla Block classes and their inherited owners,
 * plus selected vanilla/Planet consumer roots. No game behavior changes.
 *
 * Not an exhaustive entire Minecraft callgraph and NOT a mod-JAR test.
 */
final class Phase3R2fRegisteredShapeConsumerAsmResearchTest {
    private static final String MINECRAFT = "net/minecraft/";
    private static final String BASE = MINECRAFT
            + "world/level/block/state/BlockBehaviour$BlockStateBase";

    private static final Set<String> SHAPE_API = Set.of(
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

    private static final List<String> ROOTS = List.of(
            MINECRAFT + "world/level/block/Block",
            BASE,
            BASE + "$Cache",
            MINECRAFT + "world/level/block/SupportType",
            MINECRAFT + "world/level/BlockGetter",
            MINECRAFT + "world/level/BlockCollisions",
            MINECRAFT + "world/level/ClipContext",
            MINECRAFT + "world/phys/shapes/CollisionContext",
            MINECRAFT + "world/phys/shapes/EntityCollisionContext",
            MINECRAFT + "world/phys/shapes/Shapes",
            "dev/planetary/world/PlanetBlockShapeRuntime",
            "dev/planetary/client/render/PlanetBlockRenderCulling",
            "dev/planetary/client/render/PlanetClientRenderFrame");

    private record Edge(String sourceOwner, String method, String descriptor,
                        String targetOwner, String targetMethod,
                        String targetDescriptor, String opcode) {
        String toTsv() {
            return String.join("\t", sourceOwner, method, descriptor,
                    targetOwner, targetMethod, targetDescriptor, opcode);
        }
    }

    private static List<Edge> originalCalls(String internalName) throws IOException {
        List<Edge> edges = new ArrayList<>();
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(internalName + ".class")) {
            assertNotNull(input, "Exact NeoForge class missing: " + internalName);
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name,
                        String descriptor, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String target,
                                String called, String desc, boolean itf) {
                            if (SHAPE_API.contains(called)) {
                                edges.add(new Edge(internalName, name, descriptor,
                                        target, called, desc, Integer.toString(opcode)));
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return edges;
    }

    @Test
    void exactTargetShapeSupportOcclusionAndFakeWorldBoundaryOwnerMap()
            throws IOException {
        Bootstrap.bootStrap();
        Set<String> blockClasses = new TreeSet<>();
        Set<String> inherited = new TreeSet<>();
        int blocks = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            blocks++;
            Class<?> type = block.getClass();
            blockClasses.add(type.getName().replace('.', '/'));
            for (Class<?> parent = type; parent != null
                    && parent != Object.class; parent = parent.getSuperclass()) {
                if (parent.getName().startsWith("net.minecraft.")) {
                    inherited.add(parent.getName().replace('.', '/'));
                }
            }
        }
        assertEquals(1060, blocks);
        assertEquals(241, blockClasses.size());
        assertTrue(inherited.contains(MINECRAFT + "world/level/block/Block"));
        assertTrue(inherited.contains(MINECRAFT + "world/level/block/state/BlockBehaviour"));

        Set<String> owners = new TreeSet<>(inherited);
        owners.addAll(ROOTS);
        List<Edge> edges = new ArrayList<>();
        for (String owner : owners) {
            edges.addAll(originalCalls(owner));
        }
        edges.sort(Comparator.comparing(Edge::sourceOwner)
                .thenComparing(Edge::method)
                .thenComparing(Edge::descriptor)
                .thenComparing(Edge::targetOwner)
                .thenComparing(Edge::targetMethod)
                .thenComparing(Edge::targetDescriptor)
                .thenComparing(Edge::opcode));

        assertTrue(edges.size() > 50, "Compiled shape-family calls not inventoried");
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("getFaceOcclusionShape")));
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("isFaceSturdy")));
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("getBlockSupportShape")));
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("getCollisionShape")));
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("hasDynamicShape")));
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("hidesNeighborFace")));
        assertTrue(edges.stream().anyMatch(e -> e.targetMethod.equals("isAbove")));
        assertTrue(edges.stream().anyMatch(e -> e.sourceOwner.equals(
                "dev/planetary/client/render/PlanetBlockRenderCulling")));

        Path reports = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(reports);
        List<String> lines = new ArrayList<>();
        lines.add("source_owner\tsource_method\tsource_jvm_descriptor"
                + "\ttarget_owner\ttarget_method\ttarget_jvm_descriptor\tinvoke_opcode");
        edges.stream().map(Edge::toTsv).forEach(lines::add);
        Files.write(reports.resolve("phase3-neo1211-registered-shape-consumer-edges.tsv"),
                lines, StandardCharsets.UTF_8);

        Set<String> uniqueRoots = new LinkedHashSet<>();
        for (Edge edge : edges) {
            uniqueRoots.add(edge.sourceOwner());
        }
        Files.writeString(reports.resolve(
                "phase3-neo1211-registered-shape-consumer-summary.txt"),
                "minecraftVersion=1.21.1 neoForgeVersion=21.1.215\n"
                + "registeredBlockIds=" + blocks + "\n"
                + "registeredConcreteClasses=" + blockClasses.size() + "\n"
                + "actualInheritedClassOwners=" + inherited.size() + "\n"
                + "scannedClassRoots=" + owners.size() + "\n"
                + "classesWithShapeRelatedInvokes=" + uniqueRoots.size() + "\n"
                + "shapeRelatedInvokeEdges=" + edges.size() + "\n"
                + "coverage=all_registered_vanilla_Block_ancestry_plus_selected_engines\n"
                + "explicitGaps=full_engine_global_invoke_graph,mod_jar_overrides,"
                + "runtime_loaded_client,seams_and_gameplay\n",
                StandardCharsets.UTF_8);
    }
}
