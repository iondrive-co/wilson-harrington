package wh;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DestinationTest {

    @Test
    void createDestination_shouldInitializeCorrectly() {
        final Destination destination = DestinationType.EARTH_LEO.createDestination();
        
        assertThat(destination.type).isEqualTo(DestinationType.EARTH_LEO);
        // The sale price might be zero initially before updateDaily is called
        assertThat(destination.salePricePerKg).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    void updateDaily_shouldUpdateSalePrice() {
        final Destination destination = DestinationType.MARS.createDestination();
        final double initialPrice = destination.salePricePerKg;
        
        destination.updateDaily(30, 365);
        
        assertThat(destination.salePricePerKg).isNotEqualTo(initialPrice);
    }
    
    @Test
    void allDestinationTypes_shouldCreateValidDestinations() {
        for (DestinationType type : DestinationType.values()) {
            final Destination destination = type.createDestination();
            
            assertThat(destination).isNotNull();
            assertThat(destination.type).isEqualTo(type);
            
            // Update and verify it doesn't throw exceptions
            destination.updateDaily(1, 365);
            // If we got here without exception, the test passes
        }
    }
}
