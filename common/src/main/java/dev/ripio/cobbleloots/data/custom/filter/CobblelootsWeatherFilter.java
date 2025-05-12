package dev.ripio.cobbleloots.data.custom.filter;

public class CobblelootsWeatherFilter {
  private final boolean rain;
  private final boolean thunder;
  private final boolean clear;

  public CobblelootsWeatherFilter(boolean rain, boolean thunder, boolean clear) {
    this.rain = rain;
    this.thunder = thunder;
    this.clear = clear;
  }

  public boolean getRain() {
    return rain;
  }

  public boolean getThunder() {
    return thunder;
  }

  public boolean getClear() {
    return clear;
  }
}
