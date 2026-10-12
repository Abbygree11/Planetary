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
 * Stage 2.3C-10. Reads actual Gradle-resolved NeoForge 21.1.215 classes
 * and compiled Planetary Mixin annotation contracts; no game Mixin weaving.
 */
final class Phase23C10FluidFlowInteractionAsmContractTest {
    private static final String W="net/minecraft/world/";
    private static final String FLUID=W+"level/material/";
    private static final String FLOW=FLUID+"FlowingFluid";
    private static final String FLUID_STATE=FLUID+"FluidState";
    private static final String LAVA=FLUID+"LavaFluid";
    private static final String WATER=FLUID+"WaterFluid";
    private static final String LIQUID=W+"level/block/LiquidBlock";
    private static final String CONTAINER=W+"level/block/LiquidBlockContainer";
    private static final String WATERLOGGED=W+"level/block/SimpleWaterloggedBlock";
    private static final String BUCKET=W+"item/BucketItem";
    private static final String INTERACTION="net/neoforged/neoforge/fluids/FluidInteractionRegistry";
    private static final String BASE="net/neoforged/neoforge/fluids/BaseFlowingFluid";
    private static final String EXTENSION="net/neoforged/neoforge/common/extensions/IFluidStateExtension";
    private static final String LEVEL="net/minecraft/world/level/Level";
    private static final String LEVEL_MIXIN="dev/planetary/mixin/LevelMixin";
    private static final String P="Lnet/minecraft/core/BlockPos;";
    private static final String BS="Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String FS="Lnet/minecraft/world/level/material/FluidState;";
    private static final String L="Lnet/minecraft/world/level/Level;";
    private static final String GETTER="Lnet/minecraft/world/level/BlockGetter;";
    private static final String ACCESS="Lnet/minecraft/world/level/LevelAccessor;";
    private static final String D="Lnet/minecraft/core/Direction;";
    private static final String F="Lnet/minecraft/world/level/material/Fluid;";
    private static final String VEC="Lnet/minecraft/world/phys/Vec3;";
    private static final String SHAPE="Lnet/minecraft/world/phys/shapes/VoxelShape;";
    private static final String PLAYER="Lnet/minecraft/world/entity/player/Player;";
    private static final String HIT="Lnet/minecraft/world/phys/BlockHitResult;";
    private static final String ITEM_STACK="Lnet/minecraft/world/item/ItemStack;";
    private static final String CIR="Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String INJECT="Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT="Lorg/spongepowered/asm/mixin/injection/At;";
    private record Call(int opcode,String owner,String name,String desc){}
    private record Method(String name,String desc,int flags){}

    @Test void fluidStateDispatchPreservesDynamicFluidTypeAndWorldVectors() throws IOException {
        assertTrue(has(call(FLUID_STATE,"tick","("+L+P+")V"),"tick"));
        assertTrue(has(call(FLUID_STATE,"getFlow","("+GETTER+P+")"+VEC),"getFlow"));
        assertTrue(has(call(FLUID_STATE,"getShape","("+GETTER+P+")"+SHAPE),"getShape"));
        assertTrue(has(call(FLUID_STATE,"createLegacyBlock","()"+BS),"createLegacyBlock"));
        assertTrue(has(call(FLUID_STATE,"canBeReplacedWith",
                "("+GETTER+P+F+D+")Z"),"canBeReplacedWith"));
        assertTrue(has(call(FLUID_STATE,"tick","("+L+P+")V"),"getType"));
        assertTrue(has(call(FLUID_STATE,"getFlow","("+GETTER+P+")"+VEC),"getType"));
    }

