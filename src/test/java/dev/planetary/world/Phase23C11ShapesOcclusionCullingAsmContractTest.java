package dev.planetary.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 2.3C-11, exact Gradle-resolved NeoForge 21.1.215 shapes/support/
 * occlusion/render contracts and compiled Planetary Mixin selectors.
 * NOT a runtime transformed Mixin or mod/gameplay test.
 */
final class Phase23C11ShapesOcclusionCullingAsmContractTest {
    private static final String R="net/minecraft/";
    private static final String W=R+"world/";
    private static final String B=W+"level/block/";
    private static final String BASE=B+"state/BlockBehaviour$BlockStateBase";
    private static final String BLOCK=B+"Block";
    private static final String SUPPORT=B+"SupportType";
    private static final String COLLISIONS=W+"level/BlockCollisions";
    private static final String SHAPES=W+"phys/shapes/Shapes";
    private static final String MODEL=R+"client/renderer/block/ModelBlockRenderer";
    private static final String WRAPPER="net/neoforged/neoforge/client/model/BakedModelWrapper";
    private static final String SHAPE_MIXIN="dev/planetary/mixin/BlockStateShapeMixin";
    private static final String CULL_MIXIN="dev/planetary/mixin/BlockRenderCullingMixin";
    private static final String PLANET_CULL="dev/planetary/client/render/PlanetBlockRenderCulling";
    private static final String GETTER="Lnet/minecraft/world/level/BlockGetter;";
    private static final String POS="Lnet/minecraft/core/BlockPos;";
    private static final String STATE="Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String DIR="Lnet/minecraft/core/Direction;";
    private static final String CTX="Lnet/minecraft/world/phys/shapes/CollisionContext;";
    private static final String SHAPE="Lnet/minecraft/world/phys/shapes/VoxelShape;";
    private static final String SUPPORT_TYPE="Lnet/minecraft/world/level/block/SupportType;";
    private static final String CIR="Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String INJECT="Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT="Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String SIMPLE="("+GETTER+POS+")"+SHAPE;
    private static final String CONTEXT="("+GETTER+POS+CTX+")"+SHAPE;
    private static final String CULL="("+STATE+GETTER+POS+DIR+POS+")Z";
    private static final String PCULL="("+STATE+GETTER+POS+DIR+POS+")Ljava/util/Optional;";
    private static final String MODEL_TESSELLATE=
            "(Lnet/minecraft/world/level/BlockAndTintGetter;"+
            "Lnet/minecraft/client/resources/model/BakedModel;"+STATE+POS+
            "Lcom/mojang/blaze3d/vertex/PoseStack;"+
            "Lcom/mojang/blaze3d/vertex/VertexConsumer;"+
            "ZLnet/minecraft/util/RandomSource;JI"+
            "Lnet/neoforged/neoforge/client/model/data/ModelData;"+
            "Lnet/minecraft/client/renderer/RenderType;)V";

    private record Call(int opcode,String owner,String name,String desc){}
    private record Member(String name,String desc,int flags){}
    private record InjectSpec(List<String> selectors,List<String> at,List<Boolean> cancel){}

    @Test void blockStateShapeEntryPointsAndCanonicalSupportCachesAreSeparate()
            throws IOException {
        for(String type:List.of("getShape","getCollisionShape")){
            assertTrue(declares(BASE,type,SIMPLE),type+" simple overload");
            assertTrue(declares(BASE,type,CONTEXT),type+" contextual overload");
        }
        for(String type:List.of("getVisualShape")){
            assertTrue(declares(BASE,type,CONTEXT),type);
        }
        for(String type:List.of("getInteractionShape","getBlockSupportShape","getOcclusionShape")){
            assertTrue(declares(BASE,type,SIMPLE),type);
        }
        String face="("+GETTER+POS+DIR+")"+SHAPE;
        assertTrue(declares(BASE,"getFaceOcclusionShape",face));
        List<Call> faceMethod=calls(BASE,"getFaceOcclusionShape",face);
        assertTrue(has(faceMethod,"getOcclusionShape"));
        assertTrue(has(faceMethod,"getFaceShape"));
        assertTrue(declares(BASE,"isFaceSturdy",
                "("+GETTER+POS+DIR+SUPPORT_TYPE+")Z"));
        List<Call> support=calls(BASE,"isFaceSturdy",
                "("+GETTER+POS+DIR+SUPPORT_TYPE+")Z");
        assertTrue(has(support,"isSupporting"),
                "Noncached dynamic mod BlockState should defer to SupportType");
        assertTrue(has(support,"isFaceSturdy"),
                "State cache path must remain independent of dynamic SupportType");
        assertTrue(declares(BASE,"getLightBlock","("+GETTER+POS+")I"));
        assertTrue(declares(BASE,"getShadeBrightness","("+GETTER+POS+")F"));
    }

