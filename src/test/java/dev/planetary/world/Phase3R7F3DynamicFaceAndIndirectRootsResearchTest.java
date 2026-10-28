package dev.planetary.world;

import dev.planetary.client.render.PlanetBlockRenderCulling;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 3R-7F-3 RESEARCH ONLY. Original target 21.1.215 JVM source/call
 * ownership plus a controlled mocked dynamic FACE OCCLUSION reproduction
 * of the current Planetary state-only cache. Mocked behaviors do not
 * prove a specific installed mod is broken or a Minecraft visual result.
 */
final class Phase3R7F3DynamicFaceAndIndirectRootsResearchTest {
    private static final String BLOCK = "net/minecraft/world/level/block/Block";
    private static final String BASE = "net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String STATE = "net/minecraft/world/level/block/state/BlockState";
    private static final String PLANET = "dev/planetary/client/render/PlanetBlockRenderCulling";
    private static final Set<String> GEOMETRY_NAMES = Set.of(
            "getShape", "getCollisionShape", "getVisualShape", "getInteractionShape",
            "getOcclusionShape", "getBlockSupportShape", "getFaceOcclusionShape",
            "isFaceSturdy", "isSupporting", "getFaceShape", "isFaceFull",
            "isShapeFullBlock", "mergedFaceOccludes", "skipRendering",
            "hidesNeighborFace", "supportsExternalFaceHiding", "getBlockFloorHeight",
            "getBlockShape", "joinIsNotEmpty", "hasDynamicShape", "getOffset",
            "hasOffsetFunction", "isAbove", "isDescending", "getBlockEntity", "getModelData");

    private static Path out(String name) throws Exception {
        Path dir = Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary"));
        Files.createDirectories(dir);
        return dir.resolve("phase3-r7f3-" + name);
    }

    @Test
    void actualCurrentPlanetaryCacheReturnsStaleFaceVisibilityForMockedContextChangingOwner()
            throws Exception {
        Level level = mock(Level.class);
        BlockState source = mock(BlockState.class);
        BlockState adjacent = mock(BlockState.class);
        BlockPos pos = new BlockPos(20, 20, 0);
        BlockPos neighbor = pos.west();
        PlanetGravityField field = new PlanetGravityField(new PlanetCore(0, 0, 0, 20));

        when(level.getBlockState(neighbor)).thenReturn(adjacent);
        when(adjacent.canOcclude()).thenReturn(true);
        when(source.getFaceOcclusionShape(level, pos, Direction.DOWN))
                .thenReturn(Shapes.block(), Shapes.empty());
        when(adjacent.getFaceOcclusionShape(level, neighbor, Direction.EAST))
                .thenReturn(Shapes.block());

        PlanetGravityRuntime.bind(level, field);
        try {
            boolean first = PlanetBlockRenderCulling.shouldRenderFace(
                    source, level, pos, Direction.WEST, neighbor).orElseThrow();
            boolean second = PlanetBlockRenderCulling.shouldRenderFace(
                    source, level, pos, Direction.WEST, neighbor).orElseThrow();

            assertFalse(first, "Original first result: full source and neighbor faces occlude");
            // Deliberate observed regression (not desired behavior): the second
            // method invocation bypasses the new dynamic face state entirely.
            assertFalse(second, "Current Planetary cache returns stale hidden result");
            verify(source, times(1)).getFaceOcclusionShape(level, pos, Direction.DOWN);
            assertTrue(source.getFaceOcclusionShape(level, pos, Direction.DOWN).isEmpty(),
                    "Independent fresh contextual face is empty and would be visible");
            // The same inputs in the source's cache key now have a different
            // geometrically correct result: true, not the cached false.
            Files.write(out("dynamic-face-stale-cache.tsv"), List.of(
                    "case\tcontext\tcachedVisibility\tfreshSourceFaceEmpty\tcorrectVisibility\tevidence",
                    "first\tmock_source_full\tfalse\tfalse\tfalse\tMOCKED_PLANETARY_CURRENT_CODE",
                    "after_context_change\tmock_source_empty\tfalse\ttrue\ttrue\tMOCKED_PLANETARY_CURRENT_CODE"
            ), StandardCharsets.UTF_8);
        } finally {
            PlanetGravityRuntime.unbind(level, field);
        }
    }

    private record Call(String source, String method, String descriptor,
                        String target, String called, String targetDescriptor, int opcode) {}

    private static List<Call> calls(String owner, byte[] bytes) {
        List<Call> results = new ArrayList<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int a, String method, String desc,
                                             String sig, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int op, String target, String called,
                                                String targetDesc, boolean isInterface) {
                        results.add(new Call(owner, method, desc, target,
                                called, targetDesc, op));
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        return results;
    }

