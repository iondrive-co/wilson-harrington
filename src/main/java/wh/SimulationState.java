package wh;

import java.util.ArrayList;
import java.util.List;

public class SimulationState {

    public static final int KGS_WATER_MINED_PER_DAY = 5_000;
    public static final AsteroidState ASTEROID_STATE = AsteroidState.wilsonHarrington();
    public static final boolean ENABLE_ORBITAL_MECHANICS = true;
    public static final boolean ENABLE_AEROBRAKING = true;
    // A value of 1 uses the normal orbital mechanics, less than 1 makes it possible to achieve unrealistic transfers
    public static final float DIFFICULTY_SCALE = 0.3f;

    private final List<Hauler> haulers = new ArrayList<>();

    public SimulationState(final List<Hauler> initialHaulers) {
        haulers.addAll(initialHaulers);
    }

    public Hauler getCurrentHauler() {
        return haulers.get(0);
    }
}