    @Test void vanillaCullingUsesNeoForgeModFaceHidingAndOcclusion()
            throws IOException {
        assertTrue(declares(BLOCK,"shouldRenderFace",CULL));
        List<Call> body=calls(BLOCK,"shouldRenderFace",CULL);
        for(String call:List.of("skipRendering","hidesNeighborFace",
                "supportsExternalFaceHiding","getFaceOcclusionShape",
                "joinIsNotEmpty","canOcclude")){
            assertTrue(has(body,call),"Missing culling lifecycle or mod extension: "+call);
        }
        assertTrue(declares(SHAPES,"mergedFaceOccludes",
                "("+SHAPE+SHAPE+DIR+")Z"));
        assertTrue(has(calls(SHAPES,"mergedFaceOccludes",
                "("+SHAPE+SHAPE+DIR+")Z"),"joinIsNotEmpty"));
    }

    @Test void physicalCollisionAndFullCubeIdentityPreserved() throws IOException {
        assertTrue(has(allCalls(COLLISIONS),"getCollisionShape"));
        assertTrue(has(allCalls(COLLISIONS),"joinIsNotEmpty"));
        assertTrue(has(allCalls(COLLISIONS),"block"),
                "Full-cube collision fast-path must stay available");
        assertTrue(declares(BLOCK,"isShapeFullBlock","("+SHAPE+")Z"));
        assertTrue(declares(BLOCK,"isFaceFull","("+SHAPE+DIR+")Z"));
        assertTrue(has(calls(BLOCK,"isFaceFull","("+SHAPE+DIR+")Z"),"isShapeFullBlock"));
    }

    @Test void exactNeoForgeRendererConsumesBakedModelsAndSeparateAO()
            throws IOException {
        assertTrue(declares(MODEL,"tesselateBlock",MODEL_TESSELLATE));
        List<Call> render=calls(MODEL,"tesselateBlock",MODEL_TESSELLATE);
        assertTrue(has(render,"tesselateWithAO"));
        assertTrue(has(render,"tesselateWithoutAO"));
        assertTrue(has(allCalls(MODEL),"shouldRenderFace"));
        assertTrue(has(allCalls(MODEL),"getQuads"));
        // NeoForge extends the BakedModel output API rather than replacing the
        // vanilla physical BlockPos or Direction.
        assertTrue(members(WRAPPER).stream().anyMatch(m->m.name.equals("getQuads")));
    }

    @Test void allEightExistingPlanetShapePairsPinActualOwnerAndScope()
            throws IOException {
        String[] names={
                "planetary$enterShapeSimple","planetary$exitShapeSimple",
                "planetary$enterShapeContext","planetary$exitShapeContext",
                "planetary$enterCollisionSimple","planetary$exitCollisionSimple",
                "planetary$enterCollisionContext","planetary$exitCollisionContext",
                "planetary$enterVisual","planetary$exitVisual",
                "planetary$enterInteraction","planetary$exitInteraction",
                "planetary$enterSupportCanonical","planetary$exitSupportCanonical",
                "planetary$enterOcclusionCanonical","planetary$exitOcclusionCanonical"
        };
        String[] owners={
                "getShape","getShape","getShape","getShape",
                "getCollisionShape","getCollisionShape",
                "getCollisionShape","getCollisionShape",
                "getVisualShape","getVisualShape",
                "getInteractionShape","getInteractionShape",
                "getBlockSupportShape","getBlockSupportShape",
                "getOcclusionShape","getOcclusionShape"
        };
        String[] targets={
                SIMPLE,SIMPLE,CONTEXT,CONTEXT,SIMPLE,SIMPLE,CONTEXT,CONTEXT,
                CONTEXT,CONTEXT,SIMPLE,SIMPLE,SIMPLE,SIMPLE,SIMPLE,SIMPLE
        };
        for(int i=0;i<names.length;i++){
            boolean exit=(i&1)!=0;
            boolean withContext=targets[i].equals(CONTEXT);
            String handler="("+GETTER+POS+(withContext?CTX:"")+CIR+")V";
            String target=owners[i]+targets[i];
            assertTrue(declares(BASE,owners[i],targets[i]),"Target method "+target);
            assertTrue(declares(SHAPE_MIXIN,names[i],handler),"Mixin handler "+names[i]);
            InjectSpec annotation=inject(SHAPE_MIXIN,names[i],handler);
            assertEquals(List.of(target),annotation.selectors());
            assertEquals(List.of(exit?"RETURN":"HEAD"),annotation.at());
            assertEquals(exit?List.of(true):List.of(),annotation.cancel(),
                    "HEAD should not be implicitly treated as cancellable");
            assertTrue(has(calls(SHAPE_MIXIN,names[i],handler),
                    exit?(i>=12?"finishCanonical":"finishPhysical"):"enterQuery"));
        }
    }

