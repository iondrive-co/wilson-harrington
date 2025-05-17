package wh;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationStateTest {

    @Test
    void constructor_shouldInitializeWithHaulers() {
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final SimulationState state = new SimulationState(List.of(hauler));
        
        assertThat(state.getCurrentHauler()).isEqualTo(hauler);
    }
    
    @Test
    void asteroidState_shouldBeInitialized() {
        assertThat(SimulationState.ASTEROID_STATE).isNotNull();
        assertThat(SimulationState.ASTEROID_STATE.getDistanceFromSun()).isGreaterThan(0);
    }
    
    @Test
    void kgsWaterMinedPerDay_shouldBePositive() {
        assertThat(SimulationState.KGS_WATER_MINED_PER_DAY).isPositive();
    }
}
