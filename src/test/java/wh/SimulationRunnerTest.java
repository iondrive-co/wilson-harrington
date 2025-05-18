package wh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThatCode;

class SimulationRunnerTest {

    private ShipmentCalculator shipmentCalculator;
    private SimulationRunner simulationRunner;
    private Hauler hauler;
    private List<Destination> destinations;

    @BeforeEach
    void setUp() {
        this.shipmentCalculator = new ShipmentCalculator();
        this.simulationRunner = new SimulationRunner(shipmentCalculator);
        this.hauler = new Hauler(
            HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5))
        );
        
        this.destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            destinations.add(type.createDestination());
        }
    }
    
    @Test
    void simulateDay_shouldNotThrowException() {
        // This should not throw an exception
        assertThatCode(() -> {
            simulationRunner.simulateDay(destinations, hauler, 5000, 10);
        }).doesNotThrowAnyException();
    }
}
