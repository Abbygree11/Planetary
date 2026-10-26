package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Planetary 3R-2k RESEARCH ONLY: exact original NeoForge 21.1.215
 * compiled RenderChunkRegion / SectionCompiler / ModelData producer-owner ABI.
 * Does not execute the client renderer, transform Mixins, or install mods.
 * Optional sources must be explicitly marked absent, not invented.
 */
final class Phase3R2kRenderRegionModelDataProvenanceAsmTest {
    private static final String MC = "net/minecraft/";
    private static final String NEO = "net/neoforged/neoforge/";
    private static final String REGION = MC + "client/renderer/chunk/RenderChunkRegion";
    private static final String SECTION = MC + "client/renderer/chunk/SectionCompiler";
    private static final String CACHE = MC + "client/renderer/chunk/RenderChunkRegionCache";
    private static final String BLOCK_TINT = MC + "world/level/BlockAndTintGetter";
    private static final String BLOCK_GETTER = MC + "world/level/BlockGetter";
    private static final String DYNAMIC_MODEL = NEO + "client/model/IDynamicBakedModel";
    private static final String BAKED_MODEL = MC + "client/resources/model/BakedModel";
    private static final String MODEL_DATA = NEO + "client/model/data/ModelData";
    private static final List<String> OWNERS = List.of(
            REGION, SECTION, CACHE, BLOCK_TINT, BLOCK_GETTER, DYNAMIC_MODEL,
            BAKED_MODEL, MODEL_DATA,
            MC + "client/multiplayer/ClientLevel",
            MC + "client/renderer/LevelRenderer",
            MC + "client/renderer/chunk/SectionRenderDispatcher",
            MC + "client/renderer/chunk/RenderRegionCache",
            MC + "client/renderer/chunk/RenderSectionRegion",
            MC + "client/renderer/block/BlockRenderDispatcher",
            MC + "client/renderer/block/ModelBlockRenderer",
            MC + "client/renderer/block/LiquidBlockRenderer",
            MC + "world/level/chunk/LevelChunk",
            MC + "world/level/Level",
            MC + "world/level/BlockAndTintGetter",
            NEO + "client/model/data/ModelDataManager",
            NEO + "client/model/data/ModelProperty",
            NEO + "client/model/IDynamicBakedModel",
            NEO + "client/ChunkRenderTypeSet"
    );
    private static final Set<String> IMPORTANT = Set.of(
            "compile", "createRegion", "getRegion", "getBlockState", "getBlockEntity",
            "getModelData", "getQuads", "getRenderTypes", "getBlockTint",
            "getLightEngine", "getShade", "getBrightness", "getLevel",
            "getChunk", "getBlockAndTintGetter", "getAppearance", "getFluidState",
            "getBlockRenderTypes", "renderBatched", "renderModel", "tesselateBlock",
            "getRenderChunk", "getBlockEntities", "getModelDataManager",
            "getChunkSource", "getBiome", "getBlockModel", "getBlockRenderer",
            "getBlockRenderDispatcher", "getBlockPos"
    );
    private record Owner(String className, String superName, String interfaces,
                         String sha256, boolean present) {
        String tsv() { return String.join("\t", className, superName,
                interfaces, sha256, Boolean.toString(present)); }
    }
    private record Field(String owner, String name, String desc, int access) {
        String tsv() { return String.join("\t", owner, name, desc,
                Integer.toString(access)); }
    }
    private record Method(String owner, String name, String desc, int access) {
        String tsv() { return String.join("\t", owner, name, desc,
                Integer.toString(access)); }
    }
    private record Call(String owner, String method, String sourceDesc,
                        String target, String targetMethod, String targetDesc, int opcode) {
        String tsv() { return String.join("\t", owner, method, sourceDesc,
                target, targetMethod, targetDesc, Integer.toString(opcode)); }
    }

