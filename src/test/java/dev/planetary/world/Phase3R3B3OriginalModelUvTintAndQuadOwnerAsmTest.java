package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 3R-3B-3, original Minecraft 1.21.1 / NeoForge 21.1.215 Java21
 * RESEARCH ONLY. Source model bake, UV, tint, ModelData and render-type owners.
 *
 * This is selected original classfile evidence, NOT runtime virtual dispatch,
 * an installed/modded client, Mixin-weaving, or screenshot acceptance.
 */
final class Phase3R3B3OriginalModelUvTintAndQuadOwnerAsmTest {
    private static final String MC = "net/minecraft/";
    private static final String NF = "net/neoforged/neoforge/";
    private static final String MODEL = MC + "client/resources/model/BakedModel";
    private static final String QUAD = MC + "client/renderer/block/model/BakedQuad";
    private static final String FACE_BAKERY = MC + "client/renderer/block/model/FaceBakery";
    private static final String COLORS = MC + "client/color/block/BlockColors";
    private static final String SECTION_COMPILER = MC + "client/renderer/chunk/SectionCompiler";
    private static final String MODEL_RENDERER = MC + "client/renderer/block/ModelBlockRenderer";
    private static final String ALTERNATE = NF + "client/model/lighting/LightPipelineAwareModelBlockRenderer";
    private static final String WRAPPER = NF + "client/model/BakedModelWrapper";

    private static final List<String> OWNERS = List.of(
            MODEL, QUAD, FACE_BAKERY,
            MC + "client/renderer/block/model/BlockFaceUV",
            MC + "client/renderer/block/model/BlockElementFace",
            MC + "client/renderer/block/model/BlockElementRotation",
            MC + "client/renderer/block/model/BlockModel",
            MC + "client/resources/model/SimpleBakedModel",
            MC + "client/resources/model/MultiPartBakedModel",
            MC + "client/resources/model/WeightedBakedModel",
            MC + "client/resources/model/ModelBakery",
            MC + "client/resources/model/ModelManager",
            MC + "client/resources/model/ModelState",
            MC + "client/renderer/texture/TextureAtlasSprite",
            MC + "client/renderer/texture/SpriteContents",
            COLORS, MC + "client/color/block/BlockColor",
            MODEL_RENDERER, SECTION_COMPILER,
            MC + "client/renderer/block/BlockRenderDispatcher",
            NF + "client/extensions/IBakedModelExtension",
            NF + "client/model/IDynamicBakedModel",
            WRAPPER, NF + "client/model/IQuadTransformer",
            NF + "client/model/QuadTransformers",
            ALTERNATE
    );

    private static final Set<String> TRACED = Set.of(
            "bakeQuad", "bake", "makeVertices", "fillVertex", "fillVertexNormal",
            "recalculateWinding", "applyElementRotation", "applyModelRotation",
            "getVertices", "getDirection", "getTintIndex", "getSprite",
            "isShade", "hasAmbientOcclusion", "getQuads", "getModelData",
            "getRenderTypes", "getRenderType", "getColor", "getBlockTint",
            "getAppearance", "getU", "getV", "uvShrink", "getUVs",
            "getTexture", "getParticleIcon", "getAtlasLocation",
            "getModel", "getBlockModel", "getBlockRenderer",
            "getVertex", "getSprite", "getTintIndex", "getFaceShape",
            "getFaceOcclusionShape", "getBlockState", "compile",
            "renderBatched", "tesselateBlock", "render", "apply",
            "transform", "rotate", "getTransformation", "getRotation",
            "getRenderPasses", "getRenderTypes", "getBlockRenderTypes",
            "useAmbientOcclusion", "getShade", "getLightColor",
            "getLightEmission", "getOffset", "getRenderShape",
            "getMissingModel", "getModelDataManager"
    );

    private record Owner(String name, String hash, String superclass,
                         String interfaces, boolean present) {
        String row() { return String.join("\t", name, hash, superclass, interfaces,
                Boolean.toString(present)); }
    }
    private record Method(String owner, String name, String desc, int access,
                          boolean traced) {
        String row() { return String.join("\t", owner, name, desc,
                Integer.toString(access), Boolean.toString(traced)); }
    }
    private record Field(String owner, String name, String desc, int access) {
        String row() { return String.join("\t", owner, name, desc, Integer.toString(access)); }
    }
    private record Invoke(String source, String sourceMethod, String sourceDesc,
                          String target, String targetMethod, String targetDesc, int opcode) {
        String row() { return String.join("\t", source, sourceMethod, sourceDesc, target,
                targetMethod, targetDesc, Integer.toString(opcode)); }
    }
    private record Instruction(String owner, String name, String desc, int ordinal,
                               int opcode, String kind, String operand) {
        String row() { return String.join("\t", owner, name, desc,
                Integer.toString(ordinal), Integer.toString(opcode), kind,
                operand.replace('\n', ' ').replace('\t', ' ')); }
    }

