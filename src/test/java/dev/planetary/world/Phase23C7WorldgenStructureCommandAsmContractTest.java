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
 * Phase 2.3C-7: actual Gradle-loaded NeoForge 21.1.215 alternate writers,
 * structure processors and compiled Planetary Level/ChunkGenerator gates.
 * Source/bytecode contracts != actual Mixin weave or in-game mod acceptance.
 */
final class Phase23C7WorldgenStructureCommandAsmContractTest {
    private static final String ROOT = "net/minecraft/";
    private static final String L = ROOT + "world/level/";
    private static final String BLOCK_STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String BLOCK_POS = "Lnet/minecraft/core/BlockPos;";
    private static final String BLOCK_ENTITY = "Lnet/minecraft/world/level/block/entity/BlockEntity;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String ACCESS = "Lnet/minecraft/world/level/LevelAccessor;";
    private static final String SRV_ACCESS = "Lnet/minecraft/world/level/ServerLevelAccessor;";
    private static final String RANDOM = "Lnet/minecraft/util/RandomSource;";
    private static final String SETTINGS =
            "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;";
    private static final String STRUCTURE =
            L + "levelgen/structure/templatesystem/StructureTemplate";
    private static final String INFO = "L" + STRUCTURE + "$StructureBlockInfo;";
    private static final String LIST = "Ljava/util/List;";
    private static final String GEN = "net/minecraft/server/level/WorldGenRegion";
    private static final String FEATURE = L + "levelgen/feature/Feature";
    private static final String INPUT = ROOT + "commands/arguments/blocks/BlockInput";
    private static final String SET_BLOCK = ROOT + "server/commands/SetBlockCommand";
    private static final String FILL = ROOT + "server/commands/FillCommand";
    private static final String CLONE = ROOT + "server/commands/CloneCommands";
    private static final String NBT = "Lnet/minecraft/nbt/CompoundTag;";
    private static final String PROVIDER = "Lnet/minecraft/core/HolderLookup$Provider;";
    private static final String CI_BOOL =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    private static final String INJECT =
            "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String AT =
            "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String SET4 = "(" + BLOCK_POS + BLOCK_STATE + "II)Z";
    private static final String SET3 = "(" + BLOCK_POS + BLOCK_STATE + "I)Z";
    private static final String PLACE =
            "(" + SRV_ACCESS + BLOCK_POS + BLOCK_POS + SETTINGS + RANDOM + "I)Z";
    private static final String PROCESS6 =
            "(" + SRV_ACCESS + BLOCK_POS + BLOCK_POS + SETTINGS + LIST +
                    "L" + STRUCTURE + ";)" + LIST;
    private static final String INPUT_PLACE =
            "(Lnet/minecraft/server/level/ServerLevel;" + BLOCK_POS + "I)Z";

    private record Method(String name, String descriptor, int flags) {}
    private record Call(int opcode, String owner, String name, String desc) {}

    @Test
    void generationUsesChunkWriterNotRuntimeBlockItemPlacement() throws IOException {
        assertTrue(declares(GEN, "ensureCanWrite", "(" + BLOCK_POS + ")Z"));
        assertTrue(declares(GEN, "setBlock", SET4));
        List<Call> body = calls(GEN, "setBlock", SET4);
        assertTrue(has(body, "ensureCanWrite"));
        assertTrue(has(body, "getChunk"));
        assertTrue(has(body, "setBlockState"),
                "WorldGenRegion must write ChunkAccess directly");
        assertTrue(has(body, "setBlockEntityNbt"),
                "ProtoChunk DUMMY BlockEntity NBT path not accounted for");
        assertTrue(has(body, "newBlockEntity"));
        assertFalse(body.stream().anyMatch(c -> c.owner.equals(L + "Level") &&
                c.name.equals("setBlock")), "WorldGenRegion bypass must remain independent");
    }

