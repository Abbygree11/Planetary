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
 * 3R-4B RESEARCH ONLY: exact original MC1.21.1 / NeoForge 21.1.215
 * sign/hanging-sign/banner/skull BER model/text/pattern/skin JVM owners.
 * Static original classfiles != transformed Mixin runtime or in-game acceptance.
 */
final class Phase3R4BOriginalSignBannerSkullMethodBodyAsmTest {
    private static final String MC = "net/minecraft/";
    private static final String BER = MC + "client/renderer/blockentity/";
    private static final String BE = MC + "world/level/block/entity/";
    private static final String BLOCK = MC + "world/level/block/";

    private static final String SIGN = BER + "SignRenderer";
    private static final String HANGING = BER + "HangingSignRenderer";
    private static final String BANNER = BER + "BannerRenderer";
    private static final String SKULL = BER + "SkullBlockRenderer";
    private static final String HANGING_MODEL = HANGING + "$HangingSignModel";

    private static final List<String> CLASSES = List.of(
            SIGN, SIGN + "$SignModel",
            HANGING, HANGING_MODEL,
            BANNER, SKULL,
            BE + "SignBlockEntity", BE + "BannerBlockEntity",
            BE + "SkullBlockEntity",
            BLOCK + "StandingSignBlock", BLOCK + "WallSignBlock",
            BLOCK + "CeilingHangingSignBlock", BLOCK + "WallHangingSignBlock",
            BLOCK + "BannerBlock", BLOCK + "StandingBannerBlock",
            BLOCK + "WallBannerBlock", BLOCK + "SkullBlock",
            BLOCK + "WallSkullBlock",
            MC + "client/model/SkullModelBase",
            MC + "client/model/SkullModel",
            MC + "client/model/geom/ModelPart",
            MC + "client/renderer/MultiBufferSource",
            MC + "world/level/block/state/properties/BlockStateProperties"
    );
    private static final Set<String> METHODS = Set.of(
            "render", "renderSignWithText", "renderSign", "renderSignModel",
            "renderSignText", "translateSign", "translateSignText",
            "getSignModelRenderScale", "getSignTextRenderScale",
            "getTextOffset", "isOutlineVisible", "isRenderText",
            "renderPatterns", "renderPatternLayer", "renderSkull",
            "getRenderType", "createSkullRenderers", "createSignModel",
            "evaluateVisibleParts", "renderToBuffer",
            "getFrontText", "getBackText", "getTextLineHeight",
            "getMaxTextLineWidth", "getPatterns", "getBaseColor",
            "getOwnerProfile", "getYRotationDegrees", "getStateForPlacement",
            "getShape", "getRenderShape", "getBlockState",
            "getBlockEntity", "getValue", "getDirection",
            "getRotation", "getModel", "createBodyLayer", "createSignLayer",
            "createHangingSignLayer"
    );

    private record Owner(String name,String hash,String parent,String interfaces,
                         boolean present) {
        String tsv() { return String.join("\t",name,hash,parent,interfaces,
                Boolean.toString(present)); }
    }
    private record Member(String owner,String name,String desc,int access,
                          boolean selected) {
        String tsv() { return String.join("\t",owner,name,desc,
                Integer.toString(access),Boolean.toString(selected)); }
    }
    private record Field(String owner,String name,String desc,int access) {
        String tsv() { return String.join("\t",owner,name,desc,Integer.toString(access)); }
    }
    private record Instruction(String owner,String name,String desc,int index,
                               int opcode,String kind,String target) {
        String tsv() { return String.join("\t",owner,name,desc,
                Integer.toString(index),Integer.toString(opcode),kind,
                target.replace('\t',' ').replace('\n',' ')); }
    }

