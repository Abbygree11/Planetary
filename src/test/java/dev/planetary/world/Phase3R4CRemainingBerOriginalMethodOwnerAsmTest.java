package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** 3R-4C: original target JVM evidence for every registered BER not in 3R-4B.
 * Static method bodies only, not live Minecraft/loaded mod Mixin acceptance. */
final class Phase3R4CRemainingBerOriginalMethodOwnerAsmTest {
    private static final String ROOT="net/minecraft/client/renderer/blockentity/";
    private static final List<String> OWNERS=List.of(
        "SpawnerRenderer","PistonHeadRenderer","ChestRenderer","EnchantTableRenderer",
        "LecternRenderer","TheEndPortalRenderer","TheEndGatewayRenderer","BeaconRenderer",
        "StructureBlockRenderer","ShulkerBoxRenderer","BedRenderer","ConduitRenderer",
        "BellRenderer","CampfireRenderer","BrushableBlockRenderer","DecoratedPotRenderer",
        "TrialSpawnerRenderer","VaultRenderer");
    private static final Set<String> SELECT=Set.of("render","renderModel","renderBook",
        "renderBeam","renderBeaconBeam","renderPart","renderChest","renderBlock",
        "renderFlame","renderCube","renderItem","renderFace","renderSide","renderPattern",
        "renderBase","renderAll","getRenderType","getViewDistance","shouldRender",
        "renderLayers","renderInner","renderOuter","renderCubeLayer");
    record Owner(String name,String sha,String parent,String interfaces){
        String line(){return String.join("\t",name,sha,parent,interfaces);}
    }
    record Method(String owner,String name,String desc,int access){
        String line(){return String.join("\t",owner,name,desc,Integer.toString(access));}
    }
    record Call(String owner,String method,String desc,String target,String targetMethod,String targetDesc,int opcode){
        String line(){return String.join("\t",owner,method,desc,target,targetMethod,targetDesc,Integer.toString(opcode));}
    }
    record Op(String owner,String method,String desc,int index,int opcode,String kind,String value){
        String line(){return String.join("\t",owner,method,desc,Integer.toString(index),
            Integer.toString(opcode),kind,value.replace('\t',' ').replace('\n',' '));}
    }
    @Test void originalRemainingRegisteredRendererMethodsAndCoordinatePaths() throws Exception {
        List<Owner> owners=new ArrayList<>();List<Method> methods=new ArrayList<>();
        List<Call> calls=new ArrayList<>();List<Op> ops=new ArrayList<>();
        for(String simple:OWNERS){
            String owner=ROOT+simple;
            byte[] bytes;
            try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(owner+".class")){
                assertNotNull(in,"Original target classfile missing: "+owner);
                bytes=in.readAllBytes();
            }
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9){
                @Override public void visit(int v,int a,String name,String sig,String parent,String[] interfaces){
                    owners.add(new Owner(name,hash,parent==null?"":parent,
                        String.join(",",interfaces==null?new String[0]:interfaces)));
                }
                @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] ex){
                    methods.add(new Method(owner,method,desc,access));
                    boolean trace=SELECT.contains(method) || method.startsWith("lambda$render");
                    return new MethodVisitor(Opcodes.ASM9){
                        int n=0;
                        void add(int opcode,String kind,String value){
                            if(trace)ops.add(new Op(owner,method,desc,n++,opcode,kind,value));
                        }
                        @Override public void visitMethodInsn(int opcode,String target,String called,String cd,boolean itf){
                            calls.add(new Call(owner,method,desc,target,called,cd,opcode));
                            add(opcode,"CALL",target+"."+called+cd);
                        }
                        @Override public void visitFieldInsn(int opcode,String target,String field,String fd){
                            add(opcode,"FIELD",target+"."+field+":"+fd);
                        }
                        @Override public void visitVarInsn(int opcode,int index){add(opcode,"VAR",Integer.toString(index));}
                        @Override public void visitInsn(int opcode){add(opcode,"OP","");}
                        @Override public void visitIntInsn(int opcode,int val){add(opcode,"INT",Integer.toString(val));}
                        @Override public void visitTypeInsn(int opcode,String type){add(opcode,"TYPE",type);}
                        @Override public void visitLdcInsn(Object value){add(Opcodes.LDC,"CONST",String.valueOf(value));}
                        @Override public void visitJumpInsn(int opcode,Label label){add(opcode,"JUMP","label");}
                        @Override public void visitIincInsn(int var,int step){add(Opcodes.IINC,"IINC",var+":"+step);}
                        @Override public void visitInvokeDynamicInsn(String name,String desc,Handle bsm,Object... args){
                            add(Opcodes.INVOKEDYNAMIC,"INDY",name+desc+" bootstrap="+bsm.getOwner()+"."+bsm.getName());
                        }
                    };
                }
            },ClassReader.SKIP_FRAMES|ClassReader.SKIP_DEBUG);
        }
        assertEquals(18,owners.size());
        for(String name:OWNERS) assertTrue(methods.stream().anyMatch(x->x.owner().equals(ROOT+name)
            && x.name().equals("render")),name+" must declare a renderer method");
        // The bed's entry render delegates to renderPiece; only renderPiece mutates PoseStack.
        // Enforce the actual original nested method owner instead of a false direct render->PoseStack assumption.
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(ROOT+"BedRenderer")
            && x.method().equals("render") && x.targetMethod().equals("renderPiece")),
            "BedRenderer.render must delegate to renderPiece");
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(ROOT+"BedRenderer")
            && x.method().equals("renderPiece")
            && x.target().equals("com/mojang/blaze3d/vertex/PoseStack")),
            "BedRenderer.renderPiece must own the nested pose operations");
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(ROOT+"PistonHeadRenderer")
            && x.method().equals("render") && x.targetMethod().equals("getMovedState")));
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(ROOT+"TheEndGatewayRenderer")
            && x.target().equals(ROOT+"BeaconRenderer")));
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(ROOT+"CampfireRenderer")
            && x.method().equals("render") && x.targetMethod().equals("getItems")));
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(ROOT+"ChestRenderer")
            && x.method().equals("render") && x.target().contains("PoseStack")));
        owners.sort(Comparator.comparing(Owner::name));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name).thenComparing(Method::desc));
        calls.sort(Comparator.comparing(Call::owner).thenComparing(Call::method).thenComparing(Call::desc)
            .thenComparing(Call::target).thenComparing(Call::targetMethod).thenComparing(Call::targetDesc));
        ops.sort(Comparator.comparing(Op::owner).thenComparing(Op::method).thenComparing(Op::desc).thenComparingInt(Op::index));
        Path out=Path.of(System.getProperty("planetary.phase2.censusReportDir","build/reports/planetary"));
        Files.createDirectories(out);
        write(out,"owners","owner\tclass_sha256\tsuperclass\tinterfaces",owners.stream().map(Owner::line).toList());
        write(out,"methods","owner\tmethod\tdescriptor\taccess",methods.stream().map(Method::line).toList());
        write(out,"invokes","owner\tmethod\tdescriptor\ttarget_owner\ttarget_method\ttarget_descriptor\topcode",calls.stream().map(Call::line).toList());
        write(out,"instructions","owner\tmethod\tdescriptor\tordinal\topcode\tkind\toperand",ops.stream().map(Op::line).toList());
        Files.writeString(out.resolve("phase3-r4c-remaining-ber-summary.txt"),
            "target=Minecraft1.21.1-NeoForge21.1.215\nstage=3R-4C\n"
            +"owners="+owners.size()+"\nmethods="+methods.size()+"\ninvokes="+calls.size()
            +"\ntracedInstructions="+ops.size()+"\n"
            +"scope=original_classfile_static_JVM_not_modded_client\n",StandardCharsets.UTF_8);
    }
    private static void write(Path dir,String name,String header,List<String> rows)throws Exception{
        List<String> lines=new ArrayList<>();lines.add(header);lines.addAll(rows);
        Files.write(dir.resolve("phase3-r4c-remaining-ber-"+name+".tsv"),lines,StandardCharsets.UTF_8);
    }
}
