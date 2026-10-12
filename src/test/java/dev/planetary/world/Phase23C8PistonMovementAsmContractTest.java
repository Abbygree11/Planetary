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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 2.3C-8: exact Gradle-loaded MC 1.21.1 NeoForge 21.1.215 piston
 * algorithm/mod hooks vs Planetary's compiled Level Mixin contracts.
 * This test is NOT a transformed in-game Mixin or gameplay test.
 */
final class Phase23C8PistonMovementAsmContractTest {
    private static final String PREFIX="net/minecraft/";
    private static final String PISTON=PREFIX+"world/level/block/piston/";
    private static final String BASE=PISTON+"PistonBaseBlock";
    private static final String RESOLVER=PISTON+"PistonStructureResolver";
    private static final String MOVING=PISTON+"PistonMovingBlockEntity";
    private static final String MOVING_BLOCK=PISTON+"MovingPistonBlock";
    private static final String HEAD=PISTON+"PistonHeadBlock";
    private static final String BSB=PREFIX+"world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String LEVEL=PREFIX+"world/level/Level";
    private static final String MIXIN="dev/planetary/mixin/LevelMixin";
    private static final String STATE="Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String POS="Lnet/minecraft/core/BlockPos;";
    private static final String DIR="Lnet/minecraft/core/Direction;";
    private static final String LEV="Lnet/minecraft/world/level/Level;";
    private static final String READER="Lnet/minecraft/world/level/LevelReader;";
    private static final String BE="Lnet/minecraft/world/level/block/entity/BlockEntity;";
    private static final String TYPE="Lnet/minecraft/world/level/block/entity/BlockEntityType;";
    private static final String TICKER="Lnet/minecraft/world/level/block/entity/BlockEntityTicker;";
    private static final String CI="Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String CIR="Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String INJECT="Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT="Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String MOVE="("+LEV+POS+DIR+"Z)Z";
    private static final String TRIGGER="("+STATE+LEV+POS+"II)Z";
    private static final String PUSHABLE="("+STATE+LEV+POS+DIR+"Z"+DIR+")Z";
    private static final String TICK="("+LEV+POS+STATE+"L"+MOVING+";)V";
    private static final String STATE_WRITE="("+POS+STATE+"II)Z";
    private static final String BE_WRITE="("+BE+")V";

    private record Method(String name,String desc,int access){}
    private record Call(int opcode,String owner,String name,String desc){}

    @Test
    void eventTransactionHasExactNeoForgePrePostCancellationHooks() throws IOException {
        List<Call> body=calls(BASE,"triggerEvent",TRIGGER);
        assertTrue(has(body,"onPistonMovePre"),
                "Exact target NeoForge pre-event absent; inspect release patch");
        assertTrue(has(body,"onPistonMovePost"),
                "Exact target NeoForge post-event absent; inspect release patch");
        assertTrue(has(body,"moveBlocks"));
        assertTrue(has(body,"finalTick"));
        assertTrue(has(body,"setBlockEntity"));
        assertTrue(has(body,"blockEvent") || has(body,"gameEvent"),
                "Completion event pipeline unexpectedly changed");
        assertTrue(declares(BASE,"moveBlocks",MOVE));
        List<Call> moved=calls(BASE,"moveBlocks",MOVE);
        assertTrue(has(moved,"resolve"));
        assertTrue(has(moved,"newMovingBlockEntity"));
        assertTrue(has(moved,"setBlock"));
        assertTrue(has(moved,"setBlockEntity"));
        assertTrue(has(moved,"onDestroyedByPushReaction"),
                "NeoForge per-block destroy/actual fluid callback absent");
    }

    @Test
    void resolverPreservesModdedAdhesionAndFixedPhysicalPushGraph()
            throws IOException {
        assertTrue(declares(RESOLVER,"resolve","()Z"));
        List<Call> resolution=calls(RESOLVER,"resolve","()Z");
        assertTrue(has(resolution,"isPushable"));
        assertTrue(has(resolution,"isStickyBlock"),
                "Modded sticky block state hook absent");
        assertTrue(has(resolution,"addBranchingBlocks"));
        List<Call> all=allCalls(RESOLVER);
        assertTrue(has(all,"canStickTo"),
                "NeoForge symmetric custom mod adhesion hook absent");
        assertTrue(has(all,"relative"),
                "PistonStructureResolver traverses PHYSICAL BlockPos adjacency");
        assertTrue(has(all,"getPistonPushReaction"),
                "Destroy/block/push reaction owner not preserved");
        assertFalse(has(all,"getNearestLookingDirections"),
                "Resolver must not inherit player placement/view semantics");
        assertTrue(declares(BASE,"isPushable",PUSHABLE));
        List<Call> checks=calls(BASE,"isPushable",PUSHABLE);
        assertTrue(has(checks,"getWorldBorder"));
        assertTrue(has(checks,"getPistonPushReaction"));
    }

    @Test
    void blockStateModPushReactionExtensionIsPreserved() throws IOException {
        List<Call> body=calls(BSB,"getPistonPushReaction",
                "()Lnet/minecraft/world/level/material/PushReaction;");
        assertTrue(has(body,"getPistonPushReaction"),
                "Target should delegate dynamic piston reaction through block");
        assertTrue(has(body,"getBlock"));
    }

