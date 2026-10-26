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
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Planetary 3R-3B-1 RESEARCH ONLY.
 *
 * Freeze actual original Minecraft1.21.1/NeoForge21.1.215 owner declarations
 * and selected static JVM dispatch of block model quad/face culling/AO pipeline.
 * Does NOT render client quads or assert applied-Mixin/mod behavior. This is one
 * research bounded packet for PHASE2_VISUAL_READINESS_GATE, not Phase3 readiness.
 */
final class Phase3R3B1OriginalBakedQuadCullingConsumerAsmTest {
    private static final String MC = "net/minecraft/";
    private static final String NF = "net/neoforged/neoforge/";

    private static final String MODEL_RENDERER = MC + "client/renderer/block/ModelBlockRenderer";
    private static final String BLOCK_DISPATCHER = MC + "client/renderer/block/BlockRenderDispatcher";
    private static final String COMPILER = MC + "client/renderer/chunk/SectionCompiler";
    private static final String MODEL = MC + "client/resources/model/BakedModel";
    private static final String QUAD = MC + "client/renderer/block/model/BakedQuad";
    private static final String BLOCK = MC + "world/level/block/Block";
    private static final String STATE = MC + "world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String DYNAMIC_MODEL = NF + "client/model/IDynamicBakedModel";

    private static final List<String> OWNERS = List.of(
            MODEL_RENDERER, BLOCK_DISPATCHER, COMPILER, MODEL, QUAD,
            BLOCK, STATE, DYNAMIC_MODEL,
            MC + "client/renderer/block/ModelBlockRenderer$AmbientOcclusionFace",
            MC + "client/color/block/BlockColors",
            MC + "client/renderer/LightTexture",
            MC + "client/renderer/RenderType",
            MC + "client/renderer/chunk/RenderChunkRegion",
            NF + "client/model/BakedModelWrapper",
            NF + "client/model/lighting/SmoothQuadLighter",
            NF + "client/model/lighting/FlatQuadLighter",
            NF + "client/ClientHooks"
    );
    private static final Set<String> DISPATCH = Set.of(
            "getQuads", "getModelData", "getRenderTypes", "shouldRenderFace",
            "skipRendering", "hidesNeighborFace", "supportsExternalFaceHiding",
            "getFaceOcclusionShape", "getOcclusionShape", "getCollisionShape",
            "getBlockSupportShape", "getBrightness", "getShade", "getLightEngine",
            "getBlockTint", "getColor", "getLightColor", "getLightEmission",
            "renderBatched", "tesselateBlock", "tesselateWithAO",
            "tesselateWithoutAO", "putQuadData", "renderQuad", "getDirection",
            "getVertices", "getTintIndex", "isTinted", "getRenderShape",
            "getAppearance", "getBlockState", "getBlockEntity", "getLightMap",
            "getModel", "getBlockModel", "getBlockRenderer", "getFaceShape",
            "getModelDataManager", "calculateShape"
    );

    private record Owner(String name, String superClass, String interfaces,
                         String sha256, boolean present) {
        String tsv() {
            return String.join("\t", name, superClass, interfaces, sha256,
                    Boolean.toString(present));
        }
    }
    private record Declaration(String owner, String method, String jvmDescriptor,
                               int access) {
        String tsv() {
            return String.join("\t", owner, method, jvmDescriptor,
                    Integer.toString(access));
        }
    }
    private record Field(String owner, String name, String desc, int access) {
        String tsv() {
            return String.join("\t", owner, name, desc, Integer.toString(access));
        }
    }
    private record Invoke(String sourceOwner, String sourceMethod, String sourceDesc,
                          String targetOwner, String targetMethod, String targetDesc,
                          int opcode) {
        String tsv() {
            return String.join("\t", sourceOwner, sourceMethod, sourceDesc,
                    targetOwner, targetMethod, targetDesc, Integer.toString(opcode));
        }
    }

