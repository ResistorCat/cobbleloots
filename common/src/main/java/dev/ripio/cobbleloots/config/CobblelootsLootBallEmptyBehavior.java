package dev.ripio.cobbleloots.config;

/**
 * Defines the behavior of a loot ball when it becomes empty (no remaining
 * uses).
 * Used as a single config selector replacing the old boolean options:
 * loot_ball_drop_enabled, loot_ball_drop_automatic, and
 * loot_ball_destroy_empty.
 */
public enum CobblelootsLootBallEmptyBehavior {
    /** Destroy the loot ball without dropping anything when it becomes empty. */
    DESTROY,
    /** Automatically drop as a decorative item when the last loot is taken. */
    DROP_AUTOMATIC,
    /** Player must punch the empty loot ball with an empty hand to drop it. */
    DROP_MANUAL,
    /** Loot ball stays in the world when empty (no drop, no destroy). */
    KEEP
}
