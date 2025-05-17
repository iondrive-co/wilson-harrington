package wh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class HaulerClassTest {

    @ParameterizedTest
    @EnumSource(HaulerClass.class)
    void haulerClasses_shouldHavePositiveWeight(HaulerClass haulerClass) {
        assertThat(haulerClass.dryWeightKgs).isPositive();
    }
    
    @ParameterizedTest
    @EnumSource(HaulerClass.class)
    void haulerClasses_shouldHavePositiveCapacity(HaulerClass haulerClass) {
        assertThat(haulerClass.maxCargoKgs).isPositive();
    }
    
    @Test
    void largeHauler_shouldHaveMoreCapacityThanMedium() {
        assertThat(HaulerClass.LARGE.maxCargoKgs)
            .isGreaterThan(HaulerClass.MEDIUM.maxCargoKgs);
    }
    
    @Test
    void mediumHauler_shouldHaveMoreCapacityThanSmall() {
        assertThat(HaulerClass.MEDIUM.maxCargoKgs)
            .isGreaterThan(HaulerClass.SMALL.maxCargoKgs);
    }
    
    @Test
    void largeHauler_shouldWeighMoreThanMedium() {
        assertThat(HaulerClass.LARGE.dryWeightKgs)
            .isGreaterThan(HaulerClass.MEDIUM.dryWeightKgs);
    }
    
    @Test
    void mediumHauler_shouldWeighMoreThanSmall() {
        assertThat(HaulerClass.MEDIUM.dryWeightKgs)
            .isGreaterThan(HaulerClass.SMALL.dryWeightKgs);
    }
}
