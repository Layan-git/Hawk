package model;

/**
 * Extreme difficulty scoring strategy.
 * Provides scoring rules for extreme game mode.
 * Uses hard difficulty bomb count (44) with medium board size (13x13).
 */
public class ExtremeScoringStrategy implements ScoringStrategy {

    @Override
    public int pointsForSafeCell() {
        return 3; // 3 points for revealing a safe cell in extreme mode (same as hard)
    }

    @Override
    public int pointsForMinePenalty() {
        return -10; // -10 points penalty for hitting a mine in extreme mode (same as hard)
    }

    @Override
    public int getBaseOpenCost() {
        return 12; // extreme: 12 points cost to open question/surprise (same as hard)
    }

    @Override
    public int getGoodEffectPoints() {
        return 16; // extreme: 16 points for good surprise effect (same as hard)
    }

    @Override
    public int getBadEffectPoints() {
        return -16; // extreme: -16 points for bad surprise effect (same as hard)
    }
}
