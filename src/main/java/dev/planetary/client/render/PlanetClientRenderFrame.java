package dev.planetary.client.render;

import dev.planetary.world.PlanetBlockRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.world.PlanetBlockStateFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the canonical Planet block frame from client rendering contexts.
 *
 * <p>Chunk compilation commonly supplies a render-region wrapper rather than
 * the ClientLevel itself, so the currently bound ClientLevel is the
 * authoritative runtime fallback.</p>
 */
public final class PlanetClientRenderFrame {
    private PlanetClientRenderFrame() {
    }

    public static Optional<PlanetBlockStateFrame> stateFrameAt(
            BlockGetter getter,
            BlockPos pos
    ) {
        Objects.requireNonNull(getter, "getter");
        Objects.requireNonNull(pos, "pos");

        if (getter instanceof Level level) {
            return PlanetBlockRuntime.stateFrameAt(
                    level,
                    pos
            );
        }

        ClientLevel level =
                Minecraft.getInstance().level;
        if (level == null) {
            return Optional.empty();
        }

        return PlanetBlockRuntime.stateFrameAt(
                level,
                pos
        );
    }

    public static Set<PlanetFace> candidateFacesAt(
            BlockGetter getter,
            BlockPos pos
    ) {
        Objects.requireNonNull(getter, "getter");
        Objects.requireNonNull(pos, "pos");

        Level level;
        if (getter instanceof Level directLevel) {
            level = directLevel;
        } else {
            level = Minecraft.getInstance().level;
        }

        if (level == null) {
            return Set.of();
        }

        return PlanetBlockRuntime.fieldAt(
                level,
                pos
        ).map(field ->
                Set.copyOf(
                        field.candidateFaces(
                                pos.getX(),
                                pos.getY(),
                                pos.getZ()
                        )
                )
        ).orElseGet(Set::of);
    }
}
