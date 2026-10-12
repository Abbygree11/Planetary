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
 * Stage 2.3C-9: selected real Gradle-resolved NeoForge 21.1.215 REDSTONE
 * ABI and compiled Planetary Mixin handler/HEAD contracts.
 * NOT a woven-Mixin test, mod binary load or in-game acceptance.
 */
final class Phase23C9RedstoneSignalAsmContractTest {
    private static final String ROOT="net/minecraft/";
    private static final String W=ROOT+"world/level/";
    private static final String B=W+"block/";
    private static final String POS="Lnet/minecraft/core/BlockPos;";
    private static final String DIR="Lnet/minecraft/core/Direction;";
    private static final String STATE="Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String GETTER="Lnet/minecraft/world/level/BlockGetter;";
    private static final String SIGNAL="Lnet/minecraft/world/level/SignalGetter;";
    private static final String READER="Lnet/minecraft/world/level/LevelReader;";
    private static final String LEV="Lnet/minecraft/world/level/Level;";
    private static final String ACCESS="Lnet/minecraft/world/level/LevelAccessor;";
    private static final String BLOCK="Lnet/minecraft/world/level/block/Block;";
    private static final String BASE=B+"state/BlockBehaviour$BlockStateBase";
    private static final String SIGNAL_GETTER=W+"SignalGetter";
    private static final String WIRE=B+"RedStoneWireBlock";
    private static final String DIODE=B+"DiodeBlock";
    private static final String COMPARATOR=B+"ComparatorBlock";
    private static final String OBSERVER=B+"ObserverBlock";
    private static final String POWER_RAIL=B+"PoweredRailBlock";
    private static final String DETECTOR=B+"DetectorRailBlock";
    private static final String MIXIN_LEVEL="dev/planetary/mixin/LevelMixin";
    private static final String MIXIN_SERVER="dev/planetary/mixin/ServerLevelMixin";
    private static final String CALLBACK="Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String INJECT="Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT="Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String REDSTONE_SIGNAL="("+POS+DIR+")I";
    private static final String UPDATE_FRONT="("+LEV+POS+STATE+")V";
    private static final String WIRE_PORT="("+GETTER+POS+DIR+"Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;";
    private static final String BLOCK_PORT="("+STATE+GETTER+POS+DIR+")I";
    private static final String CHANGE="("+STATE+LEV+POS+BLOCK+POS+"Z)V";
    private static final String UPDATE_SHAPE="("+STATE+DIR+STATE+ACCESS+POS+POS+")"+STATE;

    private record Call(int opcode,String owner,String method,String descriptor){}
    private record Member(String name,String descriptor,int flags){}
    private record InjectSpec(List<String> selectors,List<String> anchors,List<Boolean> cancellable){}

    @Test void signalGetterPreservesNeoForgeWeakPowerExtensionAndPhysicalSide() throws IOException {
        assertTrue(declares(SIGNAL_GETTER,"getSignal",REDSTONE_SIGNAL));
        List<Call> body=calls(SIGNAL_GETTER,"getSignal",REDSTONE_SIGNAL);
        assertTrue(invokes(body,"getBlockState"));
        assertTrue(invokes(body,"getSignal"));
        assertTrue(invokes(body,"shouldCheckWeakPower"),
                "NeoForge weak power extension missing on actual target classpath");
        assertTrue(invokes(body,"getDirectSignalTo"));
        for(String method:List.of("getBestNeighborSignal","hasNeighborSignal"))
            assertTrue(declares(SIGNAL_GETTER,method,"("+POS+")"+(method.equals("hasNeighborSignal")?"Z":"I")));
        assertTrue(declares(SIGNAL_GETTER,"getDirectSignal",REDSTONE_SIGNAL));
        assertTrue(declares(SIGNAL_GETTER,"getControlInputSignal",
                "("+POS+DIR+"Z)I"));
        assertTrue(invokes(calls(SIGNAL_GETTER,"getBestNeighborSignal","("+POS+")I"),
                "relative"));
    }

