public class TransferCalculator {
    private static final double MU = 39.478; // GM of Sun in AU^3/year^2
    private static final double AU_PER_YEAR_TO_KM_SEC = 29.784; // Convert AU/year to km/s

    public static class TransferResult {
        public final double deltaV;          // Total delta-V in km/s
        public final double timeOfFlight;    // Transfer time in days
        public final double departureDate;   // Optimal departure date in days from epoch
        public final double[] departureV;    // Departure velocity vector [vx, vy, vz] in AU/year
        public final double[] arrivalV;      // Arrival velocity vector [vx, vy, vz] in AU/year

        public TransferResult(double deltaV, double timeOfFlight, double departureDate,
                              double[] departureV, double[] arrivalV) {
            this.deltaV = deltaV;
            this.timeOfFlight = timeOfFlight;
            this.departureDate = departureDate;
            this.departureV = departureV;
            this.arrivalV = arrivalV;
        }
    }

    public static TransferResult calculateHohmannTransfer(double[] r1, double[] v1,
                                                          double[] r2, double[] v2) {
        double radius1 = magnitude(r1);
        double radius2 = magnitude(r2);

        // Calculate velocities on initial and final circular orbits
        double v_i = Math.sqrt(MU / radius1);
        double v_f = Math.sqrt(MU / radius2);

        // Calculate semi-major axis of transfer orbit
        double a_transfer = (radius1 + radius2) / 2.0;

        // Calculate velocities at periapsis and apoapsis of transfer orbit
        double v_pe = Math.sqrt(MU * (2.0/radius1 - 1.0/a_transfer));
        double v_ap = Math.sqrt(MU * (2.0/radius2 - 1.0/a_transfer));

        // Calculate plane change
        double planeChangeDV = calculatePlaneChangeDeltaV(r1, r2, v_i);

        // Convert from AU/year to km/s
        double deltaV = (Math.abs(v_pe - v_i) + Math.abs(v_f - v_ap) + planeChangeDV) * AU_PER_YEAR_TO_KM_SEC;
        double timeOfFlight = Math.PI * Math.sqrt(Math.pow(a_transfer, 3) / MU) * 365.25;

        double[] departureV = new double[]{
                v_pe * r1[0] / radius1,
                v_pe * r1[1] / radius1,
                v_pe * r1[2] / radius1
        };

        double[] arrivalV = new double[]{
                v_ap * r2[0] / radius2,
                v_ap * r2[1] / radius2,
                v_ap * r2[2] / radius2
        };

        return new TransferResult(deltaV, timeOfFlight, 0.0, departureV, arrivalV);
    }

    public static double calculatePlaneChangeDeltaV(double[] r1, double[] r2, double v_orbit) {
        double[] h1 = normalize(crossProduct(r1, new double[]{0, 0, 1}), 1.0);
        double[] h2 = normalize(crossProduct(r2, new double[]{0, 0, 1}), 1.0);
        double angle = Math.acos(Math.min(1.0, Math.max(-1.0, dotProduct(h1, h2))));
        return v_orbit * Math.sin(angle/2);
    }

    public static TransferResult calculateDirectTransfer(double[] r1, double[] v1,
                                                         double[] r2, double[] v2,
                                                         double timeOfFlight) {
        TransferResult result = calculateHohmannTransfer(r1, new double[3], r2, new double[3]);

        // Calculate delta-V at both ends
        double dv1 = magnitude(subtract(result.departureV, v1)) * AU_PER_YEAR_TO_KM_SEC;
        double dv2 = magnitude(subtract(v2, result.arrivalV)) * AU_PER_YEAR_TO_KM_SEC;

        return new TransferResult(dv1 + dv2, timeOfFlight, 0.0,
                result.departureV, result.arrivalV);
    }

    private static double[] crossProduct(double[] a, double[] b) {
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double magnitude(double[] v) {
        return Math.sqrt(dotProduct(v, v));
    }

    private static double[] normalize(double[] v, double scale) {
        double mag = magnitude(v);
        return new double[]{
                v[0] * scale / mag,
                v[1] * scale / mag,
                v[2] * scale / mag
        };
    }

    private static double[] subtract(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }
}