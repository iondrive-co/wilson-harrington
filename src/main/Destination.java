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

    private double[] position;    // [x, y, z] in AU
    private double[] velocity;    // [vx, vy, vz] in AU/year

    private static final double EARTH_MU = 398600.4418; // km³/s²
    private static final double EARTH_ATMOSPHERE = 100.0; // km - height for aerocapture

    public Destination(DestinationType type) {
        this.type = type;
        this.position = new double[3];
        this.velocity = new double[3];
    }

    public void updateDaily(int dayInOrbit, int totalDaysInOrbit) {
        double perihelionWeight = 1.0 - (double) dayInOrbit / totalDaysInOrbit;
        this.salePricePerKg = type.calculateSalePricePerKg(perihelionWeight, ThreadLocalRandom.current());

        double meanMotion = 2 * Math.PI / totalDaysInOrbit;
        double meanAnomaly = meanMotion * dayInOrbit;

        OrbitalMechanics.OrbitalState state;
        if (Main.ENABLE_ORBITAL_MECHANICS) {
            state = OrbitalMechanics.calculateOrbitalState(type.orbitalRadius, type.eccentricity,
                    type.inclination, type.argumentOfPerihelion, type.ascendingNode, meanAnomaly);
        } else {
            state = new OrbitalMechanics.OrbitalState(new double[]{type.orbitalRadius, 0, 0}, new double[]{0, 0, 0}, 0);
        }

        this.position = state.position;
        this.velocity = state.velocity;

        double[] asteroidPos = Main.ASTEROID_STATE.getPosition();
        double[] asteroidVel = Main.ASTEROID_STATE.getVelocity();

        calculateTransfers(asteroidPos, asteroidVel);
    }

    private void calculateTransfers(double[] asteroidPos, double[] asteroidVel) {
        if (type == DestinationType.EARTH_LEO || type == DestinationType.EML1) {
            calculateEarthRelativeTransfers(asteroidPos, asteroidVel);
        } else {
            calculateHeliocentricTransfers(asteroidPos, asteroidVel);
        }
    }

    private void calculateEarthRelativeTransfers(double[] asteroidPos, double[] asteroidVel) {
        // Calculate heliocentric transfer
        TransferCalculator.TransferResult earthTransfer =
                TransferCalculator.calculateHohmannTransfer(asteroidPos, asteroidVel, position, velocity);

        // Calculate hyperbolic excess velocity (relative velocity at Earth's sphere of influence)
        double v_infinity = earthTransfer.deltaV;

        // Calculate arrival conditions
        double r_target = type == DestinationType.EARTH_LEO ? 6578.0 : 60000.0; // km
        double v_final = Math.sqrt(EARTH_MU / r_target);

        // Total deltaV is heliocentric transfer plus capture requirement
        double capture_dv = v_infinity + v_final;  // Need enough to overcome hyperbolic excess AND reach orbital velocity

        // Reduce capture dv by up to 7 km/s to allow for aerobraking
        if (Main.ENABLE_AEROBRAKING) {
            capture_dv = Math.max(0, capture_dv - 7.0);
        }

        this.deltaVEfficient = earthTransfer.deltaV + capture_dv;
        this.timeEfficient = earthTransfer.timeOfFlight;

        // Fast transfer uses same approach with 20% penalty
        TransferCalculator.TransferResult directResult =
                TransferCalculator.calculateDirectTransfer(asteroidPos, asteroidVel, position, velocity,
                        earthTransfer.timeOfFlight * 0.7);

        this.deltaVFast = directResult.deltaV + capture_dv * 1.2;
        this.timeFast = directResult.timeOfFlight;
        // There is no capture for a cycler, just pay the delta V cost
        this.deltaVCycler = earthTransfer.deltaV;
        this.timeCycler = 0.0;
    }

    private void calculateHeliocentricTransfers(double[] asteroidPos, double[] asteroidVel) {
        TransferCalculator.TransferResult hohmannResult =
                TransferCalculator.calculateHohmannTransfer(asteroidPos, asteroidVel, position, velocity);
        this.deltaVEfficient = hohmannResult.deltaV;
        this.timeEfficient = hohmannResult.timeOfFlight;

        TransferCalculator.TransferResult directResult =
                TransferCalculator.calculateDirectTransfer(asteroidPos, asteroidVel, position, velocity,
                        hohmannResult.timeOfFlight * 0.7);
        this.deltaVFast = directResult.deltaV;
        this.timeFast = directResult.timeOfFlight;

        this.deltaVCycler = hohmannResult.deltaV * 0.3;
        this.timeCycler = hohmannResult.timeOfFlight * 0.8;
    }
}