package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

/**
 * Player position packets contain world-space deltas, while Planet entities
 * store movement in their local gravity frame. Reframe the packet delta at
 * the exact ServerPlayer.move boundary, matching the proven GravityChanger
 * approach and keeping client/server collision semantics identical.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerGravityMixin {

    @Shadow
    public ServerPlayer player;

    @ModifyArgs(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;doCheckFallDamage(DDDZ)V"
            )
    )
    private void planetary$worldFallDeltaToLocal(
            Args args
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) player)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return;
        }

        double dx = args.get(0);
        double dy = args.get(1);
        double dz = args.get(2);

        PlanetFrameVector local =
                frameOptional.get().worldToLocal(
                        new PlanetFrameVector(
                                dx,
                                dy,
                                dz
                        )
                );

        args.set(0, local.x());
        args.set(1, local.y());
        args.set(2, local.z());
    }

    @ModifyArg(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"
            ),
            index = 1
    )
    private Vec3 planetary$worldMovementToLocal(
            Vec3 worldMovement
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) player)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y) {
            return worldMovement;
        }

        PlanetFrameVector local =
                frameOptional.get().worldToLocal(
                        new PlanetFrameVector(
                                worldMovement.x,
                                worldMovement.y,
                                worldMovement.z
                        )
                );

        return new Vec3(
                local.x(),
                local.y(),
                local.z()
        );
    }
}
