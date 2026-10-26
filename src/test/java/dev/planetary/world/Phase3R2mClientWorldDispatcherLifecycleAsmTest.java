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
 * 3R-2m RESEARCH ONLY: original target Minecraft1.21.1/NeoForge21.1.215
 * lifecycle method-owner and selected bytecode paths. This deliberately does
 * NOT run a Minecraft client, inject Mixins, or assert dynamic thread safety.
 */
final class Phase3R2mClientWorldDispatcherLifecycleAsmTest {
    private static final String MC = "net/minecraft/";
    private static final String NEO = "net/neoforged/neoforge/";
    private static final String MINECRAFT = MC + "client/Minecraft";
    private static final String RENDERER = MC + "client/renderer/LevelRenderer";
    private static final String DISPATCHER = MC + "client/renderer/chunk/SectionRenderDispatcher";
    private static final String SECTION = DISPATCHER + "$RenderSection";
    private static final String REBUILD = SECTION + "$RebuildTask";
    private static final String CLIENT_LEVEL = MC + "client/multiplayer/ClientLevel";
    private static final String MODEL_MANAGER = NEO + "client/model/data/ModelDataManager";
    private static final List<String> ORIGINAL_CLASSES = List.of(
        MINECRAFT, RENDERER, DISPATCHER, SECTION, REBUILD,
        SECTION + "$ResortTransparencyTask",
        SECTION + "$CompileTask",
        NEO + "event/level/LevelEvent$Unload",
        MC + "client/renderer/ViewArea",
        MC + "client/renderer/SectionOcclusionGraph",
        CLIENT_LEVEL, MC + "client/multiplayer/ClientPacketListener",
        MC + "client/multiplayer/ClientChunkCache",
        MODEL_MANAGER,
        MC + "client/renderer/chunk/RenderRegionCache",
        MC + "client/renderer/chunk/RenderChunkRegion"
    );
    private static final Set<String> TARGET_NAMES = Set.of(
        "<init>", "setLevel", "clearLevel", "allChanged", "close", "dispose",
        "disconnect", "schedule", "runTask", "run", "clearBatchQueue",
        "blockUntilClear", "rebuildSectionAsync", "rebuildSection",
        "createCompileTask", "doTask", "cancelTasks", "setDirty", "setNotDirty",
        "setNeedsUpdate", "setSectionDirty", "markSectionDirty",
        "onChunkLoaded", "onChunkUnloaded", "onChunkReady",
        "releaseBuffers", "releaseAllBuffers", "stop", "reset",
        "getModelDataManager", "requestRefresh", "refreshAt",
        "snapshotSectionRegion", "updateRenderChunks",
        "getLevel", "getChunk", "reload", "createRegion",
        "setChunkSource", "getChunkSource", "getRenderSection",
        "updateLevelInEngines", "dropAllTasks", "uploadAllPendingUploads",
        "pollTask", "cancel", "clearTintCaches",
        "setStorage", "setViewDistance", "getViewDistance",
        "lambda$setLevel$0", "lambda$setLevel$1", "lambda$allChanged$0",
        "lambda$doTask$0", "lambda$doTask$1"
    );

    private record Owner(String name, String hash, String superclass, String interfaces,
                         boolean present) {
        String tsv() { return String.join("\t", name, hash, superclass, interfaces,
                Boolean.toString(present)); }
    }
    private record Decl(String owner, String name, String descriptor, int access,
                        boolean traced) {
        String tsv() { return String.join("\t", owner, name, descriptor,
                Integer.toString(access), Boolean.toString(traced)); }
    }
    private record Instr(String owner, String name, String descriptor,
                         int ordinal, int opcode, String kind, String operand) {
        String tsv() { return String.join("\t", owner, name, descriptor,
            Integer.toString(ordinal), Integer.toString(opcode), kind,
            operand.replace('\n',' ').replace('\t',' ')); }
    }

