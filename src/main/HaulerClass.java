public enum HaulerClass {
    SMALL(2_000, 20, 500),
    MEDIUM(3_5000, 50, 1_250),
    LARGE(5_000, 125, 2_000),
    HUGE(8_000, 300, 5_000);

    final int dryWeightKgs;
    final int maxCargoTons;
    final int maxEngineWeightKgs;

    HaulerClass(final int dryWeightKgs, final int maxCargoTons, final int maxEngineWeightKgs) {
        this.dryWeightKgs = dryWeightKgs;
        this.maxCargoTons = maxCargoTons;
        this.maxEngineWeightKgs = maxEngineWeightKgs;
    }
}
