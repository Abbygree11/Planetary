package dev.planetary.debug;

import dev.planetary.PlanetaryMod;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

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

    private PlanetCapabilityDiagnostics() {
    }

    public static void registerCapabilities(
            RegisterCapabilitiesEvent event
    ) {
        Objects.requireNonNull(event, "event");

        event.registerBlock(
                SIDE_ECHO,
                (level, pos, state, blockEntity, side) -> side,
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
            }
        }

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
                invalidations.get(),
                cacheExpectedLocal
        );
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
            int cacheInvalidations,
            Direction cachedProviderSide
    ) {
    }
}
