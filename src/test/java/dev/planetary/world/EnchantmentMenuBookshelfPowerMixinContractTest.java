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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NeoForge 1.21.1-specific contract: menu enchantment power must read the
 * same projected provider position as EnchantingTableBlock.isValidBookShelf.
 *
 * <p>Read raw class resources; never directly classload a class inside the
 * mixin-reserved dev.planetary.mixin namespace.</p>
 */
final class EnchantmentMenuBookshelfPowerMixinContractTest {
    private static final String MENU =
            "net/minecraft/world/inventory/EnchantmentMenu";
    private static final String MIXIN =
            "dev/planetary/mixin/EnchantmentMenuBookshelfPowerMixin";
    private static final String LAMBDA = "lambda$slotsChanged$0";
    private static final String LAMBDA_DESC =
            "(Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;)V";
    private static final String OFFSET =
            "Lnet/minecraft/core/BlockPos;offset("
                    + "Lnet/minecraft/core/Vec3i;)"
                    + "Lnet/minecraft/core/BlockPos;";
    private static final String CALLBACK_DESC =
            "(Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/core/Vec3i;"
                    + "Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;)"
                    + "Lnet/minecraft/core/BlockPos;";

    @Test
    void neoForgeMenuBonusBodyContainsExactlyTwoIdenticalProviderOffsets()
            throws IOException {
        List<String> offsets = new ArrayList<>();
        List<String> bonusInvocations = new ArrayList<>();
        List<String> predicateInvocations = new ArrayList<>();
        boolean[] lambdaFound = {false};
        boolean[] instance = {false};

        try (InputStream in = open(MENU + ".class")) {
            new ClassReader(in).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(
                                int access,
                                String name,
                                String descriptor,
                                String signature,
                                String[] exceptions
                        ) {
                            if (!LAMBDA.equals(name)
                                    || !LAMBDA_DESC.equals(descriptor)) {
                                return null;
                            }
                            lambdaFound[0] = true;
                            instance[0] = (access & Opcodes.ACC_STATIC) == 0;

                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitMethodInsn(
                                        int opcode,
                                        String owner,
                                        String method,
                                        String descriptor,
                                        boolean isInterface
                                ) {
                                    if ("offset".equals(method)) {
                                        offsets.add("L" + owner + ";"
                                                + method + descriptor);
                                    }
                                    if ("getEnchantPowerBonus".equals(method)) {
                                        bonusInvocations.add(
                                                owner + "." + method
                                        );
                                    }
                                    if ("isValidBookShelf".equals(method)) {
                                        predicateInvocations.add(
                                                owner + "." + method
                                        );
                                    }
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
            );
        }

        assertTrue(lambdaFound[0], "EnchantmentMenu lambda descriptor changed");
        assertTrue(instance[0], "EnchantmentMenu lambda is no longer instance");
        assertEquals(List.of(OFFSET, OFFSET), offsets,
                "Both NeoForge bonus positions must be reframed together");
        assertEquals(1, bonusInvocations.size(),
                "NeoForge numeric enchantment power must stay enabled");
        assertEquals(1, predicateInvocations.size(),
                "Shared isValidBookShelf predicate must remain in use");
    }

    @Test
    void compiledMixinHasExactRedirectSignatureAndInjectionAnchor()
            throws IOException {
        List<String> mixinTargets = new ArrayList<>();
        List<String> methodTargets = new ArrayList<>();
        List<String> atTargets = new ArrayList<>();
        List<String> atValues = new ArrayList<>();
        List<String> descriptors = new ArrayList<>();
        List<Boolean> isStatic = new ArrayList<>();

        try (InputStream in = open(MIXIN + ".class")) {
            new ClassReader(in).accept(
                    new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(
                                String descriptor,
                                boolean visible
                        ) {
                            if (!"Lorg/spongepowered/asm/mixin/Mixin;"
                                    .equals(descriptor)) {
                                return null;
                            }
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public AnnotationVisitor visitArray(String name) {
                                    if (!"value".equals(name)) {
                                        return null;
                                    }
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof Type type) {
                                                mixinTargets.add(type.getInternalName());
                                            }
                                        }
                                    };
                                }
                            };
                        }

                        @Override
                        public MethodVisitor visitMethod(
                                int access,
                                String name,
                                String descriptor,
                                String signature,
                                String[] exceptions
                        ) {
                            if (!"planetary$projectBookcaseBonusPosition"
                                    .equals(name)) {
                                return null;
                            }
                            descriptors.add(descriptor);
                            isStatic.add((access & Opcodes.ACC_STATIC) != 0);

                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public AnnotationVisitor visitAnnotation(
                                        String descriptor,
                                        boolean visible
                                ) {
                                    if (!"Lorg/spongepowered/asm/mixin/"
                                            .concat("injection/Redirect;")
                                            .equals(descriptor)) {
                                        return null;
                                    }

                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public AnnotationVisitor visitArray(
                                                String field
                                        ) {
                                            if (!"method".equals(field)) {
                                                return null;
                                            }
                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override
                                                public void visit(
                                                        String ignored,
                                                        Object value
                                                ) {
                                                    methodTargets.add((String) value);
                                                }
                                            };
                                        }

                                        @Override
                                        public void visit(
                                                String field,
                                                Object value
                                        ) {
                                            if ("method".equals(field)) {
                                                methodTargets.add((String) value);
                                            }
                                        }

                                        @Override
                                        public AnnotationVisitor visitAnnotation(
                                                String field, String descriptor
                                        ) {
                                            if (!"at".equals(field)
                                                    || !"Lorg/spongepowered/asm/"
                                                    .concat("mixin/injection/At;")
                                                    .equals(descriptor)) {
                                                return null;
                                            }

                                            return new AnnotationVisitor(Opcodes.ASM9) {
                                                @Override
                                                public void visit(
                                                        String field,
                                                        Object value
                                                ) {
                                                    if ("value".equals(field)) {
                                                        atValues.add((String) value);
                                                    } else if ("target".equals(field)) {
                                                        atTargets.add((String) value);
                                                    }
                                                }
                                            };
                                        }
                                    };
                                }
                            };
                        }
                    },
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                            | ClassReader.SKIP_FRAMES
            );
        }

        assertEquals(List.of(MENU), mixinTargets);
        assertEquals(List.of(CALLBACK_DESC), descriptors);
        assertEquals(List.of(false), isStatic);
        assertEquals(List.of(LAMBDA), methodTargets);
        assertEquals(List.of("INVOKE"), atValues);
        assertEquals(List.of(OFFSET), atTargets);

        try (InputStream in = open("planetary.mixins.json")) {
            String config = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(config.contains(
                    "\"EnchantmentMenuBookshelfPowerMixin\""
            ));
            assertTrue(config.contains("\"defaultRequire\": 1"));
        }
    }

    private static InputStream open(String resource) {
        InputStream in = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resource);
        assertNotNull(in, "Missing class resource: " + resource);
        return in;
    }
}
