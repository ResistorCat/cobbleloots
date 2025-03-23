package dev.ripio.cobbleloots.config.custom;

public class CobblelootsConfigEntry<T> {
  protected String key;
  protected T defaultValue;
  protected T value;
  protected String comment;

  public CobblelootsConfigEntry(String key, T defaultValue, String comment) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.value = defaultValue;
    this.comment = comment;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public void setComment(String s) {
    this.comment = s;
  }
}
