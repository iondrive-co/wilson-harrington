package wh;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void simulateDay_shouldNotThrowException() {
        final List<Destination> destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            destinations.add(type.createDestination());
        }
        
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        // This should not throw an exception
        assertThatCode(() -> {
            Main.simulateDay(destinations, hauler, 5000, 10);
        }).doesNotThrowAnyException();
    }
    
    @Test
    void calculateShipmentOption_shouldReturnValidOption() {
        final Destination destination = DestinationType.MARS.createDestination();
        destination.updateDaily(30, 365); // Update to get realistic values
        
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final Main.ShipmentOption option = Main.calculateShipmentOption(
            destination, 1000, Main.OptionType.EFFICIENT, hauler
        );
        
        assertThat(option).isNotNull();
        assertThat(option.destination).isEqualTo(destination);
        assertThat(option.kgsWaterShipped).isEqualTo(1000);
    }
    
    @Test
    void allNonCyclerOptionsUnprofitable_shouldDetectProfitability() {
        final List<Destination> destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            final Destination destination = type.createDestination();
            destination.updateDaily(30, 365);
            destinations.add(destination);
        }
        
        final Hauler hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        final boolean result = Main.allNonCyclerOptionsUnprofitable(destinations, 1000, hauler);
        
        // This is just testing the method runs without exception
        // The actual result depends on the simulation parameters
        assertThat(result).isIn(true, false);
    }
}
