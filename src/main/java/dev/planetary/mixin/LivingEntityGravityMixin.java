package dev.planetary.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Planet movement intentionally keeps LivingEntity's vanilla jump/travel
 * implementation. Entity.deltaMovement is stored in the local gravity frame,
 * so vanilla Y is the entity's local UP and vanilla XZ is its local floor.
 *
 * <p>Special movement modes such as fluids, climbing and elytra will get
 * dedicated hooks as they are enabled; land/air movement needs no replacement
 * implementation.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityGravityMixin {
}
