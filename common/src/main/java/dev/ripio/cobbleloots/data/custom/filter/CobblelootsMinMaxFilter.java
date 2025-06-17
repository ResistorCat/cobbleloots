package dev.ripio.cobbleloots.data.custom.filter;

public class CobblelootsMinMaxFilter {
    private final int min;
    private final int max;

    public CobblelootsMinMaxFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean isInRange(int val) {
        return val >= this.min && val <= this.max;
    }
}
