package dev.planetary.mixin;

import dev.planetary.world.PlanetBlockShapeRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Rotates position-aware BlockState shapes from canonical local block axes into
 * physical world axes exactly once per public shape query.
 *
 * <p>Support and occlusion shapes deliberately remain canonical in this phase.
 * Their methods still participate in the scope so nested default getShape calls
 * cannot accidentally become physical.</p>
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateShapeMixin {
    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterShapeSimple(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitShapeSimple(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishPhysical(
                        getter,
                        pos,
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterShapeContext(
            BlockGetter getter,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitShapeContext(
            BlockGetter getter,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishPhysical(
                        getter,
                        pos,
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterCollisionSimple(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitCollisionSimple(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishPhysical(
                        getter,
                        pos,
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterCollisionContext(
            BlockGetter getter,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitCollisionContext(
            BlockGetter getter,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishPhysical(
                        getter,
                        pos,
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getVisualShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterVisual(
            BlockGetter getter,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getVisualShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitVisual(
            BlockGetter getter,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishPhysical(
                        getter,
                        pos,
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getInteractionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterInteraction(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getInteractionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitInteraction(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishPhysical(
                        getter,
                        pos,
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getBlockSupportShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterSupportCanonical(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getBlockSupportShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitSupportCanonical(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishCanonical(
                        cir.getReturnValue()
                )
        );
    }

    @Inject(
            method = "getOcclusionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD")
    )
    private void planetary$enterOcclusionCanonical(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        PlanetBlockShapeRuntime.enterQuery();
    }

    @Inject(
            method = "getOcclusionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void planetary$exitOcclusionCanonical(
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        cir.setReturnValue(
                PlanetBlockShapeRuntime.finishCanonical(
                        cir.getReturnValue()
                )
        );
    }
}
