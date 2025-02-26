package wh;

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

    public Destination(DestinationType type) {
        this.type = type;
        this.position = new double[3];
    }

    public void updateDaily(int dayInOrbit, int totalDaysInOrbit) {
        double perihelionWeight = 1.0 - (double) dayInOrbit / totalDaysInOrbit;
        this.salePricePerKg = type.calculateSalePricePerKg(perihelionWeight, ThreadLocalRandom.current());
        double meanMotion = 2 * Math.PI / totalDaysInOrbit;
        double meanAnomaly = meanMotion * dayInOrbit;
        if (SimulationState.ENABLE_ORBITAL_MECHANICS) {
            this.position = MathsUtil.calculateOrbitalState(type.orbitalRadius, type.eccentricity,
                    type.inclination, type.argumentOfPerihelion, type.ascendingNode, meanAnomaly);
        } else {
            this.position = new double[]{type.orbitalRadius, 0, 0};
        }
        final double[] asteroidPos = SimulationState.ASTEROID_STATE.getPosition();
        final double[] results = (type == DestinationType.EARTH_LEO || type == DestinationType.EML1) ?
                MathsUtil.calculateEarthRelativeTransfers(asteroidPos, position,
                        type == DestinationType.EARTH_LEO, SimulationState.ENABLE_AEROBRAKING) :
                MathsUtil.calculateHeliocentricTransfers(asteroidPos, position);
        this.deltaVEfficient = results[0];
        this.timeEfficient = results[1];
        this.deltaVFast = results[2];
        this.timeFast = results[3];
        this.deltaVCycler = results[4];
        this.timeCycler = results[5];
    }
}