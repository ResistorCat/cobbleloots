package dev.ripio.cobbleloots.data.debug;

public record FilterCheckResult(
    String filterKey,
    boolean passed,
    String expected,
    String actual
) {}
