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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3R-2l RESEARCH ONLY: exact original NeoForge21.1.215 bytecode of
 * region snapshot creation/getters and worker section rebuild paths.
 * Neither a control/data-flow interpreter nor proof of runtime Level identity,
 * lambda capture, modded execution or client Mixin application.
 */
final class Phase3R2lRegionSnapshotWorkerBytecodeTraceTest {
    private static final String MC = "net/minecraft/";
    private static final String NEO = "net/neoforged/neoforge/";
    private static final String CACHE = MC + "client/renderer/chunk/RenderRegionCache";
    private static final String REGION = MC + "client/renderer/chunk/RenderChunkRegion";
    private static final String SECTION = MC + "client/renderer/chunk/SectionCompiler";
    private static final String DISPATCHER = MC + "client/renderer/chunk/SectionRenderDispatcher";
    private static final String MANAGER = NEO + "client/model/data/ModelDataManager";

    private static final List<String> OWNERS = List.of(
            CACHE, REGION, SECTION,
            MC + "client/renderer/chunk/RenderRegionCache$ChunkInfo",
            MC + "client/renderer/chunk/RenderChunk",
            DISPATCHER,
            DISPATCHER + "$RenderSection",
            DISPATCHER + "$RenderSection$RebuildTask",
            DISPATCHER + "$RenderSection$ResortTransparencyTask",
            MC + "client/renderer/LevelRenderer",
            MC + "client/multiplayer/ClientLevel",
            MANAGER,
            MC + "client/resources/model/BakedModel",
            MC + "client/renderer/block/BlockRenderDispatcher",
            MC + "world/level/Level"
    );

    private static final Set<String> TRACK = Set.of(
            "<init>", "createRegion", "getModelData", "getModelDataManager",
            "getBlockState", "getBlockEntity", "getBlockTint", "getLightEngine",
            "compile", "compileSection", "rebuildSection", "rebuildSectionAsync",
            "scheduleRebuild", "createCompileTask", "doTask", "getRegion",
            "setDirty", "isDirty", "setNotDirty", "releaseBuffers",
            "getAt", "snapshot", "getSnapshot", "getModelDataSnapshot",
            "snapshotSectionRegion", "refreshAt", "requestRefresh",
            "requestModelDataUpdate", "getSection", "getChunk",
            "getRenderChunk", "getOrCreate", "schedule", "cancelTasks",
            "getChunkInfo",
            "run", "lambda$createRegion$0", "lambda$createRegion$1",
            "lambda$compile$0", "lambda$compile$1"
    );

    private record ClassInfo(String owner, String hash, String superName,
                             String interfaces, boolean present) {
        String tsv() { return String.join("\t", owner, hash, superName,
                interfaces, Boolean.toString(present)); }
    }
    private record Signature(String owner, String name, String desc, int access) {
        String tsv() { return String.join("\t", owner, name, desc,
                Integer.toString(access)); }
    }
    private record Instruction(String owner, String method, String descriptor,
                               int ordinal, int opcode, String kind, String operand) {
        String tsv() { return String.join("\t", owner, method, descriptor,
                Integer.toString(ordinal), Integer.toString(opcode), kind,
                operand.replace('\n', ' ').replace('\t', ' ')); }
    }

    @Test
    void traceOriginalRenderRegionSnapshotAndSectionWorkerMethods()
            throws Exception {
        List<ClassInfo> classes = new ArrayList<>();
        List<Signature> signatures = new ArrayList<>();
        List<Instruction> instructions = new ArrayList<>();
        for (String owner : OWNERS) {
            byte[] bytes;
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(owner + ".class")) {
                if (in == null) {
                    classes.add(new ClassInfo(owner, "", "", "", false));
                    continue;
                }
                bytes = in.readAllBytes();
            }
            String hash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name,
                                  String signature, String superName, String[] interfaces) {
                    classes.add(new ClassInfo(name, hash,
                            superName == null ? "" : superName,
                            String.join(",", interfaces == null ? new String[0] : interfaces),
                            true));
                }

