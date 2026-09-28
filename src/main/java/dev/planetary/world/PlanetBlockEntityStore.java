package dev.planetary.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Stores real vanilla/modded BlockEntity instances by canonical planet
 * coordinates while giving every instance a stable ordinary vanilla BlockPos.
 *
 * <p>The BlockPos is virtual: {@link PlanetVanillaPosCodec} encodes the face
 * identity and face-local coordinates into a reversible vanilla position.
 * This lets vanilla BlockEntity internals keep using BlockPos without making
 * the physical cube geometry pretend to be one global Cartesian grid.</p>
 */
public final class PlanetBlockEntityStore {
    private final PlanetVanillaPosCodec codec;
    private final Map<PlanetBlockPos, BlockEntity> blockEntities = new HashMap<>();

    public PlanetBlockEntityStore(PlanetVanillaPosCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    public PlanetVanillaPosCodec codec() {
        return codec;
    }

    public Optional<BlockEntity> find(PlanetBlockPos pos) {
        return Optional.ofNullable(blockEntities.get(
                Objects.requireNonNull(pos, "pos")
        ));
    }

    public BlockEntity get(PlanetBlockPos pos) {
        return blockEntities.get(Objects.requireNonNull(pos, "pos"));
    }

    public BlockEntity getByVanillaPos(BlockPos vanillaPos) {
        Optional<PlanetBlockPos> planetPos = codec.tryDecode(
                Objects.requireNonNull(vanillaPos, "vanillaPos")
        );
        return planetPos.map(blockEntities::get).orElse(null);
    }

    public Optional<BlockEntity> createForState(
            PlanetBlockPos pos,
            BlockState state
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(state, "state");

        if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
            return Optional.empty();
        }

        BlockPos vanillaPos = codec.encode(pos);
        BlockEntity blockEntity = entityBlock.newBlockEntity(vanillaPos, state);
        if (blockEntity == null) {
            return Optional.empty();
        }

        put(pos, blockEntity);
        return Optional.of(blockEntity);
    }

    /**
     * Reconciles the BlockEntity lifecycle after a block-state replacement.
     *
     * <p>If the existing BlockEntity supports the new state, the same instance
     * is retained and only its cached state is updated. Otherwise it is removed
     * and a new vanilla/modded BlockEntity is created when the new block
     * provides one.</p>
     */
    public BlockEntity reconcileBlockState(
            PlanetBlockPos pos,
            BlockState newState
    ) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(newState, "newState");

        BlockEntity existing = blockEntities.get(pos);
        if (existing != null && existing.isValidBlockState(newState)) {
            existing.clearRemoved();
            existing.setBlockState(newState);
            return existing;
        }

        if (existing != null) {
            remove(pos);
        }

        return createForState(pos, newState).orElse(null);
    }

    public void put(PlanetBlockPos pos, BlockEntity blockEntity) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(blockEntity, "blockEntity");

        BlockPos expectedVanillaPos = codec.encode(pos);
        if (!expectedVanillaPos.equals(blockEntity.getBlockPos())) {
            throw new IllegalArgumentException(
                    "BlockEntity has vanilla position " + blockEntity.getBlockPos()
                            + " but " + pos + " encodes to " + expectedVanillaPos
            );
        }

        BlockEntity previous = blockEntities.put(pos, blockEntity);
        if (previous != null && previous != blockEntity) {
            previous.setRemoved();
        }
        blockEntity.clearRemoved();
    }

    public BlockEntity remove(PlanetBlockPos pos) {
        BlockEntity removed = blockEntities.remove(
                Objects.requireNonNull(pos, "pos")
        );
        if (removed != null) {
            removed.setRemoved();
        }
        return removed;
    }

    public Set<PlanetBlockPos> positions() {
        return Set.copyOf(blockEntities.keySet());
    }

    public int size() {
        return blockEntities.size();
    }

    public boolean isEmpty() {
        return blockEntities.isEmpty();
    }

    public void clear() {
        for (BlockEntity blockEntity : blockEntities.values()) {
            blockEntity.setRemoved();
        }
        blockEntities.clear();
    }
}
