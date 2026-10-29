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
 * Planetary 3R-7F-4 research only: original Minecraft1.21.1 / NeoForge21.1.215
 * reload listener and late worker JMV source owners.
 * Does not run the client, actual worker races, installed mods or GPU driver.
 */
final class Phase3R7F4ReloadRegistrationLateWorkerAsmTest {
    private static final String MC = "net/minecraft/";
    private static final String NF = "net/neoforged/neoforge/";
    private static final String R = MC + "client/renderer/";
    private static final String S = R + "chunk/SectionRenderDispatcher";
    private static final String SECTION = S + "$RenderSection";
    private static final String GRAPH = R + "SectionOcclusionGraph";
    private static final String LEVEL = R + "LevelRenderer";
    private static final String MINECRAFT = MC + "client/Minecraft";
    private static final String RESOURCE = MC + "server/packs/resources/";
    private static final String RELOADABLE = RESOURCE + "ReloadableResourceManager";
    private static final String BER = R + "blockentity/BlockEntityRenderDispatcher";
    private static final String MODEL = MC + "client/resources/model/ModelManager";
    private static final String TEXTURE = R + "texture/TextureManager";
    // A deliberately finite owner subset; optional NeoForge listener event is
    // inventoried when it is actually present in target classpath.
    private static final List<String> REQUIRED = List.of(
        GRAPH, LEVEL, MINECRAFT, RELOADABLE, RESOURCE + "PreparableReloadListener",
        RESOURCE + "SimpleReloadInstance", MODEL, TEXTURE,
        BER, R + "entity/EntityRenderDispatcher",
        S, SECTION, SECTION + "$CompileTask", SECTION + "$RebuildTask",
        SECTION + "$ResortTransparencyTask", R + "chunk/SectionCompiler",
        R + "chunk/RenderRegionCache", R + "chunk/RenderChunkRegion",
        NF + "client/ClientHooks", NF + "client/model/data/ModelDataManager"
    );
    private static final List<String> OPTIONAL = List.of(
        NF + "client/event/RegisterClientReloadListenersEvent",
        NF + "client/event/RegisterReloadListenersEvent",
        NF + "event/AddReloadListenerEvent",
        NF + "client/event/ModelEvent$RegisterAdditional",
        GRAPH + "$GraphEvents", GRAPH + "$GraphState",
        GRAPH + "$GraphStorage", S + "$UploadTask",
        RESOURCE + "ProfiledReloadInstance"
    );
    private static final Set<String> SELECT = Set.of(
        "<init>", "waitAndReset", "setLevel", "allChanged", "reset",
        "registerReloadListener", "registerReloadListenerIfNotPresent",
        "getListeners", "createReload", "reload", "onResourceManagerReload",
        "load", "apply", "prepare", "close", "start", "stop", "dispose",
        "runTask", "schedule", "pollTask", "clearBatchQueue", "blockUntilClear",
        "uploadAllPendingUploads", "uploadSectionLayer", "uploadSectionIndexBuffer",
        "doTask", "cancel", "cancelTasks", "releaseBuffers", "releaseAllBuffers",
        "compile", "createCompileTask", "rebuildSectionAsync", "rebuildSection",
        "createRegion", "onChunkLoaded", "onChunkUnloaded", "onChunkReady",
        "addAdditionalGeometry", "gatherAdditionalRenderers",
        "updateLevelInEngines", "addListener", "removeListener",
        "notifyListeners", "thenCompose", "thenApply", "whenComplete"
    );

