public enum WaterHauler {
    SMALL_STEAM(3, 20, WaterPropulsionSystem.STEAM),
    MEDIUM_STEAM(5, 50, WaterPropulsionSystem.STEAM),
    LARGE_STEAM(7, 100, WaterPropulsionSystem.STEAM);

    final int dryWeightTons;
    final int maxCargoTons;
    final WaterPropulsionSystem propulsionSystem;

    WaterHauler(final int dryWeightTons, final int maxCargoTons, final WaterPropulsionSystem propulsionSystem) {
        this.dryWeightTons = dryWeightTons;
        this.maxCargoTons = maxCargoTons;
        this.propulsionSystem = propulsionSystem;
    }
}