    @Test void virtualBlockStatePortsMustRemainModOverridable() throws IOException {
        for(String method:List.of("getSignal","getDirectSignal")) {
            String desc="("+GETTER+POS+DIR+")I";
            assertTrue(declares(BASE,method,desc));
            List<Call> body=calls(BASE,method,desc);
            assertTrue(invokes(body,"getBlock"));
            assertTrue(invokes(body,method),
                    "BlockStateBase must dynamically invoke concrete (possibly modded) Block");
        }
        assertTrue(declares(BASE,"isSignalSource","()Z"));
    }

    @Test void wireUsesModdedConnectionExtensionOnVerticalAndTangentPorts()
            throws IOException {
        assertTrue(declares(WIRE,"getConnectingSide",WIRE_PORT));
        List<Call> connection=calls(WIRE,"getConnectingSide",WIRE_PORT);
        assertTrue(count(connection,"canRedstoneConnectTo")>=2,
                "NeoForge BlockState.canRedstoneConnectTo used for horizontal, rising and falling wires");
        assertTrue(invokes(connection,"isRedstoneConductor"));
        assertTrue(invokes(connection,"above"));
        assertTrue(invokes(connection,"below"));
        String calc="("+LEV+POS+")I";
        assertTrue(declares(WIRE,"calculateTargetStrength",calc));
        assertTrue(invokes(calls(WIRE,"calculateTargetStrength",calc),"getBestNeighborSignal"));
        assertTrue(declares(WIRE,"updatePowerStrength","("+LEV+POS+STATE+")V"));
        assertTrue(invokes(calls(WIRE,"updatePowerStrength","("+LEV+POS+STATE+")V"),
                "updateNeighborsAt"));
        assertTrue(declares(WIRE,"getSignal",BLOCK_PORT));
        assertTrue(declares(WIRE,"getDirectSignal",BLOCK_PORT));
    }

    @Test void directedDiodesAndComparatorsPreserveNeoForgeNeighborCallbacks()
            throws IOException {
        List<Call> diode=calls(DIODE,"updateNeighborsInFront",UPDATE_FRONT);
        assertTrue(invokes(diode,"onNeighborNotify"),
                "NeoForge may cancel diode notification before neighborChanged");
        assertTrue(invokes(diode,"neighborChanged"));
        assertTrue(invokes(diode,"updateNeighborsAtExceptFromFacing"));
        assertTrue(invokes(calls(DIODE,"getInputSignal","("+LEV+POS+STATE+")I"),
                "getSignal"));
        assertTrue(invokes(calls(DIODE,"getAlternateSignal",
                "("+SIGNAL+POS+STATE+")I"),"getControlInputSignal"));
        assertTrue(invokes(calls(DIODE,"checkTickOnNeighbor",UPDATE_FRONT),"scheduleTick"));
        assertTrue(declares(COMPARATOR,"getWeakChanges","("+STATE+READER+POS+")Z"),
                "NeoForge comparator sensitive-weak-neighbor extension absent");
        assertTrue(declares(COMPARATOR,"onNeighborChange",
                "("+STATE+READER+POS+POS+")V"));
        assertTrue(invokes(calls(COMPARATOR,"onNeighborChange",
                "("+STATE+READER+POS+POS+")V"),"handleNeighborChanged"));
        assertTrue(invokes(calls(COMPARATOR,"getOutputSignal",
                "("+GETTER+POS+STATE+")I"),"getBlockEntity"));
        assertTrue(invokes(calls(COMPARATOR,"refreshOutputState",UPDATE_FRONT),
                "setOutputSignal"));
    }

    @Test void observerAndRailHaveIndependentSignalGraphs() throws IOException {
        assertTrue(invokes(calls(OBSERVER,"updateShape",UPDATE_SHAPE),"startSignal"));
        assertTrue(invokes(calls(OBSERVER,"tick",
                "("+STATE+"Lnet/minecraft/server/level/ServerLevel;"+POS+
                        "Lnet/minecraft/util/RandomSource;)V"),"setBlock"));
        assertTrue(declares(POWER_RAIL,"findPoweredRailSignal",
                "("+LEV+POS+STATE+"ZI)Z"));
        assertTrue(invokes(calls(POWER_RAIL,"findPoweredRailSignal",
                "("+LEV+POS+STATE+"ZI)Z"),"relative"));
        assertTrue(declares(DETECTOR,"checkPressed","("+LEV+POS+STATE+")V"));
        assertTrue(invokes(calls(DETECTOR,"checkPressed","("+LEV+POS+STATE+")V"),
                "updateNeighborsAt"));
    }

