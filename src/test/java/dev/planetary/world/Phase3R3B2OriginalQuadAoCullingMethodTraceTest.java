package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Planetary 3R-3B-2 RESEARCH ONLY: exact untransformed Minecraft 1.21.1 and
 * NeoForge 21.1.215 method-body opcodes for baked quads, culling, AO/flat light,
 * ModelData and render-pass dispatch. No Mixin weaving or live mod renderer.
 */
final class Phase3R3B2OriginalQuadAoCullingMethodTraceTest {
    private static final String MC = "net/minecraft/";
    private static final String NF = "net/neoforged/neoforge/";
    private static final String RENDERER = MC + "client/renderer/block/ModelBlockRenderer";
    private static final String DISPATCHER = MC + "client/renderer/block/BlockRenderDispatcher";
    private static final String COMPILER = MC + "client/renderer/chunk/SectionCompiler";
    private static final String BLOCK = MC + "world/level/block/Block";
    private static final String BAKED = MC + "client/resources/model/BakedModel";
    private static final String SMOOTH = NF + "client/model/lighting/SmoothQuadLighter";
    private static final String FLAT = NF + "client/model/lighting/FlatQuadLighter";
    private static final List<String> CLASSES = List.of(
            RENDERER, RENDERER + "$AmbientOcclusionFace",
            DISPATCHER, COMPILER, BLOCK, BAKED,
            MC + "client/renderer/block/model/BakedQuad",
            MC + "world/level/block/state/BlockState",
            MC + "world/level/block/state/BlockBehaviour$BlockStateBase",
            MC + "world/level/BlockAndTintGetter",
            MC + "client/color/block/BlockColors",
            MC + "client/renderer/LightTexture",
            MC + "client/renderer/block/LiquidBlockRenderer",
            MC + "client/renderer/chunk/RenderChunkRegion",
            NF + "client/extensions/IBakedModelExtension",
            NF + "client/model/IDynamicBakedModel",
            NF + "client/model/lighting/QuadLighter",
            SMOOTH, FLAT,
            NF + "client/model/BakedModelWrapper"
    );
    private static final Set<String> NAMES = Set.of(
            "tesselateBlock", "tesselateWithAO", "tesselateWithoutAO",
            "renderQuad", "renderQuadList", "renderModel", "putQuadData",
            "renderBatched", "renderSingleBlock", "renderBreakingTexture",
            "getQuads", "getModelData", "getRenderTypes", "getRenderType",
            "shouldRenderFace", "skipRendering", "hidesNeighborFace",
            "supportsExternalFaceHiding", "getFaceOcclusionShape",
            "getOcclusionShape", "getAppearance", "getShade", "getBrightness",
            "getLightColor", "getLightEngine", "getBlockTint",
            "getLightEmission", "getColor", "getTintIndex", "isTinted",
            "getVertices", "getDirection", "hasAmbientOcclusion", "isShade",
            "useAmbientOcclusion", "getBlockState", "getBlockEntity",
            "getLightmap", "calculate", "calculateShape",
            "calculateLighting", "getLightMap", "getLightmapCoordinates",
            "getRenderShape", "processQuad", "lightQuad", "shade",
            "computeLighting", "setup", "reset", "getModel", "getBlockModel",
            "getBlockRenderer", "getBlockRenderTypes", "getBlockSupportShape"
    );

    private record Owner(String owner, String hash, String parent,
                         String interfaces, boolean present) {
        String tsv() {
            return String.join("\t", owner, hash, parent, interfaces,
                    Boolean.toString(present));
        }
    }
    private record Method(String owner, String name, String desc, int access) {
        String tsv() { return String.join("\t", owner, name, desc,
                Integer.toString(access)); }
    }
    private record Instruction(String owner, String name, String desc,
                               int order, int opcode, String kind, String target) {
        String tsv() {
            return String.join("\t", owner, name, desc, Integer.toString(order),
                    Integer.toString(opcode), kind,
                    target.replace('\n', ' ').replace('\t', ' '));
        }
    }

