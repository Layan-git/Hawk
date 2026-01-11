package model;

/**
 * Easy difficulty scoring strategy.
 * Provides scoring rules for easy game mode.
 */
public class EasyScoringStrategy implements ScoringStrategy {

    @Override
    public int pointsForSafeCell() {
        return 1; // 1 point for revealing a safe cell in easy mode
    }

    @Override
    public int pointsForMinePenalty() {
        return -5; // -5 points penalty for hitting a mine in easy mode
    }

    @Override
    public int getBaseOpenCost() {
        return 5; // easy: 5 points cost to open question/surprise
    }

    @Override
    public int getGoodEffectPoints() {
        return 8; // easy: 8 points for good surprise effect
    }

    @Override
    public int getBadEffectPoints() {
        return -8; // easy: -8 points for bad surprise effect
    }
}
