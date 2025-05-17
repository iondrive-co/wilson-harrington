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
        
        final int expectedDryWeight = HaulerClass.SMALL.dryWeightKgs + 
                                     WaterPropulsionSystem.THERMAL.weightKg + 
                                     (PowerSource.SOLAR.weightKgs * 5);
        
        assertThat(hauler.getDryWeightKgs()).isEqualTo(expectedDryWeight);
    }
    
    @Test
    void kgsFuelToAccelerateTo_shouldReturnFiniteValueForAchievableDeltaV() {
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final double fuelKgs = hauler.kgsFuelToAccelerateTo(100); // 0.1 km/s
        
        assertThat(fuelKgs).isFinite();
        assertThat(fuelKgs).isGreaterThan(0);
    }
    
    @Test
    void kgsFuelToAccelerateTo_shouldReturnInfinityForUnachievableDeltaV() {
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final double fuelKgs = hauler.kgsFuelToAccelerateTo(100000); // 100 km/s - unrealistically high
        
        assertThat(fuelKgs).isEqualTo(Double.POSITIVE_INFINITY);
    }
    
    @Test
    void deltaVFromBurning_shouldBeProportionalToFuelWeight() {
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final double dryWeight = hauler.getDryWeightKgs();
        final double deltaV1 = hauler.deltaVFromBurning(1000, dryWeight);
        final double deltaV2 = hauler.deltaVFromBurning(2000, dryWeight);
        
        assertThat(deltaV2).isGreaterThan(deltaV1);
    }
}
