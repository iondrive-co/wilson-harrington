import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WaterPropulsionSystemTest {
    @Test
    public void testFuelToAccelerate() {
        // Test that higher deltaV requires more mass
        assertTrue(WaterPropulsionSystem.STEAM.fuelToAccelerate(2.0, WaterHauler.SMALL_STEAM.dryWeightTons) >
                WaterPropulsionSystem.STEAM.fuelToAccelerate(1.0, WaterHauler.SMALL_STEAM.dryWeightTons));

        // Test that zero deltaV requires no mass burnt
        assertEquals(0, WaterPropulsionSystem.STEAM.fuelToAccelerate(0.0, WaterHauler.SMALL_STEAM.dryWeightTons), 0.01);

        // Test that more efficient engines need less mass for same deltaV
        double deltaV = 2.0;
        assertTrue(WaterPropulsionSystem.STEAM.fuelToAccelerate(deltaV, WaterHauler.SMALL_STEAM.dryWeightTons) >
                WaterPropulsionSystem.PLASMA_ION.fuelToAccelerate(deltaV, WaterHauler.SMALL_STEAM.dryWeightTons));
    }

    @Test
    public void testEfficiencyOrdering() {
        double deltaV = 2.0;
        double cargoTons = 10.0;

        // Test ordering of efficiency for total mass (STEAM < ELECTROLYSIS < MICROWAVE < PLASMA)
        assertTrue(WaterPropulsionSystem.STEAM.fuelToAccelerate(deltaV, 3) >
                WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION.fuelToAccelerate(deltaV, 3));
        assertTrue(WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION.fuelToAccelerate(deltaV, 3) >
                WaterPropulsionSystem.MICROWAVE_ELECTROTHERMAL.fuelToAccelerate(deltaV, 3));
        assertTrue(WaterPropulsionSystem.MICROWAVE_ELECTROTHERMAL.fuelToAccelerate(deltaV, 3) >
                WaterPropulsionSystem.PLASMA_ION.fuelToAccelerate(deltaV, 3));
    }

    @Test
    public void sanityTestValues() {
        System.out.println(WaterPropulsionSystem.STEAM.fuelToAccelerate(8, 10));
        System.out.println(WaterPropulsionSystem.STEAM.deltaVFromBurning(9.99, 0.01));
        System.out.println(WaterPropulsionSystem.STEAM.deltaVFromBurning(34796 - 10, 10));
    }
}