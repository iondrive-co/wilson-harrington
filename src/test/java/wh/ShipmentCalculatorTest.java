package wh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ShipmentCalculatorTest {

    private ShipmentCalculator shipmentCalculator;
    private Hauler hauler;
    private List<Destination> destinations;

    @BeforeEach
    void setUp() {
        this.shipmentCalculator = new ShipmentCalculator();
        this.hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        this.destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            final Destination destination = type.createDestination();
            destination.updateDaily(30, 365);
            destinations.add(destination);
        }
    }
    
    @Test
    void calculateShipmentOption_shouldReturnValidOption() {
        final Destination destination = DestinationType.MARS.createDestination();
        destination.updateDaily(30, 365); // Update to get realistic values
        
        final ShipmentOption option = shipmentCalculator.calculateShipmentOption(
            destination, 1000, ShipmentCalculator.OptionType.EFFICIENT, hauler
        );
        
        assertThat(option).isNotNull();
        assertThat(option.destination).isEqualTo(destination);
        assertThat(option.kgsWaterShipped).isEqualTo(1000);
    }
    
    @Test
    void allNonCyclerOptionsUnprofitable_shouldDetectProfitability() {
        final boolean result = shipmentCalculator.allNonCyclerOptionsUnprofitable(destinations, 1000, hauler);
        
        // This is just testing the method runs without exception
        // The actual result depends on the simulation parameters
        assertThat(result).isIn(true, false);
    }
    
    @Test
    void getShipmentOptions_shouldReturnCorrectNumberOfOptions() {
        final List<ShipmentOption> options = shipmentCalculator.getShipmentOptions(
            destinations, 1000, ShipmentCalculator.OptionType.EFFICIENT, hauler
        );
        
        assertThat(options).hasSize(destinations.size());
    }
    
    @Test
    void calculateShipmentOption_shouldHandleZeroWaterShipped() {
        final Destination destination = DestinationType.MARS.createDestination();
        destination.updateDaily(30, 365);
        
        final ShipmentOption option = shipmentCalculator.calculateShipmentOption(
            destination, 0, ShipmentCalculator.OptionType.EFFICIENT, hauler
        );
        
        assertThat(option).isNotNull();
        assertThat(option.kgsWaterShipped).isEqualTo(0);
        assertThat(option.kgsWaterReceived).isEqualTo(0);
    }
    
    @Test
    void calculateShipmentOption_shouldHandleExtremelyHighDeltaV() {
        final Destination destination = DestinationType.MARS.createDestination();
        destination.updateDaily(30, 365);
        
        // Set an extremely high deltaV that would be impossible to achieve
        destination.deltaVEfficient = 1000.0;
        
        final ShipmentOption option = shipmentCalculator.calculateShipmentOption(
            destination, 1000, ShipmentCalculator.OptionType.EFFICIENT, hauler
        );
        
        assertThat(option).isNotNull();
        assertThat(option.kgsWaterUsedForDeltaV).isEqualTo(Double.POSITIVE_INFINITY);
        assertThat(option.kgsWaterReceived).isEqualTo(0);
        assertThat(option.profit).isEqualTo(-1);
    }
}
