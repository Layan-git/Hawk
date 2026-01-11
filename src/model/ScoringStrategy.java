package model;

/**
 * Strategy interface for scoring behavior.
 * Implements the Strategy design pattern to allow different scoring rules.
 */
public interface ScoringStrategy {

    /**
     * Returns points awarded for revealing a safe cell.
     * 
     * @return points for a safe cell
     */
    int pointsForSafeCell();

    /**
     * Returns penalty points for hitting a mine.
     * 
     * @return penalty points for a mine (typically negative)
     */
    int pointsForMinePenalty();

    /**
     * Returns the cost to open/activate a question or surprise cell.
     * 
     * @return cost in points
     */
    int getBaseOpenCost();

    /**
     * Returns points awarded for good/positive surprise effects.
     * 
     * @return points for good surprise
     */
    int getGoodEffectPoints();

    /**
     * Returns penalty points for bad/negative surprise effects.
     * 
     * @return penalty points for bad surprise
     */
    int getBadEffectPoints();
}