                @Override
                public MethodVisitor visitMethod(int access, String name,
                                                 String desc, String signature,
                                                 String[] exceptions) {
                    boolean capture = TRACK.contains(name) ||
                            (name.startsWith("lambda$") && (
                                    name.contains("createRegion") ||
                                    name.contains("getModelData") ||
                                    name.contains("compile") ||
                                    name.contains("rebuild") ||
                                    name.contains("doTask")));
                    if (!capture) return null;
                    signatures.add(new Signature(owner, name, desc, access));
                    return new MethodVisitor(Opcodes.ASM9) {
                        int ordinal;
                        void record(int opcode, String kind, String value) {
                            instructions.add(new Instruction(owner, name, desc,
                                    ordinal++, opcode, kind, value));
                        }

                        @Override
                        public void visitFieldInsn(int opcode, String target,
                                                   String field, String fieldDesc) {
                            record(opcode, "FIELD", target + "." + field + ":" + fieldDesc);
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String target,
                                                    String method, String methodDesc,
                                                    boolean isInterface) {
                            record(opcode, "CALL",
                                    target + "." + method + methodDesc
                                            + (isInterface ? ":interface" : ""));
                        }

                        @Override
                        public void visitInvokeDynamicInsn(String invoked,
                                String invokedDesc, Handle bootstrap, Object... args) {
                            StringBuilder s = new StringBuilder(invoked)
                                    .append(invokedDesc).append(";bootstrap=")
                                    .append(bootstrap.getOwner()).append(".")
                                    .append(bootstrap.getName()).append(bootstrap.getDesc());
                            for (Object arg : args) {
                                if (arg instanceof Handle h) {
                                    s.append(";handle=")
                                     .append(h.getOwner()).append(".")
                                     .append(h.getName()).append(h.getDesc());
                                }
                            }
                            record(Opcodes.INVOKEDYNAMIC, "INDY", s.toString());
                        }

                        @Override
                        public void visitVarInsn(int opcode, int varIndex) {
                            record(opcode, "VAR", Integer.toString(varIndex));
                        }

                        @Override
                        public void visitTypeInsn(int opcode, String type) {
                            record(opcode, "TYPE", type);
                        }

                        @Override
                        public void visitJumpInsn(int opcode, Label target) {
                            record(opcode, "BRANCH", "target_label");
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            record(opcode, "OP", "");
                        }

                        @Override
                        public void visitIntInsn(int opcode, int operand) {
                            record(opcode, "INT", Integer.toString(operand));
                        }

                        @Override
                        public void visitLdcInsn(Object constant) {
                            record(Opcodes.LDC, "CONST", String.valueOf(constant));
                        }

                        @Override
                        public void visitIincInsn(int var, int increment) {
                            record(Opcodes.IINC, "IINC", var + ":" + increment);
                        }

                        @Override
                        public void visitTableSwitchInsn(int min, int max,
                                                        Label defaultLabel, Label... labels) {
                            record(Opcodes.TABLESWITCH, "SWITCH", min + ":" + max);
                        }

                        @Override
                        public void visitLookupSwitchInsn(Label defaultLabel,
                                                          int[] keys, Label[] labels) {
                            record(Opcodes.LOOKUPSWITCH, "SWITCH", "cases=" + keys.length);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }

        for (String required : List.of(CACHE, REGION, SECTION, DISPATCHER, MANAGER)) {
            assertTrue(classes.stream().anyMatch(c ->
                    c.owner().equals(required) && c.present()),
                    "Missing target 21.1.215 original class " + required);
        }
        for (String mustHave : List.of("createRegion", "getModelData",
                "getBlockState", "getBlockEntity", "compile")) {
            assertTrue(signatures.stream().anyMatch(s -> s.name().equals(mustHave)),
                    "No original method body for " + mustHave);
        }
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(CACHE) && i.method().equals("createRegion")
                && i.kind().equals("CALL")
                && i.operand().contains("getModelDataManager")),
                "Target original region cache must request original Level model data manager");
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(SECTION) && i.method().equals("compile")
                && i.kind().equals("CALL") && i.operand().contains("getModelData")),
                "Target original SectionCompiler must consume model data");
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(REGION) && i.method().equals("getModelData")),
                "Region must expose original getModelData body");
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(CACHE) && i.method().equals("createRegion")
                && i.kind().equals("CALL")
                && i.operand().contains("ModelDataManager.snapshotSectionRegion")),
                "RegionCache must request the original ModelDataManager region snapshot");
        assertTrue(signatures.stream().anyMatch(x ->
                x.owner().equals(MANAGER) && x.name().equals("snapshotSectionRegion")),
                "Capture the actual snapshot body, not only the callsite");
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(REGION) && i.method().equals("getModelData")
                && i.kind().equals("FIELD")
                && i.operand().contains("RenderChunkRegion.modelDataSnapshot")),
                "Region.getModelData must directly read its own snapshot field");
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(DISPATCHER + "$RenderSection")
                && i.method().equals("createCompileTask")
                && i.kind().equals("CALL")
                && i.operand().contains("RenderRegionCache.createRegion")),
                "Compile task must originate its RenderChunkRegion from a region cache");
        assertTrue(instructions.stream().anyMatch(i ->
                i.owner().equals(DISPATCHER + "$RenderSection$RebuildTask")
                && i.method().equals("doTask")
                && i.kind().equals("CALL")
                && i.operand().contains("SectionCompiler.compile")),
                "The rebuild worker must dispatch to SectionCompiler");

        classes.sort(Comparator.comparing(ClassInfo::owner));
        signatures.sort(Comparator.comparing(Signature::owner)
                .thenComparing(Signature::name).thenComparing(Signature::desc));
        instructions.sort(Comparator.comparing(Instruction::owner)
                .thenComparing(Instruction::method)
                .thenComparing(Instruction::descriptor)
                .thenComparingInt(Instruction::ordinal));

        Path reports = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(reports);
        List<String> classLines = new ArrayList<>(List.of(
                "owner\tsha256\tsuperclass\tinterfaces\tpresent"));
        classes.stream().map(ClassInfo::tsv).forEach(classLines::add);
        Files.write(reports.resolve("phase3-r2l-original-worker-classes.tsv"),
                classLines, StandardCharsets.UTF_8);
        List<String> methods = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess"));
        signatures.stream().map(Signature::tsv).forEach(methods::add);
        Files.write(reports.resolve("phase3-r2l-original-worker-methods.tsv"),
                methods, StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\tinstruction_ordinal\topcode\tkind\toperand"));
        instructions.stream().map(Instruction::tsv).forEach(lines::add);
        Files.write(reports.resolve("phase3-r2l-original-worker-instructions.tsv"),
                lines, StandardCharsets.UTF_8);
        long absent = classes.stream().filter(x -> !x.present()).count();
        long dynamic = instructions.stream().filter(x -> x.kind().equals("INDY")).count();
        long call = instructions.stream().filter(x -> x.kind().equals("CALL")).count();
        Files.writeString(reports.resolve("phase3-r2l-original-worker-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "scope=selected_original_bytecode_not_applied_client_mixins\n"
                + "ownerNames=" + classes.size() + "\n"
                + "ownerAbsent=" + absent + "\n"
                + "selectedMethodBodies=" + signatures.size() + "\n"
                + "orderedInstructions=" + instructions.size() + "\n"
                + "directInvokes=" + call + "\n"
                + "invokeDynamic=" + dynamic + "\n"
                + "regionCreateBodies=" + signatures.stream().filter(x ->
                    x.owner().equals(CACHE) && x.name().equals("createRegion")).count() + "\n"
                + "workerBodies=" + signatures.stream().filter(x ->
                    x.owner().contains("SectionRenderDispatcher")
                    && (x.name().contains("doTask") || x.name().contains("rebuild")
                    || x.name().contains("compile") || x.name().startsWith("lambda$"))).count() + "\n"
                + "limit=syntactic_static_paths_not_runtime_object_identity_or_mods\n",
                StandardCharsets.UTF_8);
    }
}
