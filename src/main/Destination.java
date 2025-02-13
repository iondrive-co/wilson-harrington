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

        OrbitalMechanics.OrbitalState state = OrbitalMechanics.calculateOrbitalState(
                type.orbitalRadius, type.eccentricity, type.inclination,
                type.argumentOfPerihelion, type.ascendingNode, meanAnomaly);

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

        // Calculate hyperbolic excess velocity
        double v_infinity = Math.sqrt(
                Math.pow(earthTransfer.arrivalV[0] - velocity[0], 2) +
                        Math.pow(earthTransfer.arrivalV[1] - velocity[1], 2) +
                        Math.pow(earthTransfer.arrivalV[2] - velocity[2], 2)
        ) * 29.784; // Convert AU/year to km/s

        // Calculate arrival conditions
        double r_target = type == DestinationType.EARTH_LEO ? 6578.0 : 60000.0; // km

        // Calculate capture delta-V with aerobraking
        double v_peri = Math.sqrt(v_infinity * v_infinity + 2 * EARTH_MU / (EARTH_ATMOSPHERE + 6378.0));
        double v_final = Math.sqrt(EARTH_MU / r_target);

        // Assume aerobraking can remove up to 7 km/s
        double aerobrake_dv = Math.min(7.0, v_peri - v_final);
        double capture_dv = v_final - (v_peri - aerobrake_dv);

        this.deltaVEfficient = earthTransfer.deltaV + capture_dv;
        this.timeEfficient = earthTransfer.timeOfFlight;

        // Fast transfer
        TransferCalculator.TransferResult directResult =
                TransferCalculator.calculateDirectTransfer(asteroidPos, asteroidVel, position, velocity,
                        earthTransfer.timeOfFlight * 0.7);

        this.deltaVFast = directResult.deltaV + capture_dv * 1.2; // 20% more for faster capture
        this.timeFast = directResult.timeOfFlight;

        // Cycler costs nothing once established
        this.deltaVCycler = 0.0;
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