    @Test
    void originalClientWorldAndSectionDispatcherLifecycleOwnersAreTraceable() throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Decl> methods = new ArrayList<>();
        List<Instr> ins = new ArrayList<>();
        for (String owner : ORIGINAL_CLASSES) {
            byte[] data;
            try (InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(owner + ".class")) {
                if (stream == null) {
                    owners.add(new Owner(owner, "", "", "", false));
                    continue;
                }
                data = stream.readAllBytes();
            }
            String sha = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(data));
            new ClassReader(data).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int version, int access, String name,
                        String signature, String superclass, String[] interfaces) {
                    owners.add(new Owner(name, sha, superclass == null ? "" : superclass,
                        String.join(",",interfaces==null?new String[0]:interfaces), true));
                }
                @Override public MethodVisitor visitMethod(int access, String name,
                        String descriptor, String signature, String[] exceptions) {
                    boolean select = TARGET_NAMES.contains(name) ||
                        (name.startsWith("lambda$") && (
                            name.contains("setLevel") || name.contains("allChanged")
                            || name.contains("schedule") || name.contains("runTask")
                            || name.contains("rebuild") || name.contains("clear")
                            || name.contains("dispose")));
                    // Constructors of top-level Minecraft/client packet listener
                    // contain unrelated large setup graphs; retain declarations only.
                    if (name.equals("<init>") && (
                        owner.equals(MINECRAFT) ||
                        owner.endsWith("/ClientPacketListener") ||
                        owner.equals(RENDERER))) {
                        select = false;
                    }
                    methods.add(new Decl(owner,name,descriptor,access,select));
                    if(!select) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        int ordinal = 0;
                        void add(int op, String kind, String value) {
                            ins.add(new Instr(owner,name,descriptor,ordinal++,op,kind,value));
                        }
                        @Override public void visitFieldInsn(int op,String o,String n,String d) {
                            add(op,"FIELD",o+"."+n+":"+d);
                        }
                        @Override public void visitMethodInsn(int op,String o,String n,String d,
                                                               boolean itf) {
                            add(op,"CALL",o+"."+n+d+(itf?":interface":""));
                        }
                        @Override public void visitInvokeDynamicInsn(String n,String d,
                                      Handle bsm,Object...args) {
                            StringBuilder sb=new StringBuilder(n).append(d).append(";bootstrap=")
                                .append(bsm.getOwner()).append(".").append(bsm.getName());
                            for(Object a:args) if(a instanceof Handle h) {
                                sb.append(";handle=").append(h.getOwner()).append(".")
                                    .append(h.getName()).append(h.getDesc());
                            }
                            add(Opcodes.INVOKEDYNAMIC,"INDY",sb.toString());
                        }
                        @Override public void visitVarInsn(int op,int index) {
                            add(op,"VAR",Integer.toString(index));
                        }
                        @Override public void visitTypeInsn(int op,String value) {
                            add(op,"TYPE",value);
                        }
                        @Override public void visitInsn(int op) {
                            add(op,"OP","");
                        }
                        @Override public void visitLdcInsn(Object c) {
                            add(Opcodes.LDC,"CONST",String.valueOf(c));
                        }
                        @Override public void visitIntInsn(int op,int value) {
                            add(op,"INT",Integer.toString(value));
                        }
                        @Override public void visitJumpInsn(int op,Label label) {
                            add(op,"BRANCH","target_label");
                        }
                        @Override public void visitIincInsn(int index,int delta) {
                            add(Opcodes.IINC,"IINC",index+":"+delta);
                        }
                        @Override public void visitTableSwitchInsn(int min,int max,
                                                        Label def,Label...labels) {
                            add(Opcodes.TABLESWITCH,"SWITCH",min+":"+max);
                        }
                        @Override public void visitLookupSwitchInsn(Label def,int[] keys,
                                                          Label[] labels) {
                            add(Opcodes.LOOKUPSWITCH,"SWITCH","cases="+keys.length);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        for(String required:List.of(MINECRAFT,RENDERER,DISPATCHER,SECTION,
                                  REBUILD,CLIENT_LEVEL,MODEL_MANAGER)) {
            assertTrue(owners.stream().anyMatch(x->x.name().equals(required)&&x.present()),
                "Original NeoForge required lifecycle class missing: "+required);
        }
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(DISPATCHER)
                && x.name().equals("<init>") && x.kind().equals("FIELD")
                && x.operand().contains("SectionRenderDispatcher.level:")),
                "Dispatcher must bind a ClientLevel instance on construction");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(SECTION)
                && x.name().equals("cancelTasks") && x.kind().equals("CALL")
                && x.operand().contains(".cancel()")),
                "RenderSection must cancel existing rebuild or resort tasks");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(CLIENT_LEVEL)
                && x.name().equals("<init>") && x.kind().equals("CALL")
                && x.operand().contains("ModelDataManager.<init>")),
                "ClientLevel must create its ModelDataManager");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(REBUILD)
                && x.name().equals("doTask") && x.kind().equals("CALL")
                && x.operand().contains("SectionCompiler.compile")),
                "RebuildTask must call SectionCompiler");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(MINECRAFT)
                && x.name().equals("setLevel") && x.kind().equals("CALL")
                && x.operand().contains("LevelEvent$Unload.<init>")),
                "Minecraft.setLevel must construct old-Level unload hook");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(MINECRAFT)
                && x.name().equals("setLevel") && x.kind().equals("CALL")
                && x.operand().contains("Minecraft.updateLevelInEngines")),
                "World switch must delegate renderer state transition");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(MINECRAFT)
                && x.name().equals("updateLevelInEngines") && x.kind().equals("CALL")
                && x.operand().contains("LevelRenderer.setLevel")),
                "Actual original Minecraft.updateLevelInEngines must call renderer.setLevel");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(RENDERER)
                && x.name().equals("setLevel") && x.kind().equals("CALL")
                && x.operand().contains("SectionRenderDispatcher.dispose")),
                "LevelRenderer world switch must invoke dispatcher.dispose");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(DISPATCHER)
                && x.name().equals("dispose") && x.kind().equals("CALL")
                && x.operand().contains("SectionRenderDispatcher.clearBatchQueue")),
                "Dispatcher dispose must cancel pending queued compile tasks");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(DISPATCHER)
                && x.name().equals("runTask") && x.kind().equals("CALL")
                && x.operand().contains("CompletableFuture.supplyAsync")),
                "Worker dispatch must prove it can schedule async work");
        assertTrue(ins.stream().anyMatch(x -> x.owner().equals(DISPATCHER)
                && x.name().equals("dispose") && x.kind().equals("FIELD")
                && x.operand().contains("SectionRenderDispatcher.closed")),
                "Dispatcher disposal must mark closed state");

        owners.sort(Comparator.comparing(Owner::name));
        methods.sort(Comparator.comparing(Decl::owner).thenComparing(Decl::name)
                .thenComparing(Decl::descriptor));
        ins.sort(Comparator.comparing(Instr::owner).thenComparing(Instr::name)
                .thenComparing(Instr::descriptor).thenComparingInt(Instr::ordinal));
        Path out=Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary"));
        Files.createDirectories(out);
        List<String> ownerLines=new ArrayList<>(List.of(
            "owner\tclass_sha256\tsuperclass\tinterfaces\tpresent"));
        owners.stream().map(Owner::tsv).forEach(ownerLines::add);
        Files.write(out.resolve("phase3-r2m-original-client-owners.tsv"),
                ownerLines,StandardCharsets.UTF_8);
        List<String> methodLines=new ArrayList<>(List.of(
            "owner\tmethod\tjvm_descriptor\taccess\ttraced"));
        methods.stream().map(Decl::tsv).forEach(methodLines::add);
        Files.write(out.resolve("phase3-r2m-original-client-method-inventory.tsv"),
                methodLines,StandardCharsets.UTF_8);
        List<String> instructionLines=new ArrayList<>(List.of(
            "owner\tmethod\tjvm_descriptor\tordinal\topcode\tkind\toperand"));
        ins.stream().map(Instr::tsv).forEach(instructionLines::add);
        Files.write(out.resolve("phase3-r2m-original-client-instructions.tsv"),
                instructionLines,StandardCharsets.UTF_8);
        Files.writeString(out.resolve("phase3-r2m-original-client-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "kind=original_compiled_bytecode_not_transformed_client\n"
                + "classNames="+owners.size()+"\n"
                + "classesPresent="+owners.stream().filter(Owner::present).count()+"\n"
                + "methodsDeclared="+methods.size()+"\n"
                + "methodsTraced="+methods.stream().filter(Decl::traced).count()+"\n"
                + "orderedInstructions="+ins.size()+"\n"
                + "directInvokes="+ins.stream().filter(x->x.kind().equals("CALL")).count()+"\n"
                + "indySites="+ins.stream().filter(x->x.kind().equals("INDY")).count()+"\n"
                + "limit=static_selected_owners_no_loaded_mods_no_world_switch_gameplay\n",
                StandardCharsets.UTF_8);
    }
}
