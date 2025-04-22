package dev.ripio.cobbleloots.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CobblelootsConfigTest {
  @Test
  void testUnflatten() {
    // Normal case
    Map<String, Object> flattenedMap = Map.of(
        "loot_ball.bonus.enabled", true,
        "loot_ball.bonus.chance", 0.5,
        "loot_ball.bonus.multiplier", 2.0,
        "loot_ball.bonus.invisible", false
    );
    Map<String, Object> expectedMap = Map.of(
        "loot_ball", Map.of(
            "bonus", Map.of(
                "enabled", true,
                "chance", 0.5,
                "multiplier", 2.0,
                "invisible", false
            )
        )
    );
    Map<String, Object> resultMap = CobblelootsConfig.unflatten(flattenedMap);
    Assertions.assertEquals(expectedMap, resultMap);
    // Edge case: empty map
    Map<String, Object> emptyMap = Map.of();
    Map<String, Object> emptyResult = CobblelootsConfig.unflatten(emptyMap);
    Assertions.assertEquals(emptyMap, emptyResult);
    // Edge case: single key
    Map<String, Object> singleKeyMap = Map.of(
        "loot_ball", true
    );
    Map<String, Object> singleKeyExpected = Map.of(
        "loot_ball", true
    );
    Map<String, Object> singleKeyResult = CobblelootsConfig.unflatten(singleKeyMap);
    Assertions.assertEquals(singleKeyExpected, singleKeyResult);
    // Edge case: Maps values
    Map<String, Object> mapValuesMap = Map.of(
        "loot_ball", Map.of(
            "bonus", Map.of(
                "enabled", true
            )
        ),
        "other_key", Map.of(
            "another_key", 123
        )
    );
    Map<String, Object> mapValuesExpected = Map.of(
        "loot_ball", Map.of(
            "bonus", Map.of(
                "enabled", true
            )
        ),
        "other_key", Map.of(
            "another_key", 123
        )
    );
    Map<String, Object> mapValuesResult = CobblelootsConfig.unflatten(mapValuesMap);
    Assertions.assertEquals(mapValuesExpected, mapValuesResult);
  }
}
