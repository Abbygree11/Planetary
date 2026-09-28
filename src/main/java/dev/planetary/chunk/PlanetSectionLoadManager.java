package dev.planetary.chunk;

import java.util.Set;

/**
 * Single-player section loading policy for the first 2.0 implementation.
 *
 * <p>Render distance and simulation distance are measured in Minecraft's
 * familiar 16-block section units, but applied in 3D and canonicalized through
 * the six cube-planet faces.</p>
 */
public final class PlanetSectionLoadManager {
    private final int faceSizeSections;
    private final PlanetSectionAddressRadiusTracker render;
    private final PlanetSectionAddressRadiusTracker simulation;

    public PlanetSectionLoadManager(
            int faceSizeSections,
            int renderDistanceSections,
            int simulationDistanceSections
    ) {
        validateDistances(renderDistanceSections, simulationDistanceSections);
        PlanetSectionTopology.validateFaceSize(faceSizeSections);
        this.faceSizeSections = faceSizeSections;
        this.render = new PlanetSectionAddressRadiusTracker(faceSizeSections, renderDistanceSections);
        this.simulation = new PlanetSectionAddressRadiusTracker(faceSizeSections, simulationDistanceSections);
    }

    public PlanetSectionAddressActivityUpdate movePlayerTo(PlanetSectionAddress playerSection) {
        return new PlanetSectionAddressActivityUpdate(
                render.moveTo(playerSection),
                simulation.moveTo(playerSection)
        );
    }

    public PlanetSectionAddressActivityUpdate setDistances(
            int renderDistanceSections,
            int simulationDistanceSections
    ) {
        validateDistances(renderDistanceSections, simulationDistanceSections);

        return new PlanetSectionAddressActivityUpdate(
                render.resize(renderDistanceSections),
                simulation.resize(simulationDistanceSections)
        );
    }

    public PlanetSectionAddressActivityUpdate clear() {
        return new PlanetSectionAddressActivityUpdate(render.clear(), simulation.clear());
    }

    public Set<PlanetSectionAddress> renderedSections() {
        return render.activeSections();
    }

    public Set<PlanetSectionAddress> simulatedSections() {
        return simulation.activeSections();
    }

    public int faceSizeSections() {
        return faceSizeSections;
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