    @Test
    void structureNeoForgeProcessorExtensionHasRealTargetOwnerAndDescriptor()
            throws IOException {
        assertTrue(declares(STRUCTURE, "placeInWorld", PLACE));
        assertTrue(declares(STRUCTURE, "processBlockInfos", PROCESS6),
                "NeoForge six-argument mod processor hook missing in actual target");
        List<Call> body = calls(STRUCTURE, "placeInWorld", PLACE);
        assertEquals(1, count(body, STRUCTURE, "processBlockInfos",
                PROCESS6, Opcodes.INVOKESTATIC),
                "Structure writer must dispatch NeoForge six-argument processor");
        assertTrue(has(body, "mirror"));
        assertTrue(has(body, "rotate"));
        assertTrue(has(body, "setBlock"));
        assertTrue(has(body, "loadWithComponents"));
        assertTrue(has(body, "placeLiquid"));
        assertTrue(has(body, "updateFromNeighbourShapes"));
        assertTrue(has(body, "blockUpdated"));
        assertTrue(has(body, "setChanged"));
    }

    @Test
    void structureTwoPhaseBeRecreationAndPhysicalEdgeNeighbors()
            throws IOException {
        List<Call> body = calls(STRUCTURE, "placeInWorld", PLACE);
        assertTrue(has(body, "tryClear"),
                "Old BE contents need clear before BARRIER/replacement");
        assertTrue(has(body, "getBlockEntity"));
        assertTrue(has(body, "getFluidState"));
        assertTrue(has(body, "isSource"));
        assertTrue(has(body, "getKnownShape"));
        String edge3 = "(" + ACCESS + "ILnet/minecraft/world/phys/shapes/DiscreteVoxelShape;" +
                BLOCK_POS + ")V";
        String edge6 = "(" + ACCESS + "ILnet/minecraft/world/phys/shapes/DiscreteVoxelShape;III)V";
        assertTrue(declares(STRUCTURE, "updateShapeAtEdge", edge3));
        assertTrue(declares(STRUCTURE, "updateShapeAtEdge", edge6));
        List<Call> edgeCalls = calls(STRUCTURE, "updateShapeAtEdge", edge6);
        assertTrue(has(edgeCalls, "forAllFaces"));
    }

    @Test
    void commandBlockInputRecomputesShapeAndLoadsBeData()
            throws IOException {
        List<Call> body = calls(INPUT, "place", INPUT_PLACE);
        assertTrue(has(body, "updateFromNeighbourShapes"));
        assertTrue(has(body, "setBlock"));
        assertTrue(has(body, "getBlockEntity"));
        assertTrue(has(body, "loadWithComponents"));
        assertTrue(has(allCalls(SET_BLOCK), "place"));
        assertTrue(has(allCalls(SET_BLOCK), "tryClear"));
        assertTrue(has(allCalls(FILL), "place"));
        assertTrue(has(allCalls(FILL), "tryClear"));
    }

    @Test
    void cloneTransportsBeComponentsAndCopiesScheduledBlockTicks()
            throws IOException {
        List<Call> body = allCalls(CLONE);
        assertTrue(has(body, "loadCustomOnly"));
        assertTrue(has(body, "setComponents"));
        assertTrue(has(body, "copyAreaFrom"));
        assertTrue(has(body, "tryClear"));
        assertTrue(has(body, "setBlock"));
        assertTrue(has(body, "blockUpdated"));
    }

    @Test
    void featureWriterHasSeparateWorldGenPermissionGate()
            throws IOException {
        String safe = "(Lnet/minecraft/world/level/WorldGenLevel;" + BLOCK_POS +
                BLOCK_STATE + "Ljava/util/function/Predicate;)V";
        assertTrue(declares(FEATURE, "safeSetBlock", safe));
        assertTrue(has(calls(FEATURE, "safeSetBlock", safe), "setBlock"));
        String place = "(Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;" +
                "Lnet/minecraft/world/level/WorldGenLevel;" +
                "Lnet/minecraft/world/level/chunk/ChunkGenerator;" + RANDOM + BLOCK_POS + ")Z";
        assertTrue(declares(FEATURE, "place", place));
        assertTrue(has(calls(FEATURE, "place", place), "ensureCanWrite"));
    }

