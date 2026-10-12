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
 * Stage2.3C-12. Strict selected Gradle-resolved NeoForge 21.1.215 JVM
 * lighting/AO/tints/model-data ABI + compiled Planet Mixin annotations.
 * This is NOT transformed Mixin, real-client rendering or mod-jar acceptance.
 */
final class Phase23C12LightingAoTintAsmContractTest {
    private static final String MC="net/minecraft/";
    private static final String CLIENT=MC+"client/";
    private static final String MODEL=CLIENT+"renderer/block/ModelBlockRenderer";
    private static final String AO=MODEL+"$AmbientOcclusionFace";
    private static final String BLOCK_COLORS=CLIENT+"color/block/BlockColors";
    private static final String BIOME_COLORS=CLIENT+"renderer/BiomeColors";
    private static final String BLOCK_AND_TINT=MC+"world/level/BlockAndTintGetter";
    private static final String LEVEL_RENDERER=CLIENT+"renderer/LevelRenderer";
    private static final String STATE_BASE=MC+"world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String LIGHT=MC+"world/level/lighting/LevelLightEngine";
    private static final String PIPE="net/neoforged/neoforge/client/model/lighting/";
    private static final String LIGHT_MODEL=PIPE+"LightPipelineAwareModelBlockRenderer";
    private static final String SMOOTH=PIPE+"SmoothQuadLighter";
    private static final String FLAT=PIPE+"FlatQuadLighter";
    private static final String QUAD=PIPE+"QuadLighter";
    private static final String WRAPPER="net/neoforged/neoforge/client/model/BakedModelWrapper";
    private static final String PLANET_MIXIN="dev/planetary/mixin/ModelBlockRendererPlanetMixin";
    private static final String PLANET_MODEL="dev/planetary/client/render/PlanetBakedModelRotation";
    private static final String STATE="Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS="Lnet/minecraft/core/BlockPos;";
    private static final String GETTER="Lnet/minecraft/world/level/BlockGetter;";
    private static final String BAT="Lnet/minecraft/world/level/BlockAndTintGetter;";
    private static final String DIR="Lnet/minecraft/core/Direction;";
    private static final String DATA="Lnet/neoforged/neoforge/client/model/data/ModelData;";
    private static final String TYPE="Lnet/minecraft/client/renderer/RenderType;";
    private static final String BAKED="Lnet/minecraft/client/resources/model/BakedModel;";
    private static final String BLOCK_TESSEL="("+BAT+BAKED+STATE+POS+
            "Lcom/mojang/blaze3d/vertex/PoseStack;"+
            "Lcom/mojang/blaze3d/vertex/VertexConsumer;"+
            "ZLnet/minecraft/util/RandomSource;JI"+DATA+TYPE+")V";
    private static final String GET_BLOCK_COLOR="("+STATE+BAT+POS+"I)I";
    private static final String AO_CALCULATE="("+BAT+STATE+POS+DIR+"[FLjava/util/BitSet;Z)V";
    private static final String MIXIN_MODIFY="Lorg/spongepowered/asm/mixin/injection/ModifyVariable;";
    private static final String MIXIN_ARGS="Lorg/spongepowered/asm/mixin/injection/ModifyArgs;";
    private record Call(int op,String owner,String name,String desc){}
    private record Method(int acc,String name,String desc){}

    @Test void originalNeoForgeRendererChoosesModTriStateAOAndUsesBothPaths() throws IOException {
        assertTrue(declares(MODEL,"tesselateBlock",BLOCK_TESSEL));
        List<Call> calls=call(MODEL,"tesselateBlock",BLOCK_TESSEL);
        assertTrue(has(calls,"useAmbientOcclusion"),"NeoForge mod model AO policy not queried");
        assertTrue(has(calls,"getLightEmission"),"Per-world dynamic light emission must be queried");
        assertTrue(has(calls,"tesselateWithAO"));
        assertTrue(has(calls,"tesselateWithoutAO"));
        assertTrue(declares(MODEL,"tesselateWithAO",BLOCK_TESSEL));
        assertTrue(declares(MODEL,"tesselateWithoutAO",BLOCK_TESSEL));
        assertTrue(has(call(MODEL,"tesselateWithAO",BLOCK_TESSEL),"getQuads"));
        assertTrue(has(call(MODEL,"tesselateWithoutAO",BLOCK_TESSEL),"getQuads"));
        assertTrue(has(all(MODEL),"getColor"));
        assertTrue(has(all(MODEL),"getShade"));
        assertTrue(has(all(MODEL),"getLightColor"));
    }

