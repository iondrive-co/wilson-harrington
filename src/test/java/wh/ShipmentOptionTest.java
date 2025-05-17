package wh;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShipmentOptionTest {

    @Test
    void constructor_shouldCalculateReceivedWaterAndProfit() {
        final Destination destination = DestinationType.MARS.createDestination();
        destination.updateDaily(30, 365); // Update to get realistic values
        destination.salePricePerKg = 10.0; // Set a known price for testing
        
        final double kgsWaterShipped = 1000;
        final double kgsWaterUsedForDeltaV = 400;
        final double deltaV = 5.0;
        final double time = 100.0;
        
        final ShipmentOption option = new ShipmentOption(
            destination, kgsWaterShipped, kgsWaterUsedForDeltaV, deltaV, time
        );
        
        assertThat(option.kgsWaterReceived).isEqualTo(kgsWaterShipped - kgsWaterUsedForDeltaV);
        assertThat(option.profit).isEqualTo(option.kgsWaterReceived * destination.salePricePerKg);
    }
    
    @Test
    void constructor_shouldHandleNegativeWaterReceived() {
        final Destination destination = DestinationType.MARS.createDestination();
        destination.updateDaily(30, 365);
        
        final double kgsWaterShipped = 1000;
        final double kgsWaterUsedForDeltaV = 1200; // More than shipped
        final double deltaV = 5.0;
        final double time = 100.0;
        
        final ShipmentOption option = new ShipmentOption(
            destination, kgsWaterShipped, kgsWaterUsedForDeltaV, deltaV, time
        );
        
        assertThat(option.kgsWaterReceived).isEqualTo(0);
        assertThat(option.profit).isEqualTo(-1);
    }
}