    @Test
    void captureOriginalRegionAndModelDataProducerMethodDescriptorsAndCallGraph()
            throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Call> calls = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (String name : OWNERS) {
            if (!unique.add(name)) continue;
            byte[] bytes;
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(name + ".class")) {
                if (in == null) {
                    owners.add(new Owner(name, "", "", "", false));
                    continue;
                }
                bytes = in.readAllBytes();
            }
            String hash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String owner,
                                  String signature, String superName, String[] interfaces) {
                    owners.add(new Owner(owner, superName == null ? "" : superName,
                            String.join(",", interfaces == null ? new String[0] : interfaces),
                            hash, true));
                }
                @Override
                public FieldVisitor visitField(int access, String field,
                                               String desc, String signature, Object value) {
                    fields.add(new Field(name, field, desc, access));
                    return null;
                }
                @Override
                public MethodVisitor visitMethod(int access, String method,
                         String desc, String signature, String[] exceptions) {
                    if (IMPORTANT.contains(method)) {
                        methods.add(new Method(name, method, desc, access));
                    }
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String target,
                                String invoked, String targetDesc, boolean isInterface) {
                            if (IMPORTANT.contains(invoked)) {
                                calls.add(new Call(name, method, desc,
                                        target, invoked, targetDesc, opcode));
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        for (String required : List.of(REGION, SECTION, BLOCK_TINT,
                                       BLOCK_GETTER, DYNAMIC_MODEL, BAKED_MODEL, MODEL_DATA)) {
            assertTrue(owners.stream().anyMatch(x ->
                    x.className().equals(required) && x.present()),
                    "Original NeoForge target required provider missing: " + required);
        }
        assertTrue(calls.stream().anyMatch(x ->
                        x.owner().equals(SECTION) && x.targetMethod().equals("getModelData")),
                "SectionCompiler must invoke a model-data method on original target");
        assertTrue(calls.stream().anyMatch(x ->
                        x.owner().equals(SECTION) && x.targetMethod().equals("getBlockState")),
                "SectionCompiler must query block state on original target");
        assertTrue(methods.stream().anyMatch(x ->
                        x.owner().equals(REGION) && x.name().equals("getBlockState")),
                "The region getter must provide block states");
        // Do NOT assume IDynamicBakedModel directly declares getModelData:
        // that ABI may be inherited from BakedModel / NeoForge model extension.
        // Record the actual declaring owner from the target classfile.
        assertTrue(methods.stream().anyMatch(x ->
                        x.name().equals("getModelData") &&
                        (x.owner().equals(BAKED_MODEL) ||
                         x.owner().equals(DYNAMIC_MODEL) ||
                         x.owner().equals(REGION) ||
                         x.owner().equals(MC + "world/level/Level"))),
                "Original classpath must include at least one concrete model-data method provider");

        owners.sort(Comparator.comparing(Owner::className));
        fields.sort(Comparator.comparing(Field::owner).thenComparing(Field::name));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name)
                .thenComparing(Method::desc));
        calls.sort(Comparator.comparing(Call::owner).thenComparing(Call::method)
                .thenComparing(Call::sourceDesc).thenComparing(Call::target)
                .thenComparing(Call::targetMethod).thenComparing(Call::targetDesc)
                .thenComparingInt(Call::opcode));

        Path reports = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(reports);
        List<String> ownerRows = new ArrayList<>(List.of(
                "owner\tsuperclass\tinterfaces\tclass_sha256\tpresent"));
        owners.stream().map(Owner::tsv).forEach(ownerRows::add);
        Files.write(reports.resolve("phase3-r2k-region-model-owner-types.tsv"),
                    ownerRows, StandardCharsets.UTF_8);
        List<String> fieldRows = new ArrayList<>(List.of("owner\tfield\tjvm_descriptor\taccess"));
        fields.stream().map(Field::tsv).forEach(fieldRows::add);
        Files.write(reports.resolve("phase3-r2k-region-model-fields.tsv"),
                    fieldRows, StandardCharsets.UTF_8);
        List<String> methodRows = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess"));
        methods.stream().map(Method::tsv).forEach(methodRows::add);
        Files.write(reports.resolve("phase3-r2k-region-model-methods.tsv"),
                    methodRows, StandardCharsets.UTF_8);
        List<String> callRows = new ArrayList<>(List.of("source_owner\tsource_method\t"
                + "source_jvm_descriptor\ttarget_owner\ttarget_method\t"
                + "target_jvm_descriptor\tinvoke_opcode"));
        calls.stream().map(Call::tsv).forEach(callRows::add);
        Files.write(reports.resolve("phase3-r2k-region-model-invocations.tsv"),
                    callRows, StandardCharsets.UTF_8);
        Files.writeString(reports.resolve("phase3-r2k-region-model-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "coverage=SELECTED_CLASSFILES_JVM_DECLARATIONS_NOT_RUNTIME_MIXINS\n"
                + "ownersRequested=" + unique.size() + "\n"
                + "ownersPresent=" + owners.stream().filter(Owner::present).count() + "\n"
                + "ownersAbsent=" + owners.stream().filter(x -> !x.present()).count() + "\n"
                + "declaredSelectedMethods=" + methods.size() + "\n"
                + "selectedFields=" + fields.size() + "\n"
                + "selectedMethodInvocationEdges=" + calls.size() + "\n"
                + "sectionCompilerGetModelDataCallsites=" + calls.stream().filter(x ->
                    x.owner().equals(SECTION) && x.targetMethod().equals("getModelData")).count() + "\n"
                + "regionBlockStateMethodDeclarations=" + methods.stream().filter(x ->
                    x.owner().equals(REGION) && x.name().equals("getBlockState")).count() + "\n"
                + "limit=not_proof_of_installed_mods_or_actual_coordinates_or_client_execution\n",
                StandardCharsets.UTF_8);
    }
}