    @Test void neoForgeHasIndependentExperimentalSmoothAndFlatLightPipeline() throws IOException {
        assertTrue(declares(LIGHT_MODEL,"tesselateWithAO",BLOCK_TESSEL));
        assertTrue(declares(LIGHT_MODEL,"tesselateWithoutAO",BLOCK_TESSEL));
        assertTrue(has(all(LIGHT_MODEL),"render"));
        // The factory method is declared on the specialized renderer;
        // the normal render path can access its thread-local lighter fields directly.
        assertTrue(declares(LIGHT_MODEL,"getQuadLighter",
                "(Z)Lnet/neoforged/neoforge/client/model/lighting/QuadLighter;"));
        assertTrue(has(all(LIGHT_MODEL),"getQuads"));
        assertTrue(has(all(QUAD),"getShade"),"NeoForge normal-vector shading must be preserved");
        assertTrue(has(all(QUAD),"getColor"),"NeoForge quad tint must call BlockColors");
        assertTrue(has(all(SMOOTH),"getFaceOcclusionShape"),
                "NeoForge smooth lighter samples face occlusion dynamically");
        assertTrue(has(all(SMOOTH),"getLightBlock"),
                "NeoForge smooth lighter checks actual target block opacity");
        assertTrue(has(all(FLAT),"getLightColor"));
        assertTrue(has(all(FLAT),"relative"));
    }

    @Test void tintPipelineIsDynamicAndProvidesExtensionForRegisteredModBlocks() throws IOException {
        assertTrue(declares(BLOCK_COLORS,"getColor",GET_BLOCK_COLOR));
        assertTrue(has(call(BLOCK_COLORS,"getColor",GET_BLOCK_COLOR),"getBlock"));
        assertTrue(has(call(BLOCK_COLORS,"getColor",GET_BLOCK_COLOR),"getColor"),
                "BlockColors must invoke registered BlockColor with state/pos/tintIndex");
        assertTrue(has(all(BLOCK_COLORS),"onBlockColorsInit"),
                "NeoForge color registration event missing");
        for(String suffix:List.of("Grass","Foliage","Water")){
            assertTrue(declares(BIOME_COLORS,"getAverage"+suffix+"Color",
                    "("+BAT+POS+")I"),"Missing biome-color resolver for "+suffix);
        }
        assertTrue(has(all(BIOME_COLORS),"getBlockTint"));
        assertTrue(declares(WRAPPER,"useAmbientOcclusion",
                "("+STATE+DATA+TYPE+")Lnet/neoforged/neoforge/common/util/TriState;"));
        assertTrue(declares(WRAPPER,"getModelData","("+BAT+POS+STATE+DATA+")"+DATA));
    }

    @Test void worldLightingQueriesArePhysicalAndHaveDynamicBlockStateHooks() throws IOException {
        assertTrue(declares(BLOCK_AND_TINT,"getShade","("+DIR+"Z)F"));
        assertTrue(declares(BLOCK_AND_TINT,"getBlockTint",
                "("+POS+"Lnet/minecraft/world/level/ColorResolver;)I"));
        assertTrue(declares(BLOCK_AND_TINT,"getRawBrightness","("+POS+"I)I"));
        assertTrue(declares(LIGHT,"getRawBrightness","("+POS+"I)I"));
        assertTrue(has(call(LIGHT,"getRawBrightness","("+POS+"I)I"),"getLightValue"));
        // NeoForge exposes the position-aware value via IBlockStateExtension,
        // not a method declared on BlockBehaviour$BlockStateBase itself.
        assertTrue(declares(
                "net/neoforged/neoforge/common/extensions/IBlockStateExtension",
                "getLightEmission","("+GETTER+POS+")I"));
        assertTrue(declares(STATE_BASE,"getLightBlock","("+GETTER+POS+")I"));
        assertTrue(declares(STATE_BASE,"getShadeBrightness","("+GETTER+POS+")F"));
        assertTrue(has(call(STATE_BASE,"getLightBlock","("+GETTER+POS+")I"),
                "getBlock"));
        assertTrue(has(all(LEVEL_RENDERER),"getLightEmission") ||
                   has(all(LEVEL_RENDERER),"getLightColor"));
    }

