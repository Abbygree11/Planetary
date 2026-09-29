package dev.planetary.mixin;

import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.gravity.PlanetPlayerMovement;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Makes server-side player jump recognition use local UP instead of world +Y.
 *
 * <p>Vanilla recognizes a jump packet only when the incoming Y coordinate
 * rises. On a side face a perfectly normal jump changes X or Z instead, so the
 * server otherwise never calls jumpFromGround for that movement.</p>
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerGravityMixin {
    private static final double PLANETARY_JUMP_EPSILON =
            1.0E-5D;

    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
            at = @At("HEAD")
    )
    private void planetary$recognizeLocalJump(
            ServerboundMovePlayerPacket packet,
            CallbackInfo ci
    ) {
        Optional<PlanetGravityFrame> frameOptional =
                ((PlanetGravityEntity) player)
                        .planetary$gravityFrame();

        if (frameOptional.isEmpty()
                || frameOptional.get().face()
                == PlanetFace.POS_Y
                || !player.onGround()
                || packet.isOnGround()) {
            return;
        }

        Vec3 movement = new Vec3(
                packet.getX(player.getX()) - player.getX(),
                packet.getY(player.getY()) - player.getY(),
                packet.getZ(player.getZ()) - player.getZ()
        );

        if (PlanetPlayerMovement.isMovingUp(
                movement,
                frameOptional.get(),
                PLANETARY_JUMP_EPSILON
        )) {
            player.jumpFromGround();
        }
    }
}
