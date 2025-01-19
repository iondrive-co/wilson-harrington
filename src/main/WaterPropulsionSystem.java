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

    private double calculateAchievableDeltaV(double totalMass) {
        double exhaustVelocity = specificImpulse * GRAVITY;
        double finalMass = 0.001; // Almost zero mass left
        return (exhaustVelocity * Math.log(totalMass / finalMass)) / 1000.0; // Convert to km/s
    }

    public double calculateTotalTons(double targetDeltaV) {
        // Use binary search to solve m0 in the rocket equation: deltaV = Isp * g * ln(m0/m1)
        double low = 0.001;
        double high = 1000.0;
        // If 1000 tons isn't enough, keep increasing upper bound
        while (calculateAchievableDeltaV(high) < targetDeltaV) {
            high *= 10;
        }
        while (high - low > 0.0001) {
            double mid = (low + high) / 2;
            double achievableDeltaV = calculateAchievableDeltaV(mid);

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

    public double calculateRequiredPropellant(double deltaV, double cargoTons) {
        double exhaustVelocity = specificImpulse * GRAVITY;
        double massRatio = Math.exp((deltaV * 1000.0) / exhaustVelocity);
        double finalMass = cargoTons / massRatio;
        return cargoTons - finalMass;
    }
}