    @Test void planetModelInterceptorStillTargetsNeoForgeExtendedTessellation() throws IOException {
        assertTrue(declares(PLANET_MIXIN,"planetary$orientModel",
                "("+BAKED+BAT+BAKED+STATE+POS+
                "Lcom/mojang/blaze3d/vertex/PoseStack;"+
                "Lcom/mojang/blaze3d/vertex/VertexConsumer;"+
                "ZLnet/minecraft/util/RandomSource;JI"+DATA+TYPE+")"+BAKED));
        // orientForBlock lives in javac's lambda$... method generated for
        // Optional.map; the Mixin handler itself calls Optional.map.
        assertTrue(has(byName(PLANET_MIXIN,"planetary$orientModel"),"map"));
        assertTrue(has(all(PLANET_MIXIN),"orientForBlock"));
        assertTrue(has(all(PLANET_MODEL),"getQuads"));
        assertTrue(has(all(PLANET_MODEL),"getDirection"));
        assertTrue(has(all(PLANET_MODEL),"getTintIndex") ||
                   has(all(PLANET_MODEL),"getVertices"));
        List<String> variable=annotationSelector(PLANET_MIXIN,"planetary$orientModel",MIXIN_MODIFY);
        assertEquals(List.of("tesselateBlock"+BLOCK_TESSEL),variable);
        List<String> args=annotationSelector(PLANET_MIXIN,
                "planetary$orientPointedDripstoneOffset",MIXIN_ARGS);
        assertEquals(List.of("tesselateBlock"+BLOCK_TESSEL),args);
    }

    private static boolean has(List<Call> calls,String name){
        return calls.stream().anyMatch(c->c.name.equals(name));
    }
    private static boolean declares(String owner,String name,String desc)throws IOException{
        return members(owner).stream().anyMatch(m->m.name.equals(name)&&m.desc.equals(desc));
    }
    private static List<Method> members(String owner)throws IOException{
        List<Method> list=new ArrayList<>();
        read(owner).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String n,String d,
                    String sig,String[] exc){list.add(new Method(acc,n,d));return null;}
        },ClassReader.SKIP_CODE|ClassReader.SKIP_FRAMES|ClassReader.SKIP_DEBUG);
        return list;
    }
    private static List<Call> call(String owner,String name,String desc)throws IOException{
        List<Call> list=new ArrayList<>();boolean[] seen={false};
        read(owner).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String n,String d,
                    String sig,String[] exc){
                if(!name.equals(n)||!desc.equals(d))return null;
                seen[0]=true;return sink(list);
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(seen[0],"Missing exact loaded ABI "+owner+"::"+name+desc);
        return list;
    }
    private static List<Call> byName(String owner,String name)throws IOException{
        List<Call> list=new ArrayList<>();int[] seen={0};
        read(owner).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String n,String d,
                    String sig,String[] exc){
                if(!name.equals(n))return null;
                seen[0]++;return sink(list);
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(seen[0]>0,"Missing class member "+owner+"."+name);
        return list;
    }
    private static List<Call> all(String owner)throws IOException{
        List<Call> list=new ArrayList<>();
        read(owner).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String n,String d,
                    String sig,String[] exc){return sink(list);}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return list;
    }
    private static MethodVisitor sink(List<Call> out){
        return new MethodVisitor(Opcodes.ASM9){
            @Override public void visitMethodInsn(int op,String own,String name,
                    String desc,boolean itf){out.add(new Call(op,own,name,desc));}
        };
    }
    private static List<String> annotationSelector(String owner,String target,String annotation)
            throws IOException{
        List<String> selectors=new ArrayList<>();boolean[] found={false};
        read(owner).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int acc,String name,String desc,
                    String sig,String[] exc){
                if(!target.equals(name))return null;
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String desc,
                            boolean visible){
                        if(!annotation.equals(desc))return null;
                        found[0]=true;
                        return new AnnotationVisitor(Opcodes.ASM9){
                            @Override public AnnotationVisitor visitArray(String key){
                                if(!"method".equals(key))return null;
                                return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public void visit(String k,Object value){
                                        selectors.add((String)value);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(found[0],"Missing actual "+target+" annotation "+annotation);
        return selectors;
    }
    private static ClassReader read(String owner)throws IOException{
        try(InputStream in=Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner+".class")){
            assertNotNull(in,"Missing actual NeoForge class "+owner);
            return new ClassReader(in);
        }
    }
}
