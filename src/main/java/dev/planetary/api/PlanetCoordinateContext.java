package dev.planetary.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Origin/provenance of a block-space position when crossing an integration
 * boundary. A physical Minecraft BlockPos and a virtual contraption or
 * schematic BlockPos are not interchangeable merely because their three
 * numeric coordinates are equal.
 *
 * <p>Only PhysicalBlock is accepted by ordinary Planet block-frame queries.
 * A foreign integration must explicitly resolve its own coordinates into
 * physical world positions before it can invoke PlanetFrameApi. No automatic
 * Level wrapper unwrapping or silent coordinate conversion is performed.</p>
 */
public sealed interface PlanetCoordinateContext
        permits PlanetCoordinateContext.PhysicalBlock,
                PlanetCoordinateContext.ForeignBlock {

    BlockPos pos();

    static PhysicalBlock physical(Level level, BlockPos pos) {
        return new PhysicalBlock(level, pos);
    }

    static ForeignBlock foreign(ResourceLocation spaceId, BlockPos pos) {
        return new ForeignBlock(spaceId, pos);
    }

    /**
     * Caller asserts that Level and position refer to a real physical world,
     * not a Create-like virtual Level backed by unrelated block coordinates.
     */
    record PhysicalBlock(Level level, BlockPos pos)
            implements PlanetCoordinateContext {
        public PhysicalBlock {
            Objects.requireNonNull(level, "level");
            pos = Objects.requireNonNull(pos, "pos").immutable();
        }
    }

    /**
     * Named foreign coordinate system; carries no implicit physical mapping.
     * Only a matching optional mod adapter can construct a PhysicalBlock.
     */
    record ForeignBlock(ResourceLocation spaceId, BlockPos pos)
            implements PlanetCoordinateContext {
        public ForeignBlock {
            Objects.requireNonNull(spaceId, "spaceId");
            pos = Objects.requireNonNull(pos, "pos").immutable();
        }
    }
}