    @Test void flowingFluidPreservesNeoForgeSourceCreationAndDynamicStateUpdates()
            throws IOException {
        List<Call> liquid=call(FLOW,"getNewLiquid","("+L+P+BS+")"+FS);
        assertTrue(liquid.stream().anyMatch(c->c.owner.equals(
                "net/neoforged/neoforge/event/EventHooks") &&
                c.name.equals("canCreateFluidSource")),
                "NeoForge source event owner/callsite absent in target FlowingFluid");
        assertTrue(has(liquid,"relative"));
        assertTrue(has(liquid,"below"));
        assertTrue(has(liquid,"above"));
        List<Call> tick=call(FLOW,"tick","("+L+P+FS+")V");
        assertTrue(has(tick,"getNewLiquid"));
        assertTrue(has(tick,"spread"));
        assertTrue(has(tick,"scheduleTick"));
        assertTrue(has(tick,"updateNeighborsAt"));
        assertTrue(has(tick,"createLegacyBlock"));
        assertTrue(has(call(FLOW,"spread","("+L+P+FS+")V"),"spreadTo"));
    }

    @Test void flowSlopeAndWallCachesArePhysicallyScopedVanillaAlgorithms()
            throws IOException {
        assertTrue(has(call(FLOW,"getFlow","("+GETTER+P+FS+")"+VEC),"getFluidState"));
        assertTrue(has(call(FLOW,"getSpread",
                "("+L+P+BS+")Ljava/util/Map;"),"getSlopeDistance"));
        assertTrue(has(call(FLOW,"getSpread",
                "("+L+P+BS+")Ljava/util/Map;"),"getCacheKey"));
        List<Call> cache=call(FLOW,"getCacheKey","("+P+P+")S");
        assertTrue(has(cache,"getX"));
        assertTrue(has(cache,"getZ"));
        assertFalse(has(cache,"getY"),
                "Vanilla slope cache depends on X/Z only; Planet graph needs chart-aware key");
        assertTrue(has(callsByName(FLOW,"canPassThroughWall"),"mergedFaceOccludes"));
        assertTrue(has(callsByName(FLOW,"spreadTo"),"placeLiquid"),
                "Fluid spread into arbitrary LiquidBlockContainer must be retained");
        assertTrue(has(callsByName(FLOW,"getShape"),"hasSameAbove"));
    }

    @Test void exactTargetNeoForgeFluidInteractionAndWaterloggingWriters()
            throws IOException {
        String interact="("+L+P+")Z";
        assertTrue(declares(INTERACTION,"canInteract",interact));
        for(String method:List.of("onPlace","neighborChanged")){
            List<Call> body=callsByName(LIQUID,method);
            assertTrue(body.stream().anyMatch(c->c.owner.equals(INTERACTION) &&
                    c.name.equals("canInteract") && c.desc.equals(interact) &&
                    c.opcode==Opcodes.INVOKESTATIC),
                    "Missing actual 21.1.215 NeoForge reaction registry dispatch: "+method);
            assertTrue(has(body,"scheduleTick"));
        }
        assertTrue(declares(CONTAINER,"placeLiquid",
                "("+ACCESS+P+BS+FS+")Z"));
        assertTrue(declares(WATERLOGGED,"placeLiquid",
                "("+ACCESS+P+BS+FS+")Z"));
        assertTrue(has(callsByName(WATERLOGGED,"placeLiquid"),"scheduleTick"));
        assertTrue(has(callsByName(WATERLOGGED,"placeLiquid"),"setBlock"));
    }

    @Test void bucketAndModFluidTypeExtensionPreserveNonVanillaBehaviors()
            throws IOException {
        String place="("+PLAYER+L+P+HIT+ITEM_STACK+")Z";
        assertTrue(declares(BUCKET,"emptyContents",place),
                "NeoForge bucket ItemStack-aware overload must remain");
        List<Call> bucket=call(BUCKET,"emptyContents",place);
        assertTrue(has(bucket,"canPlaceLiquid"));
        assertTrue(has(bucket,"placeLiquid"));
        assertTrue(has(bucket,"getFluidType"));
        assertTrue(has(bucket,"isVaporizedOnPlacement"));
        assertTrue(declares(BASE,"canConvertToSource","("+FS+L+P+")Z"));
        assertTrue(has(call(BASE,"canConvertToSource","("+FS+L+P+")Z"),
                "getFluidType"));
        assertTrue(declares(EXTENSION,"canConvertToSource","("+L+P+")Z"));
        assertTrue(has(callsByName(LAVA,"spreadTo"),"fireFluidPlaceBlockEvent"),
                "NeoForge lava stone author must invoke fluid-place-block event");
        assertTrue(has(callsByName(LAVA,"randomTick"),"fireFluidPlaceBlockEvent"),
                "NeoForge lava fire author must invoke fluid-place-block event");
        assertTrue(declares(WATER,"canBeReplacedWith","("+FS+GETTER+P+F+D+")Z"));
    }

