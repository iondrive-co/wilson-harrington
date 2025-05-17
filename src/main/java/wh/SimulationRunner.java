package wh;

import java.util.Comparator;
import java.util.List;

public class SimulationRunner {
    private final ShipmentCalculator shipmentCalculator;

    public SimulationRunner(ShipmentCalculator shipmentCalculator) {
        this.shipmentCalculator = shipmentCalculator;
    }

    public void simulateDay(final List<Destination> destinations, final Hauler hauler,
                           final int dayInOrbit, final int totalDaysInOrbit) {
        System.out.printf("Day %d | Distance from Sun: %.3f AU\n", dayInOrbit, SimulationState.ASTEROID_STATE.getDistanceFromSun());
        SimulationState.ASTEROID_STATE.updateDistanceFromSun(dayInOrbit, totalDaysInOrbit);

        final int availableKgsWater = SimulationState.ASTEROID_STATE.storedWaterKgs + SimulationState.KGS_WATER_MINED_PER_DAY;
        final int shippableKgsWater = Math.min(availableKgsWater, hauler.type().maxCargoKgs);
        System.out.printf("Mined %d kg water, now available %d kg\n", SimulationState.KGS_WATER_MINED_PER_DAY, availableKgsWater);
        System.out.printf("%d kg hauler allows shipping %d kg of it\n", hauler.getDryWeightKgs(), shippableKgsWater);

        for (Destination destination : destinations) {
            destination.updateDaily(dayInOrbit, totalDaysInOrbit);
        }

        displayOptions("Efficient Options (descending from highest profit)", destinations, shippableKgsWater,
                ShipmentCalculator.OptionType.EFFICIENT, hauler, (o1, o2) -> Double.compare(o2.profit, o1.profit));
        displayOptions("Fast Options (descending from fastest route)", destinations, shippableKgsWater,
                ShipmentCalculator.OptionType.FAST, hauler, Comparator.comparingDouble(o -> o.time));
        displayOptions("Cycler Options (descending from highest profit)", destinations, shippableKgsWater,
                ShipmentCalculator.OptionType.CYCLER, hauler, (o1, o2) -> Double.compare(o2.profit, o1.profit));

        if (shipmentCalculator.allNonCyclerOptionsUnprofitable(destinations, shippableKgsWater, hauler)) {
            SimulationState.ASTEROID_STATE.storedWaterKgs += SimulationState.KGS_WATER_MINED_PER_DAY;
            System.out.println("No profitable non-cycler options found. Water stored for future use.\n");
        } else {
            SimulationState.ASTEROID_STATE.storedWaterKgs = Math.max(SimulationState.ASTEROID_STATE.storedWaterKgs - shippableKgsWater, 0);
        }
    }

    private void displayOptions(final String title, final List<Destination> destinations,
                               final int shippableKgsWater, final ShipmentCalculator.OptionType optionType,
                               final Hauler hauler, final Comparator<ShipmentOption> comparator) {
        final List<ShipmentOption> options = shipmentCalculator.getShipmentOptions(destinations, shippableKgsWater, optionType, hauler);

        options.sort(comparator);
        System.out.println(title + ":");
        System.out.printf("%-15s %-10s %-20s %-20s %-15s %-15s\n",
                "Destination", "Price/kg", "kg Shipped/Received", "Fuel/Delta-V", "Profit", "Time (days)");
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
}
