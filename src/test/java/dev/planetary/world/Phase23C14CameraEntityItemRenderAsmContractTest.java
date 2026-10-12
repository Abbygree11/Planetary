package dev.planetary.world;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
 * 2.3C-14 selected exact Gradle-loaded 1.21.1 / NeoForge 21.1.215 render
 * owner ABI and compiled Planet Mixin contracts. Not live Mixin application,
 * loaded third-party renderers, GPU/shaderpack or six-face visual acceptance.
 */
final class Phase23C14CameraEntityItemRenderAsmContractTest {
    private static final String M = "net/minecraft/";
    private static final String CR = M + "client/renderer/";
    private static final String ER = CR + "entity/";
    private static final String BE = CR + "blockentity/";
    private static final String MIX = "dev/planetary/mixin/";
    private static final String CAM = M + "client/Camera";
    private static final String ED = ER + "EntityRenderDispatcher";
    private static final String LR = ER + "LivingEntityRenderer";
    private static final String IR = ER + "ItemRenderer";
    private static final String IH = CR + "ItemInHandRenderer";
    private static final String G = CR + "GameRenderer";
    private static final String LV = CR + "LevelRenderer";
    private static final String FE = ER + "FallingBlockRenderer";
    private static final String PH = BE + "PistonHeadRenderer";
    private static final String IE = ER + "ItemEntityRenderer";
    private static final String IF = ER + "ItemFrameRenderer";
    private static final String MI = ER + "MinecartRenderer";
    private static final String BO = ER + "BoatRenderer";
    private static final String TT = ER + "TntRenderer";
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String REDIRECT = "Lorg/spongepowered/asm/mixin/injection/Redirect;";
    private static final String MODVAR = "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;";
    private static final String MODARGS = "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;";
    private static final String P = "Lcom/mojang/blaze3d/vertex/PoseStack;";
    private static final String MB = "Lnet/minecraft/client/renderer/MultiBufferSource;";
    private static final String E = "Lnet/minecraft/world/entity/Entity;";
    private static final String L = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String V = "Lnet/minecraft/world/phys/Vec3;";
    private static final String BP = "Lnet/minecraft/core/BlockPos;";
    private static final String FB = "Lnet/minecraft/world/entity/item/FallingBlockEntity;";
    private static final String CI = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String E_RENDER = "(" + E + "DDDFF" + P + MB + "I)V";
    private static final String L_RENDER = "(" + L + "FF" + P + MB + "I)V";
    private static final String FB_RENDER = "(" + FB + "FF" + P + MB + "I)V";

    @Test void cameraPhysicalEyeAndLocalViewAreDistinctOwners() throws IOException {
        assertMethod(CAM, "setup", "(Lnet/minecraft/world/level/BlockGetter;" + E + "ZZF)V");
        assertMethod(CAM, "setRotation", "(FFF)V");
        assertMethod(MIX + "CameraGravityMixin", "planetary$applyGravityFrameToRotation", "(FFF" + CI + ")V");
        assertMethod(MIX + "CameraGravityMixin", "planetary$applyGravityFrameToEyePosition",
                "(Lnet/minecraft/world/level/BlockGetter;" + E + "ZZF" + CI + ")V");
        assertEquals(List.of("setRotation(FFF)V"), selector(MIX + "CameraGravityMixin",
                "planetary$applyGravityFrameToRotation", INJECT));
        assertEquals(List.of("setup(Lnet/minecraft/world/level/BlockGetter;" + E + "ZZF)V"),
                selector(MIX + "CameraGravityMixin", "planetary$applyGravityFrameToEyePosition", INJECT));
        assertTrue(callsAny(MIX + "CameraGravityMixin", "cameraQuaternion"));
        assertTrue(callsAny(MIX + "CameraGravityMixin", "eyePosition"));
        assertTrue(callsAny(CAM, "setPosition"));
    }

    @Test void livingBodyAndIndependentEntityWorldEffectsCannotShareBlindPoseRotation() throws IOException {
        assertMethod(ED, "render", E_RENDER);
        assertMethod(LR, "render", L_RENDER);
        assertTrue(callsAny(ED, "renderShadow"));
        assertTrue(callsAny(ED, "renderFlame"));
        assertTrue(callsAny(ED, "renderHitbox"));
        assertTrue(declaresName(ED, "renderBlockShadow"));
        assertMethod(MIX + "LivingEntityRendererGravityMixin", "planetary$rotateLivingBody",
                "(" + L + "FF" + P + MB + "I" + CI + ")V");
        assertEquals(List.of("render" + L_RENDER), selector(MIX + "LivingEntityRendererGravityMixin",
                "planetary$rotateLivingBody", INJECT));
        assertTrue(callsAny(MIX + "LivingEntityRendererGravityMixin", "entityQuaternion"));
        assertMethod(MIX + "PlayerRendererGravityMixin", "planetary$viewVectorInLocalFrame",
                "(Lnet/minecraft/client/player/AbstractClientPlayer;F)" + V);
        assertTrue(callsAny(LV, "renderEntity"));
    }

