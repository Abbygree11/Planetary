package dev.planetary.mixin;

import dev.planetary.world.PlanetLocalBlockProjection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The same vanilla isValidBookShelf predicate runs on the client for
 * enchantment particles AND on the server from EnchantmentMenu. Only the
 * physical positions of its provider/transmitter queries change; vanilla
 * tags, short-circuit order and power-provider extension points remain intact.
 */
@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBookshelfGravityMixin {
    @Redirect(
            method = "isValidBookShelf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;offset(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;"
            )
    )
    private static BlockPos planetary$projectBookshelfProvider(
            BlockPos source,
            Vec3i authoredOffset,
            Level level,
            BlockPos tablePos,
            BlockPos bookshelfOffset
    ) {
        return PlanetLocalBlockProjection.physicalOffset(
                level,
                source,
                authoredOffset.getX(),
                authoredOffset.getY(),
                authoredOffset.getZ()
        );
    }

    @Redirect(
            method = "isValidBookShelf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"
            )
    )
    private static BlockPos planetary$projectBookshelfTransmitter(
            BlockPos source,
            int localX,
            int localY,
            int localZ,
            Level level,
            BlockPos tablePos,
            BlockPos bookshelfOffset
    ) {
        return PlanetLocalBlockProjection.physicalOffset(
                level, source, localX, localY, localZ
        );
    }
}
