public class OrbitalMechanics {

    public static class OrbitalState {
        public final double[] position;    // [x, y, z] in AU

        public OrbitalState(double[] position) {
            this.position = position;
        }
    }

    public static OrbitalState calculateOrbitalState(
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

        return new OrbitalState(position);
    }

    private static double solveKepler(double M, double e) {
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

    private static double calculateTrueAnomaly(double E, double e) {
        double cosE = Math.cos(E);
        double sinE = Math.sin(E);
        return Math.atan2(Math.sqrt(1 - e*e) * sinE, cosE - e);
    }
}