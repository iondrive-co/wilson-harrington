package wh;

import jaid.collection.DoublesVector;

public class MathsUtil {

    public static final double SUN_MU = 39.478;          // Sun's GM in AU^3/year^2
    public static final double YEAR_TO_DAYS = 365.25;    // Days in a year
    public static final double EARTH_MU = 398600.4418;   // km³/s²

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
     * Calculate the optimal phase angle for a Hohmann transfer between orbits
     * This is the angle between departure and arrival positions for minimum energy
     */
    private static double calculateOptimalPhaseAngle(double r1, double r2) {
        // For a Hohmann transfer, the optimal phase angle is based on the orbital period
        double a_transfer = (r1 + r2) / 2.0;

        // Calculate transfer time (half period of transfer orbit)
        double transferTime = Math.PI * Math.sqrt(Math.pow(a_transfer, 3) / SUN_MU);

        // During this time, the target planet will move through angle = ω * t
        double omega2 = Math.sqrt(SUN_MU / Math.pow(r2, 3));  // Angular velocity of target
        double angleSwept = omega2 * transferTime;

        // The optimal phase angle is 180° (π) minus this swept angle
        return Math.PI - angleSwept;
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

    public static double[] calculateTransfers(DoublesVector asteroidPos, DoublesVector destPos,
                                              boolean isEarthRelative, boolean enableAerobraking) {
        // Common calculations
        double r1 = asteroidPos.magnitude();  // Asteroid distance from Sun (AU)
        double r2 = destPos.magnitude();      // Destination distance from Sun (AU)
        double v1 = Math.sqrt(SUN_MU / r1);   // Asteroid orbital velocity
        double v2 = Math.sqrt(SUN_MU / r2);   // Destination orbital velocity
        double phaseAngle = asteroidPos.angleBetween(destPos);

        // ---- EFFICIENT TRANSFER (Hohmann-like) ----
        // Basic Hohmann transfer calculation
        double a_transfer = (r1 + r2) / 2.0;
        double transferPeriod = Math.PI * Math.sqrt(Math.pow(a_transfer, 3) / SUN_MU);
        double time_efficient = transferPeriod * YEAR_TO_DAYS;

        // Calculate basic Hohmann delta-V
        double v1_transfer = Math.sqrt(SUN_MU * (2/r1 - 1/a_transfer));
        double v2_transfer = Math.sqrt(SUN_MU * (2/r2 - 1/a_transfer));
        double deltaV_efficient = Math.abs(v1_transfer - v1) + Math.abs(v2 - v2_transfer);

        // Add phase angle penalty (increases as we move away from optimal alignment)
        // Optimal angle depends on the bodies' relative positions in their orbits
        double optimalAngle = Math.PI - (transferPeriod * Math.sqrt(SUN_MU / Math.pow(r2, 3)));
        double angleDeviation = Math.abs(phaseAngle - optimalAngle);
        double phaseFactor = 1.0 + 2.0 * Math.pow(Math.sin(angleDeviation / 2), 2);
        deltaV_efficient *= phaseFactor;
        deltaV_efficient *= SimulationState.DIFFICULTY_SCALE;

        // ---- FAST TRANSFER (Direct path) ----
        // Calculate time based on a more direct path between the two orbits
        // Time scales with the average orbital radius (larger orbits = longer times)
        double avgRadius = (r1 + r2) / 2.0;

        // Direct path transit time based on orbital mechanics approximation
        // Proportional to the distance but with diminishing returns for larger distances
        double distanceFactor = Math.sqrt(Math.pow(r1 - r2, 2) + 2 * r1 * r2 * (1 - Math.cos(phaseAngle)));
        double time_fast = 40 * Math.sqrt(avgRadius) * Math.pow(distanceFactor, 0.6);

        // Direct transfers scale non-linearly with distance
        // Based on approximation of Lambert's problem solution
        double radiiRatio = Math.max(r1, r2) / Math.min(r1, r2);
        double baseMultiplier = 1.5 + 0.5 * radiiRatio;  // Scales with orbit size difference

        // Average orbital velocity
        double avgVelocity = (v1 + v2) / 2.0;

        // Time efficiency penalty - shorter trips require more delta-V
        // Fast trajectory time ratio compared to a theoretical minimum transfer time
        double minTransferTime = distanceFactor * 25; // Theoretical minimum time
        double timeEfficiencyFactor = Math.max(1.5, 3.0 * minTransferTime / time_fast);

        // Calculate total delta-V for fast route
        double deltaV_fast = avgVelocity * baseMultiplier * timeEfficiencyFactor * SimulationState.DIFFICULTY_SCALE;

        // ---- CYCLER TRANSFER ----
        // Cycler parameters depend on the specific orbits involved
        double cyclerPeriod;
        double deltaV_cycler;
        double time_cycler;

        // Calculate velocity vectors and relative velocity
        DoublesVector v1_vec = asteroidPos.normalize().scale((float)v1);
        DoublesVector v2_vec = destPos.normalize().scale((float)v2);
        double relVelocity = v1_vec.distance(v2_vec);

        if (isEarthRelative) {
            // Earth-asteroid cyclers (need to match specific cycler orbits)
            // Cycler orbit with period near asteroid's orbital period
            cyclerPeriod = 1.0 * YEAR_TO_DAYS;  // 1 year period cycler (in days)

            // Rendezvous delta-V depends on relative velocity at encounter
            // Small fraction of relative velocity needed for rendezvous
            deltaV_cycler = (0.2 + (0.1 * relVelocity)) * SimulationState.DIFFICULTY_SCALE;

            // Time is more regular for established Earth cyclers
            time_cycler = cyclerPeriod * 0.4;  // Fraction of cycler period
        } else {
            // Heliocentric cyclers between asteroids/planets
            // Typically synodic period of the two bodies
            double p1 = 2 * Math.PI * Math.sqrt(Math.pow(r1, 3) / SUN_MU) * YEAR_TO_DAYS;
            double p2 = 2 * Math.PI * Math.sqrt(Math.pow(r2, 3) / SUN_MU) * YEAR_TO_DAYS;
            cyclerPeriod = (p1 * p2) / Math.abs(p1 - p2);  // Synodic period

            // Higher delta-V for non-Earth cycler rendezvous due to less frequent encounters
            deltaV_cycler = (0.3 + (0.15 * relVelocity)) * SimulationState.DIFFICULTY_SCALE;

            // Time depends on where in the cycle we encounter the cycler
            time_cycler = cyclerPeriod * 0.3;  // Fraction of synodic period
        }

        // Apply Earth capture delta-V for non-cycler transfers
        if (isEarthRelative) {
            double v_infinity = 0.5 * relVelocity;  // Approximation of hyperbolic excess velocity
            double r_target = 6578.0;  // LEO radius in km
            double v_orbit = Math.sqrt(EARTH_MU / r_target);  // Orbital velocity

            // Calculate capture delta-V with aerobraking if enabled
            double captureDV = v_infinity + v_orbit;
            if (enableAerobraking) {
                captureDV = Math.max(0, captureDV - 7.0);  // Aerobraking saves up to 7 km/s
            }

            // Add capture requirements to non-cycler trajectories
            deltaV_efficient += captureDV;
            deltaV_fast += captureDV * 1.2;  // Higher entry velocity needs more capture delta-V
        }

        return new double[] {
                deltaV_efficient, time_efficient,
                deltaV_fast, time_fast,
                deltaV_cycler, time_cycler
        };
    }


    public static double[] calculateEarthRelativeTransfers(DoublesVector asteroidPos, DoublesVector destinationPos,
                                                           boolean isLEO, boolean enableAerobraking) {
        return calculateTransfers(asteroidPos, destinationPos, true, enableAerobraking);
    }


    public static double[] calculateHeliocentricTransfers(DoublesVector asteroidPos, DoublesVector destinationPos) {
        return calculateTransfers(asteroidPos, destinationPos, false, true);
    }
}
