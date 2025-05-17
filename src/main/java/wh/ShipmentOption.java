package wh;

public class ShipmentOption {
    final Destination destination;
    final double kgsWaterShipped;
    final double kgsWaterReceived;
    final double kgsWaterUsedForDeltaV;
    final double profit;
    final double deltaV;
    final double time;

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