    @Test
    void exactOriginalBerNestedTextChainsPatternsSkinsHaveIndependentOwners()
            throws Exception {
        List<Owner> owners = new ArrayList<>();
        List<Member> methods = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Instruction> instructions = new ArrayList<>();
        for(String requested:CLASSES) {
            byte[] bytes;
            try(InputStream in=Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(requested+".class")) {
                if(in==null) {
                    owners.add(new Owner(requested,"","","",false));
                    continue;
                }
                bytes=in.readAllBytes();
            }
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int version,int access,String owner,
                        String signature,String parent,String[] interfaces) {
                    owners.add(new Owner(owner,hash,parent==null?"":parent,
                            String.join(",",interfaces==null?new String[0]:interfaces),
                            true));
                }
                @Override public FieldVisitor visitField(int access,String name,
                        String desc,String signature,Object value) {
                    fields.add(new Field(requested,name,desc,access));
                    return null;
                }
                @Override public MethodVisitor visitMethod(int access,String name,
                        String desc,String signature,String[] exceptions) {
                    boolean selected=METHODS.contains(name) ||
                            (name.startsWith("lambda$") &&
                                    (name.contains("render")||name.contains("create")));
                    methods.add(new Member(requested,name,desc,access,selected));
                    if(!selected) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        int index=0;
                        void add(int opcode,String kind,String target) {
                            instructions.add(new Instruction(requested,name,desc,
                                    index++,opcode,kind,target));
                        }
                        @Override public void visitMethodInsn(int opcode,String owner,
                                String method,String descriptor,boolean itf) {
                            add(opcode,"CALL",owner+"."+method+descriptor
                                    +(itf?":interface":""));
                        }
                        @Override public void visitFieldInsn(int opcode,String owner,
                                String field,String desc) {
                            add(opcode,"FIELD",owner+"."+field+":"+desc);
                        }
                        @Override public void visitVarInsn(int opcode,int index) {
                            add(opcode,"VAR",Integer.toString(index));
                        }
                        @Override public void visitInsn(int opcode) {
                            add(opcode,"OP","");
                        }
                        @Override public void visitTypeInsn(int opcode,String type) {
                            add(opcode,"TYPE",type);
                        }
                        @Override public void visitIntInsn(int opcode,int value) {
                            add(opcode,"INT",Integer.toString(value));
                        }
                        @Override public void visitLdcInsn(Object constant) {
                            add(Opcodes.LDC,"CONST",String.valueOf(constant));
                        }
                        @Override public void visitJumpInsn(int opcode,Label label) {
                            add(opcode,"BRANCH","label");
                        }
                        @Override public void visitIincInsn(int offset,int increment) {
                            add(Opcodes.IINC,"IINC",offset+":"+increment);
                        }
                        @Override public void visitInvokeDynamicInsn(String name,
                                String desc,Handle bootstrap,Object...args) {
                            StringBuilder sb=new StringBuilder(name).append(desc)
                                    .append(";bootstrap=").append(bootstrap.getOwner())
                                    .append(".").append(bootstrap.getName());
                            for(Object arg:args) if(arg instanceof Handle h)
                                sb.append(";handle=").append(h.getOwner())
                                        .append(".").append(h.getName())
                                        .append(h.getDesc());
                            add(Opcodes.INVOKEDYNAMIC,"INDY",sb.toString());
                        }
                        @Override public void visitTableSwitchInsn(int min,int max,
                                Label dflt,Label...labels) {
                            add(Opcodes.TABLESWITCH,"SWITCH",min+":"+max);
                        }
                        @Override public void visitLookupSwitchInsn(Label dflt,
                                int[] keys,Label[] labels) {
                            add(Opcodes.LOOKUPSWITCH,"SWITCH",Integer.toString(keys.length));
                        }
                    };
                }
            },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }

        for(String required:List.of(SIGN,HANGING,BANNER,SKULL,HANGING_MODEL,
                BE+"SignBlockEntity",BE+"BannerBlockEntity",BE+"SkullBlockEntity")) {
            assertTrue(owners.stream().anyMatch(x ->
                    x.name().equals(required)&&x.present()),
                    "Target original classfile missing: "+required);
        }
        assertTrue(owners.stream().anyMatch(x ->
                x.name().equals(HANGING)&&x.parent().equals(SIGN)),
                "HangingSignRenderer must inherit SignRenderer");
        for(String r:List.of(SIGN,HANGING,BANNER,SKULL)) {
            assertTrue(methods.stream().anyMatch(x ->
                            x.owner().equals(r)&&x.name().equals("render")),
                    "Concrete registered BER must render "+r);
        }
        assertTrue(called(instructions,SIGN,"renderSignWithText","getFrontText")
                        && called(instructions,SIGN,"renderSignWithText","getBackText"),
                "Shared sign BER must render both front and back text");
        assertTrue(called(instructions,SIGN,"renderSignWithText","renderSignText")
                        && called(instructions,SIGN,"renderSignWithText","renderSign"),
                "Text and wood/chain geometry have independently called owners");
        assertTrue(called(instructions,HANGING,"render","renderSignWithText"),
                "Hanging render reuses inherited sign/text path");
        assertTrue(methods.stream().anyMatch(x ->
                x.owner().equals(HANGING_MODEL)
                        && x.name().equals("evaluateVisibleParts")),
                "Hanging chain attachment variants own their model-part visibility");
        assertTrue(called(instructions,BANNER,"render","renderPatterns"),
                "Banner model owns nested pattern layer rendering");
        assertTrue(called(instructions,SKULL,"render","renderSkull"),
                "Skull has static nested renderer distinct from BE world transform");
        assertTrue(called(instructions,SKULL,"render","getOwnerProfile"),
                "Player skull skin/profile comes from SkullBlockEntity");

        owners.sort(Comparator.comparing(Owner::name));
        methods.sort(Comparator.comparing(Member::owner).thenComparing(Member::name)
                .thenComparing(Member::desc));
        fields.sort(Comparator.comparing(Field::owner).thenComparing(Field::name));
        instructions.sort(Comparator.comparing(Instruction::owner)
                .thenComparing(Instruction::name).thenComparing(Instruction::desc)
                .thenComparingInt(Instruction::index));
        Path path=Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary"));
        Files.createDirectories(path);
        List<String> a=new ArrayList<>(List.of(
                "owner\tclass_sha256\tsuperclass\tinterfaces\tpresent"));
        owners.stream().map(Owner::tsv).forEach(a::add);
        Files.write(path.resolve("phase3-r4b-original-ber-owners.tsv"),a,
                StandardCharsets.UTF_8);
        List<String> b=new ArrayList<>(List.of(
                "owner\tmethod\tdesc\taccess\tselected"));
        methods.stream().map(Member::tsv).forEach(b::add);
        Files.write(path.resolve("phase3-r4b-original-ber-methods.tsv"),b,
                StandardCharsets.UTF_8);
        List<String> c=new ArrayList<>(List.of(
                "owner\tfield\tdesc\taccess"));
        fields.stream().map(Field::tsv).forEach(c::add);
        Files.write(path.resolve("phase3-r4b-original-ber-fields.tsv"),c,
                StandardCharsets.UTF_8);
        List<String> d=new ArrayList<>(List.of(
                "owner\tmethod\tdesc\tordinal\topcode\tkind\toperand"));
        instructions.stream().map(Instruction::tsv).forEach(d::add);
        Files.write(path.resolve("phase3-r4b-original-ber-instructions.tsv"),d,
                StandardCharsets.UTF_8);
        Files.writeString(path.resolve("phase3-r4b-original-ber-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                +"stage=3R-4B\n"
                +"scope=selected_original_render_method_bodies_not_loaded_client\n"
                +"classNames="+owners.size()+"\n"
                +"presentClassfiles="+owners.stream().filter(Owner::present).count()+"\n"
                +"methodDeclarations="+methods.size()+"\n"
                +"selectedBodies="+methods.stream().filter(Member::selected).count()+"\n"
                +"fields="+fields.size()+"\n"
                +"instructions="+instructions.size()+"\n"
                +"calls="+instructions.stream().filter(x->x.kind().equals("CALL")).count()+"\n"
                +"indys="+instructions.stream().filter(x->x.kind().equals("INDY")).count()+"\n",
                StandardCharsets.UTF_8);
    }

    private static boolean called(List<Instruction> ins,String owner,String method,
                                  String callee) {
        return ins.stream().anyMatch(x ->
                x.owner().equals(owner)&&x.name().equals(method)
                        &&x.kind().equals("CALL")
                        &&x.target().contains("."+callee+"("));
    }
}
