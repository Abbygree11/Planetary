package dev.planetary.debug;

import dev.planetary.PlanetaryMod;
import dev.planetary.api.PlanetCapabilityAdapters;
import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.world.PlanetBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runtime acceptance probe for Planet's NeoForge sided-capability boundary.
 *
 * <p>The private capability is intentionally behavior-free: its provider
 * simply returns the Direction context it actually receives. That lets the
 * probe observe the real path through ServerLevel -> BlockCapability mixin ->
 * NeoForge provider without introducing a custom block or gameplay mechanic.</p>
 */
public final class PlanetCapabilityDiagnostics {
    public static final BlockCapability<Direction, Direction>
            SIDE_ECHO =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath(
                            PlanetaryMod.MOD_ID,
                            "internal_side_echo"
                    ),
                    Direction.class
            );

    /** Receives the physical queried side without any Planet rewrite. */
    public static final BlockCapability<Direction, Direction>
            PHYSICAL_SIDE_ECHO =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath(
                            PlanetaryMod.MOD_ID,
                            "internal_physical_side_echo"
                    ),
                    Direction.class
            );

    private static final ItemStackHandler ITEM_HANDLER =
            new ItemStackHandler(1);
    private static final FluidTank FLUID_HANDLER =
            new FluidTank(1000);
    private static final EnergyStorage ENERGY_HANDLER =
            new EnergyStorage(1000);

    private PlanetCapabilityDiagnostics() {
    }

    public static void registerCapabilities(
            RegisterCapabilitiesEvent event
    ) {
        Objects.requireNonNull(event, "event");

        event.registerBlock(
                SIDE_ECHO,
                PlanetCapabilityAdapters.canonicalLocalBlock(
                        (level, pos, state, blockEntity, side) -> side
                ),
                Blocks.STONE
        );
        event.registerBlock(
                PHYSICAL_SIDE_ECHO,
                (level, pos, state, blockEntity, side) -> side,
                Blocks.STONE
        );

        /*
         * Standard NeoForge capability probes are deliberately local-UP-only.
         * A physical query succeeds only because these specific probe
         * providers explicitly opt into canonical-local interpretation.
         */
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                PlanetCapabilityAdapters.canonicalLocalBlock(
                        (level, pos, state, blockEntity, side) ->
                                side == Direction.UP
                                        ? ITEM_HANDLER
                                        : null
                ),
                Blocks.STONE
        );
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                PlanetCapabilityAdapters.canonicalLocalBlock(
                        (level, pos, state, blockEntity, side) ->
                                side == Direction.UP
                                        ? FLUID_HANDLER
                                        : null
                ),
                Blocks.STONE
        );
        event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
                PlanetCapabilityAdapters.canonicalLocalBlock(
                        (level, pos, state, blockEntity, side) ->
                                side == Direction.UP
                                        ? ENERGY_HANDLER
                                        : null
                ),
                Blocks.STONE
        );
    }

    public static Result verify(
            ServerLevel level
    ) {
        Objects.requireNonNull(level, "level");

        PlanetGravityField field =
                PlanetGravityRuntime.find(level)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Planet gravity field is not bound"
                                )
                        );

        int sideChecks = 0;
        int physicalSideChecks = 0;

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target =
                    oneBlockFromCore(
                            field,
                            face
                    );

            if (!level.getBlockState(target).is(Blocks.STONE)) {
                throw new IllegalStateException(
                        "Capability probe expected STONE at "
                                + target
                                + " for "
                                + face
                                + ", got "
                                + level.getBlockState(target)
                );
            }

            for (Direction physicalSide :
                    Direction.values()) {
                Direction expectedLocal =
                        PlanetBlockRuntime.physicalSideToLocal(
                                level,
                                target,
                                physicalSide
                        ).orElseThrow();

                Direction providerSide =
                        level.getCapability(
                                SIDE_ECHO,
                                target,
                                physicalSide
                        );

                if (providerSide != expectedLocal) {
                    throw new IllegalStateException(
                            "Capability side mismatch at "
                                    + face
                                    + " / "
                                    + physicalSide
                                    + ": expected provider local "
                                    + expectedLocal
                                    + ", got "
                                    + providerSide
                    );
                }

                sideChecks++;

                Direction physicalProviderSide = level.getCapability(
                        PHYSICAL_SIDE_ECHO,
                        target,
                        physicalSide
                );
                if (physicalProviderSide != physicalSide) {
                    throw new IllegalStateException(
                            "Unwrapped physical capability changed at "
                                    + face + " / " + physicalSide
                                    + ": got " + physicalProviderSide
                    );
                }
                physicalSideChecks++;
            }
        }

        int standardCapabilityChecks =
                verifyStandardCapabilities(
                        level,
                        field
                );

        BlockPos cacheTarget =
                oneBlockFromCore(
                        field,
                        PlanetFace.POS_X
                );
        Direction cachePhysicalSide =
                Direction.EAST;
        Direction cacheExpectedLocal =
                PlanetBlockRuntime.physicalSideToLocal(
                        level,
                        cacheTarget,
                        cachePhysicalSide
                ).orElseThrow();

        AtomicInteger invalidations =
                new AtomicInteger();

        BlockCapabilityCache<Direction, Direction> cache =
                BlockCapabilityCache.create(
                        SIDE_ECHO,
                        level,
                        cacheTarget,
                        cachePhysicalSide,
                        () -> true,
                        invalidations::incrementAndGet
                );

        Direction first =
                cache.getCapability();

        if (first != cacheExpectedLocal) {
            throw new IllegalStateException(
                    "Capability cache first lookup mismatch: expected "
                            + cacheExpectedLocal
                            + ", got "
                            + first
            );
        }

        level.invalidateCapabilities(
                cacheTarget
        );

        if (invalidations.get() != 1) {
            throw new IllegalStateException(
                    "Capability cache invalidation listener expected 1 call, got "
                            + invalidations.get()
            );
        }

        Direction second =
                cache.getCapability();

        if (second != cacheExpectedLocal) {
            throw new IllegalStateException(
                    "Capability cache lookup after invalidation mismatch: expected "
                            + cacheExpectedLocal
                            + ", got "
                            + second
            );
        }

        return new Result(
                sideChecks,
                physicalSideChecks,
                standardCapabilityChecks,
                invalidations.get(),
                cacheExpectedLocal
        );
    }

    private static int verifyStandardCapabilities(
            ServerLevel level,
            PlanetGravityField field
    ) {
        int checks = 0;

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos target =
                    oneBlockFromCore(
                            field,
                            face
                    );

            Direction physicalLocalUp =
                    PlanetBlockRuntime.localSideToPhysical(
                            level,
                            target,
                            Direction.UP
                    ).orElseThrow();
            Direction physicalLocalDown =
                    physicalLocalUp.getOpposite();

            Object itemFromUp =
                    level.getCapability(
                            Capabilities.ItemHandler.BLOCK,
                            target,
                            physicalLocalUp
                    );
            Object fluidFromUp =
                    level.getCapability(
                            Capabilities.FluidHandler.BLOCK,
                            target,
                            physicalLocalUp
                    );
            Object energyFromUp =
                    level.getCapability(
                            Capabilities.EnergyStorage.BLOCK,
                            target,
                            physicalLocalUp
                    );

            if (itemFromUp != ITEM_HANDLER) {
                throw new IllegalStateException(
                        "Item capability local-UP mapping failed on "
                                + face
                );
            }
            if (fluidFromUp != FLUID_HANDLER) {
                throw new IllegalStateException(
                        "Fluid capability local-UP mapping failed on "
                                + face
                );
            }
            if (energyFromUp != ENERGY_HANDLER) {
                throw new IllegalStateException(
                        "Energy capability local-UP mapping failed on "
                                + face
                );
            }

            checks += 3;

            if (level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    target,
                    physicalLocalDown
            ) != null) {
                throw new IllegalStateException(
                        "Item capability must reject local DOWN on "
                                + face
                );
            }
            if (level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    target,
                    physicalLocalDown
            ) != null) {
                throw new IllegalStateException(
                        "Fluid capability must reject local DOWN on "
                                + face
                );
            }
            if (level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    target,
                    physicalLocalDown
            ) != null) {
                throw new IllegalStateException(
                        "Energy capability must reject local DOWN on "
                                + face
                );
            }

            checks += 3;
        }

        return checks;
    }

    private static BlockPos oneBlockFromCore(
            PlanetGravityField field,
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up =
                frame.worldUp();

        return new BlockPos(
                field.core().x() + up.x(),
                field.core().y() + up.y(),
                field.core().z() + up.z()
        );
    }

    public record Result(
            int sideChecks,
            int physicalSideChecks,
            int standardCapabilityChecks,
            int cacheInvalidations,
            Direction cachedProviderSide
    ) {
    }
}