    @Test
    void movingPlaceholderFactoryAndTickerHaveIndependentOwners()
            throws IOException {
        String factory="("+POS+STATE+STATE+DIR+"ZZ)"+BE;
        assertTrue(declares(MOVING_BLOCK,"newMovingBlockEntity",factory));
        String ticker="("+LEV+STATE+TYPE+")"+TICKER;
        assertTrue(declares(MOVING_BLOCK,"getTicker",ticker));
        assertTrue(declares(MOVING,"tick",TICK));
        assertTrue(declares(MOVING,"finalTick","()V"));
        assertTrue(has(calls(MOVING,"finalTick","()V"),"removeBlockEntity"));
        assertTrue(has(calls(MOVING,"finalTick","()V"),"setBlock"));
        assertTrue(has(calls(MOVING,"tick",TICK),"moveCollidedEntities"));
        assertTrue(has(calls(MOVING,"tick",TICK),"moveStuckEntities"));
        assertTrue(has(calls(MOVING,"tick",TICK),"neighborChanged"));
        assertTrue(has(calls(MOVING_BLOCK,"onRemove",
                "("+STATE+LEV+POS+STATE+"Z)V"),"finalTick"));
        assertTrue(declares(HEAD,"canSurvive",
                "("+STATE+READER+POS+")Z"));
    }

    @Test
    void slimeAndHoneyMovingEntityInteractionHasDynamicModExtension()
            throws IOException {
        String collision="("+LEV+POS+"F"+"L"+MOVING+";)V";
        List<Call> body=calls(MOVING,"moveCollidedEntities",collision);
        assertTrue(has(body,"isSlimeBlock"),
                "NeoForge slime-like mod extension must survive entity push");
        assertTrue(has(body,"moveEntityByPiston"),
                "Collision loop must dispatch to the physical entity mover helper");
        String helper="("+DIR+"Lnet/minecraft/world/entity/Entity;D"+DIR+")V";
        assertTrue(has(calls(MOVING,"moveEntityByPiston",helper),"move"),
                "The entity helper must preserve Entity.move(MoverType.PISTON, Vec3)");
        List<Call> sticky=calls(MOVING,"moveStuckEntities",collision);
        assertTrue(has(sticky,"isHorizontal"),
                "Honey physical-horizontal movement is distinct from local gravity");
    }

    @Test
    void existingPlanetLevelWriteInterceptionIsNarrowAndCancellable()
            throws IOException {
        String handler="planetary$setBlock";
        String desc="("+POS+STATE+"II"+CIR+")V";
        assertTrue(declares(MIXIN,handler,desc));
        List<Call> body=calls(MIXIN,handler,desc);
        assertTrue(has(body,"resolve"));
        assertTrue(has(body,"ifPresent"));
        List<String> selectors=new ArrayList<>(), anchors=new ArrayList<>();
        List<Boolean> cancellation=new ArrayList<>();
        read(MIXIN).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int access,String name,
                    String descriptor,String signature,String[] exceptions){
                if(!name.equals(handler)||!descriptor.equals(desc))return null;
                assertEquals(0,access&Opcodes.ACC_STATIC);
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String descriptor,boolean visible){
                        if(!descriptor.equals(INJECT))return null;
                        return new AnnotationVisitor(Opcodes.ASM9){
                            @Override public AnnotationVisitor visitArray(String field){
                                if(field.equals("method"))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public void visit(String unused,Object value){
                                        selectors.add((String)value);
                                    }
                                };
                                if(field.equals("at"))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public AnnotationVisitor visitAnnotation(
                                            String unused,String descriptor){
                                        if(!descriptor.equals(AT))return null;
                                        return new AnnotationVisitor(Opcodes.ASM9){
                                            @Override public void visit(String field,Object value){
                                                if(field.equals("value"))anchors.add((String)value);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String key,Object value){
                                if(key.equals("cancellable"))cancellation.add((Boolean)value);
                            }
                        };
                    }
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertEquals(List.of("setBlock"+STATE_WRITE),selectors);
        assertEquals(List.of("HEAD"),anchors);
        assertEquals(List.of(true),cancellation);
        assertTrue(declares(LEVEL,"setBlock",STATE_WRITE));
        assertTrue(declares(LEVEL,"setBlockEntity",BE_WRITE));
        // Passing this check does not prove actual Planetary moving-BE
        // ticker registration / progress / rendered movement.
    }

    private static boolean has(List<Call> calls,String name){
        return calls.stream().anyMatch(c->c.name.equals(name));
    }
    private static boolean declares(String cls,String name,String desc)throws IOException{
        return methods(cls).stream().anyMatch(m->m.name.equals(name)&&m.desc.equals(desc));
    }
    private static List<Method> methods(String cls)throws IOException{
        List<Method> m=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int access,String name,
                    String desc,String signature,String[] exceptions){
                m.add(new Method(name,desc,access));return null;
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return m;
    }
    private static List<Call> calls(String cls,String name,String desc)throws IOException{
        List<Call> calls=new ArrayList<>();boolean[] found={false};
        read(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int access,String n,String d,
                    String signature,String[] exceptions){
                if(!n.equals(name)||!d.equals(desc))return null;
                found[0]=true;
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public void visitMethodInsn(int opcode,String owner,
                            String method,String desc,boolean itf){
                        calls.add(new Call(opcode,owner,method,desc));
                    }
                };
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(found[0],"Missing target owner "+cls+"::"+name+desc);
        return calls;
    }
    private static List<Call> allCalls(String cls)throws IOException{
        List<Call> calls=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int access,String name,String desc,
                    String signature,String[] exceptions){
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public void visitMethodInsn(int opcode,String owner,
                            String method,String desc,boolean itf){
                        calls.add(new Call(opcode,owner,method,desc));
                    }
                };
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return calls;
    }
    private static ClassReader read(String cls)throws IOException{
        try(InputStream in=Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(cls+".class")){
            assertNotNull(in,"Missing loaded target class "+cls);
            return new ClassReader(in);
        }
    }
}
