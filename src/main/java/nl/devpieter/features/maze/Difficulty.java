package nl.devpieter.features.maze;

/**
 * Controls how often the Growing Tree algorithm follows its newest branch.
 * Higher levels create longer dead ends and a harder route to solve.
 */
public enum Difficulty {
    BABBY,
    EASY,
    MEDIUM,
    HARD,
    NIGHTMARE
}
