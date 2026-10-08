package dev.planetary.mixin;

import dev.planetary.world.PlanetLocalBlockProjection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * NeoForge 21.1.215 adds a second bookshelf-position consumer after the
 * shared EnchantingTableBlock.isValidBookShelf predicate: the menu calls
 * getBlockState(tablePos.offset(offset)).getEnchantPowerBonus(
 * level, tablePos.offset(offset)).
 *
 * <p>Both BlockPos.offset(Vec3i) INVOKEs belong to the same enclosing
 * slotsChanged lambda. Reframe BOTH at their original source so the
 * BlockState, bonus-context position and the earlier validated provider
 * all refer to the same physical block, including across Planet seams.</p>
 *
 * <p>Do not redirect the bonus itself: its float value, block extension
 * point, NeoForge EventHooks, RNG and UI menu flow must remain vanilla.</p>
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuBookshelfPowerMixin {
    @Redirect(
            method = "lambda$slotsChanged$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;offset(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos planetary$projectBookcaseBonusPosition(
            BlockPos source,
            Vec3i authoredOffset,
            ItemStack enchantItem,
            Level level,
            BlockPos tablePos
    ) {
        return PlanetLocalBlockProjection.physicalOffset(
                level,
                source,
                authoredOffset.getX(),
                authoredOffset.getY(),
                authoredOffset.getZ()
        );
    }
}
