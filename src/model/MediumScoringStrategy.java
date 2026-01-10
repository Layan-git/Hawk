package model;

/**
 * Medium difficulty scoring strategy.
 * Provides scoring rules for medium game mode.
 */
public class MediumScoringStrategy implements ScoringStrategy {

    @Override
    public int pointsForSafeCell() {
        return 2; // 2 points for revealing a safe cell in medium mode
    }

    @Override
    public int pointsForMinePenalty() {
        return -8; // -8 points penalty for hitting a mine in medium mode
    }

    @Override
    public int getBaseOpenCost() {
        return 8; // medium: 8 points cost to open question/surprise
    }

    @Override
    public int getGoodEffectPoints() {
        return 12; // medium: 12 points for good surprise effect
    }

    @Override
    public int getBadEffectPoints() {
        return -12; // medium: -12 points for bad surprise effect
    }
}
