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

  public boolean isValid(boolean rain, boolean thunder) {
    // Note: If it is thundering, it is also raining.
    // Posible combinations:
    // - THUNDERING: Rain: true, Thunder: true
    // - RAINING: Rain: true, Thunder: false
    // - CLEAR: Rain: false, Thunder: false

    return (this.rain && rain && !thunder) || (this.thunder && thunder) || (this.clear && !rain && !thunder);
  }
}
