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
 * Research-only: actual original NeoForge21.1.215 / MC1.21.1 JUnit classfiles.
 * Fluid geometry and Item GUI/BEWLR are separate render entry points.
 * Does not execute a Minecraft client, foreign mod, or assert shader/frame correctness.
 */
final class Phase3R7F1FluidItemGuiOwnerAsmTest {
    private static final String MC="net/minecraft/";
    private static final String RC=MC+"client/renderer/";
    private static final String NF="net/neoforged/neoforge/client/extensions/common/";
    private static final String BLOCK=RC+"block/BlockRenderDispatcher";
    private static final String LIQUID=RC+"block/LiquidBlockRenderer";
    private static final String ITEM=RC+"entity/ItemRenderer";
    private static final String BEWLR=RC+"BlockEntityWithoutLevelRenderer";
    private static final String FLUID=NF+"IClientFluidTypeExtensions";
    private static final String ITEM_EXT=NF+"IClientItemExtensions";
    private static final List<String> OWNERS=List.of(
        BLOCK,LIQUID,FLUID,
        MC+"world/level/material/FluidState",
        MC+"world/level/material/Fluid",
        ITEM,BEWLR,ITEM_EXT,
        RC+"ItemInHandRenderer",
        RC+"entity/ItemEntityRenderer",
        RC+"blockentity/BlockEntityRenderDispatcher",
        RC+"chunk/SectionCompiler");
    private static final Set<String> TRACE=Set.of(
        "renderLiquid","tesselate","shouldRenderFace","getFluidSprites","getTintColor",
        "renderSingleBlock","getCustomRenderer","renderByItem","renderStatic","render",
        "renderItem","renderBatched","getRenderType","getRenderTypes","getFluidType",
        "getFluidState","getBlockState","getShade","getBrightness","getLightColor",
        "getRenderer","getSprite","getTexture","getOverlayTexture","getModel");
    record Owner(String owner,String hash,String parent,String interfaces) {
        String out(){return owner+"\t"+hash+"\t"+parent+"\t"+interfaces;}}
    record Decl(String owner,String method,String desc,int access) {
        String out(){return owner+"\t"+method+"\t"+desc+"\t"+access;}}
    record Call(String owner,String method,String desc,String target,String called,String calledDesc,int opcode){
        String out(){return owner+"\t"+method+"\t"+desc+"\t"+target+"\t"+called+"\t"+calledDesc+"\t"+opcode;}}
    record Ins(String owner,String method,String desc,int order,int opcode,String kind,String arg) {
        String out(){return owner+"\t"+method+"\t"+desc+"\t"+order+"\t"+opcode+"\t"+kind+"\t"+arg.replace('\t',' ').replace('\n',' ');}}
    @Test void originalFluidRenderingAndBEWLRItemGuiOwnerDispatch()throws Exception {
        List<Owner> owners=new ArrayList<>();List<Decl> decls=new ArrayList<>();
        List<Call> calls=new ArrayList<>();List<Ins> ops=new ArrayList<>();
        for(String owner:OWNERS){
            byte[] bytes;
            try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(owner+".class")){
                assertNotNull(in,"Missing original class: "+owner);
                bytes=in.readAllBytes();
            }
            String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9){
                @Override public void visit(int v,int access,String n,String sig,String parent,String[] interfaces){
                    assertEquals(owner,n);
                    owners.add(new Owner(n,sha,parent==null?"":parent,String.join(",",interfaces==null?new String[0]:interfaces)));
                }
                @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                    decls.add(new Decl(owner,method,desc,access));
                    boolean selected=TRACE.contains(method)||method.startsWith("lambda$render");
                    return new MethodVisitor(Opcodes.ASM9) {
                        int i;
                        void add(int code,String kind,String val){
                            if(selected)ops.add(new Ins(owner,method,desc,i++,code,kind,val));}
                        @Override public void visitMethodInsn(int op,String to,String name,String desc2,boolean itf){
                            calls.add(new Call(owner,method,desc,to,name,desc2,op));
                            add(op,"CALL",to+"."+name+desc2);}
                        @Override public void visitFieldInsn(int op,String to,String name,String desc2){
                            add(op,"FIELD",to+"."+name+":"+desc2);}
                        @Override public void visitInsn(int op){add(op,"OP","");}
                        @Override public void visitVarInsn(int op,int n){add(op,"VAR",Integer.toString(n));}
                        @Override public void visitJumpInsn(int op,Label l){add(op,"JUMP","label");}
                        @Override public void visitTypeInsn(int op,String n){add(op,"TYPE",n);}
                        @Override public void visitLdcInsn(Object val){add(Opcodes.LDC,"CONST",String.valueOf(val));}
                        @Override public void visitInvokeDynamicInsn(String n,String d,Handle b,Object...args){
                            add(Opcodes.INVOKEDYNAMIC,"INDY",n+d+" bootstrap="+b.getOwner()+"."+b.getName());}
                    };
                }
            },ClassReader.SKIP_FRAMES|ClassReader.SKIP_DEBUG);
        }
        assertEquals(OWNERS.size(),owners.size());
        requireMember(decls,BLOCK,"renderLiquid");
        requireMember(decls,LIQUID,"tesselate");
        requireMember(decls,LIQUID,"shouldRenderFace");
        requireMember(decls,FLUID,"renderFluid");
        requireMember(decls,BEWLR,"renderByItem");
        requireMember(decls,ITEM,"render");
        // Actual original 2.3C-13 asserts these method-name edges; assert exact
        // declaring source and destination to prevent accidental unrelated matches.
        requireCall(calls,BLOCK,"renderLiquid",FLUID,"renderFluid");
        requireCall(calls,BLOCK,"renderLiquid",LIQUID,"tesselate");
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(LIQUID)&&x.called().equals("getFluidSprites")),
            "Fluid source sprites are looked up by the original liquid tessellator");
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(LIQUID)&&x.called().equals("getTintColor")),
            "Fluid source tint is looked up by the original liquid tessellator");
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(BLOCK)&&x.called().equals("getCustomRenderer")));
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(BLOCK)&&x.target().equals(BEWLR)
            &&x.called().equals("renderByItem")));
        owners.sort(Comparator.comparing(Owner::owner));
        decls.sort(Comparator.comparing(Decl::owner).thenComparing(Decl::method).thenComparing(Decl::desc));
        calls.sort(Comparator.comparing(Call::owner).thenComparing(Call::method).thenComparing(Call::desc)
            .thenComparing(Call::target).thenComparing(Call::called).thenComparing(Call::calledDesc));
        ops.sort(Comparator.comparing(Ins::owner).thenComparing(Ins::method).thenComparing(Ins::desc)
            .thenComparingInt(Ins::order));
        Path dir=Path.of(System.getProperty("planetary.phase2.censusReportDir","build/reports/planetary"));
        Files.createDirectories(dir);
        write(dir,"owners","owner\tsha256\tsuperclass\tinterfaces",owners.stream().map(Owner::out).toList());
        write(dir,"methods","owner\tmethod\tdescriptor\taccess",decls.stream().map(Decl::out).toList());
        write(dir,"calls","owner\tmethod\tdescriptor\ttarget\tcalled\tcalledDescriptor\topcode",calls.stream().map(Call::out).toList());
        write(dir,"instructions","owner\tmethod\tdescriptor\tordinal\topcode\tkind\toperand",ops.stream().map(Ins::out).toList());
        Files.writeString(dir.resolve("phase3-r7f1-original-fluid-item-gui-summary.txt"),
            "target=MC1.21.1-NeoForge21.1.215\nstage=3R-7F-1\nclassfiles="+owners.size()+
            "\nmethods="+decls.size()+"\ninvokes="+calls.size()+
            "\nselectedInstructions="+ops.size()+
            "\nmode=selected_original_static_JVM_not_game_mod_or_GUI_runtime\n",StandardCharsets.UTF_8);
    }
    static void requireMember(List<Decl> m,String o,String n){
        assertTrue(m.stream().anyMatch(x->x.owner().equals(o)&&x.method().equals(n)),
            "Original declared method not found "+o+"."+n);
    }
    static void requireCall(List<Call> calls,String owner,String method,String target,String called){
        assertTrue(calls.stream().anyMatch(x->x.owner().equals(owner)&&x.method().equals(method)
            &&x.target().equals(target)&&x.called().equals(called)),
            "Original exact JVM call missing "+owner+"."+method+" -> "+target+"."+called);
    }
    static void write(Path dir,String suffix,String header,List<String> rows)throws Exception {
        List<String> out=new ArrayList<>();out.add(header);out.addAll(rows);
        Files.write(dir.resolve("phase3-r7f1-original-fluid-item-gui-"+suffix+".tsv"),
            out,StandardCharsets.UTF_8);
    }
}
