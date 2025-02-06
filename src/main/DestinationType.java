import java.util.Random;

public enum DestinationType {
    MERCURY("Mercury", 7, 9, 0.5, 0.39, 8) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 8 + perihelionWeight * 2 + random.nextDouble();
        }
    },
    EARTH_LEO("Earth LEO", 1, 2, 0.5, 1.01, 6) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 5 + random.nextDouble() * 3;
        }
    },
    MARS("Mars", 1.5, 3, 1, 1.52, 7) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            double aphelionWeight = 1.0 - perihelionWeight;
            return 6 + aphelionWeight * 2 + random.nextDouble();
        }
    },
    PSYCHE("Psyche", 3, 6, 2, 2.92, 8) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            double aphelionWeight = 1.0 - perihelionWeight;
            return 6 + aphelionWeight * 2 + random.nextDouble();
        }
    },
    EML1("EML1", 1, 2, 0.5, 1.01, 6) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 5 + random.nextDouble() * 3;
        }
    },
    DA1986("1986 DA", 1.5, 3, 1, 2.42, 6) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            double aphelionWeight = 1.0 - perihelionWeight;
            return 5.5 + aphelionWeight * 1.5 + random.nextDouble();
        }
    },
    ED85("2016 ED85", 1, 2, 0.5, 1.8, 5) {
        @Override
        public double calculateSalePricePerKg(double perihelionWeight, Random random) {
            return 5 + random.nextDouble() * 2;
        }
    };

    final String name;
    final double deltaVEfficient;
    final double deltaVFast;
    final double deltaVCycler;
    final double orbitalRadius;
    final double cyclerEstablishmentDeltaV;

    DestinationType(String name, double deltaVEfficient, double deltaVFast, double deltaVCycler,
                    double orbitalRadius, double cyclerEstablishmentDeltaV) {
        this.name = name;
        this.deltaVEfficient = deltaVEfficient;
        this.deltaVFast = deltaVFast;
        this.deltaVCycler = deltaVCycler;
        this.orbitalRadius = orbitalRadius;
        this.cyclerEstablishmentDeltaV = cyclerEstablishmentDeltaV;
    }

    public abstract double calculateSalePricePerKg(double perihelionWeight, Random random);

    public Destination createDestination() {
        return new Destination(this);
    }
}
