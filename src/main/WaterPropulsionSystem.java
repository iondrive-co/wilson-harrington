/**
 * Assume that water buys us delta V through some water based propulsion system
 */
public enum WaterPropulsionSystem {
    STEAM(2_000),
    ELECTROLYSIS_COMBUSTION(4_000),
    MICROWAVE_ELECTROTHERMAL(10_000),
    PLASMA_ION(20_000);

    private final double specificImpulseMetersSec;

    WaterPropulsionSystem(double specificImpulseMetersSec) {
        this.specificImpulseMetersSec = specificImpulseMetersSec;
    }

    /**
     * Tons of water required to accelerate the dry weight + that water fuel to the target delta v
     */
    double fuelToAccelerate(double targetDeltaV, final double dryWeight) {
        // Use binary search to solve m0 (initial weight) in the rocket equation: deltaV = Isp * g * ln(m0/m1)
        double lowFuelWeightTons = 0.001;
        double highFuelWeightTons = 1000;
        // If 1000 tons isn't enough, keep increasing upper bound
        while (deltaVFromBurning((int)highFuelWeightTons, dryWeight) < targetDeltaV) {
            highFuelWeightTons *= 10;
        }
        while (highFuelWeightTons - lowFuelWeightTons > 0.0001) {
            double midFuelWeightTons = (lowFuelWeightTons + highFuelWeightTons) / 2;
            double achievableDeltaV = deltaVFromBurning(midFuelWeightTons, dryWeight);

            if (Math.abs(achievableDeltaV - targetDeltaV) < 0.0001) {
                return midFuelWeightTons;
            } else if (achievableDeltaV < targetDeltaV) {
                lowFuelWeightTons = midFuelWeightTons;
            } else {
                highFuelWeightTons = midFuelWeightTons;
            }
        }
        return (lowFuelWeightTons + highFuelWeightTons) / 2;
    }

    /**
     * If we burn all the fuel weight so that only the dry weight remains, what delta-V do we get
     */
    double deltaVFromBurning(final double fuelWeightTons, final double dryWeightTons) {
        return (specificImpulseMetersSec * Math.log((fuelWeightTons + dryWeightTons) / dryWeightTons)) / 1000.0; // Convert to km/s
    }
}
