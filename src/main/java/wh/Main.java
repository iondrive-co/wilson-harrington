package wh;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        final ShipmentCalculator shipmentCalculator = new ShipmentCalculator();
        final SimulationRunner simulationRunner = new SimulationRunner(shipmentCalculator);
        
        final SimulationState simState = new SimulationState(List.of(
                new Hauler(HaulerClass.SMALL,
                        new EnumMap<>(Map.of(WaterPropulsionSystem.THERMAL, 1)),
                        new EnumMap<>(Map.of(PowerSource.SOLAR, 5)))));
        final List<Destination> destinations = new ArrayList<>();
        for (DestinationType type : DestinationType.values()) {
            destinations.add(type.createDestination());
        }

        // TODO use this up when shipping
        final Hauler hauler = simState.getCurrentHauler();
        System.out.println("Establishment Costs for cycler " + hauler);
        System.out.printf("%-15s %-20s\n", "Destination", "kgs water fuel/Delta-V");
        for (Destination destination : destinations) {
            double requiredDeltaV = destination.type.cyclerEstablishmentDeltaV;
            double kgsFuelUsed = hauler.kgsFuelToAccelerateTo(requiredDeltaV);
            System.out.printf("%-15s %-20s\n", destination.type.name,
                    String.format("%.0f/%.1f", kgsFuelUsed, requiredDeltaV));
        }
        int totalDaysInOrbit = 1537;
        System.out.println("Press enter to start");
        System.in.read();
        for (int dayInOrbit = 1; dayInOrbit <= totalDaysInOrbit; dayInOrbit++) {
            simulationRunner.simulateDay(destinations, hauler, dayInOrbit, totalDaysInOrbit);
            Thread.sleep(5_000);
        }
    }
}
