package dev.planetary.worldgen;

/**
 * How a worldgen element behaves at a gravity boundary.
 */
public enum PlanetGenerationPlacementPolicy {
    /**
     * The element lives in seamless PlanetGenerationSpace and may freely
     * continue across a cube edge.
     *
     * <p>Examples: terrain density, caves, lakes, ore fields, biome climate
     * and other procedural fields.</p>
     */
    WRAP,

    /**
     * The element is rigid in local Minecraft coordinates and must not cross
     * a gravity boundary.
     *
     * <p>Examples: villages, temples, mansions and large template-based mod
     * structures.</p>
     */
    AVOID_EDGE
}
