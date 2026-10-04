package dev.planetary.mixin;

import dev.planetary.gravity.PlanetBlockGravity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Falling-block local gravity and focused pointed-dripstone damage diagnostics.
 */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityGravityMixin {
    @Shadow
    private BlockState blockState;

    @Shadow
    private boolean hurtEntities;

    @Shadow
    private float fallDamagePerDistance;

    @Shadow
    private int fallDamageMax;

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

    @Inject(
            method = "causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("HEAD")
    )
    private void planetary$tracePointedDripstoneDamage(
            float fallDistance,
            float damageMultiplier,
            DamageSource damageSource,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!blockState.is(Blocks.POINTED_DRIPSTONE)) {
            return;
        }

        FallingBlockEntity self =
                (FallingBlockEntity) (Object) this;
        if (self.level().isClientSide) {
            return;
        }

        Predicate<Entity> eligiblePredicate =
                EntitySelector.NO_CREATIVE_OR_SPECTATOR
                        .and(
                                EntitySelector.LIVING_ENTITY_STILL_ALIVE
                        );

        List<Entity> eligibleTargets =
                self.level().getEntities(
                        self,
                        self.getBoundingBox(),
                        eligiblePredicate
                );

        List<Entity> nearbyLiving =
                self.level().getEntities(
                        self,
                        self.getBoundingBox().inflate(1.0D),
                        EntitySelector.LIVING_ENTITY_STILL_ALIVE
                );

        System.out.println(
                "[Planetary/FallTrace] causeFallDamage"
                        + " id=" + self.getId()
                        + " arg=" + fallDistance
                        + " multiplier=" + damageMultiplier
                        + " hurtEntities=" + hurtEntities
                        + " perDistance=" + fallDamagePerDistance
                        + " max=" + fallDamageMax
                        + " onGround=" + self.onGround()
                        + " bb=" + self.getBoundingBox()
                        + " eligibleTargets=" + eligibleTargets.size()
                        + " nearbyLiving1=" + nearbyLiving.size()
        );
    }
}
