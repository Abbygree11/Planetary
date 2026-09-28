package dev.planetary.chunk;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

final class MinecraftTestBootstrap {
    private static boolean bootstrapped;

    private MinecraftTestBootstrap() {
    }

    static synchronized void ensureBootstrapped() {
        if (bootstrapped) {
            return;
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        bootstrapped = true;
    }
}
