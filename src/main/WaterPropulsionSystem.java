/**
 * Assume that water buys us delta V through some water based propulsion system
 */
public enum WaterPropulsionSystem {
    STEAM(100),
    ELECTROLYSIS_COMBUSTION(275),
    PLASMA_ION(2000),
    MICROWAVE_ELECTROTHERMAL(650);

    private static final double GRAVITY = 9.81; // m/s^2
    private final double specificImpulse;

    WaterPropulsionSystem(double specificImpulse) {
        this.specificImpulse = specificImpulse;
    }

    private double calculateAchievableDeltaV(double fuelWeight, int dryWeight) {
        double exhaustVelocity = specificImpulse * GRAVITY;
        return (exhaustVelocity * Math.log((fuelWeight + dryWeight) / dryWeight)) / 1000.0; // Convert to km/s
    }

    public double calculateTotalTons(double targetDeltaV, final int dryWeight) {
        // Use binary search to solve m0 in the rocket equation: deltaV = Isp * g * ln(m0/m1)
        double low = 0.001;
        double high = 1000.0;
        // If 1000 tons isn't enough, keep increasing upper bound
        while (calculateAchievableDeltaV(high, dryWeight) < targetDeltaV) {
            high *= 10;
        }
        while (high - low > 0.0001) {
            double mid = (low + high) / 2;
            double achievableDeltaV = calculateAchievableDeltaV(mid, dryWeight);

            if (Math.abs(achievableDeltaV - targetDeltaV) < 0.0001) {
                return mid;
            } else if (achievableDeltaV < targetDeltaV) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2;
    }

    public double calculateRequiredPropellant(double deltaV, double totalWeight) {
        double exhaustVelocity = specificImpulse * GRAVITY;
        double massRatio = Math.exp((deltaV * 1000.0) / exhaustVelocity);
        double finalMass = totalWeight / massRatio;
        return totalWeight - finalMass;
    }
}
