package dev.planetary.mixin;

import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BasePressurePlateBlock.class)
public interface BasePressurePlateBlockAccessor {
    @Accessor("type")
    BlockSetType planetary$getBlockSetType();
}
