package wh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class DestinationTypeTest {

    @ParameterizedTest
    @EnumSource(DestinationType.class)
    void calculateSalePricePerKg_shouldReturnPositiveValue(DestinationType type) {
        final double price = type.calculateSalePricePerKg(0.5, new Random(42));
        
        assertThat(price).isGreaterThan(0);
    }
    
    @Test
    void calculateSalePricePerKg_shouldVaryWithPerihelionWeight() {
        final DestinationType type = DestinationType.MARS;
        final Random random = new Random(42); // Fixed seed for reproducibility
        
        final double price1 = type.calculateSalePricePerKg(0.1, random);
        final double price2 = type.calculateSalePricePerKg(0.9, random);
        
        assertThat(price1).isNotEqualTo(price2);
    }
    
    @Test
    void createDestination_shouldReturnDestinationWithCorrectType() {
        for (DestinationType type : DestinationType.values()) {
            final Destination destination = type.createDestination();
            
            assertThat(destination.type).isEqualTo(type);
        }
    }
}
