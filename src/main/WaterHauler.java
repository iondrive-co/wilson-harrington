public enum WaterHauler {
    SMALL_STEAM(2.5, 20, WaterPropulsionSystem.STEAM),
    MEDIUM_STEAM(3.5, 50, WaterPropulsionSystem.STEAM),
    LARGE_STEAM(4.5, 150, WaterPropulsionSystem.STEAM),
    SMALL_ELECTROLYSIS(2, 20, WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION),
    MEDIUM_ELECTROLYSIS(3, 50, WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION),
    LARGE_ELECTROLYSIS(4, 150, WaterPropulsionSystem.ELECTROLYSIS_COMBUSTION);

    final double dryWeightTons;
    final int maxCargoTons;
    final WaterPropulsionSystem propulsionSystem;

    WaterHauler(final double dryWeightTons, final int maxCargoTons, final WaterPropulsionSystem propulsionSystem) {
        this.dryWeightTons = dryWeightTons;
        this.maxCargoTons = maxCargoTons;
        this.propulsionSystem = propulsionSystem;
    }
}
