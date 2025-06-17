package dev.ripio.cobbleloots.data.custom.filter;

public class CobblelootsTimeFilter {
  private final CobblelootsMinMaxFilter value;
  private final int period;

  public CobblelootsTimeFilter(CobblelootsMinMaxFilter value, int period) {
    this.value = value;
    this.period = period;
  }

  public CobblelootsMinMaxFilter getValue() {
    return value;
  }

  public int getPeriod() {
    return period;
  }
}
