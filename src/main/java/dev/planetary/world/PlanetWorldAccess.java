package dev.planetary.world;

import dev.planetary.topology.FaceTransform;
import dev.planetary.topology.PlanetDirection;
import dev.planetary.topology.PlanetTopology;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.TickPriority;

import java.util.Objects;

/**
 * First world-access bridge between cube-planet coordinates and vanilla block
 * data.
 *
 * <p>This deliberately is not a fake ServerLevel yet. It gives the later
 * Level/BlockGetter compatibility layer one canonical place for topology-aware
 * block, fluid, BlockEntity, neighbor and scheduled-tick access.</p>
 */
public final class PlanetWorldAccess {
    private final int faceSizeBlocks;
    private final PlanetBlockStateStore blocks;
    private final PlanetVanillaPosCodec vanillaPosCodec;
    private final PlanetBlockEntityStore blockEntities;
    private final PlanetNeighborUpdateQueue neighborUpdates = new PlanetNeighborUpdateQueue();
    private final PlanetTickScheduler<Block> blockTicks = new PlanetTickScheduler<>();
    private final PlanetTickScheduler<Fluid> fluidTicks = new PlanetTickScheduler<>();
    private Level boundLevel;

    public PlanetWorldAccess(int faceSizeBlocks) {
        this(
                faceSizeBlocks,
                PlanetVanillaPosCodec.PACKED_MIN_Y,
                PlanetVanillaPosCodec.PACKED_MAX_Y,
                new PlanetBlockStateStore()
        );
    }

    public PlanetWorldAccess(
            int faceSizeBlocks,
            int minLocalY,
            int maxLocalY
    ) {
        this(
                faceSizeBlocks,
                minLocalY,
                maxLocalY,
                new PlanetBlockStateStore()
        );
    }

    public PlanetWorldAccess(int faceSizeBlocks, PlanetBlockStateStore blocks) {
        this(
                faceSizeBlocks,
                PlanetVanillaPosCodec.PACKED_MIN_Y,
                PlanetVanillaPosCodec.PACKED_MAX_Y,
                blocks
        );
    }

    public PlanetWorldAccess(
            int faceSizeBlocks,
            int minLocalY,
            int maxLocalY,
            PlanetBlockStateStore blocks
    ) {
        if (faceSizeBlocks <= 0) {
            throw new IllegalArgumentException("faceSizeBlocks must be > 0");
        }
        this.faceSizeBlocks = faceSizeBlocks;
        this.blocks = Objects.requireNonNull(blocks, "blocks");
        this.vanillaPosCodec = new PlanetVanillaPosCodec(
                faceSizeBlocks,
                minLocalY,
                maxLocalY
        );
        this.blockEntities = new PlanetBlockEntityStore(vanillaPosCodec);
    }

    public int faceSizeBlocks() {
        return faceSizeBlocks;
    }

    public PlanetBlockStateStore blocks() {
        return blocks;
    }

    public PlanetVanillaPosCodec vanillaPosCodec() {
        return vanillaPosCodec;
    }

    public PlanetBlockEntityStore blockEntities() {
        return blockEntities;
    }

    public BlockEntity getBlockEntity(PlanetBlockPos pos) {
        return blockEntities.get(pos);
    }

    /**
     * Binds Planetary BlockEntities to the real Minecraft Level instance.
     *
     * <p>The virtual BlockPos codec keeps their coordinates stable; the next
     * compatibility layer will intercept accesses to that virtual range and
     * delegate them back into this PlanetWorldAccess.</p>
     */
    public void bindLevel(Level level) {
        Level newLevel = Objects.requireNonNull(level, "level");

        if (boundLevel != null && boundLevel != newLevel) {
            if (boundLevel instanceof ServerLevel serverLevel) {
                PlanetVanillaTickBridge.unbind(serverLevel, this);
            }
            PlanetLevelBridge.unbind(boundLevel, this);
        }

        PlanetLevelBridge.bind(newLevel, this);
        try {
            if (newLevel instanceof ServerLevel serverLevel) {
                PlanetVanillaTickBridge.bind(serverLevel, this);
            }
        } catch (RuntimeException exception) {
            PlanetLevelBridge.unbind(newLevel, this);
            throw exception;
        }

        this.boundLevel = newLevel;

        for (PlanetBlockPos pos : blockEntities.positions()) {
            BlockEntity blockEntity = blockEntities.get(pos);
            if (blockEntity != null) {
                blockEntity.setLevel(newLevel);
            }
        }
    }

    public void unbindLevel() {
        if (boundLevel == null) {
            return;
        }

        if (boundLevel instanceof ServerLevel serverLevel) {
            PlanetVanillaTickBridge.unbind(serverLevel, this);
        }
        PlanetLevelBridge.unbind(boundLevel, this);
        boundLevel = null;
    }

    public Level boundLevel() {
        return boundLevel;
    }

    public PlanetNeighborUpdateQueue neighborUpdates() {
        return neighborUpdates;
    }

