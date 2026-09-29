package dev.planetary.mixin;

import dev.planetary.client.PlanetScreenEffectGeometry;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Makes the first-person "inside block" overlay sample around the actual
 * gravity-relative eye instead of vanilla world-Y coordinates.
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererGravityMixin {

    @Inject(
            method = "getOverlayBlock(Lnet/minecraft/world/entity/player/Player;)Lorg/apache/commons/lang3/tuple/Pair;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void planetary$getOverlayBlock(
            Player player,
            CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) player)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        PlanetGravityFrame frame = frameOptional.get();
        Vec3 eye = player.getEyePosition();
        BlockPos.MutableBlockPos pos =
                new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; i++) {
            Vec3 sample =
                    PlanetScreenEffectGeometry.sampleEyeCorner(
                            eye,
                            player.getBbWidth(),
                            player.getScale(),
                            i,
                            frame
                    );

            pos.set(sample.x, sample.y, sample.z);
            BlockState state =
                    player.level().getBlockState(pos);

            if (state.getRenderShape()
                    != RenderShape.INVISIBLE
                    && state.isViewBlocking(
                            player.level(),
                            pos
                    )) {
                cir.setReturnValue(
                        Pair.of(
                                state,
                                pos.immutable()
                        )
                );
                return;
            }
        }

        cir.setReturnValue(null);
    }
}
