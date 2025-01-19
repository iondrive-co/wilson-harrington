import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WaterPropulsionSystemTest {
    @Test
    public void testCalculateTotalTons() {
        // Test that higher deltaV requires more mass
        assertTrue(WaterPropulsionSystem.STEAM.calculateTotalTons(2.0) >
                WaterPropulsionSystem.STEAM.calculateTotalTons(1.0));

        // Test that zero deltaV requires baseline mass
        assertEquals(1.0, WaterPropulsionSystem.STEAM.calculateTotalTons(0.0), 0.01);

        // Test that more efficient engines need less mass for same deltaV
        double deltaV = 2.0;
        assertTrue(WaterPropulsionSystem.STEAM.calculateTotalTons(deltaV) >
                WaterPropulsionSystem.PLASMA_ION.calculateTotalTons(deltaV));
    }

    @Test
    public void testCalculateRequiredPropellant() {
        double cargoTons = 10.0;

        // Test that higher deltaV uses more propellant
        assertTrue(WaterPropulsionSystem.STEAM.calculateRequiredPropellant(2.0, cargoTons) >
                WaterPropulsionSystem.STEAM.calculateRequiredPropellant(1.0, cargoTons));

        // Test edge cases
        assertEquals(0.0, WaterPropulsionSystem.STEAM.calculateRequiredPropellant(0.0, cargoTons), 0.01);
        assertEquals(0.0, WaterPropulsionSystem.STEAM.calculateRequiredPropellant(1.0, 0.0), 0.01);

        // Test that more efficient engines use less propellant
        double deltaV = 2.0;
        assertTrue(WaterPropulsionSystem.STEAM.calculateRequiredPropellant(deltaV, cargoTons) >
                WaterPropulsionSystem.PLASMA_ION.calculateRequiredPropellant(deltaV, cargoTons));
    }

    @Test
    public void testEfficiencyOrdering() {
        double deltaV = 2.0;
        double cargoTons = 10.0;

        // Test ordering of efficiency for total mass (STEAM < ELECTROLYSIS < MICROWAVE < PLASMA)
        assertTrue(WaterPropulsionSystem.STEAM.calculateTotalTons(deltaV) >
                WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION.calculateTotalTons(deltaV));
        assertTrue(WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION.calculateTotalTons(deltaV) >
                WaterPropulsionSystem.MICROWAVE_ELECTROTHERMAL.calculateTotalTons(deltaV));
        assertTrue(WaterPropulsionSystem.MICROWAVE_ELECTROTHERMAL.calculateTotalTons(deltaV) >
                WaterPropulsionSystem.PLASMA_ION.calculateTotalTons(deltaV));

        // Test same ordering for propellant usage
        assertTrue(WaterPropulsionSystem.STEAM.calculateRequiredPropellant(deltaV, cargoTons) >
                WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION.calculateRequiredPropellant(deltaV, cargoTons));
        assertTrue(WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION.calculateRequiredPropellant(deltaV, cargoTons) >
                WaterPropulsionSystem.MICROWAVE_ELECTROTHERMAL.calculateRequiredPropellant(deltaV, cargoTons));
        assertTrue(WaterPropulsionSystem.MICROWAVE_ELECTROTHERMAL.calculateRequiredPropellant(deltaV, cargoTons) >
                WaterPropulsionSystem.PLASMA_ION.calculateRequiredPropellant(deltaV, cargoTons));
    }

    @Test
    public void sanityTestValues() {
        System.out.println(WaterPropulsionSystem.STEAM.calculateTotalTons(8));
        System.out.println(WaterPropulsionSystem.STEAM.calculateRequiredPropellant(8, 10));
    }
}