    @Test void planetLevelAndServerNeighborInterceptsRemainCancellableAtHead()
            throws IOException {
        String handlerDesc="("+POS+BLOCK+CALLBACK+")V";
        String targetDesc="("+POS+BLOCK+")V";
        for(String mixin:List.of(MIXIN_LEVEL,MIXIN_SERVER)){
            assertTrue(declares(mixin,"planetary$updateNeighborsAt",handlerDesc));
            InjectSpec spec=inject(mixin,"planetary$updateNeighborsAt",handlerDesc);
            assertEquals(List.of("updateNeighborsAt"),spec.selectors());
            assertEquals(List.of("HEAD"),spec.anchors());
            assertEquals(List.of(true),spec.cancellable());
            assertTrue(invokes(calls(mixin,"planetary$updateNeighborsAt",handlerDesc),
                    "resolve"));
            assertTrue(declares(mixin.equals(MIXIN_LEVEL)?W+"Level":ROOT+"server/level/ServerLevel",
                    "updateNeighborsAt",targetDesc));
        }
        // This is only compiled selector/handler ABI proof. Stage2.3C-4 found
        // potential skipped NeoForge onNeighborNotify on Planet-bound routes;
        // neither this nor compiled bytecode is live mod event delivery.
    }

    private static boolean invokes(List<Call> calls,String name){
        return calls.stream().anyMatch(c->c.method.equals(name));
    }
    private static long count(List<Call> calls,String name){
        return calls.stream().filter(c->c.method.equals(name)).count();
    }
    private static boolean declares(String cls,String name,String desc)throws IOException{
        return members(cls).stream().anyMatch(m->m.name.equals(name)&&m.descriptor.equals(desc));
    }
    private static List<Member> members(String cls)throws IOException{
        List<Member> members=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String name,String desc,
                    String generic,String[] exceptions){
                members.add(new Member(name,desc,flags));return null;
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return members;
    }
    private static List<Call> calls(String cls,String name,String descriptor)throws IOException{
        List<Call> out=new ArrayList<>();boolean[] found={false};
        read(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String n,String d,
                    String generic,String[] exceptions){
                if(!name.equals(n)||!descriptor.equals(d))return null;
                found[0]=true;
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public void visitMethodInsn(int opcode,String owner,
                            String method,String descriptor,boolean isInterface){
                        out.add(new Call(opcode,owner,method,descriptor));
                    }
                };
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(found[0],"Missing actual classpath ABI "+cls+"."+name+descriptor);
        return out;
    }
    private static InjectSpec inject(String cls,String name,String desc)throws IOException{
        List<String> method=new ArrayList<>(),at=new ArrayList<>();
        List<Boolean> cancel=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int flags,String n,String d,
                    String signature,String[] exceptions){
                if(!name.equals(n)||!desc.equals(d))return null;
                assertEquals(0,flags&Opcodes.ACC_STATIC);
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String descriptor,boolean visible){
                        if(!descriptor.equals(INJECT))return null;
                        return new AnnotationVisitor(Opcodes.ASM9){
                            @Override public AnnotationVisitor visitArray(String key){
                                if(key.equals("method"))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public void visit(String unused,Object value){
                                        method.add((String)value);
                                    }
                                };
                                if(key.equals("at"))return new AnnotationVisitor(Opcodes.ASM9){
                                    @Override public AnnotationVisitor visitAnnotation(
                                            String unused,String descriptor){
                                        if(!descriptor.equals(AT))return null;
                                        return new AnnotationVisitor(Opcodes.ASM9){
                                            @Override public void visit(String key,Object value){
                                                if(key.equals("value"))at.add((String)value);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String key,Object value){
                                if(key.equals("cancellable"))cancel.add((Boolean)value);
                            }
                        };
                    }
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return new InjectSpec(method,at,cancel);
    }
    private static ClassReader read(String cls)throws IOException{
        try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(cls+".class")){
            assertNotNull(in,"Missing real compiled NeoForge class "+cls);
            return new ClassReader(in);
        }
    }
}
