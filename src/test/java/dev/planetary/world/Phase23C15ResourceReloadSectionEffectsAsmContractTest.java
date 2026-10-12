package dev.planetary.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 2.3C-15 selected *actual classpath* 1.21.1 NeoForge 21.1.215
 * resource, model baking, section geometry, dimension render and current
 * Planet cache ABI. Not transformed Mixins, shaderpack or loaded mod tests.
 */
final class Phase23C15ResourceReloadSectionEffectsAsmContractTest {
    private static final String MC = "net/minecraft/";
    private static final String CLIENT = MC + "client/";
    private static final String RENDER = CLIENT + "renderer/";
    private static final String NF = "net/neoforged/neoforge/client/";
    private static final String MM = CLIENT + "resources/model/ModelManager";
    private static final String SHAPER = RENDER + "block/BlockModelShaper";
    private static final String DISPATCH = RENDER + "block/BlockRenderDispatcher";
    private static final String LEVEL = RENDER + "LevelRenderer";
    private static final String HOOKS = NF + "ClientHooks";
    private static final String EVENT = NF + "event/";
    private static final String SECTION = RENDER + "chunk/SectionCompiler";
    private static final String SECTION_TASK = RENDER + "chunk/SectionRenderDispatcher$RenderSection";
    private static final String ADDITIONAL = EVENT + "AddSectionGeometryEvent";
    private static final String CONTEXT = ADDITIONAL + "$SectionRenderingContext";
    private static final String MOD_DATA = NF + "model/data/ModelDataManager";
    private static final String EFFECTS = NF + "extensions/IDimensionSpecialEffectsExtension";
    private static final String PLANET = "dev/planetary/client/render/";

    @Test void modelReloadRetainsNeoForgeModifyThenBakingCompletedLifecycle() throws IOException {
        assertMethod(MM, "reload");
        assertMethod(MM, "apply");
        assertMethod(SHAPER, "replaceCache");
        assertCalls(MM, "onModifyBakingResult");
        assertCalls(MM, "onModelBake");
        assertCalls(MM, "replaceCache");
        assertMethod(HOOKS, "onModifyBakingResult");
        assertMethod(HOOKS, "onModelBake");
        assertMethod(EVENT + "ModelEvent$ModifyBakingResult", "getModels");
        assertMethod(EVENT + "ModelEvent$BakingCompleted", "getModels");
        assertMethod(EVENT + "ModelEvent$RegisterAdditional", "register");
    }

    @Test void clientResourceReloadListenersAreIndependentOfServerReload() throws IOException {
        assertMethod(EVENT + "RegisterClientReloadListenersEvent", "registerReloadListener",
                "(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V");
        assertMethod(HOOKS, "initClientHooks");
        assertCalls(HOOKS, "registerReloadListener");
        assertMethod(DISPATCH, "onResourceManagerReload");
        assertMethod(LEVEL, "onResourceManagerReload");
        assertMethod(LEVEL, "setLevel");
        assertMethod(LEVEL, "allChanged");
    }

    @Test void additionalSectionGeometryCanBypassPerBlockBakedModelPipeline() throws IOException {
        assertMethod(ADDITIONAL, "addRenderer");
        assertMethod(ADDITIONAL, "getAdditionalRenderers");
        assertMethod(ADDITIONAL, "getLevel");
        assertMethod(CONTEXT, "getRegion");
        assertMethod(CONTEXT, "getOrCreateChunkBuffer");
        assertMethod(CONTEXT, "getQuadLighter");
        assertMethod(SECTION, "compile");
        assertCalls(SECTION, "addAdditionalGeometry");
        assertMethod(SECTION_TASK, "createCompileTask");
        assertCalls(SECTION_TASK, "gatherAdditionalRenderers");
        assertMethod(HOOKS, "gatherAdditionalRenderers");
        assertMethod(HOOKS, "addAdditionalGeometry");
    }

    @Test void dynamicModelDataHasItsOwnRefreshAndWorkerSnapshotLifecycle() throws IOException {
        assertMethod(MOD_DATA, "requestRefresh");
        assertMethod(MOD_DATA, "snapshotSectionRegion");
        assertMethod(MOD_DATA, "getAt");
        assertMethod(EVENT + "RenderLevelStageEvent", "getStage");
        assertMethod(EVENT + "RenderLevelStageEvent$RegisterStageEvent", "register");
    }

    @Test void dimensionSkyWeatherAndModRenderStagesStayIndependent() throws IOException {
        for (String m : List.of("renderSky", "renderClouds", "renderSnowAndRain", "tickRain")) {
            assertMethod(EFFECTS, m);
        }
        assertMethod(LEVEL, "renderSky");
        assertMethod(LEVEL, "renderClouds");
        assertMethod(LEVEL, "tickRain");
        assertCalls(LEVEL, "renderSnowAndRain");
        assertCalls(LEVEL, "dispatchRenderStage");
    }

    @Test void currentPlanetCachesAreNotMistakenForResourceAwareDynamicModCaches() throws IOException {
        for (String name : List.of("MODEL_CACHE", "QUAD_CACHE", "SURFACE_SEAM_MODEL_CACHE")) {
            assertField(PLANET + "PlanetBakedModelRotation", name);
        }
        assertMethod(PLANET + "PlanetBakedModelRotation", "orientForBlock");
        assertField(PLANET + "PlanetBlockRenderCulling", "OCCLUSION_CACHE");
        assertMethod(PLANET + "PlanetBlockRenderCulling", "shouldRenderFace");
        assertMethod(PLANET + "PlanetClientRenderFrame", "stateFrameAt");
        assertMethod(PLANET + "PlanetClientRenderFrame", "candidateFacesAt");
        // Merely asserting these declared owners does NOT prove their cache
        // keys account for ModelData, source position, world or reload epoch.
    }

    private static void assertField(String owner, String field) throws IOException {
        List<String> fields = new ArrayList<>();
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public FieldVisitor visitField(int access, String name, String desc,
                                                       String sig, Object value) {
                fields.add(name);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(fields.contains(field), "Missing actual Planet field " + owner + "." + field);
    }

    private static void assertMethod(String owner, String name) throws IOException {
        assertMethod(owner, name, null);
    }

    private static void assertMethod(String owner, String name, String descriptor) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String method, String desc,
                                                         String sig, String[] exceptions) {
                if (method.equals(name) && (descriptor == null || desc.equals(descriptor))) {
                    found[0] = true;
                }
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing selected target classpath method " + owner +
                "." + name + (descriptor == null ? "" : descriptor));
    }

    private static void assertCalls(String owner, String callee) throws IOException {
        final boolean[] found = {false};
        reader(owner).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int a, String n, String d,
                                                         String s, String[] e) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String calledOwner,
                                                           String name, String desc, boolean itf) {
                        if (name.equals(callee)) found[0] = true;
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        assertTrue(found[0], "Missing selected actual target method invoke " +
                owner + " -> " + callee);
    }

    private static ClassReader reader(String owner) throws IOException {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(owner + ".class")) {
            assertNotNull(input, "Missing Gradle-resolved NeoForge/Planet class " + owner);
            return new ClassReader(input);
        }
    }
}
