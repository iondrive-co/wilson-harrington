public class TransferCalculator {
    private static final double MU = 39.478; // GM of Sun in AU^3/year^2

    public static class TransferResult {
        public final double deltaV;          // Total delta-V in km/s
        public final double timeOfFlight;    // Transfer time in days
        public final double[] departureV;    // Departure velocity vector [vx, vy, vz] in AU/year
        public final double[] arrivalV;      // Arrival velocity vector [vx, vy, vz] in AU/year

        public TransferResult(double deltaV, double timeOfFlight,
                              double[] departureV, double[] arrivalV) {
            this.deltaV = deltaV;
            this.timeOfFlight = timeOfFlight;
            this.departureV = departureV;
            this.arrivalV = arrivalV;
        }
    }

    public static TransferResult calculateHohmannTransfer(double[] r1, double[] r2) {
        double radius1 = magnitude(r1);
        double radius2 = magnitude(r2);
        double orbitVel1 = Math.sqrt(MU / radius1);
        double orbitVel2 = Math.sqrt(MU / radius2);
        double a_transfer = (radius1 + radius2) / 2.0;
        double v1_t = Math.sqrt(MU * (2/radius1 - 1/a_transfer));
        double v2_t = Math.sqrt(MU * (2/radius2 - 1/a_transfer));
        double planeChangePenalty = 0.0;
        if (radius1 != radius2) {
            double angle = Math.acos(Math.min(1.0, Math.max(-1.0,
                    dotProduct(normalize(r1), normalize(r2)))));
            planeChangePenalty = orbitVel1 * Math.sin(angle/4);
        }
        double deltaV = (Math.abs(v1_t - orbitVel1) +
                Math.abs(orbitVel2 - v2_t) +
                planeChangePenalty) * Main.DIFFICULTY_SCALE;
        double timeOfFlight = Math.PI * Math.sqrt(Math.pow(a_transfer, 3) / MU) * 365.25;
        double[] departureV = scaleVector(normalize(r1), v1_t);
        double[] arrivalV = scaleVector(normalize(r2), v2_t);
        return new TransferResult(deltaV, timeOfFlight, departureV, arrivalV);
    }

    public static TransferResult calculateDirectTransfer(double[] r1, double[] r2, double timeOfFlight) {
        TransferResult hohmann = calculateHohmannTransfer(r1, r2);
        return new TransferResult(hohmann.deltaV * 1.5, timeOfFlight,
                hohmann.departureV, hohmann.arrivalV);
    }

    private static double[] normalize(double[] v) {
        double mag = magnitude(v);
        if (mag < 1e-10) return new double[]{0, 0, 0};
        return scaleVector(v, 1.0/mag);
    }

    private static double[] scaleVector(double[] v, double scale) {
        return new double[]{v[0] * scale, v[1] * scale, v[2] * scale};
    }

    private static double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double magnitude(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
}