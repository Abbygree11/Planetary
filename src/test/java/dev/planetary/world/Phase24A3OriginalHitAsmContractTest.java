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
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/** Strict exact NeoForge 21.1.215 selected ABI test, not applied Mixin proof. */
final class Phase24A3OriginalHitAsmContractTest {
    private static final String USE = "net/minecraft/world/item/context/UseOnContext";
    private static final String PLACE = "net/minecraft/world/item/context/BlockPlaceContext";
    private static final String MIXIN = "dev/planetary/mixin/UseOnContextOriginalHitInvoker";
    private static final String SIGNATURE = "()Lnet/minecraft/world/phys/BlockHitResult;";

    @Test
    void useOnContextOwnsProtectedPhysicalHitGetterInheritedByPlacementContext()
            throws IOException {
        boolean[] found = {false};
        reader(USE).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String desc,
                    String signature, String[] exceptions) {
                if ("getHitResult".equals(name) && SIGNATURE.equals(desc)) {
                    found[0] = true;
                    assertTrue((access & Opcodes.ACC_PROTECTED) != 0);
                }
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "NeoForge loaded class must expose getHitResult");
        assertEquals(USE, reader(PLACE).getSuperName());
    }

    @Test
    void compiledMixinHasExactInvokerAndExternalTypedInterface() throws IOException {
        boolean[] extendsInterface = {false};
        boolean[] invoker = {false};
        reader(MIXIN).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public void visit(int version, int access, String name, String signature,
                    String superName, String[] interfaces) {
                extendsInterface[0] = Arrays.asList(interfaces)
                        .contains("dev/planetary/api/PlanetOriginalHitSource");
            }
            @Override public MethodVisitor visitMethod(int access, String name, String desc,
                    String signature, String[] exceptions) {
                if (!"planetary$originalHit".equals(name) || !SIGNATURE.equals(desc)) return null;
                assertEquals(0, access & Opcodes.ACC_STATIC);
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                        if (!"Lorg/spongepowered/asm/mixin/gen/Invoker;".equals(annotation)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public void visit(String key, Object value) {
                                if ("value".equals(key) && "getHitResult".equals(value)) {
                                    invoker[0] = true;
                                }
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(extendsInterface[0]);
        assertTrue(invoker[0]);
        try (InputStream stream = resource("planetary.mixins.json")) {
            String config = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(config.contains("\"UseOnContextOriginalHitInvoker\""));
            assertTrue(config.contains("\"defaultRequire\": 1"));
        }
    }

    private static ClassReader reader(String name) throws IOException {
        try (InputStream input = resource(name + ".class")) {
            return new ClassReader(input);
        }
    }

    private static InputStream resource(String name) {
        InputStream result = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        assertNotNull(result, "Missing actual resolved resource " + name);
        return result;
    }
}