    @Test void itemsAreRenderedThroughSeveralPositionIndependentOwners() throws IOException {
        assertTrue(declaresName(IR, "renderStatic"));
        assertTrue(declaresName(IR, "render"));
        assertTrue(declaresName(IH, "renderHandsWithItems"));
        assertTrue(declaresName(G, "renderHand"));
        assertTrue(declaresName(IE, "render"));
        assertTrue(declaresName(IF, "render"));
        assertTrue(callsAny(IE, "renderMultipleFromCount"));
        assertTrue(callsAny(IF, "renderStatic"));
        assertTrue(callsAny(IR, "getCustomRenderer"));
        assertTrue(callsAny(IF, "post"), "NeoForge item-frame event path should survive");
        assertTrue(callsAny(G, "renderHandsWithItems"));
    }

    @Test void movingPistonAndVehicleOwnerPathsAreIndependent() throws IOException {
        assertMethod(FE, "render", FB_RENDER);
        assertMethod(MIX + "FallingBlockRendererGravityMixin", "planetary$localRenderBlockPos",
                "(" + BP + FB + "FF" + P + MB + "I)" + BP);
        assertEquals(List.of("render" + FB_RENDER), selector(MIX + "FallingBlockRendererGravityMixin",
                "planetary$localRenderBlockPos", MODVAR));
        assertEquals(List.of("render" + FB_RENDER), selector(MIX + "FallingBlockRendererGravityMixin",
                "planetary$translateFromLocalDownAnchor", MODARGS));
        assertTrue(declaresName(PH, "renderBlock"));
        assertTrue(callsAny(PH, "renderPistonMovedBlocks"),
                "NeoForge custom moving-block hook must not be bypassed");
        for (String owner : List.of(MI, BO, TT)) {
            assertTrue(declaresName(owner, "render"), "Missing independently owned render " + owner);
        }
    }

    @Test void renderStagesAndRegisteredPlanetMixinPolicyRemainIndependent() throws IOException {
        assertTrue(callsAny(LV, "dispatchRenderStage"));
        assertTrue(callsAny(G, "dispatchRenderStage"));
        assertTrue(declaresName("net/neoforged/neoforge/client/event/RenderLevelStageEvent", "getStage"));
        String json;
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("planetary.mixins.json")) {
            assertNotNull(in, "Missing current Planetary Mixin registration");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String registered : List.of("CameraGravityMixin",
                "LivingEntityRendererGravityMixin", "PlayerRendererGravityMixin",
                "ScreenEffectRendererGravityMixin", "FallingBlockRendererGravityMixin")) {
            assertTrue(json.contains("\"" + registered + "\""), "Unregistered active client owner " + registered);
        }
        assertFalse(json.contains("\"EntityRenderDispatcherGravityMixin\""),
                "Global dispatcher rotation must not be re-enabled without duplicate-rotation review");
    }

    private static boolean declaresName(String owner, String name) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                if (n.equals(name)) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return found[0];
    }

    private static void assertMethod(String owner, String name, String desc) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                if (n.equals(name) && d.equals(desc)) found[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Missing actual target classpath method " + owner + "." + name + desc);
    }

    private static boolean callsAny(String owner, String calleeName) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int op, String o, String n, String d, boolean itf) {
                        if (n.equals(calleeName)) found[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return found[0];
    }

    private static List<String> selector(String owner, String name, String annotation) throws IOException {
        List<String> methods = new ArrayList<>();
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                if (!n.equals(name)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public AnnotationVisitor visitAnnotation(String d, boolean visible) {
                        if (!annotation.equals(d)) return null;
                        found[0] = true;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override public AnnotationVisitor visitArray(String key) {
                                if (!"method".equals(key)) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override public void visit(String k, Object value) {
                                        methods.add((String) value);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(found[0], "Missing compiled Planet Mixin injector " + owner + "::" + name);
        return methods;
    }

    private static ClassReader reader(String owner) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(in, "Missing loaded NeoForge/Planet classfile " + owner);
            return new ClassReader(in);
        }
    }
}
