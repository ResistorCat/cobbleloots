package dev.ripio.cobbleloots.config.custom;

public class CobblelootsMinMaxConfigEntry<T> extends CobblelootsConfigEntry<T> {
  protected T min;
  protected T max;

  public CobblelootsMinMaxConfigEntry(String key, T defaultValue, T min, T max, String comment) {
    super(key, defaultValue, comment);
    this.min = min;
    this.max = max;
  }

  @Override
  public void setValue(T value) {
    if (value instanceof Number number && this.min instanceof Number _min && this.max instanceof Number _max) {
      if (number.doubleValue() > _max.doubleValue()) number = _max;
      if (number.doubleValue() < _min.doubleValue()) number = _min;
      // Check if the value is a number
      super.setValue((T) number);
    } else {
      throw new IllegalArgumentException("Value must be a number");
    }

  }
}
