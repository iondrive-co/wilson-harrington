public enum WaterHauler {
    SMALL_STEAM(3, 20, WaterPropulsionSystem.STEAM),
    MEDIUM_STEAM(4, 50, WaterPropulsionSystem.STEAM),
    LARGE_STEAM(5, 150, WaterPropulsionSystem.STEAM),
    SMALL_ELECTROLYSIS(2, 20, WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION),
    MEDIUM_ELECTROLYSIS(3, 50, WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION),
    LARGE_ELECTROLYSIS(4, 150, WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION);

    final int dryWeightTons;
    final int maxCargoTons;
    final WaterPropulsionSystem propulsionSystem;

    WaterHauler(final int dryWeightTons, final int maxCargoTons, final WaterPropulsionSystem propulsionSystem) {
        this.dryWeightTons = dryWeightTons;
        this.maxCargoTons = maxCargoTons;
        this.propulsionSystem = propulsionSystem;
    }
}
