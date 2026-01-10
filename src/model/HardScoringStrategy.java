package model;

/**
 * Hard difficulty scoring strategy.
 * Provides scoring rules for hard game mode.
 */
public class HardScoringStrategy implements ScoringStrategy {

    @Override
    public int pointsForSafeCell() {
        return 3; // 3 points for revealing a safe cell in hard mode
    }

    @Override
    public int pointsForMinePenalty() {
        return -10; // -10 points penalty for hitting a mine in hard mode
    }

    @Override
    public int getBaseOpenCost() {
        return 12; // hard: 12 points cost to open question/surprise
    }

    @Override
    public int getGoodEffectPoints() {
        return 16; // hard: 16 points for good surprise effect
    }

    @Override
    public int getBadEffectPoints() {
        return -16; // hard: -16 points for bad surprise effect
    }
}