    @Test
    void originalModelPipelineOwnerAndQuadDispatchGraphHasExactTargetAbi() throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Declaration> declarations = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Invoke> invokes = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (String owner : OWNERS) {
            if (!unique.add(owner)) continue;
            byte[] bytes;
            try (InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(owner + ".class")) {
                if (stream == null) {
                    owners.add(new Owner(owner, "", "", "", false));
                    continue;
                }
                bytes = stream.readAllBytes();
            }
            String sha = HexFormat.of().formatHex(MessageDigest
                    .getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int version, int access, String name,
                        String signature, String superName, String[] interfaces) {
                    owners.add(new Owner(name, superName == null ? "" : superName,
                            String.join(",", interfaces == null ? new String[0] : interfaces),
                            sha, true));
                }
                @Override public FieldVisitor visitField(int access, String field,
                        String desc, String signature, Object value) {
                    fields.add(new Field(owner, field, desc, access));
                    return null;
                }
                @Override public MethodVisitor visitMethod(int access, String method,
                        String desc, String signature, String[] exceptions) {
                    declarations.add(new Declaration(owner, method, desc, access));
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public void visitMethodInsn(int opcode,
                                String target, String invoked, String targetDesc,
                                boolean isInterface) {
                            if (DISPATCH.contains(invoked)) {
                                invokes.add(new Invoke(owner, method, desc,
                                        target, invoked, targetDesc, opcode));
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        for (String required : List.of(MODEL_RENDERER, BLOCK_DISPATCHER,
                COMPILER, MODEL, QUAD, BLOCK, STATE, DYNAMIC_MODEL)) {
            assertTrue(owners.stream().anyMatch(x ->
                    x.name().equals(required) && x.present()),
                    "Missing original NeoForge source owner: " + required);
        }
        assertTrue(declarations.stream().anyMatch(x ->
                        x.owner().equals(MODEL_RENDERER)
                        && x.method().equals("tesselateBlock")),
                "Original target must declare ModelBlockRenderer.tesselateBlock");
        assertTrue(declarations.stream().anyMatch(x ->
                        x.owner().equals(BLOCK_DISPATCHER)
                        && x.method().equals("renderBatched")),
                "Original target must declare BlockRenderDispatcher.renderBatched");
        assertTrue(declarations.stream().anyMatch(x ->
                        x.owner().equals(BLOCK)
                        && x.method().equals("shouldRenderFace")),
                "Original Block.shouldRenderFace is required");
        assertTrue(declarations.stream().anyMatch(x ->
                        x.owner().equals(DYNAMIC_MODEL)
                        && x.method().equals("getQuads")),
                "NeoForge IDynamicBakedModel must declare quad overloads");
        assertTrue(invokes.stream().anyMatch(x ->
                        x.sourceOwner().equals(COMPILER) &&
                        x.targetMethod().equals("getModelData")),
                "Original SectionCompiler must query model data");
        assertTrue(invokes.stream().anyMatch(x ->
                        x.sourceOwner().equals(COMPILER) &&
                        x.targetMethod().equals("renderBatched")),
                "Original SectionCompiler must pass region/model to BlockRenderDispatcher");

        owners.sort(Comparator.comparing(Owner::name));
        fields.sort(Comparator.comparing(Field::owner).thenComparing(Field::name));
        declarations.sort(Comparator.comparing(Declaration::owner)
                .thenComparing(Declaration::method).thenComparing(Declaration::jvmDescriptor));
        invokes.sort(Comparator.comparing(Invoke::sourceOwner)
                .thenComparing(Invoke::sourceMethod).thenComparing(Invoke::sourceDesc)
                .thenComparing(Invoke::targetOwner).thenComparing(Invoke::targetMethod)
                .thenComparing(Invoke::targetDesc).thenComparingInt(Invoke::opcode));

        Path out = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(out);
        List<String> os = new ArrayList<>(List.of(
                "owner\tsuperclass\tinterfaces\tclass_sha256\tpresent"));
        owners.stream().map(Owner::tsv).forEach(os::add);
        Files.write(out.resolve("phase3-r3b1-original-model-owners.tsv"), os,
                StandardCharsets.UTF_8);
        List<String> ds = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess"));
        declarations.stream().map(Declaration::tsv).forEach(ds::add);
        Files.write(out.resolve("phase3-r3b1-original-model-methods.tsv"), ds,
                StandardCharsets.UTF_8);
        List<String> fs = new ArrayList<>(List.of(
                "owner\tfield\tjvm_descriptor\taccess"));
        fields.stream().map(Field::tsv).forEach(fs::add);
        Files.write(out.resolve("phase3-r3b1-original-model-fields.tsv"), fs,
                StandardCharsets.UTF_8);
        List<String> cs = new ArrayList<>(List.of(
                "source_owner\tsource_method\tsource_descriptor\ttarget_owner\t"
                + "target_method\ttarget_descriptor\tinvoke_opcode"));
        invokes.stream().map(Invoke::tsv).forEach(cs::add);
        Files.write(out.resolve("phase3-r3b1-original-model-invokes.tsv"), cs,
                StandardCharsets.UTF_8);
        Files.writeString(out.resolve("phase3-r3b1-original-model-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "stage=3R-3B-1\n"
                + "scope=original_selected_static_bytecode_not_applied_mod_client\n"
                + "ownersRequested=" + unique.size() + "\n"
                + "ownersPresent=" + owners.stream().filter(Owner::present).count() + "\n"
                + "ownersAbsent=" + owners.stream().filter(x -> !x.present()).count() + "\n"
                + "declaredMethods=" + declarations.size() + "\n"
                + "fields=" + fields.size() + "\n"
                + "selectedInvokes=" + invokes.size() + "\n"
                + "modelRendererGetQuadsSites=" + invokes.stream().filter(x ->
                        x.sourceOwner().equals(MODEL_RENDERER) &&
                        x.targetMethod().equals("getQuads")).count() + "\n"
                + "modelRendererShouldRenderFaceSites=" + invokes.stream().filter(x ->
                        x.sourceOwner().equals(MODEL_RENDERER) &&
                        x.targetMethod().equals("shouldRenderFace")).count() + "\n"
                + "blockFaceOcclusionSites=" + invokes.stream().filter(x ->
                        x.sourceOwner().equals(BLOCK) &&
                        x.sourceMethod().equals("shouldRenderFace") &&
                        x.targetMethod().equals("getFaceOcclusionShape")).count() + "\n"
                + "limit=method_declarations_selected_invokes_not_runtime_virtual_subtype_or_loaded_mod\n",
                StandardCharsets.UTF_8);
    }
}
