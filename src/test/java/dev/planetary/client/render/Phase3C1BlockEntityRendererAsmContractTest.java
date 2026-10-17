package dev.planetary.client.render;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classpath ABI of the real NeoForge 21.1.215 selected BER owners.
 * A compiled callback does not prove that the client Mixin was applied.
 */
final class Phase3C1BlockEntityRendererAsmContractTest {
    private static final String BER = "net/minecraft/client/renderer/blockentity/";
    private static final String MIXIN = "dev/planetary/mixin/";
    private static final String MID =
            "FLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;II";

    private record Owner(String renderer, String entity) {
        String descriptor() {
            return "(Lnet/minecraft/world/level/block/entity/" + entity + ";" + MID + ")V";
        }
        String callback() {
            return "(Lnet/minecraft/world/level/block/entity/" + entity
                    + ";" + MID + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
        }
    }

    private static final List<Owner> OWNERS = List.of(
            new Owner("SignRenderer", "SignBlockEntity"),
            new Owner("HangingSignRenderer", "SignBlockEntity"),
            new Owner("BannerRenderer", "BannerBlockEntity"),
            new Owner("SkullBlockRenderer", "SkullBlockEntity")
    );

    @Test
    void fourExactRenderersEachHaveTwoNarrowCompiledHeadReturnCallbacks()
            throws IOException {
        String config;
        try (InputStream in = resource("planetary.mixins.json")) {
            config = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (Owner owner : OWNERS) {
            assertEquals(1, count(owner.renderer(), BER, "render",
                    owner.descriptor()), owner.renderer() + " declaring owner");
            String mixin = owner.renderer() + "PlanetFrameMixin";
            assertTrue(config.contains("\"" + mixin + "\""), mixin);
            assertEquals(1, count(mixin, MIXIN,
                    "planetary$beginBlockFrame", owner.callback()));
            assertEquals(1, count(mixin, MIXIN,
                    "planetary$finishBlockFrame", owner.callback()));
        }
        assertFalse(config.contains("BlockEntityRenderDispatcherPlanetFrameMixin"),
                "Global BER wrapping would also rotate physical/camera effects");
        assertEquals(1, count("BedRendererGravityMixin", MIXIN,
                "planetary$pushLocalFrame",
                "(Lnet/minecraft/world/level/block/entity/BedBlockEntity;"
                        + MID + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"),
                "Existing bed renderer remains an independent owner");
    }

    private static int count(String owner, String pkg, String name,
            String descriptor) throws IOException {
        int[] found = {0};
        try (InputStream stream = resource(pkg + owner + ".class")) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String method,
                        String desc, String sig, String[] exceptions) {
                    if (method.equals(name) && desc.equals(descriptor)) {
                        assertEquals(0, access & Opcodes.ACC_STATIC, owner + "/" + name);
                        found[0]++;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return found[0];
    }

    private static InputStream resource(String path) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path);
        if (in == null) throw new IOException("Missing compiled resource: " + path);
        return in;
    }
}
