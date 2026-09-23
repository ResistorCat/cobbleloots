package dev.ripio.cobbleloots.data.debug;

import java.util.List;

public record LootBallRuleReport(
    int ruleIndex,
    int weight,
    boolean overallPassed,
    List<FilterCheckResult> filterResults
) {}
