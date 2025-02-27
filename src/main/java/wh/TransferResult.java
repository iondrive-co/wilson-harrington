package wh;

import jaid.collection.DoublesVector;

public record TransferResult(
        double deltaV,          // Total delta-V in km/s
        double timeOfFlight,    // Transfer time in days
        DoublesVector departureV,    // Departure velocity vector [vx, vy, vz] in AU/year
        DoublesVector arrivalV)  {    // Arrival velocity vector [vx, vy, vz] in AU/year) {
}