    @Test
    void proveOriginalUvAndModelTintOwnerBoundaries() throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Invoke> invokes = new ArrayList<>();
        List<Instruction> operations = new ArrayList<>();

        for (String name : OWNERS) {
            byte[] bytes;
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(name + ".class")) {
                if (in == null) {
                    owners.add(new Owner(name, "", "", "", false));
                    continue;
                }
                bytes = in.readAllBytes();
            }
            String sha = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int version, int access, String owner,
                        String sig, String superclass, String[] ifaces) {
                    owners.add(new Owner(name, sha, superclass == null ? "" : superclass,
                            String.join(",", ifaces == null ? new String[0] : ifaces), true));
                }
                @Override public FieldVisitor visitField(int access, String field,
                        String desc, String sig, Object constant) {
                    fields.add(new Field(name, field, desc, access));
                    return null;
                }
                @Override public MethodVisitor visitMethod(int access, String method,
                        String desc, String sig, String[] exceptions) {
                    boolean trace = TRACED.contains(method) ||
                            (method.startsWith("lambda$") &&
                                    (method.contains("getQuads") ||
                                     method.contains("bake") ||
                                     method.contains("getModelData")));
                    methods.add(new Method(name, method, desc, access, trace));
                    return new MethodVisitor(Opcodes.ASM9) {
                        int ordinal;
                        void op(int opcode, String kind, String operand) {
                            if (trace) operations.add(new Instruction(name, method, desc,
                                    ordinal++, opcode, kind, operand));
                        }
                        @Override public void visitFieldInsn(int opcode, String owner,
                                String field, String fd) {
                            op(opcode, "FIELD", owner + "." + field + ":" + fd);
                        }
                        @Override public void visitMethodInsn(int opcode, String owner,
                                String called, String cd, boolean itf) {
                            invokes.add(new Invoke(name, method, desc, owner, called,
                                    cd, opcode));
                            op(opcode, "CALL", owner + "." + called + cd
                                    + (itf ? ":interface" : ""));
                        }
                        @Override public void visitInvokeDynamicInsn(String invoked,
                                String desc, Handle bootstrap, Object... args) {
                            op(Opcodes.INVOKEDYNAMIC, "INDY", invoked + desc
                                    + ";bootstrap=" + bootstrap.getOwner()
                                    + "." + bootstrap.getName());
                        }
                        @Override public void visitInsn(int opcode) { op(opcode, "OP", ""); }
                        @Override public void visitVarInsn(int opcode, int index) {
                            op(opcode, "VAR", Integer.toString(index));
                        }
                        @Override public void visitTypeInsn(int opcode, String type) {
                            op(opcode, "TYPE", type);
                        }
                        @Override public void visitIntInsn(int opcode, int value) {
                            op(opcode, "INT", Integer.toString(value));
                        }
                        @Override public void visitJumpInsn(int opcode, Label label) {
                            op(opcode, "BRANCH", "target_label");
                        }
                        @Override public void visitLdcInsn(Object value) {
                            op(Opcodes.LDC, "CONST", String.valueOf(value));
                        }
                        @Override public void visitIincInsn(int index, int delta) {
                            op(Opcodes.IINC, "IINC", index + ":" + delta);
                        }
                        @Override public void visitTableSwitchInsn(int a, int b,
                                Label d, Label... labels) {
                            op(Opcodes.TABLESWITCH, "SWITCH", a + ":" + b);
                        }
                        @Override public void visitLookupSwitchInsn(Label d,
                                int[] keys, Label[] labels) {
                            op(Opcodes.LOOKUPSWITCH, "SWITCH", "cases=" + keys.length);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        for (String required : List.of(MODEL, QUAD, FACE_BAKERY, COLORS,
                SECTION_COMPILER, MODEL_RENDERER, WRAPPER, ALTERNATE)) {
            assertTrue(owners.stream().anyMatch(x ->
                    x.name().equals(required) && x.present()),
                    "Missing original 21.1.215 classfile: " + required);
        }
        for (String method : List.of("getVertices", "getTintIndex",
                "getDirection", "getSprite")) {
            assertTrue(methods.stream().anyMatch(x ->
                    x.owner().equals(QUAD) && x.name().equals(method)),
                    "BakedQuad ABI lacks " + method);
        }
        assertTrue(methods.stream().anyMatch(x ->
                x.owner().equals(FACE_BAKERY) && x.name().equals("bakeQuad")),
                "Original FaceBakery must declare model texture quad baking");
        assertTrue(methods.stream().anyMatch(x ->
                x.owner().equals(COLORS) && x.name().equals("getColor")),
                "Original BlockColors dynamic color callback is required");
        assertTrue(invokes.stream().anyMatch(x ->
                x.source().equals(SECTION_COMPILER)
                && x.sourceMethod().equals("compile")
                && x.targetMethod().equals("getModelData")),
                "SectionCompiler must consume region/model data");
        assertTrue(invokes.stream().anyMatch(x ->
                x.source().equals(SECTION_COMPILER)
                && x.sourceMethod().equals("compile")
                && x.targetMethod().equals("getRenderTypes")),
                "SectionCompiler must ask BakedModel for its render pass");
        assertTrue(invokes.stream().anyMatch(x ->
                x.source().equals(MODEL_RENDERER)
                && x.targetMethod().equals("getQuads")),
                "Original tesselation must invoke BakedModel quads");
        assertTrue(invokes.stream().anyMatch(x ->
                x.source().equals(ALTERNATE)
                && x.sourceMethod().equals("render")
                && x.targetMethod().equals("getQuads")),
                "NeoForge independent experimental renderer must use BakedModel quads");

        owners.sort(Comparator.comparing(Owner::name));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name)
                .thenComparing(Method::desc));
        fields.sort(Comparator.comparing(Field::owner).thenComparing(Field::name)
                .thenComparing(Field::desc));
        invokes.sort(Comparator.comparing(Invoke::source)
                .thenComparing(Invoke::sourceMethod).thenComparing(Invoke::sourceDesc)
                .thenComparing(Invoke::target).thenComparing(Invoke::targetMethod)
                .thenComparing(Invoke::targetDesc).thenComparingInt(Invoke::opcode));
        operations.sort(Comparator.comparing(Instruction::owner)
                .thenComparing(Instruction::name).thenComparing(Instruction::desc)
                .thenComparingInt(Instruction::ordinal));
        Path out = Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary"));
        Files.createDirectories(out);
        List<String> ownerLines = new ArrayList<>(List.of(
                "owner\tsha256\tsuperclass\tinterfaces\tpresent"));
        owners.stream().map(Owner::row).forEach(ownerLines::add);
        Files.write(out.resolve("phase3-r3b3-model-uv-tint-owners.tsv"),
                ownerLines, StandardCharsets.UTF_8);
        List<String> methodLines = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess\ttraced"));
        methods.stream().map(Method::row).forEach(methodLines::add);
        Files.write(out.resolve("phase3-r3b3-model-uv-tint-methods.tsv"),
                methodLines, StandardCharsets.UTF_8);
        List<String> fieldLines = new ArrayList<>(List.of(
                "owner\tfield\tjvm_descriptor\taccess"));
        fields.stream().map(Field::row).forEach(fieldLines::add);
        Files.write(out.resolve("phase3-r3b3-model-uv-tint-fields.tsv"),
                fieldLines, StandardCharsets.UTF_8);
        List<String> invokeLines = new ArrayList<>(List.of(
                "source_owner\tsource_method\tsource_descriptor\ttarget_owner\t"
                + "target_method\ttarget_descriptor\topcode"));
        invokes.stream().map(Invoke::row).forEach(invokeLines::add);
        Files.write(out.resolve("phase3-r3b3-model-uv-tint-invokes.tsv"),
                invokeLines, StandardCharsets.UTF_8);
        List<String> instructionLines = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\tordinal\topcode\tkind\toperand"));
        operations.stream().map(Instruction::row).forEach(instructionLines::add);
        Files.write(out.resolve("phase3-r3b3-model-uv-tint-instructions.tsv"),
                instructionLines, StandardCharsets.UTF_8);
        Files.writeString(out.resolve("phase3-r3b3-model-uv-tint-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "stage=3R-3B-3\n"
                + "scope=selected_original_classfiles_and_method_calls_not_loaded_game\n"
                + "classNames=" + OWNERS.size() + "\n"
                + "classesPresent=" + owners.stream().filter(Owner::present).count() + "\n"
                + "classesAbsent=" + owners.stream().filter(x -> !x.present()).count() + "\n"
                + "methodDeclarations=" + methods.size() + "\n"
                + "methodBodiesTraced=" + methods.stream().filter(Method::traced).count() + "\n"
                + "fields=" + fields.size() + "\n"
                + "allDirectInvokes=" + invokes.size() + "\n"
                + "tracedOrderedInstructions=" + operations.size() + "\n"
                + "tracedCallInstructions=" + operations.stream().filter(x ->
                        x.kind().equals("CALL")).count() + "\n"
                + "faceBakeryBakingMethods=" + methods.stream().filter(x ->
                        x.owner().equals(FACE_BAKERY)
                        && x.name().equals("bakeQuad")).count() + "\n"
                + "modelRendererGetQuadsSites=" + invokes.stream().filter(x ->
                        x.source().equals(MODEL_RENDERER)
                        && x.targetMethod().equals("getQuads")).count() + "\n"
                + "limit=original_static_jvm_not_real_ae2_create_client_or_model_uv_acceptance\n",
                StandardCharsets.UTF_8);
    }
}
