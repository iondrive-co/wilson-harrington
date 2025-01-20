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
     * Tons of water required to accelerate the dry weight + that water fuel to the target delta v
     */
    double tonsFuelToAccelerateTo(double targetDeltaV) {
        final double dryWeightTons = getDryWeightKgs() / 1_000.0;
        // Use binary search to solve m0 (initial weight) in the rocket equation: deltaV = Isp * g * ln(m0/m1)
        double lowFuelWeightTons = 0.001;
        double highFuelWeightTons = 1000;
        // If 1000 tons isn't enough, keep increasing upper bound
        while (deltaVFromBurning((int)highFuelWeightTons, dryWeightTons) < targetDeltaV) {
            highFuelWeightTons *= 10;
        }
        while (highFuelWeightTons - lowFuelWeightTons > 0.0001) {
            double midFuelWeightTons = (lowFuelWeightTons + highFuelWeightTons) / 2;
            double achievableDeltaV = deltaVFromBurning(midFuelWeightTons, dryWeightTons);

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
        return (getImpulseMetersSec() * Math.log((fuelWeightTons + dryWeightTons) / dryWeightTons)) / 1000.0; // Convert to km/s
    }
}
