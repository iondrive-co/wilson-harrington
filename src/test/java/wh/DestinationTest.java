package wh;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DestinationTest {

    @Test
    void createDestination_shouldInitializeCorrectly() {
        final Destination destination = DestinationType.EARTH_LEO.createDestination();
        
        assertThat(destination.type).isEqualTo(DestinationType.EARTH_LEO);
        // The sale price might be zero initially before updateDaily is called
        assertThat(destination.salePricePerKg).isGreaterThanOrEqualTo(0);
    }
}
