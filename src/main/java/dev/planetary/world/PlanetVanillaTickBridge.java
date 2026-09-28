package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Routes the two concrete LevelTicks instances owned by a bound ServerLevel.
 *
 * <p>Vanilla code often bypasses LevelAccessor.scheduleTick and talks directly
 * to getBlockTicks()/getFluidTicks(), including hasScheduledTick queries. By
 * routing at LevelTicks itself, both APIs observe the same Planetary queue.</p>
 */
public final class PlanetVanillaTickBridge {
    private static final Map<LevelTicks<?>, Binding> BINDINGS =
            new IdentityHashMap<>();

    private PlanetVanillaTickBridge() {
    }

    public static synchronized void bind(
            ServerLevel level,
            PlanetWorldAccess world
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(world, "world");

        LevelTicks<Block> blockTicks = level.getBlockTicks();
        LevelTicks<Fluid> fluidTicks = level.getFluidTicks();

        bind(blockTicks, world, TickKind.BLOCK);
        try {
            bind(fluidTicks, world, TickKind.FLUID);
        } catch (RuntimeException exception) {
            unbind(blockTicks, world, TickKind.BLOCK);
            throw exception;
        }
    }

    public static synchronized void unbind(
            ServerLevel level,
            PlanetWorldAccess world
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(world, "world");

        unbind(level.getBlockTicks(), world, TickKind.BLOCK);
        unbind(level.getFluidTicks(), world, TickKind.FLUID);
    }

    public static boolean schedule(
            LevelTicks<?> vanillaTicks,
            ScheduledTick<?> tick
    ) {
        Objects.requireNonNull(vanillaTicks, "vanillaTicks");
        Objects.requireNonNull(tick, "tick");

        Binding binding = getBinding(vanillaTicks);
        if (binding == null) {
            return false;
        }

        Optional<PlanetBlockPos> position =
                binding.world().vanillaPosCodec().tryDecode(tick.pos());
        if (position.isEmpty()) {
            return false;
        }

        return switch (binding.kind()) {
            case BLOCK -> {
                if (!(tick.type() instanceof Block block)) {
                    yield false;
                }

                binding.world().blockTicks().schedule(
                        block,
                        position.get(),
                        tick.triggerTick(),
                        tick.priority(),
                        tick.subTickOrder()
                );
                yield true;
            }
            case FLUID -> {
                if (!(tick.type() instanceof Fluid fluid)) {
                    yield false;
                }

                binding.world().fluidTicks().schedule(
                        fluid,
                        position.get(),
                        tick.triggerTick(),
                        tick.priority(),
                        tick.subTickOrder()
                );
                yield true;
            }
        };
    }

    public static Optional<Boolean> hasScheduledTick(
            LevelTicks<?> vanillaTicks,
            BlockPos vanillaPos,
            Object type
    ) {
        Objects.requireNonNull(vanillaTicks, "vanillaTicks");
        Objects.requireNonNull(vanillaPos, "vanillaPos");
        Objects.requireNonNull(type, "type");

        Binding binding = getBinding(vanillaTicks);
        if (binding == null) {
            return Optional.empty();
        }

        Optional<PlanetBlockPos> position =
                binding.world().vanillaPosCodec().tryDecode(vanillaPos);
        if (position.isEmpty()) {
            return Optional.empty();
        }

        return switch (binding.kind()) {
            case BLOCK -> type instanceof Block block
                    ? Optional.of(
                            binding.world().blockTicks()
                                    .hasScheduledTick(position.get(), block)
                    )
                    : Optional.empty();
            case FLUID -> type instanceof Fluid fluid
                    ? Optional.of(
                            binding.world().fluidTicks()
                                    .hasScheduledTick(position.get(), fluid)
                    )
                    : Optional.empty();
        };
    }

    public static Optional<Boolean> willTickThisTick(
            LevelTicks<?> vanillaTicks,
            BlockPos vanillaPos,
            Object type
    ) {
        Objects.requireNonNull(vanillaTicks, "vanillaTicks");
        Objects.requireNonNull(vanillaPos, "vanillaPos");
        Objects.requireNonNull(type, "type");

        Binding binding = getBinding(vanillaTicks);
        if (binding == null) {
            return Optional.empty();
        }

        Optional<PlanetBlockPos> position =
                binding.world().vanillaPosCodec().tryDecode(vanillaPos);
        if (position.isEmpty()) {
            return Optional.empty();
        }

        return switch (binding.kind()) {
            case BLOCK -> type instanceof Block block
                    ? Optional.of(
                            binding.world().blockTicks()
                                    .willTickThisTick(position.get(), block)
                    )
                    : Optional.empty();
            case FLUID -> type instanceof Fluid fluid
                    ? Optional.of(
                            binding.world().fluidTicks()
                                    .willTickThisTick(position.get(), fluid)
                    )
                    : Optional.empty();
        };
    }

    public static int additionalCount(LevelTicks<?> vanillaTicks) {
        Objects.requireNonNull(vanillaTicks, "vanillaTicks");

        Binding binding = getBinding(vanillaTicks);
        if (binding == null) {
            return 0;
        }

        return switch (binding.kind()) {
            case BLOCK -> binding.world().blockTicks().size();
            case FLUID -> binding.world().fluidTicks().size();
        };
    }

    private static synchronized Binding getBinding(LevelTicks<?> ticks) {
        return BINDINGS.get(ticks);
    }

    private static void bind(
            LevelTicks<?> ticks,
            PlanetWorldAccess world,
            TickKind kind
    ) {
        Objects.requireNonNull(ticks, "ticks");

        Binding newBinding = new Binding(world, kind);
        Binding previous = BINDINGS.put(ticks, newBinding);
        if (previous != null && !previous.equals(newBinding)) {
            BINDINGS.put(ticks, previous);
            throw new IllegalStateException(
                    "LevelTicks instance is already bound to another Planetary queue"
            );
        }
    }

    private static void unbind(
            LevelTicks<?> ticks,
            PlanetWorldAccess world,
            TickKind kind
    ) {
        Binding expected = new Binding(world, kind);
        if (BINDINGS.get(ticks) != null
                && BINDINGS.get(ticks).equals(expected)) {
            BINDINGS.remove(ticks);
        }
    }

    private enum TickKind {
        BLOCK,
        FLUID
    }

    private record Binding(
            PlanetWorldAccess world,
            TickKind kind
    ) {
        private Binding {
            Objects.requireNonNull(world, "world");
            Objects.requireNonNull(kind, "kind");
        }
    }
}
