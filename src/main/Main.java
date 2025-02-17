import java.util.*;

public class Main {
    public static final Hauler HAULER = new Hauler(HaulerClass.SMALL,
            new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
            new EnumMap<>(Map.of(PowerSource.SOLAR, 5)));
    public static final int KGS_WATER_MINED_PER_DAY = 5_000;
    public static final AsteroidState ASTEROID_STATE = AsteroidState.wilsonHarrington();
    public static final boolean ENABLE_ORBITAL_MECHANICS = true;
    public static final boolean ENABLE_AEROBRAKING = true;

    enum OptionType {
        EFFICIENT, FAST, CYCLER
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
        System.out.printf("Day %d | Distance from Sun: %.3f AU\n", dayInOrbit, asteroidState.getDistanceFromSun());
        asteroidState.updateDistanceFromSun(dayInOrbit, totalDaysInOrbit);

        final int availableKgsWater = asteroidState.storedWaterKgs + KGS_WATER_MINED_PER_DAY;
        final int shippableKgsWater = Math.min(availableKgsWater, HAULER.type().maxCargoKgs);
        System.out.printf("Mined %d kg water, now available %d kg\n", KGS_WATER_MINED_PER_DAY, availableKgsWater);
        System.out.printf("%d kg hauler allows shipping %d kg of it\n", HAULER.getDryWeightKgs(), shippableKgsWater);

        for (Destination destination : destinations) {
            destination.updateDaily(dayInOrbit, totalDaysInOrbit);
        }

        displayOptions("Efficient Options", destinations, shippableKgsWater, OptionType.EFFICIENT);
        displayOptions("Fast Options", destinations, shippableKgsWater, OptionType.FAST);
        displayOptions("Cycler Options", destinations, shippableKgsWater, OptionType.CYCLER);

        if (allNonCyclerOptionsUnprofitable(destinations, shippableKgsWater)) {
            asteroidState.storedWaterKgs += KGS_WATER_MINED_PER_DAY;
            System.out.println("No profitable non-cycler options found. Water stored for future use.\n");
        } else {
            asteroidState.storedWaterKgs = Math.max(asteroidState.storedWaterKgs - shippableKgsWater, 0);
        }
    }

    private static void displayOptions(final String title, final List<Destination> destinations,
                                       final int shippableKgsWater, final OptionType optionType) {
        final List<ShipmentOption> options = new ArrayList<>();
        for (Destination destination : destinations) {
            options.add(calculateShipmentOption(destination, shippableKgsWater, optionType));
        }

        options.sort((o1, o2) -> Double.compare(o2.profit, o1.profit));
        System.out.println(title + ":");
        System.out.printf("%-15s %-10s %-20s %-20s %-15s %-15s\n",
                "Destination", "Price/kg", "Shipped/Received (kg)", "kg Water Fuel/Delta-V", "Profit", "Time (days)");
        for (ShipmentOption option : options) {
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
        double deltaV;
        double time;
        switch (optionType) {
            case EFFICIENT -> {
                deltaV = destination.deltaVEfficient;
                time = destination.timeEfficient;
            }
            case FAST -> {
                deltaV = destination.deltaVFast;
                time = destination.timeFast;
            }
            case CYCLER -> {
                deltaV = destination.deltaVCycler;
                time = destination.timeCycler;
            }
            default -> throw new IllegalArgumentException("Invalid option type");
        }

        double kgsWaterUsedForDeltaV = HAULER.kgsFuelToAccelerateTo(deltaV);
        return new ShipmentOption(destination, shippableKgsWater, kgsWaterUsedForDeltaV, deltaV, time);
    }

    public static void main(String[] args) throws Exception {
        List<Destination> destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            destinations.add(type.createDestination());
        }
        System.out.println("Establishment Costs for cycler " + HAULER);
        System.out.printf("%-15s %-20s\n", "Destination", "kgs water fuel/Delta-V");
        for (Destination destination : destinations) {
            double requiredDeltaV = destination.type.cyclerEstablishmentDeltaV;
            double kgsFuelUsed = HAULER.kgsFuelToAccelerateTo(requiredDeltaV);
            System.out.printf("%-15s %-20s\n", destination.type.name,
                    String.format("%.0f/%.1f", kgsFuelUsed, requiredDeltaV));
        }
        int totalDaysInOrbit = 1537;
        System.out.println("Press enter to start");
        System.in.read();
        for (int dayInOrbit = 1; dayInOrbit <= totalDaysInOrbit; dayInOrbit++) {
            simulateDay(destinations, ASTEROID_STATE, dayInOrbit, totalDaysInOrbit);
            Thread.sleep(5_000);
        }
    }
}