    public PlanetTickScheduler<Block> blockTicks() {
        return blockTicks;
    }

    public PlanetTickScheduler<Fluid> fluidTicks() {
        return fluidTicks;
    }

    public PlanetBlockPos relative(PlanetBlockPos pos, Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return PlanetBlockTopology.step(
                Objects.requireNonNull(pos, "pos"),
                PlanetVanillaDirection.fromVanilla(direction),
                faceSizeBlocks
        );
    }

    public PlanetBlockPos relative(PlanetBlockPos pos, PlanetDirection direction) {
        return PlanetBlockTopology.step(
                Objects.requireNonNull(pos, "pos"),
                Objects.requireNonNull(direction, "direction"),
                faceSizeBlocks
        );
    }

    /**
     * Returns both the adjacent block and the target-local direction pointing
     * back to the source. The latter matters at a cube edge, where simply
     * taking vanilla direction.getOpposite() is not enough.
     */
    public PlanetNeighborRef neighbor(PlanetBlockPos source, Direction direction) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(direction, "direction");

        PlanetDirection localDirection = PlanetVanillaDirection.fromVanilla(direction);
        PlanetDirection backDirection = localDirection.opposite();

        if (localDirection.isHorizontal()
                && PlanetBlockTopology.isOnEdge(source, localDirection, faceSizeBlocks)) {
            FaceTransform transform = PlanetTopology.edgeTransform(source.face(), localDirection);
            backDirection = transform.transformDirection(localDirection).opposite();
        }

