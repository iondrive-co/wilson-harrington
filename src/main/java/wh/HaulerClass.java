package wh;

public enum HaulerClass {
    SMALL(2_000, 20_000, 500),
    MEDIUM(3_500, 50_000, 1_250),
    LARGE(5_000, 125_000, 2_000),
    HUGE(8_000, 300_000, 5_000);

    final int dryWeightKgs;
    final int maxCargoKgs;
    final int maxEngineWeightKgs;

    HaulerClass(final int dryWeightKgs, final int maxCargoKgs, final int maxEngineWeightKgs) {
        this.dryWeightKgs = dryWeightKgs;
        this.maxCargoKgs = maxCargoKgs;
        this.maxEngineWeightKgs = maxEngineWeightKgs;
    }
}
