package wh;

import jaid.collection.DoublesVector;

public class AsteroidState {
    private final double semiMajorAxis;      // in AU
    private final double eccentricity;        // dimensionless
    private final double inclination;         // in degrees
    private final double argumentPerihelion;  // in degrees
    private final double ascendingNode;       // in degrees
    private final double meanAnomalyEpoch;    // in degrees, position at epoch
    private DoublesVector position;    // [x, y, z] in AU
    int storedWaterKgs;

    public AsteroidState(double semiMajorAxis, double eccentricity, double inclination,
                         double argumentPerihelion, double ascendingNode, double meanAnomalyEpoch) {
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.argumentPerihelion = argumentPerihelion;
        this.ascendingNode = ascendingNode;
        this.meanAnomalyEpoch = meanAnomalyEpoch;
        this.storedWaterKgs = 0;

        // Initialize with day 1 position
        updateOrbitalState(1, 1537);
    }

    public void updateDistanceFromSun(int dayInOrbit, int totalDaysInOrbit) {
        updateOrbitalState(dayInOrbit, totalDaysInOrbit);
    }

    private void updateOrbitalState(int dayInOrbit, int totalDaysInOrbit) {
        double meanMotion = 2 * Math.PI / totalDaysInOrbit;  // radians per day
        double meanAnomaly = Math.toRadians(meanAnomalyEpoch) + (meanMotion * dayInOrbit);

        this.position = MathsUtil.calculateOrbitalState(
                semiMajorAxis, eccentricity, inclination, argumentPerihelion, ascendingNode, meanAnomaly);
    }

    public double getDistanceFromSun() {
        return position.magnitude();
    }

    public DoublesVector getPosition() {
        return position;
    }

    public static AsteroidState wilsonHarrington() {
        return new AsteroidState(
                2.6249,   // semi-major axis (AU)
                0.63175,  // eccentricity
                2.7992,   // inclination (degrees)
                95.441,   // argument of perihelion (degrees)
                266.77,   // longitude of ascending node (degrees)
                356.37    // mean anomaly at epoch (degrees)
        );
    }
}