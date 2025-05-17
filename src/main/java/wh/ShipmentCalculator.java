package wh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShipmentCalculator {

    public enum OptionType {
        EFFICIENT, FAST, CYCLER
    }

    public ShipmentOption calculateShipmentOption(final Destination destination, final int shippableKgsWater,
                                                 final OptionType optionType, final Hauler hauler) {
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

        double kgsWaterUsedForDeltaV = hauler.kgsFuelToAccelerateTo(deltaV);
        return new ShipmentOption(destination, shippableKgsWater, kgsWaterUsedForDeltaV, deltaV, time);
    }

    public boolean allNonCyclerOptionsUnprofitable(final List<Destination> destinations,
                                                  final int shippableKgsWater, final Hauler hauler) {
        for (Destination destination : destinations) {
            ShipmentOption efficient = calculateShipmentOption(destination, shippableKgsWater, OptionType.EFFICIENT, hauler);
            ShipmentOption fast = calculateShipmentOption(destination, shippableKgsWater, OptionType.FAST, hauler);
            if (efficient.profit > 0 || fast.profit > 0) {
                return false;
            }
        }
        return true;
    }

    public List<ShipmentOption> getShipmentOptions(final List<Destination> destinations,
                                                  final int shippableKgsWater, final OptionType optionType,
                                                  final Hauler hauler) {
        final List<ShipmentOption> options = new ArrayList<>();
        for (Destination destination : destinations) {
            options.add(calculateShipmentOption(destination, shippableKgsWater, optionType, hauler));
        }
        return options;
    }
}
