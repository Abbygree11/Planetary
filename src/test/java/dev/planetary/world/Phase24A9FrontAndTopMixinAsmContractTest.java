package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exact NeoForge 21.1.215 classpath owner and compiled Mixin handler checks.
 * Does not claim runtime client transformation or modded gameplay.
 */
final class Phase24A9FrontAndTopMixinAsmContractTest {
    private static final String PLACE =
            "(Lnet/minecraft/world/item/context/BlockPlaceContext;)"
                    + "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String CALLBACK =
            "(Lnet/minecraft/world/item/context/BlockPlaceContext;"
                    + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V";

    @Test
    void actualCrafterAndJigsawOwnersHaveReturnOnlyPlacementAdapters() throws IOException {
        verify("CrafterBlock", "CrafterBlockLocalOrientationMixin",
                "planetary$canonicalCrafterPlacement");
        verify("JigsawBlock", "JigsawBlockLocalOrientationMixin",
                "planetary$canonicalJigsawPlacement");
    }

    private static void verify(String blockName, String mixin, String callback)
            throws IOException {
        String owner = "net/minecraft/world/level/block/" + blockName + ".class";
        AtomicInteger ownerMethod = new AtomicInteger();
        new ClassReader(resource(owner)).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (name.equals("getStateForPlacement") && descriptor.equals(PLACE)) {
                    ownerMethod.incrementAndGet();
                    assertEquals(0, access & Opcodes.ACC_STATIC, blockName);
                }
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertEquals(1, ownerMethod.get(), "Real defining method owner must match");

        AtomicInteger handlerCount = new AtomicInteger();
        AtomicBoolean correctTarget = new AtomicBoolean();
        AtomicBoolean returnHook = new AtomicBoolean();
        AtomicBoolean cancellable = new AtomicBoolean();
        String mixinPath = "dev/planetary/mixin/" + mixin + ".class";
        new ClassReader(resource(mixinPath)).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (!name.equals(callback)) return null;
                handlerCount.incrementAndGet();
                assertEquals(CALLBACK, descriptor, mixin);
                assertEquals(0, access & Opcodes.ACC_STATIC, mixin);
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String descriptor,
                            boolean visible) {
                        if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) {
                            return null;
                        }
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String name, Object value) {
                                if (name.equals("cancellable")) {
                                    cancellable.set(Boolean.TRUE.equals(value));
                                }
                            }

                            @Override
                            public AnnotationVisitor visitArray(String name) {
                                if (name.equals("method")) {
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override public void visit(String unused, Object value) {
                                            if (("getStateForPlacement" + PLACE).equals(value)) {
                                                correctTarget.set(true);
                                            }
                                        }
                                    };
                                }
                                if (name.equals("at")) {
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public AnnotationVisitor visitAnnotation(
                                                String unused, String descriptor) {
                                            if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/At;")) {
                                                return null;
                                            }
                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override public void visit(
                                                        String key, Object value) {
                                                    if (key.equals("value")
                                                            && value.equals("RETURN")) {
                                                        returnHook.set(true);
                                                    }
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
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertEquals(1, handlerCount.get(), mixin);
        assertTrue(correctTarget.get(), mixin + " must target exact ABI");
        assertTrue(returnHook.get(), mixin + " must preserve original RETURN");
        assertTrue(cancellable.get(), mixin + " must permit replacement of only property");

        try (InputStream config = resource("planetary.mixins.json")) {
            String json = new String(config.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"" + mixin + "\""), mixin);
        }
    }

    private static InputStream resource(String resource) throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource);
        if (stream == null) throw new IOException("Missing classpath resource: " + resource);
        return stream;
    }
}
