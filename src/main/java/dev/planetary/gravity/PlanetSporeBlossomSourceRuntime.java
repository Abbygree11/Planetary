package dev.planetary.gravity;

import dev.planetary.world.PlanetBlockFrameContext;
import dev.planetary.world.PlanetLocalBlockOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Minecraft 1.21.1 SporeBlossomBlock.animateTick algorithm, isolated as a
 * version-sensitive integration adapter. The source is reimplemented ONLY
 * on rotated Planet blocks: vanilla is untouched on +Y/non-Planet worlds.
 *
 * <p>The same RNG calls, order and collision predicates are retained.
 * Crucially, each candidate physical cell and its transported sub-cell
 * particle position share the same local chart across Planet seams.</p>
 *
 * <p>Review against vanilla SporeBlossomBlock.animateTick on every port.</p>
 */
public final class PlanetSporeBlossomSourceRuntime {
    private PlanetSporeBlossomSourceRuntime() {
    }

    public static void emit(
            Level level,
            PlanetBlockFrameContext source,
            RandomSource random
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(random, "random");

        BlockPos pos = source.pos();

        // Vanilla first consumes two doubles for one falling blossom
        // particle; the authored height 0.7 is in the LOCAL block cell.
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + random.nextDouble();

        Vec3 falling = PlanetParticleEmitter.transformVanillaLocalEmitter(
                pos, source.frame(), x, y, z
        );
        level.addParticle(
                ParticleTypes.FALLING_SPORE_BLOSSOM,
                falling.x, falling.y, falling.z,
                0.0D, 0.0D, 0.0D
        );

        // Vanilla samples exactly 14 attempted neighboring cells.
        for (int attempt = 0; attempt < 14; attempt++) {
            int localX = Mth.nextInt(random, -10, 10);
            int localY = -random.nextInt(10);
            int localZ = Mth.nextInt(random, -10, 10);

            PlanetBlockFrameContext target = PlanetLocalBlockOffset.traverse(
                    source, localX, localY, localZ
            );
            BlockPos targetPos = target.pos();
            BlockState targetState = level.getBlockState(targetPos);

            // Do not advance RNG for opaque/full-block candidates: vanilla
            // samples jitter ONLY when it will emit an airborne particle.
            if (!targetState.isCollisionShapeFullBlock(level, targetPos)) {
                double jitterX = random.nextDouble();
                double jitterY = random.nextDouble();
                double jitterZ = random.nextDouble();

                Vec3 physical = PlanetParticleEmitter.transformVanillaLocalEmitter(
                        targetPos,
                        target.frame(),
                        targetPos.getX() + jitterX,
                        targetPos.getY() + jitterY,
                        targetPos.getZ() + jitterZ
                );

                level.addParticle(
                        ParticleTypes.SPORE_BLOSSOM_AIR,
                        physical.x, physical.y, physical.z,
                        0.0D, 0.0D, 0.0D
                );
            }
        }
    }
}
