import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    final static WaterHauler WATER_HAULER = WaterHauler.SMALL_STEAM;
    static final int WATER_MINED_PER_DAY_TONS = 5;

    enum OptionType {
        EFFICIENT, FAST, CYCLER
    }

    public enum DestinationType {
        MERCURY("Mercury", 7, 9, 0.5, 120, 45, 15, 0.39, 8) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                return 8000 + perihelionWeight * 2000 + random.nextDouble() * 1000;
            }
        },
        EARTH_LEO("Earth LEO", 1, 2, 0.5, 60, 15, 10, 1.01, 6) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                return 5000 + random.nextDouble() * 3000;
            }
        },
        MARS("Mars", 1.5, 3, 1, 75, 20, 15, 1.52, 7) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                double aphelionWeight = 1.0 - perihelionWeight;
                return 6000 + aphelionWeight * 2000 + random.nextDouble() * 1000;
            }
        },
        PSYCHE("Psyche", 3, 6, 2, 360, 180, 90, 2.92, 8) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                double aphelionWeight = 1.0 - perihelionWeight;
                return 6000 + aphelionWeight * 2000 + random.nextDouble() * 1000;
            }
        },
        EML1("EML1", 1, 2, 0.5, 60, 15, 10, 1.01, 6) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                return 5000 + random.nextDouble() * 3000;
            }
        },
        DA1986("1986 DA", 1.5, 3, 1, 120, 60, 30, 2.42, 6) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                double aphelionWeight = 1.0 - perihelionWeight;
                return 5500 + aphelionWeight * 1500 + random.nextDouble() * 1000;
            }
        },
        ED85("2016 ED85", 1, 2, 0.5, 90, 30, 15, 1.8, 5) {
            @Override
            public double calculateSalePrice(double perihelionWeight, Random random) {
                return 5000 + random.nextDouble() * 2000;
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

        public abstract double calculateSalePrice(double perihelionWeight, Random random);

        public Destination createDestination() {
            return new Destination(this);
        }
    }

    static class Destination {
        final DestinationType type;
        double salePricePerTon;
        double timeEfficient;

        public Destination(DestinationType type) {
            this.type = type;
            this.timeEfficient = type.timeEfficient;
        }

        public void updateDaily(int dayInOrbit, int totalDaysInOrbit) {
            double perihelionWeight = 1.0 - (double) dayInOrbit / totalDaysInOrbit;
            this.salePricePerTon = type.calculateSalePrice(perihelionWeight, ThreadLocalRandom.current());
            this.timeEfficient = calculateHohmannTransferTime(2.613, type.orbitalRadius);
        }

        private double calculateHohmannTransferTime(double r1, double r2) {
            double G = 39.478;
            double M = 1.0;
            return Math.PI * Math.sqrt(Math.pow((r1 + r2) / 2, 3) / (G * M)) * 365.25;
        }
    }

    static class AsteroidState {
        double orbitalRadius;
        int storedWaterTons;

        public AsteroidState(double orbitalRadius) {
            this.orbitalRadius = orbitalRadius;
            this.storedWaterTons = 0;
        }

        public void updateOrbitalRadius(int dayInOrbit, int totalDaysInOrbit) {
            double phase = (2 * Math.PI * dayInOrbit) / totalDaysInOrbit;
            this.orbitalRadius = 0.991 + 3.231 * (1 + Math.sin(phase)) / 2;
        }
    }

    static class ShipmentOption {
        Destination destination;
        double waterShipped;
        double waterReceived;
        double waterUsedForDeltaV;
        double profit;
        double deltaV;
        double time;

        public ShipmentOption(Destination destination, double waterShipped, double waterUsedForDeltaV, double deltaV, double time) {
            this.destination = destination;
            this.waterShipped = waterShipped;
            this.waterUsedForDeltaV = waterUsedForDeltaV;
            this.waterReceived = Math.max(waterShipped - waterUsedForDeltaV, 0);
            this.profit = waterReceived > 0 ? waterReceived * destination.salePricePerTon : -1;
            this.deltaV = deltaV;
            this.time = time;
        }
    }

    public static void simulateDay(List<Destination> destinations, AsteroidState asteroidState,
                                   int dayInOrbit, int totalDaysInOrbit, int topN) {
        System.out.printf("Day %d | Distance from Sun: %.3f AU\n", dayInOrbit, asteroidState.orbitalRadius);
        asteroidState.updateOrbitalRadius(dayInOrbit, totalDaysInOrbit);
        final int availableWater = asteroidState.storedWaterTons + WATER_MINED_PER_DAY_TONS;
        final int shippableWater = Math.min(availableWater, WATER_HAULER.maxCargoTons);
        System.out.printf("Mined %d tons water, now available %d tons, shippable %d tons\n", WATER_MINED_PER_DAY_TONS,
                availableWater, shippableWater);

        for (Destination destination : destinations) {
            destination.updateDaily(dayInOrbit, totalDaysInOrbit);
        }

        displayTopOptions("Efficient Options", destinations, shippableWater, OptionType.EFFICIENT, topN);
        displayTopOptions("Fast Options", destinations, shippableWater, OptionType.FAST, topN);
        displayTopOptions("Cycler Options", destinations, shippableWater, OptionType.CYCLER, topN);

        if (allNonCyclerOptionsUnprofitable(destinations, shippableWater)) {
            asteroidState.storedWaterTons += WATER_MINED_PER_DAY_TONS;
            System.out.println("No profitable non-cycler options found. Water stored for future use.\n");
        } else {
            asteroidState.storedWaterTons -= shippableWater;
        }
    }

    private static void displayTopOptions(String title, List<Destination> destinations,
                                          final int shippableWater, OptionType optionType, int topN) {
        System.out.println(title + ":");
        System.out.printf("%-15s %-10s %-20s %-20s %-15s %-15s\n",
                "Destination", "PPT ($)", "Shipped/Received (t)", "Fuel Used/Delta-V", "Profit ($)", "Time (days)");

        List<ShipmentOption> options = new ArrayList<>();
        for (Destination destination : destinations) {
            ShipmentOption option = calculateShipmentOption(destination, shippableWater, optionType);
            options.add(option);
        }

        options.sort((o1, o2) -> Double.compare(o2.profit, o1.profit));
        for (int i = 0; i < Math.min(topN, options.size()); i++) {
            ShipmentOption option = options.get(i);
            System.out.printf("%-15s %-10.2f %-20s %-20s %-15s %-15.2f\n",
                    option.destination.type.name,
                    option.destination.salePricePerTon,
                    String.format("%.2f/%s", option.waterShipped,
                            option.waterReceived > 0 ? String.format("%.2f", option.waterReceived) : "-"),
                    String.format("%.2f/%.2f", option.waterUsedForDeltaV, option.deltaV),
                    option.profit > 0 ? String.format("%.2f", option.profit) : "-",
                    option.time);
        }
        System.out.println();
    }

    public static boolean allNonCyclerOptionsUnprofitable(List<Destination> destinations, final int shippableTons) {
        for (Destination destination : destinations) {
            ShipmentOption efficient = calculateShipmentOption(destination, shippableTons, OptionType.EFFICIENT);
            ShipmentOption fast = calculateShipmentOption(destination, shippableTons, OptionType.FAST);
            if (efficient.profit > 0 || fast.profit > 0) {
                return false;
            }
        }
        return true;
    }

    public static ShipmentOption calculateShipmentOption(Destination destination,
                                                         int shippableTons, OptionType optionType) {
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

        double weight = shippableTons + WATER_HAULER.dryWeightTons;
        double waterUsedForDeltaV = WATER_HAULER.propulsionSystem.calculateRequiredPropellant(deltaV, weight);
        return new ShipmentOption(destination, shippableTons, waterUsedForDeltaV, deltaV, time);
    }

    public static void main(String[] args) throws Exception {
        List<Destination> destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            destinations.add(type.createDestination());
        }
        System.out.println("Establishment Costs for cycler:");
        System.out.printf("%-15s %-20s\n", "Destination", "Fuel Used/Delta-V");
        for (Destination destination : destinations) {
            double fuelUsed = WATER_HAULER.propulsionSystem.calculateTotalTons(
                    destination.type.cyclerEstablishmentDeltaV, WATER_HAULER.dryWeightTons);
            System.out.printf("%-15s %-20s\n", destination.type.name,
                    String.format("%.2f/%.2f", fuelUsed, destination.type.cyclerEstablishmentDeltaV));
        }

        AsteroidState asteroidState = new AsteroidState(2.613);

        int totalDaysInOrbit = 1537;
        int topN = 3;
        System.out.println("Press enter to start");
        System.in.read();
        for (int dayInOrbit = 1; dayInOrbit <= totalDaysInOrbit; dayInOrbit++) {
            simulateDay(destinations, asteroidState, dayInOrbit, totalDaysInOrbit, topN);
            Thread.sleep(5_000);
        }
    }
}