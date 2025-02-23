package dev.ripio.cobbleloots.data.custom;

public class CobblelootsLootBallHeight {
    private final Integer min;
    private final Integer max;

    public CobblelootsLootBallHeight(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }
}
