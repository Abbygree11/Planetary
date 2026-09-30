package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetParticleSpawn;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Makes ambient fluid drip emission use local DOWN instead of global -Y.
 *
 * <p>The first implementation intentionally handles the common full-block
 * case (water above/inside ordinary solid blocks). More complex partial-shape
 * projection belongs to the general local block-shape layer.</p>
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelFluidDripGravityMixin {
    @Redirect(
            method = "doAnimateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos planetary$fluidLocalBelow(
            BlockPos pos
    ) {
        ClientLevel self =
                (ClientLevel) (Object) this;

        return pos.relative(
                PlanetBlockGravity.localDown(
                        self,
                        pos
                )
        );
    }

    @Inject(
            method = "trySpawnDripParticles",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$spawnDripOnLocalDownFace(
            BlockPos blockPos,
            BlockState blockState,
            ParticleOptions particleData,
            boolean shapeDownSolid,
            CallbackInfo ci
    ) {
        ClientLevel self =
                (ClientLevel) (Object) this;

        Optional<PlanetGravityFrame> frameOptional =
                PlanetBlockGravity.frameAt(
                        self,
                        blockPos
                );

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        ci.cancel();

        if (!blockState.getFluidState().isEmpty()
                || blockState.is(BlockTags.IMPERMEABLE)) {
            return;
        }

        VoxelShape shape =
                blockState.getCollisionShape(
                        self,
                        blockPos
                );

        if (shape.isEmpty()) {
            return;
        }

        Direction down =
                PlanetBlockGravity.localDown(
                        self,
                        blockPos,
                        frameOptional.get().face()
                );

        BlockPos beyond =
                blockPos.relative(down);
        BlockState beyondState =
                self.getBlockState(beyond);

        if (!beyondState.getFluidState().isEmpty()
                || beyondState.isFaceSturdy(
                        self,
                        beyond,
                        down.getOpposite()
                )) {
            return;
        }

        PlanetParticleSpawn.spawnOnLocalDownFace(
                self,
                blockPos,
                self.random,
                particleData
        );
    }
}
