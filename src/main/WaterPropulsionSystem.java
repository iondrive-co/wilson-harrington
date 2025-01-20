/**
 * Convert water to delta V
 */
public enum WaterPropulsionSystem {
    THERMAL("Heats water into steam for propulsion", 5_000, 0.25, 5, 100, 2_500_000, 10),
    ELECTROLYSIS("Split water into hydrogen and oxygen via electrolysis, combusting the gases for propulsion", 3_000, 0.3, 3, 50, 1_000_000, 30),
    ELECTROSTATIC("Ionize water molecules and use electric fields to accelerate them for thrust", 10_000, 0.6, 25, 225, 7_500_000, 10),
    PLASMA("Lasers heat and ionize water into plasma", 15_000, 0.7, 75, 300, 10_000_000, 8),
    MAGNETOHYDRODYNAMIC("Ionize water and use magnetic and electric fields to accelerate ions for propulsion", 50_000, 0.7, 50, 1_000, 35_000_000, 5),
    FUSION("Use fusion reactions to convert water into superheated plasma fir thrust", 100_000, 0.8, 250_000, 2_500, 50_000_000, 2);

    final String description;
    final double specificImpulseMetersSec;
    final double conversionEfficiency;
    final int requiredPowerKW;
    final int weightKg;
    final int cost;
    final int maxPerShip;

    WaterPropulsionSystem(final String description, int specificImpulseMetersSec, double conversionEfficiency,
                          int requiredPowerKW, int weightKg, int cost, int maxPerShip) {
        this.description = description;
        this.specificImpulseMetersSec = specificImpulseMetersSec;
        this.conversionEfficiency = conversionEfficiency;
        this.requiredPowerKW = requiredPowerKW;
        this.weightKg = weightKg;
        this.cost = cost;
        this.maxPerShip = maxPerShip;
    }
}
