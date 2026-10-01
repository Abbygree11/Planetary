package dev.planetary.debug;

import dev.planetary.api.PlanetFrameApi;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/**
 * Runtime stress harness for the common third-party pattern
 * worldPosition.relative(blockStateLocalFacing).
 */
public final class PlanetCompatibilityDiagnostics {
    private PlanetCompatibilityDiagnostics() {
    }

    public static Result verify(ServerLevel level) {
        Objects.requireNonNull(level, "level");

        PlanetGravityField field =
                PlanetGravityRuntime.find(level)
                        .orElseThrow();

        int checks = 0;
        int rawMismatches = 0;

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos source =
                    oneBlockFromCore(field, face);

            for (Direction localDirection : Direction.values()) {
                PlanetFrameApi.BlockNeighbor neighbor =
                        PlanetFrameApi.localNeighbor(
                                level,
                                source,
                                localDirection
                        ).orElseThrow();

                BlockPos expected =
                        source.relative(
                                neighbor.physicalDirection()
                        );
                if (!expected.equals(neighbor.targetPos())) {
                    throw new IllegalStateException(
                            "PlanetFrameApi neighbor lost physical adjacency at "
                                    + face + " / " + localDirection
                    );
                }

                BlockPos rawForeignTarget =
                        source.relative(localDirection);
                if (!rawForeignTarget.equals(neighbor.targetPos())) {
                    rawMismatches++;
                }

                checks++;
            }
        }

        if (rawMismatches == 0) {
            throw new IllegalStateException(
                    "Expected raw BlockPos.relative mismatches on rotated faces"
            );
        }

        return new Result(checks, rawMismatches);
    }

    private static BlockPos oneBlockFromCore(
            PlanetGravityField field,
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up = frame.worldUp();

        return new BlockPos(
                field.core().x() + up.x(),
                field.core().y() + up.y(),
                field.core().z() + up.z()
        );
    }

    public record Result(
            int neighborChecks,
            int rawRelativeMismatches
    ) {
    }
}
