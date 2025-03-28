package dev.ripio.cobbleloots.util.enums;

public enum CobblelootsSourceType {
  GENERATION("generation"),
  SPAWNING("spawning"),
  FISHING("fishing"),
  ARCHAEOLOGY("archaeology");

  private final String name;

  CobblelootsSourceType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static CobblelootsSourceType fromName(String name) {
    for (CobblelootsSourceType type : values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }
    return null;
  }

  public static boolean isValid(String name) {
    return fromName(name) != null;
  }

  public static boolean isValid(CobblelootsSourceType type) {
    return type != null;
  }

  public static int getLength() {
    return values().length;
  }
}
