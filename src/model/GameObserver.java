package model;

/**
 * Observer interface for the Observer design pattern.
 * Classes implementing this interface will be notified when the game state changes.
 */
public interface GameObserver {
    
    /**
     * Called when the game state is updated.
     * 
     * @param score the current score
     * @param lives the current number of lives remaining
     */
    void onGameUpdated(int score, int lives);
}