    private static byte[] read(String klass) throws Exception {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(klass + ".class")) {
            assertNotNull(input, "Original class absent: " + klass);
            return input.readAllBytes();
        }
    }

    private static String phase(String owner, String scope, String called) {
        if (called.equals("getBlockEntity"))
            return "CONTEXT_ONLY_NOT_AUTOMATIC_SHAPE_CALL";
        if (called.equals("getOffset"))
            return "OFFSET_DESCRIPTOR_REQUIRES_SEMANTIC_CHECK";
        if (owner.startsWith("net/minecraft/world/level/levelgen/")
                || owner.contains("/structure/")
                || owner.startsWith("net/minecraft/world/level/chunk/"))
            return "PHASE9_WORLDGEN_OR_CHUNK";
        if (owner.startsWith("net/minecraft/world/entity/")
                || owner.startsWith("net/minecraft/server/level/"))
            return "PHASE7_ENTITY_ACTOR";
        if (owner.startsWith("net/minecraft/world/item/"))
            return "PHASE2_ITEM_AUTHOR";
        if (owner.startsWith("net/minecraft/client/renderer/")
                || owner.startsWith("net/neoforged/neoforge/client/model/")
                || owner.startsWith("net/neoforged/neoforge/client/model/lighting/"))
            return "PHASE3_RENDER_CONSUMER";
        if (owner.startsWith("net/minecraft/client/")
                || owner.startsWith("net/neoforged/neoforge/client/"))
            return "PHASE3_CLIENT_OR_EFFECT";
        if (owner.startsWith("net/minecraft/world/level/block/")
                || scope.equals("registered_block_ancestry"))
            return "PHASE3A_BLOCK_GEOMETRY";
        return "PHASE3A_CROSS_PHASE_JVM_REVIEW";
    }

    /**
     * Re-discover all original Minecraft/NeoForge classfiles, not merely
     * selected 543 caller owners; this mirrors the prior 3R-2g discovery.
     * Bounded to actual class resources supplied by this JUnit runtime.
     */
    private static Set<String> originalOwners() throws Exception {
        Set<String> names = new TreeSet<>();
        for (String root : List.of(
                "net/minecraft/world/level/block/Block.class",
                "net/neoforged/neoforge/common/extensions/IBlockExtension.class",
                "net/neoforged/neoforge/client/model/IDynamicBakedModel.class")) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(root);
            if (resource == null) continue;
            if (resource.getProtocol().equals("jar")) {
                JarURLConnection conn = (JarURLConnection) resource.openConnection();
                try (JarFile jar = new JarFile(Path.of(conn.getJarFileURL().toURI()).toFile())) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var e = entries.nextElement();
                        String name = e.getName();
                        if (!e.isDirectory() && name.endsWith(".class") &&
                                (name.startsWith("net/minecraft/") ||
                                name.startsWith("net/neoforged/neoforge/"))) {
                            names.add(name.substring(0, name.length() - 6));
                        }
                    }
                }
            } else if (resource.getProtocol().equals("file")) {
                Path rootDir = Path.of(resource.toURI());
                for (int i = 0; i < root.split("/").length; i++) rootDir = rootDir.getParent();
                final Path prefix = rootDir;
                for (String packageName : List.of("net/minecraft", "net/neoforged/neoforge")) {
                    Path dir = prefix.resolve(packageName);
                    if (!Files.isDirectory(dir)) continue;
                    try (var files = Files.walk(dir)) {
                        files.filter(Files::isRegularFile)
                                .filter(f -> f.toString().endsWith(".class"))
                                .forEach(f -> {
                                    String relative = prefix.relativize(f).toString().replace('\\', '/');
                                    names.add(relative.substring(0, relative.length() - 6));
                                });
                    }
                }
            } else {
                fail("Unsupported original resource protocol " + resource.getProtocol());
            }
        }
        assertEquals(9430, names.size(), "Original 21.1.215 3R-2g full classpath census");
        return names;
    }

    @Test
    void originalSourceOwnersFaceOcclusionPlusAll543DiscoveredCallsitesClassified()
            throws Exception {
        byte[] block = read(BLOCK);
        byte[] base = read(BASE);
        byte[] planet = read(PLANET);
        List<Call> originalBlock = calls(BLOCK, block);
        List<Call> originalBase = calls(BASE, base);
        List<Call> planetary = calls(PLANET, planet);
        long faceCalls = originalBlock.stream().filter(c ->
                c.method().equals("shouldRenderFace")
                        && c.called().equals("getFaceOcclusionShape")
                        && c.target().equals(STATE)).count();
        assertEquals(2, faceCalls, "Original vanilla must query both contextual face shapes");
        assertTrue(originalBase.stream().anyMatch(c -> c.method().equals("getFaceOcclusionShape")
                && c.called().equals("getOcclusionShape")),
                "Original base dispatch into contextual occlusion block owner");
        assertTrue(planetary.stream().filter(c -> c.method().equals("shouldRenderFace"))
                        .filter(c -> c.called().equals("getFaceOcclusionShape")).count() >= 2);

        // Regenerate exact original target's complete DISCOVERED direct-API
        // call corpus; the old 543 corpus is never silently collapsed into 144.
        new Phase3R2gEngineShapeCallerCensusTest()
                .enumerateUnregisteredEngineGeometryAndSupportConsumers();
        Path dir = out("original-543-root-classification.tsv");
        Path census = dir.getParent().resolve("phase3-neo1211-engine-shape-consumer-edges.tsv");
        List<String> lines = Files.readAllLines(census, StandardCharsets.UTF_8);
        assertEquals(544, lines.size(), "Original 543 direct matched call rows + header");
        List<String> result = new ArrayList<>();
        result.add(lines.get(0) + "\tprovisional_semantic_bucket\tindirect_dispatch_evidence_limit");
        Map<String, Integer> totals = new HashMap<>();
        Set<String> sources = new TreeSet<>();
        for (String row : lines.subList(1, lines.size())) {
            String[] c = row.split("\t", -1);
            assertEquals(9, c.length, "Exact nine-column legacy original call edge");
            assertTrue(GEOMETRY_NAMES.contains(c[4]), "Unknown original traced API " + c[4]);
            String bucket = phase(c[0], c[7], c[4]);
            totals.merge(bucket, 1, Integer::sum);
            sources.add(c[0]);
            result.add(row + "\t" + bucket
                    + "\tDIRECT_JVM_NAME_MATCH_NOT_VIRTUAL_REFLECTION_OR_LOADED_MOD");
        }
        assertEquals(543, result.size() - 1);
        Files.write(dir, result, StandardCharsets.UTF_8);

        // Independently check all 9430 original classes for invokedynamic
        // method handles and suspicious string constants that the legacy
        // direct-invoke scan cannot observe. A name literal is *not* proof
        // of a reflective call. Virtual dispatch and external mods still OPEN.
        Set<String> allOriginal = originalOwners();
        List<String> indirect = new ArrayList<>();
        List<String> literals = new ArrayList<>();
        indirect.add("caller\tmethod\tdescriptor\thandleOwner\thandleName\ttargetDescriptor");
        literals.add("caller\tmethod\tdescriptor\tstringLiteral\tevidenceLevel");
        Set<String> relevantLiterals = Set.of(
                "getFaceOcclusionShape", "getOcclusionShape", "getShape",
                "getCollisionShape", "getBlockSupportShape", "skipRendering",
                "hidesNeighborFace", "getModelData");
        for (String owner : allOriginal) {
            byte[] b = read(owner);
            new ClassReader(b).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int a, String method, String desc,
                                                 String sig, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitInvokeDynamicInsn(String n, String d,
                                                           Handle bootstrap, Object... args) {
                            for (Object arg : args) {
                                if (arg instanceof Handle h && GEOMETRY_NAMES.contains(h.getName())) {
                                    indirect.add(owner + "\t" + method + "\t" + desc + "\t"
                                            + h.getOwner() + "\t" + h.getName() + "\t" + h.getDesc());
                                }
                            }
                        }
                        @Override
                        public void visitLdcInsn(Object constant) {
                            if (constant instanceof String s && relevantLiterals.contains(s)) {
                                literals.add(owner + "\t" + method + "\t" + desc + "\t"
                                        + s + "\tLDC_STRING_ONLY_NOT_REFLECTION_PROOF");
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        }
        indirect.sort(Comparator.naturalOrder());
        literals.sort(Comparator.naturalOrder());
        Files.write(out("all-9430-invokedynamic-handles.tsv"), indirect, StandardCharsets.UTF_8);
        Files.write(out("all-9430-potential-reflection-literals.tsv"), literals, StandardCharsets.UTF_8);
        List<String> original = new ArrayList<>();
        original.add("owner\tsha256\toriginalDirectFaceCalls\tselectionLimit");
        original.add(BLOCK + "\t" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(block))
                + "\t" + faceCalls + "\tORIGINAL_TARGET_JVM_NO_LIVE_MODS");
        original.add(BASE + "\t" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(base))
                + "\t-\tORIGINAL_TARGET_JVM_CONTEXT_CACHE_BRANCH");
        original.add(PLANET + "\t" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(planet))
                + "\t-\tPLANETARY_CURRENT_COMPILED_UNTRANSFORMED");
        Files.write(out("original-owner-sha.tsv"), original, StandardCharsets.UTF_8);
        List<String> summary = new ArrayList<>(List.of(
                "stage=3R-7F-3",
                "target=Minecraft1.21.1-NeoForge21.1.215",
                "originalMatchedDirectEdges=" + (result.size() - 1),
                "selectedCallingOwners=" + sources.size(),
                "originalClassfilesRescanned=" + allOriginal.size(),
                "originalBlockFaceCalls=" + faceCalls,
                "originalInvokeDynamicGeometryHandles=" + (indirect.size() - 1),
                "originalSuspiciousNameLiterals=" + (literals.size() - 1),
                "scope=original_direct_543_and_invokedynamic_name_literals_in_ALL_9430_classfiles",
                "notProof=complete_virtual_dispatch,actual_reflective_invocation,mod_jars,visual_game"));
        totals.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> summary.add("bucket." + e.getKey() + "=" + e.getValue()));
        Files.write(out("summary.txt"), summary, StandardCharsets.UTF_8);
    }
}
