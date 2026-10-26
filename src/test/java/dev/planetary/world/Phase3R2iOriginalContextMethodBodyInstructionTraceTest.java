package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3R-2i RESEARCH ONLY: original target-version method-body instruction
 * probes. Instruction order, field constants, and branch opcodes are stronger
 * evidence than a method-name call graph, but not runtime virtual dispatch.
 */
final class Phase3R2iOriginalContextMethodBodyInstructionTraceTest {
    private static final String N = "net/minecraft/";
    private static final String EXT = "net/neoforged/neoforge/common/extensions/IBlockStateExtension";
    private static final String SUPPORT = N + "world/level/block/SupportType";
    private static final String PISTON = N + "world/level/block/piston/PistonMovingBlockEntity";

    private record Selection(String owner, String name) {}
    private static final List<Selection> SELECT = List.of(
            new Selection(SUPPORT + "$1", "isSupporting"),
            new Selection(SUPPORT + "$2", "isSupporting"),
            new Selection(SUPPORT + "$3", "isSupporting"),
            new Selection(PISTON, "getCollisionShape"),
            new Selection(PISTON, "moveCollidedEntities"),
            new Selection(PISTON, "moveStuckEntities"),
            new Selection(N + "world/level/CollisionGetter", "isUnobstructed"),
            new Selection(N + "world/level/CollisionGetter", "noCollision"),
            new Selection(EXT, "hidesNeighborFace"),
            new Selection(EXT, "supportsExternalFaceHiding")
    );
    private record Instr(String owner, String name, String methodDescriptor,
                         int instruction, int opcode, String kind, String operand) {
        String asTsv() {
            return String.join("\t", owner, name, methodDescriptor,
                    Integer.toString(instruction), Integer.toString(opcode),
                    kind, operand.replace('\t', ' ').replace('\n', ' '));
        }
    }
    @Test
    void traceOriginalSupportAndMovingPistonMethodBodyToExactInstructions() throws Exception {
        List<Instr> results = new ArrayList<>();
        List<String> hashes = new ArrayList<>(List.of("class_owner\tclass_sha256"));
        for (String owner : SELECT.stream().map(Selection::owner).distinct().toList()) {
            byte[] bytes;
            try (InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(owner + ".class")) {
                assertNotNull(stream, "Original NeoForge class missing: " + owner);
                bytes = stream.readAllBytes();
            }
            hashes.add(owner + "\t" + HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes)));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String sig, String[] errors) {
                    if (!SELECT.contains(new Selection(owner, name))) {
                        return null;
                    }
                    return new MethodVisitor(Opcodes.ASM9) {
                        int ordinal = 0;
                        private void emit(int op, String kind, String operand) {
                            results.add(new Instr(owner, name, desc, ordinal++,
                                    op, kind, operand));
                        }
                        @Override
                        public void visitMethodInsn(int opcode, String target, String method,
                                                    String descriptor, boolean itf) {
                            emit(opcode, "CALL", target + "." + method + descriptor);
                        }
                        @Override
                        public void visitFieldInsn(int opcode, String target, String field,
                                                   String descriptor) {
                            emit(opcode, "FIELD", target + "." + field + ":" + descriptor);
                        }
                        @Override
                        public void visitTypeInsn(int opcode, String type) {
                            emit(opcode, "TYPE", type);
                        }
                        @Override
                        public void visitInsn(int opcode) {
                            emit(opcode, "OP", "");
                        }
                        @Override
                        public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
                            emit(opcode, "JUMP", "symbolic_label");
                        }
                        @Override
                        public void visitVarInsn(int opcode, int var) {
                            emit(opcode, "VAR", Integer.toString(var));
                        }
                        @Override
                        public void visitIntInsn(int opcode, int operand) {
                            emit(opcode, "INT", Integer.toString(operand));
                        }
                        @Override
                        public void visitLdcInsn(Object cst) {
                            emit(Opcodes.LDC, "LDC", cst.toString());
                        }
                        @Override
                        public void visitIincInsn(int var, int increment) {
                            emit(Opcodes.IINC, "IINC", var + ":" + increment);
                        }
                        @Override
                        public void visitInvokeDynamicInsn(String method, String descriptor,
                                org.objectweb.asm.Handle bsm, Object... args) {
                            emit(Opcodes.INVOKEDYNAMIC, "INDY",
                                    method + descriptor + ":" + bsm.getOwner() + "." + bsm.getName());
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertEquals(10, SELECT.size());
        for (Selection selection : SELECT) {
            assertTrue(results.stream().anyMatch(x -> x.owner().equals(selection.owner())
                    && x.name().equals(selection.name())),
                    "No traced instruction body for " + selection);
        }
        for (int i = 1; i <= 3; i++) {
            String owner = SUPPORT + "$" + i;
            assertTrue(results.stream().anyMatch(x -> x.owner().equals(owner)
                    && x.kind().equals("CALL")
                    && x.operand().contains("getBlockSupportShape")));
        }
        assertTrue(results.stream().anyMatch(x -> x.owner().equals(PISTON)
                && x.kind().equals("CALL")
                && x.operand().contains("getCollisionShape")));

        Path reportDir = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(reportDir);
        List<String> data = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\tinstruction_ordinal\topcode\tkind\toperand"));
        results.stream().map(Instr::asTsv).forEach(data::add);
        Files.write(reportDir.resolve("phase3-r2i-original-support-piston-method-body.tsv"),
                data, StandardCharsets.UTF_8);
        Files.write(reportDir.resolve("phase3-r2i-original-support-piston-class-hashes.tsv"),
                hashes, StandardCharsets.UTF_8);
        Files.writeString(reportDir.resolve(
                "phase3-r2i-original-support-piston-instruction-summary.txt"),
                "methodBodies=" + SELECT.size() + "\n"
                + "classfiles=" + (hashes.size() - 1) + "\n"
                + "instructions=" + results.size() + "\n"
                + "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "proof=unapplied_original_classfile_instruction_trace\n"
                + "notProof=runtime_mod_dispatch,client_mixin,gameplay\n",
                StandardCharsets.UTF_8);
    }
}