        return new PlanetNeighborRef(
                relative(source, direction),
                direction,
                PlanetVanillaDirection.toVanilla(backDirection)
        );
    }

    public BlockState getBlockState(PlanetBlockPos pos) {
        return blocks.getBlockState(pos);
    }

    public FluidState getFluidState(PlanetBlockPos pos) {
        return blocks.getFluidState(pos);
    }

    /**
     * Stores the exact vanilla/modded BlockState, reconciles the corresponding
     * real BlockEntity lifecycle, and returns the previous BlockState.
     */
    public BlockState setBlockState(PlanetBlockPos pos, BlockState state) {
        BlockState previous = blocks.setBlockState(pos, state);
        if (previous != state) {
            BlockEntity blockEntity = blockEntities.reconcileBlockState(pos, state);
            if (blockEntity != null && boundLevel != null) {
                blockEntity.setLevel(boundLevel);
            }
        }
        return previous;
    }

    /**
     * Vanilla-compatible Level#setBlock path for a Planetary position.
     *
     * <p>The raw state is stored before the old block's onRemove callback, but
     * the old BlockEntity is deliberately kept alive until that callback gets
     * a chance to remove it. This mirrors LevelChunk#setBlockState and is
     * required by vanilla/modded block lifecycle code.</p>
     */
    public boolean setBlock(
            PlanetBlockPos pos,
            BlockState state,
            int flags,
            int recursionLeft
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(state, "state");

        if (boundLevel == null) {
            BlockState previous = (flags & Block.UPDATE_NEIGHBORS) != 0
                    ? setBlockStateAndUpdateNeighbors(pos, state)
                    : setBlockState(pos, state);
            return previous != state;
        }

        Level level = boundLevel;
        BlockPos vanillaPos = vanillaPosCodec.encode(pos);

        // Do not reconcile BlockEntities yet. Vanilla invokes oldState.onRemove
        // after the section state changed but while the old BE can still exist.
        BlockState previous = blocks.setBlockState(pos, state);
        if (previous == state) {
            return false;
        }

        boolean movedByPiston =
                (flags & Block.UPDATE_MOVE_BY_PISTON) != 0;

        if (!level.isClientSide) {
            previous.onRemove(
                    level,
                    vanillaPos,
                    state,
                    movedByPiston
            );
        } else if (!previous.is(state.getBlock())
                && previous.hasBlockEntity()) {
            blockEntities.remove(pos);
        }

        // onRemove is allowed to replace the block again. LevelChunk aborts
        // the outer placement when the requested block no longer owns the pos.
        if (!blocks.getBlockState(pos).is(state.getBlock())) {
            return false;
        }

        if (!level.isClientSide) {
            state.onPlace(
                    level,
                    vanillaPos,
                    previous,
                    movedByPiston
            );
        }

        // onPlace may itself change the state. Reconcile against the actual
        // stored state when it still belongs to the requested block.
        BlockState actualState = blocks.getBlockState(pos);
        if (actualState.is(state.getBlock())) {
            BlockEntity blockEntity =
                    blockEntities.reconcileBlockState(pos, actualState);
            if (blockEntity != null) {
                blockEntity.setLevel(level);
            }
        }

        // Vanilla markAndNotifyBlock performs notifications only when the
        // requested state is still exactly the state stored at this position.
        actualState = blocks.getBlockState(pos);
        if (actualState == state) {
            if ((flags & Block.UPDATE_NEIGHBORS) != 0) {
                level.blockUpdated(vanillaPos, previous.getBlock());
            }

            if ((flags & Block.UPDATE_KNOWN_SHAPE) == 0
                    && recursionLeft > 0) {
                int shapeFlags = flags
                        & ~(Block.UPDATE_NEIGHBORS
                        | Block.UPDATE_SUPPRESS_DROPS);

                previous.updateIndirectNeighbourShapes(
                        level,
                        vanillaPos,
                        shapeFlags,
                        recursionLeft - 1
                );
                state.updateNeighbourShapes(
                        level,
                        vanillaPos,
                        shapeFlags,
                        recursionLeft - 1
                );
                state.updateIndirectNeighbourShapes(
                        level,
                        vanillaPos,
                        shapeFlags,
                        recursionLeft - 1
                );
            }

            level.onBlockStateChange(
                    vanillaPos,
                    previous,
                    actualState
            );
            state.onBlockStateChange(
                    level,
                    vanillaPos,
                    previous
            );
        }

        return true;
    }

    /**
     * Stores a block and queues the six topology-aware neighbor updates when
     * the state actually changed.
     */
    public BlockState setBlockStateAndUpdateNeighbors(PlanetBlockPos pos, BlockState state) {
        BlockState previous = setBlockState(pos, state);
        if (previous != state) {
            if (boundLevel != null) {
                PlanetVanillaNeighborUpdates.updateAll(
                        boundLevel,
                        this,
                        pos,
                        previous.getBlock()
                );
            } else {
                neighborUpdates.enqueueAllNeighbors(
                        this,
                        pos,
                        previous.getBlock()
                );
            }
        }
        return previous;
    }

    public void updateNeighborsAt(PlanetBlockPos pos, Block sourceBlock) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(sourceBlock, "sourceBlock");

        if (boundLevel != null) {
            PlanetVanillaNeighborUpdates.updateAll(
                    boundLevel,
                    this,
                    pos,
                    sourceBlock
            );
        } else {
            neighborUpdates.enqueueAllNeighbors(
                    this,
                    pos,
                    sourceBlock
            );
        }
    }

    public void updateNeighborsAtExceptFromFacing(
            PlanetBlockPos pos,
            Block sourceBlock,
            Direction exceptDirection
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(sourceBlock, "sourceBlock");
        Objects.requireNonNull(exceptDirection, "exceptDirection");

        if (boundLevel != null) {
            PlanetVanillaNeighborUpdates.updateExcept(
                    boundLevel,
                    this,
                    pos,
                    sourceBlock,
                    exceptDirection
            );
        } else {
            neighborUpdates.enqueueNeighborsExcept(
                    this,
                    pos,
                    sourceBlock,
                    exceptDirection
            );
        }
    }

    public BlockState getNeighborState(PlanetBlockPos pos, Direction direction) {
        return getBlockState(relative(pos, direction));
    }

    public FluidState getNeighborFluidState(PlanetBlockPos pos, Direction direction) {
        return getFluidState(relative(pos, direction));
    }

    public boolean scheduleBlockTick(
            PlanetBlockPos pos,
            Block block,
            long triggerTick,
            TickPriority priority
    ) {
        return blockTicks.schedule(block, pos, triggerTick, priority);
    }

    public boolean scheduleFluidTick(
            PlanetBlockPos pos,
            Fluid fluid,
            long triggerTick,
            TickPriority priority
    ) {
        return fluidTicks.schedule(fluid, pos, triggerTick, priority);
    }

    public void runScheduledBlockTicks(
            ServerLevel level,
            long gameTime,
            int maxTicks
    ) {
        requireBoundServerLevel(level);

        var due = blockTicks.collectDueForExecution(gameTime, maxTicks);
        try {
            for (PlanetScheduledTick<Block> tick : due) {
                blockTicks.markRunning(tick);

                BlockState state = getBlockState(tick.position());
                if (state.is(tick.type())) {
                    state.tick(
                            level,
                            vanillaPosCodec.encode(tick.position()),
                            level.getRandom()
                    );
                }
            }
        } finally {
            blockTicks.finishExecutionBatch();
        }
    }

    public void runScheduledFluidTicks(
            ServerLevel level,
            long gameTime,
            int maxTicks
    ) {
        requireBoundServerLevel(level);

        var due = fluidTicks.collectDueForExecution(gameTime, maxTicks);
        try {
            for (PlanetScheduledTick<Fluid> tick : due) {
                fluidTicks.markRunning(tick);

                FluidState state = getFluidState(tick.position());
                if (state.is(tick.type())) {
                    state.tick(
                            level,
                            vanillaPosCodec.encode(tick.position())
                    );
                }
            }
        } finally {
            fluidTicks.finishExecutionBatch();
        }
    }

    private void requireBoundServerLevel(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        if (boundLevel != level) {
            throw new IllegalStateException(
                    "ServerLevel is not bound to this PlanetWorldAccess"
            );
        }
    }
}
