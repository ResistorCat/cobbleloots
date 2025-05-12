package dev.ripio.cobbleloots.data.custom.filter;

public class CobblelootsPositionFilter {
  private final CobblelootsMinMaxFilter x;
  private final CobblelootsMinMaxFilter y;
  private final CobblelootsMinMaxFilter z;

  public CobblelootsPositionFilter(CobblelootsMinMaxFilter x, CobblelootsMinMaxFilter y, CobblelootsMinMaxFilter z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public CobblelootsMinMaxFilter getX() {
    return x;
  }

  public CobblelootsMinMaxFilter getY() {
    return y;
  }

  public CobblelootsMinMaxFilter getZ() {
    return z;
  }
}
