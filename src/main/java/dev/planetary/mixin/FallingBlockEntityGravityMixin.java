package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import dev.planetary.gravity.PlanetFallingBlockSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Falling-block local gravity semantics.
 */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityGravityMixin {
    /**
     * Vanilla constructs a falling block at (x + 0.5, y, z + 0.5), i.e. at
     * the center of the source block's physical world-DOWN face.
     *
     * <p>Generalize that anchor to the center of the source block's local-DOWN
     * face before the entity is added to the level. This keeps its entity
     * position, rotated AABB and rendered block centered on the same physical
     * source cell on every Planet face.</p>
     */
    @ModifyArgs(
            method = "fall(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/entity/item/FallingBlockEntity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private static void planetary$anchorOnLocalDownFace(
            Args args,
            Level level,
            BlockPos sourcePos,
            BlockState state
    ) {
        Direction down =
                PlanetBlockGravity.localDown(
                        level,
                        sourcePos
                );

        if (down == Direction.DOWN) {
            return;
        }

        Vec3 anchor =
                PlanetFallingBlockSpawn.anchor(
                        sourcePos,
                        down
                );

        args.set(1, anchor.x);
        args.set(2, anchor.y);
        args.set(3, anchor.z);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos planetary$localBelow(
            BlockPos pos
    ) {
        FallingBlockEntity self =
                (FallingBlockEntity) (Object) this;

        return pos.relative(
                PlanetBlockGravity.localDown(
                        self.level(),
                        pos
                )
        );
    }
}
