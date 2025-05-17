package wh;

import org.junit.jupiter.api.Test;
import java.util.EnumMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HaulerTest {

    @Test
    void constructor_withInsufficientPower_shouldThrowException() {
        assertThatThrownBy(() -> {
            new Hauler(
                HaulerClass.SMALL,
                new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 5)),
                new EnumMap<>(Map.of(PowerSource.SOLAR, 1))
            );
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Required power");
    }
    
    @Test
    void getImpulseMetersSec_shouldCalculateCorrectly() {
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final double expectedImpulse = WaterPropulsionSystem.THERMAL.specificImpulseMetersSec * 
                                      WaterPropulsionSystem.THERMAL.conversionEfficiency;
        
        assertThat(hauler.getImpulseMetersSec()).isEqualTo(expectedImpulse);
    }
    
    @Test
    void getDryWeightKgs_shouldCalculateCorrectly() {
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final int expectedWeight = HaulerClass.SMALL.dryWeightKgs + 
                                  WaterPropulsionSystem.THERMAL.weightKg + 
                                  (PowerSource.SOLAR.weightKgs * 5);
        
        assertThat(hauler.getDryWeightKgs()).isEqualTo(expectedWeight);
    }
}
