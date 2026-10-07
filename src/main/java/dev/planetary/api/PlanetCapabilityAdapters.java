package dev.planetary.api;

import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import java.util.Objects;

/**
 * Explicit opt-in for capability providers whose Direction context describes
 * the receiving block's canonical Planet-local side.
 *
 * <p>NeoForge Direction contexts are PHYSICAL by default. Only the provider
 * wrapped here observes canonical LOCAL directions in an active Planet field.
 * In particular, an unrelated provider registered for the same block and
 * capability is NOT affected. Queried world positions and cache keys remain
 * physical. A null side stays null.</p>
 *
 * <p>Wrap the provider while handling RegisterCapabilitiesEvent; do not wrap
 * a caller's Level.getCapability invocation or store local sides in
 * BlockCapabilityCache. This is a semantic contract of the receiver, never
 * a universal interpretation of the Direction type.</p>
 */
public final class PlanetCapabilityAdapters {
    private PlanetCapabilityAdapters() {
    }

    public static <T> IBlockCapabilityProvider<T, Direction> canonicalLocalBlock(
            IBlockCapabilityProvider<T, Direction> provider
    ) {
        Objects.requireNonNull(provider, "provider");
        return (level, pos, state, blockEntity, physicalSide) ->
                provider.getCapability(
                        level,
                        pos,
                        state,
                        blockEntity,
                        physicalSide == null
                                ? null
                                : PlanetBlockRuntime.physicalSideToLocal(
                                        level,
                                        pos,
                                        physicalSide
                                ).orElse(physicalSide)
                );
    }

    /**
     * BlockEntity-specific counterpart for RegisterCapabilitiesEvent.
     * Before a BlockEntity is attached to a Level, preserve the physical
     * query rather than invent a frame.
     */
    public static <BE extends BlockEntity, T>
    ICapabilityProvider<BE, Direction, T> canonicalLocalBlockEntity(
            ICapabilityProvider<BE, Direction, T> provider
    ) {
        Objects.requireNonNull(provider, "provider");
        return (blockEntity, physicalSide) -> {
            if (physicalSide == null || blockEntity.getLevel() == null) {
                return provider.getCapability(blockEntity, physicalSide);
            }
            Direction canonicalSide =
                    PlanetBlockRuntime.physicalSideToLocal(
                            blockEntity.getLevel(),
                            blockEntity.getBlockPos(),
                            physicalSide
                    ).orElse(physicalSide);
            return provider.getCapability(blockEntity, canonicalSide);
        };
    }
}
