package dev.ripio.cobbleloots.data.custom.filter;

public class CobblelootsLightFilter {
  private final CobblelootsMinMaxFilter block;
  private final CobblelootsMinMaxFilter sky;

  public CobblelootsLightFilter(CobblelootsMinMaxFilter block, CobblelootsMinMaxFilter sky) {
    this.block = block;
    this.sky = sky;
  }

  public CobblelootsMinMaxFilter getBlock() {
    return this.block;
  }

  public CobblelootsMinMaxFilter getSky() {
    return this.sky;
  }

  public boolean isInRange(int block, int sky) {
    return this.block.isInRange(block) && this.sky.isInRange(sky);
  }
}
