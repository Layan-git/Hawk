package model;

import model.Board.Difficulty;

public class GameManger {

    public enum GameStatus {
        READY,
        RUNNING,
        WON,
        LOST
    }

    private Difficulty difficulty;
    private Board board;

    private int score;
    private int lives;
    private int maxLives;

    private GameStatus status;

    // -------------------------------
    // Constructor
    // -------------------------------
    public void GameManager(Difficulty difficulty) {
        this.difficulty = difficulty;
        configureLivesByDifficulty(difficulty);
        startNewGame();
    }

    // -------------------------------
    // Lives by difficulty
    // -------------------------------
    private void configureLivesByDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY  -> maxLives = 10;
            case MEDIUM -> maxLives = 8;
            case HARD  -> maxLives = 6;
        }
    }

    // -------------------------------
    // Start new game
    // -------------------------------
    public void startNewGame() {
        this.board = new Board(difficulty);
        this.score = 0;
        this.lives = maxLives;
        this.status = GameStatus.RUNNING;
    }

    // -------------------------------
    // Mine hit (no points — only life loss)
    // -------------------------------
    public void processMineHit() {
        loseLife();
    }

    // -------------------------------
    // Point effects for Surprise/Question
    // -------------------------------
    public int getGoodEffectPoints() {
        return switch (difficulty) {
            case EASY -> 5;     // +5
            case MEDIUM -> 7;   // +7
            case HARD -> 12;    // +12
        };
    }
    
    public int getBaseOpenCost() {
        return switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 8;
            case HARD -> 12;
        };
    }

    public void applyOpenCost() {
        addPoints(getBaseOpenCost() + 1); // +1 על פתיחה
    }

    public int getBadEffectPoints() {
        return switch (difficulty) {
            case EASY -> -8;     // -8
            case MEDIUM -> -12;  // -12
            case HARD -> -16;    // -16
        };
    }

    public void applyPositiveEffect() {
        addPoints(getGoodEffectPoints());
    }

    public void applyNegativeEffect() {
        addPoints(getBadEffectPoints());
    }
    
 // +1 point for safe cells (empty or number)
    public void awardSafeCellPoint() {
        addPoints(1);
    }


    // -------------------------------
    // Reveal handling — SAFE logic is NOT used yet (iteration 2)
    // -------------------------------
    public RevealOutcome handleReveal(int row, int col) {
        if (status != GameStatus.RUNNING) {
            return RevealOutcome.INVALID;
        }

        Cell cell = board.getCell(row, col);
        if (cell == null) {
            return RevealOutcome.INVALID;
        }

        if (cell.isRevealed() || cell.isFlagged()) {
            return RevealOutcome.ALREADY_REVEALED;
        }

        board.reveal(row, col);

        // hit mine
        if (cell.isMine()) {
            loseLife();

            if (lives <= 0) {
                status = GameStatus.LOST;
                return RevealOutcome.GAME_LOST;
            }
            return RevealOutcome.MINE;
        }

        // SAFE reveal (points will be added in iteration 2 only)
        if (checkWinCondition()) {
            status = GameStatus.WON;
            return RevealOutcome.GAME_WON;
        }

        return RevealOutcome.SAFE;
    }

    // -------------------------------
    // Add/remove points & lives
    // -------------------------------
    public void addPoints(int points) {
        this.score += points;
    }

    private void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    private boolean checkWinCondition() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isMine() && !cell.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    // -------------------------------
    // Getters
    // -------------------------------
    public Board getBoard()       { return board; }
    public int getScore()         { return score; }
    public int getLives()         { return lives; }
    public int getMaxLives()      { return maxLives; }
    public GameStatus getStatus() { return status; }

    public enum RevealOutcome {
        INVALID,
        ALREADY_REVEALED,
        SAFE,
        MINE,
        GAME_WON,
        GAME_LOST
    }
}
