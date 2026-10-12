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
 * Stage 2.3C-13: strictly selected compiled target 1.21.1 / NeoForge 21.1.215
 * client dispatch, BER, BEWLR, custom fluids and Planet's BedRenderer Mixin.
 * This test cannot validate shaderpacks, GPU instancing or transformed Mixins.
 */
final class Phase23C13AlternateRenderPipelinesAsmContractTest {
    private static final String MC = "net/minecraft/";
    private static final String BLOCK = MC + "client/renderer/block/";
    private static final String BE = MC + "client/renderer/blockentity/";
    private static final String NF = "net/neoforged/neoforge/";
    private static final String DISPATCH = BLOCK + "BlockRenderDispatcher";
    private static final String LIQUID = BLOCK + "LiquidBlockRenderer";
    private static final String BE_DISPATCH = BE + "BlockEntityRenderDispatcher";
    private static final String BE_RENDER = BE + "BlockEntityRenderer";
    private static final String BED = BE + "BedRenderer";
    private static final String EXT = NF + "client/extensions/common/IClientFluidTypeExtensions";
    private static final String EVENT = NF + "client/event/EntityRenderersEvent$RegisterRenderers";
    private static final String BEWLR = MC + "client/renderer/BlockEntityWithoutLevelRenderer";
    private static final String PLANET = "dev/planetary/mixin/BedRendererGravityMixin";
    private static final String P = "Lcom/mojang/blaze3d/vertex/PoseStack;";
    private static final String MB = "Lnet/minecraft/client/renderer/MultiBufferSource;";
    private static final String BC = "Lnet/minecraft/world/level/block/entity/BlockEntity;";
    private static final String BBE = "Lnet/minecraft/world/level/block/entity/BedBlockEntity;";
    private static final String TINT = "Lnet/minecraft/world/level/BlockAndTintGetter;";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String FLUID = "Lnet/minecraft/world/level/material/FluidState;";
    private static final String VC = "Lcom/mojang/blaze3d/vertex/VertexConsumer;";
    private static final String BED_RENDER = "(" + BBE + "F" + P + MB + "II)V";
    private static final String FLUID_RENDER = "(" + FLUID + TINT + POS + VC + STATE + ")Z";
    private static final String DISPATCH_LIQUID = "(" + POS + TINT + VC + STATE + FLUID + ")V";
    private static final String MIXIN_INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";

    @Test void blockEntityRendererIsIndependentDynamicDispatch() throws IOException {
        assertMethod(BE_RENDER, "render", "(" + BC + "F" + P + MB + "II)V");
        assertMethod(BE_DISPATCH, "render", "(" + BC + "F" + P + MB + ")V");
        assertTrue(invokesAny(BE_DISPATCH, "getRenderer"));
        assertTrue(invokesAny(BE_DISPATCH, "render"));
        assertMethod(BED, "render", BED_RENDER);
        assertTrue(invokesAny(BED, "renderPiece"));
        assertMethod(EVENT, "registerBlockEntityRenderer",
                "(Lnet/minecraft/world/level/block/entity/BlockEntityType;" +
                "Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider;)V");
        assertTrue(invokesAny(EVENT, "register"), "Mod BER registration must retain its dispatch");
    }

    @Test void itemCustomRendererHasSeparateNoLevelOwner() throws IOException {
        assertMethod(BEWLR, "renderByItem",
                "(Lnet/minecraft/world/item/ItemStack;" +
                "Lnet/minecraft/world/item/ItemDisplayContext;" + P + MB + "II)V");
        assertTrue(invokesAny(DISPATCH, "getCustomRenderer"),
                "NeoForge animated-block item path must retain IClientItemExtensions");
        assertTrue(invokesAny(DISPATCH, "renderByItem"));
    }

    @Test void customFluidHookCanBypassVanillaLiquidTessellation() throws IOException {
        assertMethod(DISPATCH, "renderLiquid", DISPATCH_LIQUID);
        assertMethod(EXT, "renderFluid", FLUID_RENDER);
        assertTrue(invokesAny(DISPATCH, "renderFluid"),
                "NeoForge fluid replacement hook lost");
        assertTrue(invokesAny(DISPATCH, "tesselate"),
                "Ordinary fluid fallback lost");
        assertMethod(LIQUID, "tesselate", "(" + TINT + POS + VC + STATE + FLUID + ")V");
        assertMethod(LIQUID, "shouldRenderFace",
                "(" + TINT + POS + FLUID + STATE +
                "Lnet/minecraft/core/Direction;" + STATE + ")Z");
        assertTrue(invokesAny(LIQUID, "getFluidSprites"), "Mod fluid sprites not used");
        assertTrue(invokesAny(LIQUID, "getTintColor"), "Mod fluid tint not used");
    }

    @Test void planetBedOnlyHooksExactBedRendererNotUniversalBer() throws IOException {
        assertMethod(PLANET, "planetary$pushLocalFrame",
                "(" + BBE + "F" + P + MB + "IILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V");
        assertMethod(PLANET, "planetary$popLocalFrame",
                "(" + BBE + "F" + P + MB + "IILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V");
        assertEquals(List.of("render" + BED_RENDER),
                injectedSelectors("planetary$pushLocalFrame"));
        assertEquals(List.of("render" + BED_RENDER),
                injectedSelectors("planetary$popLocalFrame"));
        assertTrue(invokesAny(PLANET, "applyAroundBlockCenter"));
        // No assertion that other BERs, shaderpack passes, or Flywheel inherit this hook.
    }

    private static void assertMethod(String owner, String name, String desc) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                                                         String s, String[] ex) {
                if (name.equals(n) && desc.equals(d)) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Missing actual classpath owner " + owner + "." + name + desc);
    }

    private static boolean invokesAny(String owner, String target) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                                                         String s, String[] ex) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int op, String o, String n,
                                                           String d, boolean itf) {
                        if (n.equals(target)) found[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return found[0];
    }

    private static List<String> injectedSelectors(String handler) throws IOException {
        List<String> selectors = new ArrayList<>();
        final boolean[] found = {false};
        reader(PLANET).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                                                         String s, String[] ex) {
                if (!handler.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(String d, boolean visible) {
                        if (!MIXIN_INJECT.equals(d)) return null;
                        found[0] = true;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public AnnotationVisitor visitArray(String key) {
                                if (!"method".equals(key)) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String k, Object value) {
                                        selectors.add((String) value);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Missing compiled @Inject for " + handler);
        return selectors;
    }

    private static ClassReader reader(String owner) throws IOException {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(input, "Missing Gradle target-version class " + owner);
            return new ClassReader(input);
        }
    }
}
