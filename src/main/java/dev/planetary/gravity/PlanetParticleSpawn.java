package dev.planetary.gravity;

import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Particle emission helpers whose "bottom face" follows local gravity.
 */
public final class PlanetParticleSpawn {
    private PlanetParticleSpawn() {
    }

    public static boolean spawnOnLocalDownFace(
            Level level,
            BlockPos pos,
            RandomSource random,
            ParticleOptions particle
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(level, pos);

        if (frameOptional.isEmpty()) {
            return false;
        }

        PlanetGravityFrame frame = frameOptional.get();
        PlanetVector down = frame.worldDown();
        PlanetVector east =
                frame.worldAxis(PlanetDirection.EAST);
        PlanetVector south =
                frame.worldAxis(PlanetDirection.SOUTH);

        double tangentX = random.nextDouble() - 0.5D;
        double tangentZ = random.nextDouble() - 0.5D;

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        double faceOffset = 0.55D;

        level.addParticle(
                particle,
                centerX
                        + down.x() * faceOffset
                        + east.x() * tangentX
                        + south.x() * tangentZ,
                centerY
                        + down.y() * faceOffset
                        + east.y() * tangentX
                        + south.y() * tangentZ,
                centerZ
                        + down.z() * faceOffset
                        + east.z() * tangentX
                        + south.z() * tangentZ,
                0.0D,
                0.0D,
                0.0D
        );

        return true;
    }
}