    @Test
    void traceOriginalModelQuadAoAndNeighborOcclusionMethodBodies() throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Instruction> ops = new ArrayList<>();
        for (String requested : CLASSES) {
            byte[] bytes;
            try (InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(requested + ".class")) {
                if (stream == null) {
                    owners.add(new Owner(requested, "", "", "", false));
                    continue;
                }
                bytes = stream.readAllBytes();
            }
            String hash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String owner,
                        String signature, String parent, String[] interfaces) {
                    owners.add(new Owner(owner, hash, parent == null ? "" : parent,
                            String.join(",", interfaces == null
                                    ? new String[0] : interfaces), true));
                }
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                        String signature, String[] exceptions) {
                    if (!NAMES.contains(name) && !(name.startsWith("lambda$")
                            && (name.contains("tesselate") ||
                                    name.contains("getQuads") ||
                                    name.contains("renderModel")))) {
                        return null;
                    }
                    methods.add(new Method(requested, name, desc, access));
                    return new MethodVisitor(Opcodes.ASM9) {
                        int order;
                        void add(int opcode, String kind, String value) {
                            ops.add(new Instruction(requested, name, desc,
                                    order++, opcode, kind, value));
                        }
                        @Override
                        public void visitFieldInsn(int opcode, String owner,
                                String field, String desc) {
                            add(opcode, "FIELD", owner + "." + field + ":" + desc);
                        }
                        @Override
                        public void visitMethodInsn(int opcode, String owner,
                                String method, String desc, boolean itf) {
                            add(opcode, "CALL", owner + "." + method + desc
                                    + (itf ? ":interface" : ""));
                        }
                        @Override
                        public void visitInvokeDynamicInsn(String name, String desc,
                                Handle bootstrap, Object... args) {
                            StringBuilder s = new StringBuilder(name).append(desc)
                                    .append(";bootstrap=").append(bootstrap.getOwner())
                                    .append(".").append(bootstrap.getName());
                            for (Object arg : args) {
                                if (arg instanceof Handle h) {
                                    s.append(";handle=").append(h.getOwner())
                                            .append(".").append(h.getName())
                                            .append(h.getDesc());
                                }
                            }
                            add(Opcodes.INVOKEDYNAMIC, "INDY", s.toString());
                        }
                        @Override public void visitInsn(int opcode) {
                            add(opcode, "OP", "");
                        }
                        @Override public void visitIntInsn(int opcode, int operand) {
                            add(opcode, "INT", Integer.toString(operand));
                        }
                        @Override public void visitVarInsn(int opcode, int index) {
                            add(opcode, "VAR", Integer.toString(index));
                        }
                        @Override public void visitTypeInsn(int opcode, String type) {
                            add(opcode, "TYPE", type);
                        }
                        @Override public void visitJumpInsn(int opcode, Label label) {
                            add(opcode, "BRANCH", "target_label");
                        }
                        @Override public void visitLdcInsn(Object constant) {
                            add(Opcodes.LDC, "CONST", String.valueOf(constant));
                        }
                        @Override public void visitIincInsn(int index, int amount) {
                            add(Opcodes.IINC, "IINC", index + ":" + amount);
                        }
                        @Override public void visitTableSwitchInsn(int from, int to,
                                Label dflt, Label... labels) {
                            add(Opcodes.TABLESWITCH, "SWITCH", from + ":" + to);
                        }
                        @Override public void visitLookupSwitchInsn(Label dflt,
                                int[] keys, Label[] labels) {
                            add(Opcodes.LOOKUPSWITCH, "SWITCH", "cases=" + keys.length);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }

        for (String required : List.of(RENDERER, DISPATCHER, COMPILER, BAKED,
                BLOCK, SMOOTH, FLAT)) {
            assertTrue(owners.stream().anyMatch(o ->
                            o.owner().equals(required) && o.present()),
                    "Required original NeoForge owner absent: " + required);
        }
        for (String m : List.of("tesselateBlock", "tesselateWithAO",
                "tesselateWithoutAO")) {
            assertTrue(methods.stream().anyMatch(x ->
                            x.owner().equals(RENDERER) && x.name().equals(m)),
                    "Original ModelBlockRenderer method absent: " + m);
        }
        assertTrue(ops.stream().anyMatch(x ->
                x.owner().equals(RENDERER) && x.kind().equals("CALL")
                        && x.target().contains(BAKED + ".getQuads")),
                "Original model renderer must query original BakedModel quads");
        assertTrue(ops.stream().anyMatch(x ->
                x.owner().equals(RENDERER) && x.kind().equals("CALL")
                        && x.target().contains(BLOCK + ".shouldRenderFace")),
                "Original side-culling dispatch must remain accounted for");
        assertTrue(ops.stream().anyMatch(x ->
                x.owner().equals(BLOCK) && x.name().equals("shouldRenderFace")
                        && x.kind().equals("CALL")
                        && x.target().contains("getFaceOcclusionShape")),
                "Original Block.shouldRenderFace must call occlusion shape");
        assertTrue(ops.stream().anyMatch(x ->
                x.owner().equals(COMPILER) && x.kind().equals("CALL")
                        && x.target().contains("getModelData")),
                "Compiler must carry ModelData through actual original method");

        owners.sort(Comparator.comparing(Owner::owner));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name)
                .thenComparing(Method::desc));
        ops.sort(Comparator.comparing(Instruction::owner)
                .thenComparing(Instruction::name).thenComparing(Instruction::desc)
                .thenComparingInt(Instruction::order));
        Path dir = Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary"));
        Files.createDirectories(dir);
        List<String> os = new ArrayList<>(List.of(
                "owner\tclass_sha256\tsuperclass\tinterfaces\tpresent"));
        owners.stream().map(Owner::tsv).forEach(os::add);
        Files.write(dir.resolve("phase3-r3b2-original-quad-ao-owners.tsv"), os,
                StandardCharsets.UTF_8);
        List<String> ms = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess"));
        methods.stream().map(Method::tsv).forEach(ms::add);
        Files.write(dir.resolve("phase3-r3b2-original-quad-ao-methods.tsv"), ms,
                StandardCharsets.UTF_8);
        List<String> is = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\tinstruction_ordinal\topcode\tkind\toperand"));
        ops.stream().map(Instruction::tsv).forEach(is::add);
        Files.write(dir.resolve("phase3-r3b2-original-quad-ao-instructions.tsv"), is,
                StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("phase3-r3b2-original-quad-ao-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "stage=3R-3B-2\n"
                + "scope=selected_original_method_body_not_live_client_or_mod\n"
                + "requestedClassNames=" + CLASSES.size() + "\n"
                + "presentClassfiles=" + owners.stream().filter(Owner::present).count() + "\n"
                + "absentClassfiles=" + owners.stream().filter(x -> !x.present()).count() + "\n"
                + "selectedMethodBodies=" + methods.size() + "\n"
                + "orderedInstructions=" + ops.size() + "\n"
                + "directInvokes=" + ops.stream().filter(x ->
                        x.kind().equals("CALL")).count() + "\n"
                + "fieldAccesses=" + ops.stream().filter(x ->
                        x.kind().equals("FIELD")).count() + "\n"
                + "invokedynamicSites=" + ops.stream().filter(x ->
                        x.kind().equals("INDY")).count() + "\n"
                + "rendererGetQuadsCalls=" + ops.stream().filter(x ->
                        x.owner().equals(RENDERER) && x.kind().equals("CALL")
                        && x.target().contains(BAKED + ".getQuads")).count() + "\n"
                + "rendererShouldRenderFaceCalls=" + ops.stream().filter(x ->
                        x.owner().equals(RENDERER) && x.kind().equals("CALL")
                        && x.target().contains(BLOCK + ".shouldRenderFace")).count() + "\n"
                + "limit=not_a_world-provenance_dynamic_face_gameplay_acceptance\n",
                StandardCharsets.UTF_8);
    }
}
