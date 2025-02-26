package wh;

import java.util.Random;

public enum DestinationType {
    // Values from NASA JPL Small-Body Database where available
    MERCURY("Mercury", 7, 9, 0.5, 0.39, 8,
            0.205,      // eccentricity
            7.005,      // inclination (degrees)
            77.456,     // argument of perihelion (degrees)
            48.331) {   // longitude of ascending node (degrees)
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 8 + perihelionWeight * 2 + random.nextDouble();
        }
    },
    EARTH_LEO("Earth LEO", 1, 2, 0.5, 1.01, 6,
            0.0167,     // Earth's orbital eccentricity
            0.0,        // Assuming equatorial LEO
            102.9,      // Earth's argument of perihelion
            348.739) {  // Earth's ascending node
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 5 + random.nextDouble() * 3;
        }
    },
    MARS("Mars", 1.5, 3, 1, 1.52, 7,
            0.0934,     // Mars orbital eccentricity
            1.850,      // inclination
            286.502,    // argument of perihelion
            49.558) {   // ascending node
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            double aphelionWeight = 1.0 - perihelionWeight;
            return 6 + aphelionWeight * 2 + random.nextDouble();
        }
    },
    PSYCHE("Psyche", 3, 6, 2, 2.92, 8,
            0.140,      // actual Psyche orbital elements
            3.095,
            95.417,
            150.195) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            double aphelionWeight = 1.0 - perihelionWeight;
            return 6 + aphelionWeight * 2 + random.nextDouble();
        }
    },
    EML1("EML1", 1, 2, 0.5, 1.01, 6,
            0.0,        // Lagrange points are essentially circular
            5.145,      // Approximate inclination relative to ecliptic
            0.0,        // N/A for nearly circular orbit
            0.0) {      // N/A for nearly circular orbit
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 5 + random.nextDouble() * 3;
        }
    },
    DA1986("1986 DA", 1.5, 3, 1, 2.42, 6,
            0.2228,     // actual orbital elements for 1986 DA
            4.305,
            132.462,
            272.459) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            double aphelionWeight = 1.0 - perihelionWeight;
            return 5.5 + aphelionWeight * 1.5 + random.nextDouble();
        }
    },
    ED85("2016 ED85", 1, 2, 0.5, 1.8, 5,
            0.1957,     // actual orbital elements for 2016 ED85
            4.512,
            266.121,
            147.469) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 5 + random.nextDouble() * 2;
        }
    };

    final String name;
    final double deltaVEfficient;
    final double deltaVFast;
    final double deltaVCycler;
    final double orbitalRadius;  // semi-major axis in AU
    final double cyclerEstablishmentDeltaV;

    final double eccentricity;           // orbital eccentricity (0 = circular, 1 = parabolic)
    final double inclination;            // orbital inclination in degrees
    final double argumentOfPerihelion;   // argument of perihelion in degrees
    final double ascendingNode;          // longitude of ascending node in degrees

    DestinationType(String name, double deltaVEfficient, double deltaVFast, double deltaVCycler,
                    double orbitalRadius, double cyclerEstablishmentDeltaV,
                    double eccentricity, double inclination,
                    double argumentOfPerihelion, double ascendingNode) {
        this.name = name;
        this.deltaVEfficient = deltaVEfficient;
        this.deltaVFast = deltaVFast;
        this.deltaVCycler = deltaVCycler;
        this.orbitalRadius = orbitalRadius;
        this.cyclerEstablishmentDeltaV = cyclerEstablishmentDeltaV;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.argumentOfPerihelion = argumentOfPerihelion;
        this.ascendingNode = ascendingNode;
    }

    public abstract double calculateSalePricePerKg(double perihelionWeight, Random random);

    public Destination createDestination() {
        return new Destination(this);
    }
}