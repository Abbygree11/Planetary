package dev.planetary.worldgen;

import dev.planetary.topology.PlanetCore;
import dev.planetary.topology.PlanetGravityField;
import net.minecraft.world.phys.Vec3;

public final class PlanetWorldSettings {
    public static final int CORE_X = 0;
    public static final int CORE_Y = 128;
    public static final int CORE_Z = 0;
    public static final int RADIUS = 48;
    public static final int DIAMETER = RADIUS * 2 + 1;

    public static final int MIN_Y = -64;
    public static final int HEIGHT = 384;

    private static final PlanetCore CORE =
            new PlanetCore(CORE_X, CORE_Y, CORE_Z, RADIUS);
    private static final PlanetGravityField GRAVITY_FIELD =
            new PlanetGravityField(CORE);

    private PlanetWorldSettings() {
    }

    public static PlanetCore core() {
        return CORE;
    }

    public static PlanetGravityField gravityField() {
        return GRAVITY_FIELD;
    }

    public static Vec3 spawn() {
        return new Vec3(
                CORE_X + 0.5D,
                CORE_Y + RADIUS + 1.0D,
                CORE_Z + 0.5D
        );
    }
}
