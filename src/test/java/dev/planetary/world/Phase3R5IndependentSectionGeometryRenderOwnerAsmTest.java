package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Research-only: exact target Minecraft1.21.1 / NeoForge21.1.215 independently
 * authored section geometry and world/entity/BE rendering entry points.
 * Not Flywheel/Iris runtime, frame correctness or loaded mods.
 */
final class Phase3R5IndependentSectionGeometryRenderOwnerAsmTest {
    static final String MC="net/minecraft/client/renderer/";
    static final String NEO="net/neoforged/neoforge/client/";
    static final String CHUNK=MC+"chunk/";
    static final String EV=NEO+"event/AddSectionGeometryEvent";
    static final String HOOKS=NEO+"ClientHooks";
    static final String SECTION=CHUNK+"SectionCompiler";
    static final String RENDER_SECTION=CHUNK+"SectionRenderDispatcher$RenderSection";
    static final List<String> OWNERS=List.of(
        HOOKS,EV,EV+"$AdditionalSectionRenderer",EV+"$SectionRenderingContext",
        SECTION,RENDER_SECTION,MC+"LevelRenderer",
        MC+"blockentity/BlockEntityRenderDispatcher",
        MC+"block/BlockRenderDispatcher",MC+"entity/EntityRenderDispatcher");
    static final Set<String> SELECT=Set.of("gatherAdditionalRenderers","addAdditionalGeometry",
        "addRenderer","getAdditionalRenderers","getLevel","getSectionOrigin","getRegion",
        "getPoseStack","getOrCreateChunkBuffer","getQuadLighter","createCompileTask",
        "compile","render","renderItem","renderLevel","renderEntities","getRenderer",
        "renderBatched","dispatchRenderStage","getRenderBoundingBox","setupAndRender");
    record Owner(String owner,String hash,String parent,String interfaces){
        String row(){return owner+"\t"+hash+"\t"+parent+"\t"+interfaces;}}
    record Method(String owner,String method,String descriptor,int access){
        String row(){return owner+"\t"+method+"\t"+descriptor+"\t"+access;}}
    record Call(String owner,String method,String descriptor,String target,String called,String calledDesc,int opcode){
        String row(){return owner+"\t"+method+"\t"+descriptor+"\t"+target+"\t"+called+"\t"+calledDesc+"\t"+opcode;}}
    record Op(String owner,String method,String descriptor,int index,int opcode,String kind,String value){
        String row(){return owner+"\t"+method+"\t"+descriptor+"\t"+index+"\t"+opcode+"\t"+kind+"\t"+value.replace('\t',' ').replace('\n',' ');}}
    @Test void exactNeoForgeSectionGeometryAndIndependentRendererOwners()throws Exception {
        List<Owner> owners=new ArrayList<>();List<Method> methods=new ArrayList<>();
        List<Call> calls=new ArrayList<>();List<Op> instructions=new ArrayList<>();
        for(String owner:OWNERS) {
            byte[] bytes;
            try(InputStream stream=Thread.currentThread().getContextClassLoader().getResourceAsStream(owner+".class")){
                assertNotNull(stream,"Original NeoForge21.1.215 class unavailable: "+owner);
                bytes=stream.readAllBytes();
            }
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9){
                @Override public void visit(int ver,int access,String actual,String signature,String parent,String[] interfaces){
                    assertEquals(owner,actual);
                    owners.add(new Owner(owner,hash,parent==null?"":parent,String.join(",",interfaces==null?new String[0]:interfaces)));
                }
                @Override public MethodVisitor visitMethod(int access,String method,String desc,String signature,String[] exceptions){
                    methods.add(new Method(owner,method,desc,access));
                    boolean trace=SELECT.contains(method) || method.startsWith("lambda$addAdditionalGeometry")
                        || method.startsWith("lambda$compile");
                    return new MethodVisitor(Opcodes.ASM9){
                        int index;
                        void add(int opcode,String kind,String value){
                            if(trace)instructions.add(new Op(owner,method,desc,index++,opcode,kind,value));}
                        @Override public void visitMethodInsn(int opcode,String target,String name,String d,boolean itf){
                            calls.add(new Call(owner,method,desc,target,name,d,opcode));
                            add(opcode,"CALL",target+"."+name+d);}
                        @Override public void visitFieldInsn(int opcode,String target,String field,String d){
                            add(opcode,"FIELD",target+"."+field+":"+d);}
                        @Override public void visitInsn(int opcode){add(opcode,"OP","");}
                        @Override public void visitVarInsn(int opcode,int var){add(opcode,"VAR",Integer.toString(var));}
                        @Override public void visitTypeInsn(int opcode,String type){add(opcode,"TYPE",type);}
                        @Override public void visitLdcInsn(Object x){add(Opcodes.LDC,"CONST",String.valueOf(x));}
                        @Override public void visitIntInsn(int opcode,int operand){add(opcode,"INT",Integer.toString(operand));}
                        @Override public void visitJumpInsn(int opcode,Label label){add(opcode,"JUMP","label");}
                        @Override public void visitInvokeDynamicInsn(String name,String d,Handle b,Object...args){
                            add(Opcodes.INVOKEDYNAMIC,"INDY",name+d+" bootstrap="+b.getOwner()+"."+b.getName());}
                    };
                }
            },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }
        assertEquals(OWNERS.size(),owners.size());
        assertTrue(has(calls,HOOKS,"gatherAdditionalRenderers",EV,"<init>"),
            "Main-thread section event registration owner must be real");
        assertTrue(has(calls,HOOKS,"addAdditionalGeometry",EV+"$SectionRenderingContext","<init>"),
            "Worker callback must construct section region/pose context");
        assertTrue(has(calls,HOOKS,"addAdditionalGeometry",EV+"$AdditionalSectionRenderer","render"),
            "Worker must dispatch independently registered geometry callbacks");
        assertTrue(has(calls,SECTION,"compile",HOOKS,"addAdditionalGeometry"),
            "Chunk mesh compile must inject independent section geometry");
        assertTrue(has(calls,RENDER_SECTION,"createCompileTask",HOOKS,"gatherAdditionalRenderers"),
            "Scheduling stage must gather renderer callbacks for actual section level");
        assertTrue(has(calls,EV,"addRenderer","java/util/List","add"),
            "Event must own callback list");
        assertTrue(methods.stream().anyMatch(m->m.owner().equals(EV+"$SectionRenderingContext")
            && m.method().equals("getRegion")), "Section callback must expose original region");
        assertTrue(methods.stream().anyMatch(m->m.owner().equals(EV+"$SectionRenderingContext")
            && m.method().equals("getPoseStack")));
        assertTrue(methods.stream().anyMatch(m->m.owner().equals(EV)
            && m.method().equals("getLevel")));
        assertTrue(methods.stream().anyMatch(m->m.owner().equals(EV+"$SectionRenderingContext")
            && m.method().equals("getOrCreateChunkBuffer")),
            "Callback must expose a chunk RenderType buffer writer");
        owners.sort(Comparator.comparing(Owner::owner));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::method)
            .thenComparing(Method::descriptor));
        calls.sort(Comparator.comparing(Call::owner).thenComparing(Call::method)
            .thenComparing(Call::descriptor).thenComparing(Call::target)
            .thenComparing(Call::called).thenComparing(Call::calledDesc));
        instructions.sort(Comparator.comparing(Op::owner).thenComparing(Op::method)
            .thenComparing(Op::descriptor).thenComparingInt(Op::index));
        Path dir=Path.of(System.getProperty("planetary.phase2.censusReportDir", "build/reports/planetary"));Files.createDirectories(dir);
        save(dir,"owners","owner\tsha256\tsuperclass\tinterfaces",owners.stream().map(Owner::row).toList());
        save(dir,"methods","owner\tmethod\tdescriptor\taccess",methods.stream().map(Method::row).toList());
        save(dir,"calls","owner\tmethod\tdescriptor\ttarget\tcalled\tcalledDescriptor\topcode",calls.stream().map(Call::row).toList());
        save(dir,"instructions","owner\tmethod\tdescriptor\tordinal\topcode\tkind\toperand",instructions.stream().map(Op::row).toList());
        Files.writeString(dir.resolve("phase3-r5-original-independent-render-summary.txt"),
            "target=MC1.21.1-NeoForge21.1.215\nstage=3R-5\nclassfiles="+owners.size()
            +"\nmethods="+methods.size()+"\ninvokes="+calls.size()
            +"\nselectedInstructions="+instructions.size()
            +"\nmode=original_static_JVM_and_selected_section_producer_edges_not_Flywheel_Iris_client\n",
            StandardCharsets.UTF_8);
    }
    static boolean has(List<Call> calls,String owner,String method,String target,String called){
        return calls.stream().anyMatch(c->c.owner().equals(owner)&&c.method().equals(method)
            && (target==null||c.target().equals(target))&&(called==null||c.called().equals(called)));}
    static void save(Path dir,String suffix,String header,List<String> content)throws Exception{
        List<String> rows=new ArrayList<>();rows.add(header);rows.addAll(content);
        Files.write(dir.resolve("phase3-r5-original-independent-render-"+suffix+".tsv"),
            rows,StandardCharsets.UTF_8);
    }
}
