package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Selected original NeoForge21.1.215 reload, world epoch and async render-worker
 * source owners. Static ASM only; NO actual running client, Flywheel, Iris, Mixin or mod proof.
 */
final class Phase3R6ReloadWorkerEpochOriginalAsmTest {
    static final String MC="net/minecraft/";
    static final String R="net/minecraft/client/renderer/";
    static final String N="net/neoforged/neoforge/client/";
    static final String CHUNK=R+"chunk/SectionRenderDispatcher";
    static final String BER=R+"blockentity/BlockEntityRenderDispatcher";
    static final String HOOKS=N+"ClientHooks";
    static final String EV=N+"event/AddSectionGeometryEvent";
    static final String MANAGER=N+"model/data/ModelDataManager";
    static final List<String> OWNERS=List.of(
        MC+"client/resources/model/ModelManager",
        R+"texture/TextureManager",
        MC+"server/packs/resources/ReloadableResourceManager",
        MC+"server/packs/resources/PreparableReloadListener",
        BER, R+"entity/EntityRenderDispatcher",
        R+"LevelRenderer", CHUNK, CHUNK+"$RenderSection$RebuildTask",
        MC+"client/Minecraft", HOOKS, EV, MANAGER,
        R+"chunk/RenderRegionCache",R+"chunk/SectionCompiler");
    static final Set<String> TRACE=Set.of(
        "reload","onResourceManagerReload","registerReloadListener","registerReloadListenerIfNotPresent",
        "apply","prepare","close","clear","setLevel","updateLevelInEngines","dispose",
        "clearBatchQueue","blockUntilClear","uploadAllPendingUploads","cancelTasks",
        "cancel","doTask","compile","createRegion","requestRefresh","refreshAt",
        "snapshotSectionRegion","gatherAdditionalRenderers","addAdditionalGeometry",
        "getLevel","getRegion","createCompileTask","allChanged","reset");
    record Owner(String name,String sha,String parent,String interfaces){
        String row(){return name+"\t"+sha+"\t"+parent+"\t"+interfaces;}}
    record Method(String owner,String name,String desc,int access){
        String row(){return owner+"\t"+name+"\t"+desc+"\t"+access;}}
    record Call(String owner,String method,String desc,String target,String name,String targetDesc,int opcode){
        String row(){return owner+"\t"+method+"\t"+desc+"\t"+target+"\t"+name+"\t"+targetDesc+"\t"+opcode;}}
    record Op(String owner,String method,String desc,int order,int opcode,String kind,String arg){
        String row(){return owner+"\t"+method+"\t"+desc+"\t"+order+"\t"+opcode+"\t"+kind+"\t"+arg.replace('\t',' ').replace('\n',' ');}}
    @Test void originalReloadWorldAndSectionWorkerMethodOwnership() throws Exception {
        List<Owner> owners=new ArrayList<>();List<Method> methods=new ArrayList<>();
        List<Call> calls=new ArrayList<>();List<Op> ops=new ArrayList<>();
        for(String name:OWNERS) {
            byte[] bytes;
            try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(name+".class")){
                assertNotNull(in,"Missing original 21.1.215 owner: "+name);
                bytes=in.readAllBytes();
            }
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9){
                @Override public void visit(int version,int access,String actual,String signature,String parent,String[] interfaces){
                    assertEquals(name,actual);
                    owners.add(new Owner(actual,hash,parent==null?"":parent,String.join(",",interfaces==null?new String[0]:interfaces)));
                }
                @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                    methods.add(new Method(name,method,desc,access));
                    boolean track=TRACE.contains(method)||method.startsWith("lambda$reload")||
                        method.startsWith("lambda$doTask")||method.startsWith("lambda$compile");
                    return new MethodVisitor(Opcodes.ASM9){
                        int order=0;
                        void add(int op,String kind,String arg){
                            if(track)ops.add(new Op(name,method,desc,order++,op,kind,arg));}
                        @Override public void visitMethodInsn(int op,String target,String called,String d,boolean isInterface){
                            calls.add(new Call(name,method,desc,target,called,d,op));
                            add(op,"CALL",target+"."+called+d);}
                        @Override public void visitFieldInsn(int op,String target,String field,String d){
                            add(op,"FIELD",target+"."+field+":"+d);}
                        @Override public void visitVarInsn(int op,int v){add(op,"VAR",Integer.toString(v));}
                        @Override public void visitInsn(int op){add(op,"OP","");}
                        @Override public void visitTypeInsn(int op,String type){add(op,"TYPE",type);}
                        @Override public void visitIntInsn(int op,int operand){add(op,"INT",Integer.toString(operand));}
                        @Override public void visitLdcInsn(Object value){add(Opcodes.LDC,"CONST",String.valueOf(value));}
                        @Override public void visitJumpInsn(int op,Label label){add(op,"JUMP","label");}
                        @Override public void visitInvokeDynamicInsn(String n,String d,Handle b,Object...args){
                            add(Opcodes.INVOKEDYNAMIC,"INDY",n+d+" bootstrap="+b.getOwner()+"."+b.getName());}
                    };
                }
            },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }
        assertEquals(OWNERS.size(),owners.size());
        for(String[] pair:new String[][]{
            {MC+"client/resources/model/ModelManager","reload"},
            {R+"texture/TextureManager","reload"},
            {MC+"server/packs/resources/ReloadableResourceManager","registerReloadListener"},
            {BER,"onResourceManagerReload"},
            {R+"LevelRenderer","setLevel"},
            {CHUNK,"dispose"},
            {CHUNK,"uploadAllPendingUploads"},
            {MANAGER,"snapshotSectionRegion"},
            {HOOKS,"gatherAdditionalRenderers"},
            {HOOKS,"addAdditionalGeometry"},
            {MC+"client/Minecraft","setLevel"}}) {
            assertTrue(methods.stream().anyMatch(m->m.owner().equals(pair[0])&&m.name().equals(pair[1])),
                "Missing selected original lifecycle member: "+Arrays.toString(pair));
        }
        require(calls,BER,"onResourceManagerReload",R+"blockentity/BlockEntityRenderers","createEntityRenderers");
        require(calls,CHUNK,"dispose",CHUNK,"uploadAllPendingUploads");
        require(calls,HOOKS,"addAdditionalGeometry",EV+"$AdditionalSectionRenderer","render");
        require(calls,R+"chunk/SectionCompiler","compile",HOOKS,"addAdditionalGeometry");
        owners.sort(Comparator.comparing(Owner::name));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name).thenComparing(Method::desc));
        calls.sort(Comparator.comparing(Call::owner).thenComparing(Call::method)
            .thenComparing(Call::desc).thenComparing(Call::target).thenComparing(Call::name)
            .thenComparing(Call::targetDesc));
        ops.sort(Comparator.comparing(Op::owner).thenComparing(Op::method)
            .thenComparing(Op::desc).thenComparingInt(Op::order));
        Path dir=Path.of(System.getProperty("planetary.phase2.censusReportDir","build/reports/planetary"));
        Files.createDirectories(dir);
        save(dir,"owners","owner\tsha256\tsuperclass\tinterfaces",owners.stream().map(Owner::row).toList());
        save(dir,"methods","owner\tmethod\tdescriptor\taccess",methods.stream().map(Method::row).toList());
        save(dir,"calls","owner\tmethod\tdescriptor\ttarget\tcalled\tcalledDescriptor\topcode",calls.stream().map(Call::row).toList());
        save(dir,"instructions","owner\tmethod\tdescriptor\tordinal\topcode\tkind\toperand",ops.stream().map(Op::row).toList());
        Files.writeString(dir.resolve("phase3-r6-original-reload-worker-summary.txt"),
            "target=MC1.21.1-NeoForge21.1.215\nstage=3R-6\nclassfiles="+owners.size()+
            "\nmethods="+methods.size()+"\ninvokes="+calls.size()+
            "\nselectedInstructions="+ops.size()+
            "\nmode=original_selected_static_JVM_not_resource_reload_client_mod_or_GPU_runtime\n",
            StandardCharsets.UTF_8);
    }
    static void require(List<Call> calls,String src,String method,String dst,String called){
        assertTrue(calls.stream().anyMatch(c->c.owner().equals(src)&&c.method().equals(method)
            &&c.target().equals(dst)&&c.name().equals(called)),
            "Missing original JVM direct call "+src+"."+method+" -> "+dst+"."+called);
    }
    static void save(Path dir,String suffix,String header,List<String> lines)throws Exception{
        List<String> result=new ArrayList<>();result.add(header);result.addAll(lines);
        Files.write(dir.resolve("phase3-r6-original-reload-worker-"+suffix+".tsv"),result,StandardCharsets.UTF_8);
    }
}
