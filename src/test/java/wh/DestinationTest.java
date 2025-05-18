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
    
    @Test
    void updateDaily_shouldHandleEdgeCaseDays() {
        final Destination destination = DestinationType.MARS.createDestination();
        
        // Test with day 0 (edge case)
        assertThatCode(() -> destination.updateDaily(0, 365)).doesNotThrowAnyException();
        
        // Test with negative day (edge case)
        assertThatCode(() -> destination.updateDaily(-10, 365)).doesNotThrowAnyException();
        
        // Test with day > totalDays (edge case)
        assertThatCode(() -> destination.updateDaily(366, 365)).doesNotThrowAnyException();
        
        // Test with zero totalDays (edge case that could cause division by zero)
        assertThatCode(() -> destination.updateDaily(1, 0)).doesNotThrowAnyException();
    }
    
    @Test
    void updateDaily_shouldUpdateTransferValues() {
        final Destination destination = DestinationType.MARS.createDestination();
        
        // Update and capture initial values
        destination.updateDaily(1, 365);
        final double initialDeltaVEfficient = destination.deltaVEfficient;
        final double initialTimeEfficient = destination.timeEfficient;
        
        // Update to a different day and verify values change
        destination.updateDaily(180, 365);
        
        assertThat(destination.deltaVEfficient).isNotEqualTo(initialDeltaVEfficient);
        assertThat(destination.timeEfficient).isNotEqualTo(initialTimeEfficient);
    }
}