    @Test void planetLevelFluidReadIsCancellableHeadButNotFluidTopology()
            throws IOException {
        String handler="planetary$getFluidState";
        String target="getFluidState";
        String desc="("+P+CIR+")V";
        assertTrue(declares(LEVEL_MIXIN,handler,desc));
        assertTrue(declares(LEVEL,target,"("+P+")"+FS));
        assertTrue(has(call(LEVEL_MIXIN,handler,desc),"resolve"));
        assertTrue(has(call(LEVEL_MIXIN,handler,desc),"ifPresent"));
        List<String> selectors=new ArrayList<>(),anchors=new ArrayList<>();
        List<Boolean> cancels=new ArrayList<>();
        reader(LEVEL_MIXIN).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String name,String descriptor,
                    String generic,String[] exceptions){
                if(!handler.equals(name)||!desc.equals(descriptor))return null;
                assertEquals(0,acc&Opcodes.ACC_STATIC);
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String ann,boolean visible){
                        if(!INJECT.equals(ann))return null;
                        return new AnnotationVisitor(Opcodes.ASM9){
                            @Override public AnnotationVisitor visitArray(String key){
                                if("method".equals(key))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public void visit(String k,Object val){selectors.add((String)val);}
                                };
                                if("at".equals(key))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public AnnotationVisitor visitAnnotation(String k,String ann){
                                        if(!AT.equals(ann))return null;
                                        return new AnnotationVisitor(Opcodes.ASM9){
                                            @Override public void visit(String k,Object val){
                                                if("value".equals(k))anchors.add((String)val);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String k,Object v){
                                if("cancellable".equals(k))cancels.add((Boolean)v);
                            }
                        };
                    }
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertEquals(List.of(target),selectors);
        assertEquals(List.of("HEAD"),anchors);
        assertEquals(List.of(true),cancels);
    }

    private static boolean has(List<Call> body,String name){
        return body.stream().anyMatch(c->c.name.equals(name));
    }
    private static boolean declares(String cls,String method,String desc)throws IOException{
        return members(cls).stream().anyMatch(m->m.name.equals(method)&&m.desc.equals(desc));
    }
    private static List<Method> members(String cls)throws IOException{
        List<Method> out=new ArrayList<>();
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String name,String descriptor,
                    String generic,String[] exceptions){
                out.add(new Method(name,descriptor,flags));return null;
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return out;
    }
    private static List<Call> callsByName(String cls,String method)throws IOException{
        List<Call> out=new ArrayList<>();int[] count={0};
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String name,String descriptor,
                    String generic,String[] exceptions){
                if(!method.equals(name))return null;
                count[0]++;
                return sink(out);
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(count[0]>0,"Missing declared method in actual classpath: "+cls+"::"+method);
        return out;
    }
    private static List<Call> call(String cls,String method,String descriptor)throws IOException{
        List<Call> out=new ArrayList<>();boolean[] found={false};
        reader(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String name,String desc,
                    String generic,String[] exceptions){
                if(!method.equals(name)||!descriptor.equals(desc))return null;
                found[0]=true;return sink(out);
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(found[0],"Missing exact NeoForge ABI "+cls+"::"+method+descriptor);
        return out;
    }
    private static MethodVisitor sink(List<Call> out){
        return new MethodVisitor(Opcodes.ASM9){
            @Override public void visitMethodInsn(int opcode,String owner,String name,
                    String desc,boolean isInterface){
                out.add(new Call(opcode,owner,name,desc));
            }
        };
    }
    private static ClassReader reader(String cls)throws IOException{
        try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(cls+".class")){
            assertNotNull(in,"Missing actual NeoForge target class "+cls);
            return new ClassReader(in);
        }
    }
}
