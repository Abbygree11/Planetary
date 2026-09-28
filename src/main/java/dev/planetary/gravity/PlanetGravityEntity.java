package dev.planetary.gravity;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;

import java.util.Optional;

/**
 * Internal view implemented by the Entity mixin.
 */
public interface PlanetGravityEntity {
    Optional<PlanetGravityFrame> planetary$gravityFrame();

    Optional<PlanetFace> planetary$gravityFace();
}