    @Test void frameAwareCullingMixinAndCachePreserveBothSides()
            throws IOException {
        String handler="planetary$frameAwareCulling";
        String desc="("+STATE+GETTER+POS+DIR+POS+CIR+")V";
        assertTrue(declares(CULL_MIXIN,handler,desc));
        int flags=members(CULL_MIXIN).stream().filter(m->m.name.equals(handler)
                &&m.desc.equals(desc)).findFirst().orElseThrow().flags;
        assertTrue((flags&Opcodes.ACC_STATIC)!=0);
        InjectSpec spec=inject(CULL_MIXIN,handler,desc);
        assertEquals(List.of("shouldRenderFace"+CULL),spec.selectors());
        assertEquals(List.of("HEAD"),spec.at());
        assertEquals(List.of(true),spec.cancel());
        assertTrue(has(calls(CULL_MIXIN,handler,desc),"shouldRenderFace"));
        assertTrue(declares(PLANET_CULL,"shouldRenderFace",PCULL));
        List<Call> body=calls(PLANET_CULL,"shouldRenderFace",PCULL);
        assertTrue(has(body,"stateFrameAt"));
        assertTrue(has(body,"worldToLocal"));
        assertTrue(has(body,"hidesNeighborFace"));
        assertTrue(has(body,"supportsExternalFaceHiding"));
        assertTrue(has(body,"getFaceOcclusionShape"));
        assertTrue(has(body,"joinIsNotEmpty"));
        // This does NOT certify frame-specific cache correctness for
        // dynamically position-dependent modded VoxelShapes.
    }

    private static boolean has(List<Call> calls,String name){
        return calls.stream().anyMatch(c->c.name.equals(name));
    }
    private static boolean declares(String cls,String name,String desc)throws IOException{
        return members(cls).stream().anyMatch(m->m.name.equals(name)&&m.desc.equals(desc));
    }
    private static List<Member> members(String cls)throws IOException{
        List<Member> out=new ArrayList<>();
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String n,String d,
                    String sig,String[] exceptions){
                out.add(new Member(n,d,flags));return null;
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return out;
    }
    private static List<Call> allCalls(String cls)throws IOException{
        List<Call> calls=new ArrayList<>();
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String name,String desc,
                    String signature,String[] exceptions){return sink(calls);}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return calls;
    }
    private static List<Call> calls(String cls,String method,String desc)throws IOException{
        List<Call> out=new ArrayList<>();boolean[] found={false};
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String name,String d,
                    String sig,String[] exceptions){
                if(!name.equals(method)||!d.equals(desc))return null;
                found[0]=true;return sink(out);
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(found[0],"Missing loaded target "+cls+"::"+method+desc);
        return out;
    }
    private static MethodVisitor sink(List<Call> out){
        return new MethodVisitor(Opcodes.ASM9){
            @Override public void visitMethodInsn(int opcode,String owner,String name,
                    String desc,boolean itf){out.add(new Call(opcode,owner,name,desc));}
        };
    }
    private static InjectSpec inject(String cls,String method,String desc)throws IOException{
        List<String> selectors=new ArrayList<>(),anchors=new ArrayList<>();
        List<Boolean> cancel=new ArrayList<>();
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String n,String d,
                    String sig,String[] exceptions){
                if(!n.equals(method)||!d.equals(desc))return null;
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String annotation,boolean visible){
                        if(!INJECT.equals(annotation))return null;
                        return new AnnotationVisitor(Opcodes.ASM9){
                            @Override public AnnotationVisitor visitArray(String key){
                                if("method".equals(key))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public void visit(String key,Object value){selectors.add((String)value);}
                                };
                                if("at".equals(key))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public AnnotationVisitor visitAnnotation(String key,String ann){
                                        if(!AT.equals(ann))return null;
                                        return new AnnotationVisitor(Opcodes.ASM9){
                                            @Override public void visit(String key,Object value){
                                                if("value".equals(key))anchors.add((String)value);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String key,Object value){
                                if("cancellable".equals(key))cancel.add((Boolean)value);
                            }
                        };
                    }
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return new InjectSpec(selectors,anchors,cancel);
    }
    private static ClassReader reader(String cls)throws IOException{
        try(InputStream in=Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(cls+".class")){
            assertNotNull(in,"Missing exact compiled target "+cls);
            return new ClassReader(in);
        }
    }
}
