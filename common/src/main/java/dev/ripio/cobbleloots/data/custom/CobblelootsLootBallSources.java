package dev.ripio.cobbleloots.data.custom;

import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;

import java.util.List;

public class CobblelootsLootBallSources {
  private final List<CobblelootsSourceFilter> generation;
  private final List<CobblelootsSourceFilter> spawning;
  private final List<CobblelootsSourceFilter> fishing;
  private final List<CobblelootsSourceFilter> archaeology;

  public CobblelootsLootBallSources(List<CobblelootsSourceFilter> generation, List<CobblelootsSourceFilter> spawning, List<CobblelootsSourceFilter> fishing, List<CobblelootsSourceFilter> archaeology) {
    this.generation = generation;
    this.spawning = spawning;
    this.fishing = fishing;
    this.archaeology = archaeology;
  }

  public List<CobblelootsSourceFilter> getGeneration() {
    return generation;
  }

  public List<CobblelootsSourceFilter> getSpawning() {
    return spawning;
  }

  public List<CobblelootsSourceFilter> getFishing() { return fishing; }

  public List<CobblelootsSourceFilter> getArchaeology() {
    return archaeology;
  }

  public boolean isEmpty() {
    return generation.isEmpty() && spawning.isEmpty() && fishing.isEmpty() && archaeology.isEmpty();
  }

}
