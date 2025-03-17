package wh;

import jaid.collection.DoublesVector;

public class MathsUtil {

    public static final double SUN_MU = 39.478;          // Sun's GM in AU^3/year^2
    public static final double YEAR_TO_DAYS = 365.25;    // Days in a year
    public static final double EARTH_MU = 398600.4418; // km³/s²

    /**
     * Solves Kepler's equation for eccentric anomaly
     * @param M Mean anomaly in radians
     * @param e Eccentricity
     * @return Eccentric anomaly in radians
     */
    public static double solveKepler(double M, double e) {
        double E = M;  // Initial guess
        double delta = 1.0;
        int iter = 0;
        while (Math.abs(delta) > 1e-8 && iter < 10) {
            delta = (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
            E -= delta;
            iter++;
        }
        return E;
    }

    /**
     * Calculates true anomaly from eccentric anomaly
     * @param E Eccentric anomaly in radians
     * @param e Eccentricity
     * @return True anomaly in radians
     */
    public static double calculateTrueAnomaly(double E, double e) {
        double cosE = Math.cos(E);
        double sinE = Math.sin(E);
        return Math.atan2(Math.sqrt(1 - e*e) * sinE, cosE - e);
    }

    /**
     * Calculates orbital state (position) from orbital elements
     * @param semiMajorAxis Semi-major axis in AU
     * @param eccentricity Eccentricity (dimensionless)
     * @param inclination Inclination in degrees
     * @param argumentPerihelion Argument of perihelion in degrees
     * @param ascendingNode Longitude of ascending node in degrees
     * @param meanAnomaly Mean anomaly in radians
     * @return Orbital state with position in AU
     */
    public static DoublesVector calculateOrbitalState(
            double semiMajorAxis, double eccentricity, double inclination,
            double argumentPerihelion, double ascendingNode, double meanAnomaly) {

        // Solve Kepler's equation
        double eccentricAnomaly = solveKepler(meanAnomaly, eccentricity);

        // Calculate true anomaly
        double trueAnomaly = calculateTrueAnomaly(eccentricAnomaly, eccentricity);

        // Calculate position in orbital plane
        double p = semiMajorAxis * (1 - eccentricity * eccentricity);
        double radius = p / (1 + eccentricity * Math.cos(trueAnomaly));

        double xPrime = radius * Math.cos(trueAnomaly);
        double yPrime = radius * Math.sin(trueAnomaly);

        // Convert angles to radians
        double inc = Math.toRadians(inclination);
        double argPeri = Math.toRadians(argumentPerihelion);
        double node = Math.toRadians(ascendingNode);

        // Calculate position vector
        double[] position = new double[3];
        position[0] = xPrime * (Math.cos(argPeri) * Math.cos(node) -
                Math.sin(argPeri) * Math.cos(inc) * Math.sin(node)) -
                yPrime * (Math.sin(argPeri) * Math.cos(node) +
                        Math.cos(argPeri) * Math.cos(inc) * Math.sin(node));

        position[1] = xPrime * (Math.cos(argPeri) * Math.sin(node) +
                Math.sin(argPeri) * Math.cos(inc) * Math.cos(node)) +
                yPrime * (Math.cos(argPeri) * Math.cos(inc) * Math.cos(node) -
                        Math.sin(argPeri) * Math.sin(node));

        position[2] = xPrime * Math.sin(argPeri) * Math.sin(inc) +
                yPrime * Math.cos(argPeri) * Math.sin(inc);

        return new DoublesVector(position);
    }

    /**
     * Calculates a Hohmann transfer between two positions in space
     *
     * @param r1 Current position vector [x, y, z] in AU
     * @param r2 Target position vector [x, y, z] in AU
     * @return Transfer parameters including delta-V and time
     */
    public static TransferResult calculateHohmannTransfer(final DoublesVector r1, final DoublesVector r2) {
        // Calculate the magnitudes of the position vectors (distances from Sun)
        double radius1 = r1.magnitude();
        double radius2 = r2.magnitude();

        // Calculate the orbital velocities at the current and target positions
        double orbitVel1 = Math.sqrt(SUN_MU / radius1);  // Circular orbit velocity at r1
        double orbitVel2 = Math.sqrt(SUN_MU / radius2);  // Circular orbit velocity at r2

        // Calculate semi-major axis of the transfer ellipse
        double a_transfer = (radius1 + radius2) / 2.0;

        // Calculate required velocities at departure and arrival points using vis-viva equation
        double v1_t = Math.sqrt(SUN_MU * (2/radius1 - 1/a_transfer));  // Velocity at periapsis of transfer
        double v2_t = Math.sqrt(SUN_MU * (2/radius2 - 1/a_transfer));  // Velocity at apoapsis of transfer

        // Calculate additional delta-V needed for plane change
        double planeChangePenalty = 0.0;
        if (radius1 != radius2) {
            // Calculate angle between position vectors
            double angle = r1.angleBetween(r2);

            // Apply simplified plane change cost (only a fraction of the full angle)
            planeChangePenalty = orbitVel1 * Math.sin(angle/4);
        }

        // Calculate total delta-V with difficulty scaling factor
        double deltaV = (Math.abs(v1_t - orbitVel1) +
                Math.abs(orbitVel2 - v2_t) +
                planeChangePenalty) * SimulationState.DIFFICULTY_SCALE;

        // Calculate time of flight using Kepler's third law (half-orbit of transfer ellipse)
        double timeOfFlight = Math.PI * Math.sqrt(Math.pow(a_transfer, 3) / SUN_MU) * YEAR_TO_DAYS;

        // Calculate velocity vectors at departure and arrival
        DoublesVector departureV = r1.normalize().scale((float)v1_t);
        DoublesVector arrivalV = r2.normalize().scale((float)v2_t);

        return new TransferResult(deltaV, timeOfFlight, departureV, arrivalV);
    }

    /**
     * Calculates a faster (but more expensive) direct transfer
     *
     * @param r1 Current position vector [x, y, z] in AU
     * @param r2 Target position vector [x, y, z] in AU
     * @param timeOfFlight Desired time of flight in days
     * @return Transfer parameters including delta-V and time
     */
    public static TransferResult calculateDirectTransfer(final DoublesVector r1, final DoublesVector r2, double timeOfFlight) {
        // Using Hohmann calculation with penalty factor for faster transfer
        TransferResult hohmann = calculateHohmannTransfer(r1, r2);
        return new TransferResult(hohmann.deltaV() * 1.5, timeOfFlight,
                hohmann.departureV(), hohmann.arrivalV());
    }

    /**
     * Calculates Earth-relative transfer parameters
     *
     * @param asteroidPos Asteroid position
     * @param destinationPos Destination position
     * @param isLEO Whether the destination is LEO (true) or EML1 (false)
     * @param enableAerobraking Whether aerobraking is enabled
     * @return Array of [deltaVEfficient, timeEfficient, deltaVFast, timeFast, deltaVCycler, timeCycler]
     */
    public static double[] calculateEarthRelativeTransfers(final DoublesVector asteroidPos, final DoublesVector destinationPos,
                                                           boolean isLEO, boolean enableAerobraking) {
        // Calculate heliocentric transfer
        TransferResult earthTransfer = calculateHohmannTransfer(asteroidPos, destinationPos);

        // Calculate hyperbolic excess velocity (relative velocity at Earth's sphere of influence)
        double v_infinity = earthTransfer.deltaV();

        // Calculate arrival conditions
        double r_target = isLEO ? 6578.0 : 60000.0; // km
        double v_final = Math.sqrt(EARTH_MU / r_target);

        // Total deltaV is heliocentric transfer plus capture requirement
        double capture_dv = v_infinity + v_final;  // Need enough to overcome hyperbolic excess AND reach orbital velocity

        // Reduce capture dv by up to 7 km/s to allow for aerobraking
        if (enableAerobraking) {
            capture_dv = Math.max(0, capture_dv - 7.0);
        }

        double deltaVEfficient = earthTransfer.deltaV() + capture_dv;
        double timeEfficient = earthTransfer.timeOfFlight();

        // Fast transfer uses same approach with 20% penalty
        TransferResult directResult = calculateDirectTransfer(
                asteroidPos, destinationPos, earthTransfer.timeOfFlight() * 0.7);

        double deltaVFast = directResult.deltaV() + capture_dv * 1.2;
        double timeFast = directResult.timeOfFlight();


        // For Earth orbit cyclers, we need small delta-V to transfer on/off but travel time is still significant
        double deltaVCycler = 0.2;
        double timeCycler = timeEfficient * 0.75;

        return new double[] {
                deltaVEfficient, timeEfficient,
                deltaVFast, timeFast,
                deltaVCycler, timeCycler
        };
    }

    /**
     * Calculates heliocentric transfer parameters
     *
     * @param asteroidPos Asteroid position
     * @param destinationPos Destination position
     * @return Array of [deltaVEfficient, timeEfficient, deltaVFast, timeFast, deltaVCycler, timeCycler]
     */
    public static double[] calculateHeliocentricTransfers(final DoublesVector asteroidPos, final DoublesVector destinationPos) {
        TransferResult hohmannResult = calculateHohmannTransfer(asteroidPos, destinationPos);
        double deltaVEfficient = hohmannResult.deltaV();
        double timeEfficient = hohmannResult.timeOfFlight();

        TransferResult directResult = calculateDirectTransfer(
                asteroidPos, destinationPos, hohmannResult.timeOfFlight() * 0.7);
        double deltaVFast = directResult.deltaV();
        double timeFast = directResult.timeOfFlight();

        // For cycler routes to non-Earth destinations we need a small delta-V to rendezvous with the cycler
        double deltaVCycler = 0.3;
        double timeCycler = hohmannResult.timeOfFlight() * 0.8;

        return new double[] {
                deltaVEfficient, timeEfficient,
                deltaVFast, timeFast,
                deltaVCycler, timeCycler
        };
    }
}
