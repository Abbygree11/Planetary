package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Target NeoForge 21.1.215 selected ASM contract; not a substitute for a
 * running client/server Mixin weave or loaded third-party mod acceptance.
 */
final class Phase24A4TrapdoorPlacementAsmContractTest {
    private static final String B = "net/minecraft/world/level/block/";
    private static final String I = "net/minecraft/world/item/";
    private static final String M = "dev/planetary/mixin/TrapDoorBlockLocalPlacementMixin";
    private static final String PLACE = "Lnet/minecraft/world/item/context/BlockPlaceContext;";
    private static final String STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String GET = "(" + PLACE + ")" + STATE;
    private static final String CALLBACK = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";

    @Test
    void oneInheritedTrapdoorAuthorCoversWoodIronAndUnwaxedCopper() throws IOException {
        assertTrue(declares(B+"TrapDoorBlock", "getStateForPlacement", GET));
        assertFalse(declares(B+"HorizontalDirectionalBlock", "getStateForPlacement", GET));
        assertFalse(declares(B+"WeatheringCopperTrapDoorBlock", "getStateForPlacement", GET));
        // BlockItem remains the normal item dispatcher; keep its virtual
        // getPlacementState path and NeoForge custom callback chain.
        assertTrue(declares(I+"BlockItem", "getPlacementState", GET));
        assertTrue(calls(I+"BlockItem", "getPlacementState", GET, "getStateForPlacement"));
        assertTrue(declares(B+"TrapDoorBlock", "neighborChanged", null));
        assertTrue(declares(B+"TrapDoorBlock", "updateShape", null));
        assertTrue(declares(B+"WeatheringCopperTrapDoorBlock", "randomTick", null));
    }

    @Test
    void compiledMixinTargetsOnlyTrapdoorPlacementReturnAndIsCancellable() throws IOException {
        boolean[] target = {false};
        boolean[] injection = {false};
        boolean[] atReturn = {false};
        boolean[] cancel = {false};
        boolean[] method = {false};
        reader(M).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!"Lorg/spongepowered/asm/mixin/Mixin;".equals(desc)) return null;
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitArray(String name) {
                        if (!"value".equals(name)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public void visit(String name, Object value) {
                                if (value instanceof Type type
                                        && type.getInternalName().equals(B+"TrapDoorBlock")) target[0]=true;
                            }
                        };
                    }
                };
            }
            @Override public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                if (!"planetary$targetCanonicalTrapdoor".equals(name)) return null;
                assertEquals("("+PLACE+CALLBACK+")V",descriptor);
                assertEquals(0, access & Opcodes.ACC_STATIC);
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        if (!"Lorg/spongepowered/asm/mixin/injection/Inject;".equals(desc)) return null;
                        injection[0]=true;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public void visit(String key, Object value) {
                                if ("cancellable".equals(key) && Boolean.TRUE.equals(value))cancel[0]=true;
                            }
                            @Override public AnnotationVisitor visitArray(String key) {
                                if ("method".equals(key)) {
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public void visit(String unused, Object value) {
                                            if (("getStateForPlacement"+GET).equals(value))method[0]=true;
                                        }
                                    };
                                }
                                if ("at".equals(key)) {
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public AnnotationVisitor visitAnnotation(String unused, String desc) {
                                            if (!"Lorg/spongepowered/asm/mixin/injection/At;".equals(desc)) return null;
                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override public void visit(String n, Object value) {
                                                    if ("value".equals(n) && "RETURN".equals(value))atReturn[0]=true;
                                                }
                                            };
                                        }
                                    };
                                }
                                return null;
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(target[0],"Mixin target trapdoor");
        assertTrue(injection[0],"@Inject");
        assertTrue(cancel[0],"cancellable=true");
        assertTrue(method[0],"exact descriptor");
        assertTrue(atReturn[0],"RETURN, never HEAD");
        try (InputStream input=resource("planetary.mixins.json")) {
            String config=new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(config.contains("\"TrapDoorBlockLocalPlacementMixin\""));
            assertTrue(config.contains("\"defaultRequire\": 1"));
        }
    }

    private static boolean declares(String clazz,String name,String desc)throws IOException {
        boolean[] found={false};
        reader(clazz).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access,String n,String d,
                    String sig,String[] exceptions) {
                if(n.equals(name) && (desc==null||d.equals(desc)))found[0]=true;
                return null;
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_FRAMES|ClassReader.SKIP_DEBUG);
        return found[0];
    }

    private static boolean calls(String clazz,String name,String desc,String invoked) throws IOException {
        boolean[] found={false};
        reader(clazz).accept(new ClassVisitor(Opcodes.ASM9){
            @Override public MethodVisitor visitMethod(int access,String n,String d,
                    String sig,String[] exceptions) {
                if(!name.equals(n)||!desc.equals(d))return null;
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public void visitMethodInsn(int opcode,String owner,String method,String descriptor,boolean itf){
                        if(method.equals(invoked))found[0]=true;
                    }
                };
            }
        },ClassReader.SKIP_FRAMES|ClassReader.SKIP_DEBUG);
        return found[0];
    }

    private static ClassReader reader(String path)throws IOException {
        try(InputStream in=resource(path+".class")) {return new ClassReader(in);}
    }
    private static InputStream resource(String path) {
        InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        assertNotNull(in,"Missing actual NeoForge class "+path);
        return in;
    }
}
