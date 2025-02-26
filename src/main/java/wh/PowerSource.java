package wh;

public enum PowerSource {

    SOLAR(50, 1, 500_000),
    BEAMED(10, 10, 2_500_000),
    FUSION(2_500_000, 250_000, 100_000_000);

    final int weightKgs;
    final int powerKw;
    final int cost;

    PowerSource(final int weightKgs, final int powerKw, final int cost) {
        this.weightKgs = weightKgs;
        this.powerKw = powerKw;
        this.cost = cost;
    }
}
