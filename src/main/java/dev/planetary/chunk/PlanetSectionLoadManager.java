package dev.planetary.chunk;

import java.util.Set;

/**
 * Single-player section loading policy for the first 2.0 implementation.
 *
 * <p>Render distance and simulation distance are both measured in the same
 * 16-block units users already know from Minecraft, but applied in 3D.
 * Simulation distance must never exceed render distance.</p>
 */
public final class PlanetSectionLoadManager {
    private final PlanetSectionRadiusTracker render;
    private final PlanetSectionRadiusTracker simulation;

    public PlanetSectionLoadManager(int renderDistanceSections, int simulationDistanceSections) {
        validateDistances(renderDistanceSections, simulationDistanceSections);
        this.render = new PlanetSectionRadiusTracker(renderDistanceSections);
        this.simulation = new PlanetSectionRadiusTracker(simulationDistanceSections);
    }

    public PlanetSectionActivityUpdate movePlayerTo(PlanetSectionPos playerSection) {
        return new PlanetSectionActivityUpdate(
                render.moveTo(playerSection),
                simulation.moveTo(playerSection)
        );
    }

    public PlanetSectionActivityUpdate setDistances(
            int renderDistanceSections,
            int simulationDistanceSections
    ) {
        validateDistances(renderDistanceSections, simulationDistanceSections);

        return new PlanetSectionActivityUpdate(
                render.resize(renderDistanceSections),
                simulation.resize(simulationDistanceSections)
        );
    }

    public PlanetSectionActivityUpdate clear() {
        return new PlanetSectionActivityUpdate(render.clear(), simulation.clear());
    }

    public Set<PlanetSectionPos> renderedSections() {
        return render.activeSections();
    }

    public Set<PlanetSectionPos> simulatedSections() {
        return simulation.activeSections();
    }

    public int renderDistanceSections() {
        return render.radiusSections();
    }

    public int simulationDistanceSections() {
        return simulation.radiusSections();
    }

    private static void validateDistances(int renderDistanceSections, int simulationDistanceSections) {
        if (renderDistanceSections < 0) {
            throw new IllegalArgumentException("renderDistanceSections must be >= 0");
        }
        if (simulationDistanceSections < 0) {
            throw new IllegalArgumentException("simulationDistanceSections must be >= 0");
        }
        if (simulationDistanceSections > renderDistanceSections) {
            throw new IllegalArgumentException(
                    "simulationDistanceSections must be <= renderDistanceSections"
            );
        }
    }
}