    record Owner(String name, String sha, String parent, String interfaces, boolean required) {
        String row() {
            return String.join("\t", name, sha, parent, interfaces, Boolean.toString(required));
        }
    }
    record Decl(String owner, String name, String desc, int access) {
        String row() {return owner + "\t" + name + "\t" + desc + "\t" + access;}
    }
    record Invoke(String owner, String name, String desc,
                  String target, String called, String calledDesc, int op) {
        String row() {
            return owner + "\t" + name + "\t" + desc + "\t" + target + "\t"
                    + called + "\t" + calledDesc + "\t" + op;
        }
    }
    record Instruction(String owner, String name, String desc, int ordinal,
                       int opcode, String kind, String arg) {
        String row() {
            return owner + "\t" + name + "\t" + desc + "\t" + ordinal + "\t"
                    + opcode + "\t" + kind + "\t" + arg.replace('\t',' ').replace('\n',' ');
        }
    }
    @Test
    void originalReloadRegistrationGraphAndLateSectionWorkChains() throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Decl> methods = new ArrayList<>();
        List<Invoke> calls = new ArrayList<>();
        List<Instruction> instructions = new ArrayList<>();
        for (String name: concat(REQUIRED, OPTIONAL)) {
            byte[] bytes;
            try (InputStream input = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(name + ".class")) {
                if (input == null) {
                    if (REQUIRED.contains(name)) fail("Missing ORIGINAL required owner: " + name);
                    continue;
                }
                bytes = input.readAllBytes();
            }
            String hash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int v, int access, String actual,
                                           String sig, String parent, String[] ifaces) {
                    assertEquals(name,actual);
                    owners.add(new Owner(name,hash,parent==null?"":parent,
                            String.join(",",ifaces==null?new String[0]:ifaces),REQUIRED.contains(name)));
                }
                @Override public MethodVisitor visitMethod(int access, String method,
                                                  String desc, String sig, String[] exceptions) {
                    methods.add(new Decl(name,method,desc,access));
                    boolean selected=SELECT.contains(method)
                            ||method.startsWith("lambda$waitAndReset")
                            ||method.startsWith("lambda$runTask")
                            ||method.startsWith("lambda$doTask")
                            ||method.startsWith("lambda$reload")
                            ||method.startsWith("lambda$createReload")
                            ||method.startsWith("lambda$setLevel");
                    return new MethodVisitor(Opcodes.ASM9) {
                        int pos;
                        void add(int op,String kind,String operand){
                            if (selected) instructions.add(new Instruction(name,method,desc,pos++,
                                    op,kind,operand));
                        }
                        @Override public void visitMethodInsn(int op,String to,String called,
                                                               String calledDesc,boolean itf) {
                            calls.add(new Invoke(name,method,desc,to,called,calledDesc,op));
                            add(op,"CALL",to+"."+called+calledDesc);
                        }
                        @Override public void visitFieldInsn(int op,String to,String field,String d){
                            add(op,"FIELD",to+"."+field+":"+d);
                        }
                        @Override public void visitInvokeDynamicInsn(String n,String d,Handle bsm,
                                                                    Object...args) {
                            StringBuilder info=new StringBuilder(n).append(d)
                                    .append(";bootstrap=").append(bsm.getOwner())
                                    .append(".").append(bsm.getName());
                            for(Object a:args) if(a instanceof Handle h) {
                                info.append(";handle=").append(h.getOwner()).append(".")
                                    .append(h.getName()).append(h.getDesc());
                            }
                            add(Opcodes.INVOKEDYNAMIC,"INDY",info.toString());
                        }
                        @Override public void visitInsn(int op){add(op,"OP","");}
                        @Override public void visitVarInsn(int op,int slot){add(op,"VAR",Integer.toString(slot));}
                        @Override public void visitIntInsn(int op,int val){add(op,"INT",Integer.toString(val));}
                        @Override public void visitTypeInsn(int op,String type){add(op,"TYPE",type);}
                        @Override public void visitLdcInsn(Object val){add(Opcodes.LDC,"LDC",String.valueOf(val));}
                        @Override public void visitJumpInsn(int op,Label l){add(op,"BRANCH","label");}
                        @Override public void visitTableSwitchInsn(int min,int max,Label d,Label... ls){
                            add(Opcodes.TABLESWITCH,"SWITCH",min+":"+max);
                        }
                        @Override public void visitLookupSwitchInsn(Label d,int[] ks,Label[] ls){
                            add(Opcodes.LOOKUPSWITCH,"SWITCH","cases="+ks.length);
                        }
                    };
                }
            },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }
        for (String required:REQUIRED) {
            assertTrue(owners.stream().anyMatch(x->x.name.equals(required)),
                    "Required original owner not traced: "+required);
        }
        // Previously established exact target registration/reload owners.
        member(methods,GRAPH,"waitAndReset");
        member(methods,RELOADABLE,"registerReloadListener");
        member(methods,MODEL,"reload");
        member(methods,TEXTURE,"reload");
        member(methods,SECTION+"$RebuildTask","doTask");
        member(methods,S,"uploadAllPendingUploads");
        invoke(calls,LEVEL,"setLevel",GRAPH,"waitAndReset");
        invoke(calls,BER,"onResourceManagerReload",R+"blockentity/BlockEntityRenderers","createEntityRenderers");
        invoke(calls,S,"dispose",S,"uploadAllPendingUploads");
        invoke(calls,SECTION+"$RebuildTask","doTask",R+"chunk/SectionCompiler","compile");
        assertTrue(calls.stream().anyMatch(c->c.owner.equals(S)
                         && c.name.equals("runTask")
                         && c.target.equals("java/util/concurrent/CompletableFuture")
                         && c.called.equals("supplyAsync")),
                "Expected original asynchronous section compilation");
        assertTrue(calls.stream().anyMatch(c->c.owner.equals(RELOADABLE)
                         && c.name.equals("registerReloadListener")),
                "Original resource listener registration owner must have an invocation");
        // A full reload registry cannot be inferred from selected class list:
        // export exact matched CALL owners without asserting unknown mod runtime.
        List<String> registration = new ArrayList<>();
        registration.add("caller\tmethod\tcalledOwner\tcalledName\tdescriptor\tevidence");
        for (Invoke c:calls) if (c.called.contains("ReloadListener")
                ||c.called.equals("registerReloadListener")
                ||c.called.equals("registerReloadListenerIfNotPresent")
                ||(c.called.equals("reload")&&(c.owner.equals(RELOADABLE)||c.owner.equals(MINECRAFT)))
                ||(c.name.equals("waitAndReset"))
                ||(c.name.equals("runTask")&&c.target.contains("CompletableFuture"))) {
            registration.add(c.owner+"\t"+c.name+"\t"+c.target+"\t"
                    +c.called+"\t"+c.calledDesc+"\tEXACT_ORIGINAL_SELECTED_JVM_CALL");
        }
        owners.sort(Comparator.comparing(Owner::name));
        methods.sort(Comparator.comparing(Decl::owner).thenComparing(Decl::name).thenComparing(Decl::desc));
        calls.sort(Comparator.comparing(Invoke::owner).thenComparing(Invoke::name)
                .thenComparing(Invoke::desc).thenComparing(Invoke::target).thenComparing(Invoke::called)
                .thenComparing(Invoke::calledDesc));
        instructions.sort(Comparator.comparing(Instruction::owner).thenComparing(Instruction::name)
                .thenComparing(Instruction::desc).thenComparingInt(Instruction::ordinal));
        Path dir=Path.of(System.getProperty("planetary.phase2.censusReportDir","build/reports/planetary"));
        Files.createDirectories(dir);
        write(dir,"owners","owner\tsha256\tsuperclass\tinterfaces\trequired",
                owners.stream().map(Owner::row).toList());
        write(dir,"methods","owner\tmethod\tdescriptor\taccess",methods.stream().map(Decl::row).toList());
        write(dir,"calls","owner\tmethod\tdescriptor\ttarget\tcalled\tcalledDescriptor\topcode",
                calls.stream().map(Invoke::row).toList());
        write(dir,"instructions","owner\tmethod\tdescriptor\tordinal\topcode\tkind\toperand",
                instructions.stream().map(Instruction::row).toList());
        Files.write(dir.resolve("phase3-r7f4-original-reload-registration-graph-calls.tsv"),
                registration,StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("phase3-r7f4-original-reload-graph-summary.txt"),
                "stage=3R-7F-4\n"
                +"target=Minecraft1.21.1-NeoForge21.1.215\n"
                +"requiredOwners="+REQUIRED.size()+"\n"
                +"optionalOwnersFound="+(owners.size()-REQUIRED.size())+"\n"
                +"owners="+owners.size()+"\nmethods="+methods.size()
                +"\ninvokes="+calls.size()+"\nselectedInstructions="+instructions.size()
                +"\nregistrationRelatedEdges="+(registration.size()-1)+"\n"
                +"proof=exact-original-selected-bytecode-method-bodies-and-registrations\n"
                +"notProof=all_mod_listeners,actual_runtime_reload,worker_joins,GPU_upload_race,applied_mixins\n",
                StandardCharsets.UTF_8);
    }
    private static List<String> concat(List<String> a,List<String> b) {
        List<String> o=new ArrayList<>(a);o.addAll(b);return o;
    }
    private static void member(List<Decl> methods,String owner,String name) {
        assertTrue(methods.stream().anyMatch(d->d.owner.equals(owner)&&d.name.equals(name)),
                "Missing original declared owner "+owner+"."+name);
    }
    private static void invoke(List<Invoke> calls,String owner,String name,String to,String targetName) {
        assertTrue(calls.stream().anyMatch(c->c.owner.equals(owner)&&c.name.equals(name)
                &&c.target.equals(to)&&c.called.equals(targetName)),
                "Missing original edge "+owner+"."+name+" -> "+to+"."+targetName);
    }
    private static void write(Path dir,String suffix,String header,List<String> rows)throws Exception {
        List<String> out=new ArrayList<>();out.add(header);out.addAll(rows);
        Files.write(dir.resolve("phase3-r7f4-original-reload-graph-"+suffix+".tsv"),out,StandardCharsets.UTF_8);
    }
}
