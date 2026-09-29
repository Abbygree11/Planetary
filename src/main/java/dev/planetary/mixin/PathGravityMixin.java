package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetGravityEntity;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Path.class)
public abstract class PathGravityMixin {
    @Shadow
    @Final
    private List<Node> nodes;

    /**
     * Vanilla centers path nodes in X/Z and uses integer Y as feet height.
     * Planet local feet instead sit on the local-DOWN face of the air cell.
     */
    @Inject(
            method = "getEntityPosAtNode",
            at = @At("HEAD"),
            cancellable = true
    )
    private void planetary$localNodeAnchor(
            Entity entity,
            int index,
            CallbackInfoReturnable<Vec3> cir
    ) {
        Node node = this.nodes.get(index);

        Optional<PlanetGravityFrame> frame =
                PlanetBlockGravity.frameAt(
                        entity.level(),
                        node.x + 0.5D,
                        node.y + 0.5D,
                        node.z + 0.5D,
                        entity instanceof PlanetGravityEntity gravityEntity
                                ? gravityEntity.planetary$gravityFace().orElse(null)
                                : null
                );

        if (frame.isEmpty()) {
            return;
        }

        PlanetVector up =
                frame.get().worldUp();

        cir.setReturnValue(
                new Vec3(
                        node.x + 0.5D
                                - up.x() * 0.5D,
                        node.y + 0.5D
                                - up.y() * 0.5D,
                        node.z + 0.5D
                                - up.z() * 0.5D
                )
        );
    }
}
