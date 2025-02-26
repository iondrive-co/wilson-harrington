package wh;

public record TransferResult(
        double deltaV,          // Total delta-V in km/s
        double timeOfFlight,    // Transfer time in days
        double[] departureV,    // Departure velocity vector [vx, vy, vz] in AU/year
        double[] arrivalV)  {    // Arrival velocity vector [vx, vy, vz] in AU/year) {
}