    @Test
    void existingPlanetaryRuntimeLevelMixinOnlyCoversBoundRealLevel()
            throws IOException {
        String cls="dev/planetary/mixin/LevelMixin";
        String handler="planetary$setBlock";
        String handlerDesc="(" + BLOCK_POS + BLOCK_STATE + "II" + CI_BOOL + ")V";
        assertTrue(declares(cls, handler, handlerDesc));
        List<Call> body=calls(cls, handler, handlerDesc);
        assertTrue(has(body, "resolve"));
        assertTrue(has(body, "ifPresent"));
        List<String> selectors=new ArrayList<>(), anchors=new ArrayList<>();
        List<Boolean> cancel=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name,
                    String desc, String sig, String[] exceptions) {
                if(!handler.equals(name)||!handlerDesc.equals(desc))return null;
                assertEquals(0,access & Opcodes.ACC_STATIC);
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(String d,boolean visible) {
                        if(!INJECT.equals(d))return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public AnnotationVisitor visitArray(String field) {
                                if(field.equals("method")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String ignored,Object val) {
                                        selectors.add((String)val);
                                    }
                                };
                                if(field.equals("at")) return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public AnnotationVisitor visitAnnotation(String ignored,String d) {
                                        if(!AT.equals(d))return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override public void visit(String p,Object val) {
                                                if(p.equals("value"))anchors.add((String)val);
                                            }
                                        };
                                    }
                                };
                                return null;
                            }
                            @Override public void visit(String field,Object val) {
                                if(field.equals("cancellable"))cancel.add((Boolean)val);
                            }
                        };
                    }
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertEquals(List.of("setBlock"+SET4),selectors);
        assertEquals(List.of("HEAD"),anchors);
        assertEquals(List.of(true),cancel);
        assertTrue(declares(L + "Level","setBlock",SET4));
        assertTrue(declares(GEN,"setBlock",SET4));
    }

    private static boolean has(List<Call> calls,String member) {
        return calls.stream().anyMatch(c -> c.name.equals(member));
    }
    private static long count(List<Call> calls,String owner,String name,String desc,int opcode) {
        return calls.stream().filter(c -> c.owner.equals(owner) &&
                c.name.equals(name)&&c.desc.equals(desc)&&c.opcode==opcode).count();
    }
    private static boolean declares(String cls,String name,String desc) throws IOException {
        return methods(cls).stream().anyMatch(m -> m.name.equals(name)&&m.descriptor.equals(desc));
    }
    private static List<Method> methods(String cls)throws IOException{
        List<Method> result=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access,String n,String d,
                    String sig,String[] ex) {
                result.add(new Method(n,d,access));
                return null;
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return result;
    }
    private static List<Call> allCalls(String cls)throws IOException{
        List<Call> result=new ArrayList<>();
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access,String n,String d,
                    String sig,String[] ex) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode,String owner,
                            String name,String desc,boolean itf) {
                        result.add(new Call(opcode,owner,name,desc));
                    }
                };
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        return result;
    }
    private static List<Call> calls(String cls,String name,String desc)throws IOException{
        List<Call> result=new ArrayList<>();boolean[] found={false};
        read(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access,String n,String d,
                    String sig,String[] ex) {
                if(!name.equals(n)||!desc.equals(d))return null;
                found[0]=true;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode,String owner,
                            String method,String d,boolean itf) {
                        result.add(new Call(opcode,owner,method,d));
                    }
                };
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        assertTrue(found[0],"missing actual target "+cls+"::"+name+desc);
        return result;
    }
    private static ClassReader read(String cls)throws IOException{
        try(InputStream in=Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(cls+".class")){
            assertNotNull(in,"missing class in target NeoForge 21.1.215 "+cls);
            return new ClassReader(in);
        }
    }
}
