import java.util.concurrent.ThreadLocalRandom;

class Destination {
    final DestinationType type;
    double salePricePerKg;
    double timeEfficient;
    double timeFast;
    double timeCycler;
    double deltaVEfficient;
    double deltaVFast;
    double deltaVCycler;

    public Destination(DestinationType type) {
        this.type = type;
    }

    public void updateDaily(int dayInOrbit, int totalDaysInOrbit) {
        double perihelionWeight = 1.0 - (double) dayInOrbit / totalDaysInOrbit;
        this.salePricePerKg = type.calculateSalePricePerKg(perihelionWeight, ThreadLocalRandom.current());

        double phase = (2 * Math.PI * dayInOrbit) / totalDaysInOrbit;
        double asteroidDistance = 0.991 + 3.231 * (1 + Math.sin(phase)) / 2;

        this.timeEfficient = calculateHohmannTransferTime(asteroidDistance, type.orbitalRadius);
        this.timeFast = calculateFastTransferTime(asteroidDistance, type.orbitalRadius);
        this.timeCycler = calculateCyclerTime(asteroidDistance, type.orbitalRadius);

        this.deltaVEfficient = calculateHohmannDeltaV(asteroidDistance, type.orbitalRadius);
        this.deltaVFast = calculateFastDeltaV(asteroidDistance, type.orbitalRadius);
        this.deltaVCycler = calculateCyclerDeltaV(asteroidDistance, type.orbitalRadius);
    }

    private double calculateHohmannTransferTime(double r1, double r2) {
        double G = 39.478;
        double M = 1.0;
        return Math.PI * Math.sqrt(Math.pow((r1 + r2) / 2, 3) / (G * M)) * 365.25;
    }

    private double calculateFastTransferTime(double r1, double r2) {
        double directDistance = Math.abs(r2 - r1);
        double avgRadius = (r1 + r2) / 2;
        double scaleFactor = 0.4;

        double G = 39.478;
        double M = 1.0;
        double time = scaleFactor * Math.PI * Math.sqrt(Math.pow(avgRadius, 3) / (G * M)) * 365.25;
        double orbitChangePenalty = Math.pow(directDistance, 0.3) * 5;

        return time + orbitChangePenalty;
    }

    private double calculateCyclerTime(double r1, double r2) {
        double baseTime = calculateHohmannTransferTime(r1, r2);
        double minDistance = Math.min(0.991, r2);
        double minTime = calculateHohmannTransferTime(minDistance, r2);
        return 0.7 * minTime + 0.3 * baseTime;
    }

    private double calculateHohmannDeltaV(double r1, double r2) {
        // Standard Hohmann transfer deltaV calculation
        double mu = 39.478; // GM of Sun in AU^3/year^2

        // Calculate velocities
        double v1 = Math.sqrt(mu / r1);  // Initial orbital velocity
        double v2 = Math.sqrt(mu / r2);  // Final orbital velocity

        // Transfer orbit velocities
        double a = (r1 + r2) / 2;  // Semi-major axis of transfer orbit
        double vt1 = Math.sqrt(mu * (2/r1 - 1/a));  // Velocity at periapsis of transfer
        double vt2 = Math.sqrt(mu * (2/r2 - 1/a));  // Velocity at apoapsis of transfer

        // Total deltaV is sum of both burns
        return Math.abs(vt1 - v1) + Math.abs(v2 - vt2);
    }

    private double calculateFastDeltaV(double r1, double r2) {
        // Fast transfer uses more direct path but requires more deltaV
        double hohmannDeltaV = calculateHohmannDeltaV(r1, r2);
        double directnessFactor = 1.5 + Math.abs(r2 - r1) * 0.2;  // More direct paths need more deltaV
        return hohmannDeltaV * directnessFactor;
    }

    private double calculateCyclerDeltaV(double r1, double r2) {
        // Cyclers use established orbits, so deltaV is lower once established
        double hohmannDeltaV = calculateHohmannDeltaV(r1, r2);
        return hohmannDeltaV * 0.3;  // Significant reduction due to using established orbit
    }
}