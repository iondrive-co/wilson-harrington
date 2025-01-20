import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    final static Hauler WATER_HAULER = new Hauler(HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5)));
    static final int KGS_WATER_MINED_PER_DAY = 5_000;

    enum OptionType {
        EFFICIENT, FAST, CYCLER
    }

    public enum DestinationType {
        MERCURY("Mercury", 7, 9, 0.5, 120, 45, 15, 0.39, 8) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                return 8 + perihelionWeight * 2 + random.nextDouble();
            }
        },
        EARTH_LEO("Earth LEO", 1, 2, 0.5, 60, 15, 10, 1.01, 6) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                return 5 + random.nextDouble() * 3;
            }
        },
        MARS("Mars", 1.5, 3, 1, 75, 20, 15, 1.52, 7) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                double aphelionWeight = 1.0 - perihelionWeight;
                return 6 + aphelionWeight * 2 + random.nextDouble();
            }
        },
        PSYCHE("Psyche", 3, 6, 2, 360, 180, 90, 2.92, 8) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                double aphelionWeight = 1.0 - perihelionWeight;
                return 6 + aphelionWeight * 2 + random.nextDouble();
            }
        },
        EML1("EML1", 1, 2, 0.5, 60, 15, 10, 1.01, 6) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                return 5 + random.nextDouble() * 3;
            }
        },
        DA1986("1986 DA", 1.5, 3, 1, 120, 60, 30, 2.42, 6) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                double aphelionWeight = 1.0 - perihelionWeight;
                return 5.5 + aphelionWeight * 1.5 + random.nextDouble();
            }
        },
        ED85("2016 ED85", 1, 2, 0.5, 90, 30, 15, 1.8, 5) {
            @Override
            public double calculateSalePricePerKg(double perihelionWeight, Random random) {
                return 5 + random.nextDouble() * 2;
            }
        };

        final String name;
        final double deltaVEfficient;
        final double deltaVFast;
        final double deltaVCycler;
        final double timeEfficient;
        final double timeFast;
        final double timeCycler;
        final double orbitalRadius;
        final double cyclerEstablishmentDeltaV;

        DestinationType(String name, double deltaVEfficient, double deltaVFast, double deltaVCycler,
                        double timeEfficient, double timeFast, double timeCycler,
                        double orbitalRadius, double cyclerEstablishmentDeltaV) {
            this.name = name;
            this.deltaVEfficient = deltaVEfficient;
            this.deltaVFast = deltaVFast;
            this.deltaVCycler = deltaVCycler;
            this.timeEfficient = timeEfficient;
            this.timeFast = timeFast;
            this.timeCycler = timeCycler;
            this.orbitalRadius = orbitalRadius;
            this.cyclerEstablishmentDeltaV = cyclerEstablishmentDeltaV;
        }

        public abstract double calculateSalePricePerKg(double perihelionWeight, Random random);

        public Destination createDestination() {
            return new Destination(this);
        }
    }

    static class Destination {
        final DestinationType type;
        double salePricePerKg;
        double timeEfficient;

        public Destination(DestinationType type) {
            this.type = type;
            this.timeEfficient = type.timeEfficient;
        }

        public void updateDaily(int dayInOrbit, int totalDaysInOrbit) {
            double perihelionWeight = 1.0 - (double) dayInOrbit / totalDaysInOrbit;
            this.salePricePerKg = type.calculateSalePricePerKg(perihelionWeight, ThreadLocalRandom.current());
            this.timeEfficient = calculateHohmannTransferTime(2.613, type.orbitalRadius);
        }

        private double calculateHohmannTransferTime(double r1, double r2) {
            double G = 39.478;
            double M = 1.0;
            return Math.PI * Math.sqrt(Math.pow((r1 + r2) / 2, 3) / (G * M)) * 365.25;
        }
    }

    static class AsteroidState {
        double distanceFromSunAUs;
        int storedWaterKgs;

        public AsteroidState(double distanceFromSunAUs) {
            this.distanceFromSunAUs = distanceFromSunAUs;
            this.storedWaterKgs = 0;
        }

        public void updateDistanceFromSun(int dayInOrbit, int totalDaysInOrbit) {
            double phase = (2 * Math.PI * dayInOrbit) / totalDaysInOrbit;
            this.distanceFromSunAUs = 0.991 + 3.231 * (1 + Math.sin(phase)) / 2;
        }
    }

    static class ShipmentOption {
        Destination destination;
        double kgsWaterShipped;
        double kgsWaterReceived;
        double kgsWaterUsedForDeltaV;
        double profit;
        double deltaV;
        double time;

        public ShipmentOption(Destination destination, double kgsWaterShipped, double kgsWaterUsedForDeltaV,
                              double deltaV, double time) {
            this.destination = destination;
            this.kgsWaterShipped = kgsWaterShipped;
            this.kgsWaterUsedForDeltaV = kgsWaterUsedForDeltaV;
            this.kgsWaterReceived = Math.max(kgsWaterShipped - kgsWaterUsedForDeltaV, 0);
            this.profit = kgsWaterReceived > 0 ? kgsWaterReceived * destination.salePricePerKg : -1;
            this.deltaV = deltaV;
            this.time = time;
        }
    }

    public static void simulateDay(final List<Destination> destinations, final AsteroidState asteroidState,
                                   final int dayInOrbit, final int totalDaysInOrbit) {
        System.out.printf("Day %d | Distance from Sun: %.3f AU\n", dayInOrbit, asteroidState.distanceFromSunAUs);
        asteroidState.updateDistanceFromSun(dayInOrbit, totalDaysInOrbit);
        final int availableKgsWater = asteroidState.storedWaterKgs + KGS_WATER_MINED_PER_DAY;
        final int shippableKgsWater = Math.min(availableKgsWater, WATER_HAULER.type().maxCargoKgs);
        System.out.printf("Mined %d kg water, now available %d kg\n", KGS_WATER_MINED_PER_DAY, availableKgsWater);
        System.out.printf("%d kg hauler allows shipping %d kg of it\n", WATER_HAULER.getDryWeightKgs(), shippableKgsWater);

        for (Destination destination : destinations) {
            destination.updateDaily(dayInOrbit, totalDaysInOrbit);
        }

        displayTopOptions("Efficient Options", destinations, shippableKgsWater, OptionType.EFFICIENT);
        displayTopOptions("Fast Options", destinations, shippableKgsWater, OptionType.FAST);
        displayTopOptions("Cycler Options", destinations, shippableKgsWater, OptionType.CYCLER);

        if (allNonCyclerOptionsUnprofitable(destinations, shippableKgsWater)) {
            asteroidState.storedWaterKgs += KGS_WATER_MINED_PER_DAY;
            System.out.println("No profitable non-cycler options found. Water stored for future use.\n");
        } else {
            asteroidState.storedWaterKgs = Math.max(asteroidState.storedWaterKgs - shippableKgsWater, 0);
        }
    }

    private static void displayTopOptions(final String title, final List<Destination> destinations,
                                          final int shippableKgsWater, final OptionType optionType) {
        final List<ShipmentOption> options = new ArrayList<>();
        for (Destination destination : destinations) {
            ShipmentOption option = calculateShipmentOption(destination, shippableKgsWater, optionType);
            options.add(option);
        }
        options.sort((o1, o2) -> Double.compare(o2.profit, o1.profit));
        System.out.println(title + ":");
        System.out.printf("%-15s %-10s %-20s %-20s %-15s %-15s\n",
                "Destination", "Price/kg", "Shipped/Received (kg)", "kg Water Fuel/Delta-V", "Profit", "Time (days)");
        for (int i = 0; i < options.size(); i++) {
            ShipmentOption option = options.get(i);
            System.out.printf("%-15s %-10.2f %-20s %-20s %-15s %-15.2f\n",
                    option.destination.type.name,
                    option.destination.salePricePerKg,
                    String.format("%.0f/%s", option.kgsWaterShipped,
                            option.kgsWaterReceived > 0 ? String.format("%.0f", option.kgsWaterReceived) : "-"),
                    String.format("%.0f/%.2f", option.kgsWaterUsedForDeltaV, option.deltaV),
                    option.profit > 0 ? String.format("%.1f", option.profit) : "-",
                    option.time);
        }
        System.out.println();
    }

    public static boolean allNonCyclerOptionsUnprofitable(List<Destination> destinations, final int shippableKgsWater) {
        for (Destination destination : destinations) {
            ShipmentOption efficient = calculateShipmentOption(destination, shippableKgsWater, OptionType.EFFICIENT);
            ShipmentOption fast = calculateShipmentOption(destination, shippableKgsWater, OptionType.FAST);
            if (efficient.profit > 0 || fast.profit > 0) {
                return false;
            }
        }
        return true;
    }

    public static ShipmentOption calculateShipmentOption(final Destination destination, final int shippableKgsWater,
                                                         final OptionType optionType) {
        double deltaV, time;
        switch (optionType) {
            case EFFICIENT -> {
                deltaV = destination.type.deltaVEfficient;
                time = destination.timeEfficient;
            }
            case FAST -> {
                deltaV = destination.type.deltaVFast;
                time = destination.type.timeFast;
            }
            case CYCLER -> {
                deltaV = destination.type.deltaVCycler;
                time = destination.type.timeCycler;
            }
            default -> throw new IllegalArgumentException("Invalid option type");
        }

        double kgsWaterUsedForDeltaV = WATER_HAULER.kgsFuelToAccelerateTo(deltaV);
        return new ShipmentOption(destination, shippableKgsWater, kgsWaterUsedForDeltaV, deltaV, time);
    }

    public static void main(String[] args) throws Exception {
        List<Destination> destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            destinations.add(type.createDestination());
        }
        System.out.println("Establishment Costs for cycler " + WATER_HAULER);
        System.out.printf("%-15s %-20s\n", "Destination", "kgs water fuel/Delta-V");
        for (Destination destination : destinations) {
            double kgsFuelUsed = WATER_HAULER.kgsFuelToAccelerateTo(destination.type.cyclerEstablishmentDeltaV);
            System.out.printf("%-15s %-20s\n", destination.type.name,
                    String.format("%.0f/%.1f", kgsFuelUsed, destination.type.cyclerEstablishmentDeltaV));
        }

        AsteroidState asteroidState = new AsteroidState(2.613);
        int totalDaysInOrbit = 1537;
        System.out.println("Press enter to start");
        System.in.read();
        for (int dayInOrbit = 1; dayInOrbit <= totalDaysInOrbit; dayInOrbit++) {
            simulateDay(destinations, asteroidState, dayInOrbit, totalDaysInOrbit);
            Thread.sleep(5_000);
        }
    }
}