package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks actual Gradle-resolved NeoForge 21.1.215 declarers and the
 * compiled Planetary owner callbacks. This does not weave Mixins in a game.
 */
final class Phase24A10Rotation16OwnerAsmContractTest {
    private static final String CONTEXT =
            "(Lnet/minecraft/world/item/context/BlockPlaceContext;)"
                    + "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String HANDLER =
            "(Lnet/minecraft/world/item/context/BlockPlaceContext;"
                    + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V";
    private static final String VANILLA = "net/minecraft/world/level/block/";
    private static final String MIXIN = "dev/planetary/mixin/";

    private static final List<String> OWNER_CLASSES = List.of(
            "StandingSignBlock", "BannerBlock", "SkullBlock", "CeilingHangingSignBlock");
    private static final List<String> MIXINS = List.of(
            "StandingSignBlockRotation16Mixin", "BannerBlockRotation16Mixin",
            "SkullBlockRotation16Mixin", "CeilingHangingSignBlockRotation16Mixin");
    private static final List<String> SUPPORT = List.of(
            "StandingSignBlockLocalSupportMixin", "BannerBlockLocalSupportMixin",
            "CeilingHangingSignBlockLocalSupportMixin");

    @Test
    void allFourRealOwnerMethodsDeclareTheirOwnOneStateWriter() throws IOException {
        for (String owner : OWNER_CLASSES) {
            assertEquals(1, countMethod(VANILLA + owner + ".class",
                    "getStateForPlacement", CONTEXT), owner);
        }
    }

    @Test
    void allFourNarrowReturnAdaptersAndSupportMixinsCompiledAndRegistered()
            throws IOException {
        String config;
        try (InputStream in = resource("planetary.mixins.json")) {
            config = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String mixin : MIXINS) {
            assertTrue(config.contains("\"" + mixin + "\""), mixin);
            assertEquals(1, countMethod(MIXIN + mixin + ".class",
                    "planetary$canonical16AnglePlacement", HANDLER), mixin);
        }
        for (String mixin : SUPPORT) {
            assertTrue(config.contains("\"" + mixin + "\""), mixin);
        }
        assertEquals(1, countMethod(MIXIN + "StandingSignBlockLocalSupportMixin.class",
                "planetary$localSupportBelow",
                "(Lnet/minecraft/core/BlockPos;"
                + "Lnet/minecraft/world/level/block/state/BlockState;"
                + "Lnet/minecraft/world/level/LevelReader;"
                + "Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"));
        assertEquals(1, countMethod(MIXIN + "BannerBlockLocalSupportMixin.class",
                "planetary$localSupportBelow",
                "(Lnet/minecraft/core/BlockPos;"
                + "Lnet/minecraft/world/level/block/state/BlockState;"
                + "Lnet/minecraft/world/level/LevelReader;"
                + "Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"));
    }

    private static int countMethod(String path, String sought, String desc)
            throws IOException {
        int[] count = {0};
        try (InputStream in = resource(path)) {
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name,
                        String descriptor, String signature, String[] exceptions) {
                    if (name.equals(sought) && descriptor.equals(desc)) {
                        assertEquals(0, access & Opcodes.ACC_STATIC,
                                "Instance-only " + path + "#" + sought);
                        count[0]++;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                    | ClassReader.SKIP_FRAMES);
        }
        return count[0];
    }

    private static InputStream resource(String path) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path);
        if (in == null) throw new IOException("No compiled class " + path);
        return in;
    }
}
