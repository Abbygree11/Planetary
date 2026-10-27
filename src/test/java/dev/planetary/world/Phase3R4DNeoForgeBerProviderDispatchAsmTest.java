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
 * 3R-4D exact original NeoForge 21.1.215 method-owner/dispatch research.
 * Does not claim an installed mod's renderer, Mixin weaving, or live game acceptance.
 */
final class Phase3R4DNeoForgeBerProviderDispatchAsmTest {
    private static final String BER="net/minecraft/client/renderer/blockentity/";
    private static final String EVENT="net/neoforged/neoforge/client/event/EntityRenderersEvent$RegisterRenderers";
    private static final List<String> OWNERS=List.of(
        EVENT, BER+"BlockEntityRenderers", BER+"BlockEntityRenderDispatcher",
        BER+"BlockEntityRenderer", BER+"BlockEntityRendererProvider",
        BER+"BlockEntityRendererProvider$Context",
        "net/neoforged/neoforge/client/extensions/IBlockEntityRendererExtension",
        "net/neoforged/neoforge/client/ClientHooks");
    private static final Set<String> SELECT=Set.of("registerBlockEntityRenderer",
        "register","createEntityRenderers","getRenderer","render","renderItem",
        "setupAndRender","tryRender","onResourceManagerReload","prepare",
        "getRenderBoundingBox","shouldRender","shouldRenderOffScreen");
    record Owner(String owner,String sha,String parent,String interfaces,int access) {
        String line(){return owner+"\t"+sha+"\t"+parent+"\t"+interfaces+"\t"+access;}
    }
    record Method(String owner,String name,String descriptor,int access){
        String line(){return owner+"\t"+name+"\t"+descriptor+"\t"+access;}
    }
    record Invoke(String owner,String method,String descriptor,String target,String called,String calledDesc,int opcode){
        String line(){return owner+"\t"+method+"\t"+descriptor+"\t"+target+"\t"+called+"\t"+calledDesc+"\t"+opcode;}
    }
    record Instruction(String owner,String method,String descriptor,int ordinal,int opcode,String kind,String operand){
        String line(){return owner+"\t"+method+"\t"+descriptor+"\t"+ordinal+"\t"+opcode+"\t"+kind+"\t"+operand.replace('\t',' ').replace('\n',' ');}
    }
    @Test void originalNeoForgeProviderRegistrationInstantiationAndDispatcherSelection() throws Exception {
        List<Owner> owners=new ArrayList<>();
        List<Method> methods=new ArrayList<>();
        List<Invoke> invokes=new ArrayList<>();
        List<Instruction> instructions=new ArrayList<>();
        for (String name:OWNERS) {
            byte[] data;
            try(InputStream stream=Thread.currentThread().getContextClassLoader().getResourceAsStream(name+".class")){
                assertNotNull(stream,"Missing exact original 21.1.215 class: "+name);
                data=stream.readAllBytes();
            }
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
            new ClassReader(data).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int ver,int acc,String actual,String sig,String parent,String[] interfaces){
                    assertEquals(name,actual);
                    owners.add(new Owner(actual,hash,parent==null?"":parent,
                        String.join(",",interfaces==null?new String[0]:interfaces),acc));
                }
                @Override public MethodVisitor visitMethod(int access,String method,String descriptor,String sig,String[] exceptions){
                    methods.add(new Method(name,method,descriptor,access));
                    boolean trace=SELECT.contains(method) || method.startsWith("lambda$createEntityRenderers")
                        || method.startsWith("lambda$render");
                    return new MethodVisitor(Opcodes.ASM9){
                        int n=0;
                        void add(int op,String type,String value){
                            if(trace)instructions.add(new Instruction(name,method,descriptor,n++,op,type,value));
                        }
                        @Override public void visitMethodInsn(int op,String target,String called,String desc,boolean itf){
                            invokes.add(new Invoke(name,method,descriptor,target,called,desc,op));
                            add(op,"CALL",target+"."+called+desc);
                        }
                        @Override public void visitFieldInsn(int op,String target,String field,String desc){
                            add(op,"FIELD",target+"."+field+":"+desc);
                        }
                        @Override public void visitVarInsn(int op,int var){add(op,"VAR",Integer.toString(var));}
                        @Override public void visitInsn(int op){add(op,"OP","");}
                        @Override public void visitIntInsn(int op,int value){add(op,"INT",Integer.toString(value));}
                        @Override public void visitTypeInsn(int op,String type){add(op,"TYPE",type);}
                        @Override public void visitLdcInsn(Object value){add(Opcodes.LDC,"CONST",String.valueOf(value));}
                        @Override public void visitJumpInsn(int op,Label l){add(op,"JUMP","label");}
                        @Override public void visitInvokeDynamicInsn(String n,String d,Handle bsm,Object... args){
                            add(Opcodes.INVOKEDYNAMIC,"INDY",n+d+" bootstrap="+bsm.getOwner()+"."+bsm.getName());
                        }
                    };
                }
            },ClassReader.SKIP_FRAMES|ClassReader.SKIP_DEBUG);
        }
        assertEquals(OWNERS.size(),owners.size());
        for (String name:OWNERS) assertTrue(methods.stream().anyMatch(m->m.owner().equals(name)),
            "Missing original method inventory: "+name);
        assertTrue(invokes.stream().anyMatch(i->i.owner().equals(EVENT)
            && i.method().equals("registerBlockEntityRenderer")
            && i.target().equals(BER+"BlockEntityRenderers")
            && i.called().equals("register")),"NeoForge event must register via original BER factory map");
        assertTrue(invokes.stream().anyMatch(i->i.owner().equals(BER+"BlockEntityRenderers")
            && i.method().equals("register")
            && i.called().equals("put")),"Original BER register must write provider map");
        assertTrue(invokes.stream().anyMatch(i->i.owner().equals(BER+"BlockEntityRenderDispatcher")
            && i.method().equals("getRenderer") && i.called().equals("get")),
            "Renderer dispatcher must select from instantiated type-to-renderer map");
        assertTrue(invokes.stream().anyMatch(i->i.owner().equals(BER+"BlockEntityRenderDispatcher")
            && i.method().equals("onResourceManagerReload")
            && i.target().equals(BER+"BlockEntityRenderers")
            && i.called().equals("createEntityRenderers")),
            "Reload must reinstantiate registered BER providers");
        // Do not assume class weaving into vanilla SignRenderer covers all providers.
        owners.sort(Comparator.comparing(Owner::owner));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name)
            .thenComparing(Method::descriptor));
        invokes.sort(Comparator.comparing(Invoke::owner).thenComparing(Invoke::method)
            .thenComparing(Invoke::descriptor).thenComparing(Invoke::target)
            .thenComparing(Invoke::called).thenComparing(Invoke::calledDesc));
        instructions.sort(Comparator.comparing(Instruction::owner).thenComparing(Instruction::method)
            .thenComparing(Instruction::descriptor).thenComparingInt(Instruction::ordinal));
        Path dir=Path.of(System.getProperty("planetary.phase2.censusReportDir","build/reports/planetary"));
        Files.createDirectories(dir);
        write(dir,"owners","owner\tsha256\tsuperclass\tinterfaces\taccess",owners.stream().map(Owner::line).toList());
        write(dir,"methods","owner\tmethod\tdescriptor\taccess",methods.stream().map(Method::line).toList());
        write(dir,"invokes","owner\tmethod\tdescriptor\ttarget_owner\ttarget_method\ttarget_descriptor\topcode",
            invokes.stream().map(Invoke::line).toList());
        write(dir,"instructions","owner\tmethod\tdescriptor\tordinal\topcode\tkind\toperand",
            instructions.stream().map(Instruction::line).toList());
        Files.writeString(dir.resolve("phase3-r4d-original-ber-provider-summary.txt"),
            "target=Minecraft1.21.1-NeoForge21.1.215\n"
            +"stage=3R-4D\nclassfiles="+owners.size()+"\nmethods="+methods.size()
            +"\ninvokes="+invokes.size()+"\nselectedInstructions="+instructions.size()
            +"\nscope=original_factory_and_dispatcher_static_JVM_not_loaded_mod\n", StandardCharsets.UTF_8);
    }
    private static void write(Path dir,String suffix,String header,List<String> rows)throws Exception{
        List<String> lines=new ArrayList<>();lines.add(header);lines.addAll(rows);
        Files.write(dir.resolve("phase3-r4d-original-ber-provider-"+suffix+".tsv"),
            lines,StandardCharsets.UTF_8);
    }
}
