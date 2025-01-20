import java.util.EnumMap;

public record Hauler(HaulerClass type, EnumMap<WaterPropulsionSystem, Integer> engines, EnumMap<PowerSource, Integer> power) {
    public Hauler {
        final int requiredPower = engines.entrySet().stream()
                .mapToInt(entry -> entry.getKey().requiredPowerKW * entry.getValue())
                .sum();
        final int availablePower = power.entrySet().stream()
                .mapToInt(entry -> entry.getKey().powerKw * entry.getValue())
                .sum();
        if (requiredPower > availablePower) {
            throw new IllegalArgumentException("Required power " + requiredPower + "kW in " + engines + " but only " +
                    availablePower + "kW was available in " + power);
        }
        if (!engines.entrySet().stream().filter(e -> e.getValue() > e.getKey().maxPerShip).toList().isEmpty()) {
            throw new IllegalArgumentException("Too many engines of a type in " + engines);
        }
    }

    public double getImpulseMetersSec() {
        return engines.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().specificImpulseMetersSec * entry.getKey().conversionEfficiency * entry.getValue())
                .sum();
    }

    public int getDryWeightKgs() {
        return type.dryWeightKgs +
                engines.entrySet().stream().mapToInt(entry -> entry.getKey().weightKg * entry.getValue()).sum() +
                power.entrySet().stream().mapToInt(entry -> entry.getKey().weightKgs * entry.getValue()).sum();
    }

    /**
     * Amount of water required to accelerate the dry weight + that water fuel to the target delta v
     */
    double kgsFuelToAccelerateTo(double targetDeltaV) {
        final double dryWeightKgs = getDryWeightKgs();
        // Use binary search to solve m0 (initial weight) in the rocket equation: deltaV = Isp * g * ln(m0/m1)
        double lowFuelWeightKgs = 0.001;
        double highFuelWeightKgs = 1_000_000;
        // If highFuelWeightKgs isn't enough, keep increasing upper bound
        while (deltaVFromBurning((int)highFuelWeightKgs, dryWeightKgs) < targetDeltaV) {
            highFuelWeightKgs *= 10;
        }
        while (highFuelWeightKgs - lowFuelWeightKgs > 0.0001) {
            double midFuelWeightKgs = (lowFuelWeightKgs + highFuelWeightKgs) / 2;
            double achievableDeltaV = deltaVFromBurning(midFuelWeightKgs, dryWeightKgs);

            if (Math.abs(achievableDeltaV - targetDeltaV) < 0.0001) {
                return midFuelWeightKgs;
            } else if (achievableDeltaV < targetDeltaV) {
                lowFuelWeightKgs = midFuelWeightKgs;
            } else {
                highFuelWeightKgs = midFuelWeightKgs;
            }
        }
        return (lowFuelWeightKgs + highFuelWeightKgs) / 2;
    }

    /**
     * If we burn all the fuel weight so that only the dry weight remains, what delta-V do we get
     */
    double deltaVFromBurning(final double fuelWeightKgs, final double dryWeightKgs) {
        return (getImpulseMetersSec() * Math.log((fuelWeightKgs + dryWeightKgs) / dryWeightKgs)) / 1000.0; // Convert to km/s
    }

    @Override
    public String toString() {
        return "Capacity " + type.maxCargoKgs + "kg dry weight " + getDryWeightKgs() + "kgs impulse " +
                getImpulseMetersSec() + "m/s";
    }
}
