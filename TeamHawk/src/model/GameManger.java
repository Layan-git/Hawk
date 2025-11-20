package model;

import model.Board.Difficulty;

public class GameManger {

    public enum GameStatus {
        READY,
        RUNNING,
        WON,
        LOST
    }

    public enum RevealOutcome {
        INVALID,
        ALREADY_REVEALED,
        SAFE,
        MINE,
        GAME_WON,
        GAME_LOST
    }

    // --------------------------
    // Configurable scoring rules
    // --------------------------
    private static final int SAFE_REVEAL_POINTS = 10;
    private static final int MINE_PENALTY_POINTS = -50;

    private Difficulty difficulty = null;
    private Board board;

    // score + lives live *inside* GameManager now
    private int score;
    private int lives;
    private int maxLives;

    private GameStatus status;

    // --------------------------
    // Constructor
    // --------------------------
    public void GameManager(Board.Difficulty difficulty) {
        this.difficulty = difficulty;
        configureLivesByDifficulty(difficulty);
        startNewGame();
    }

    // starting lives per difficulty – change numbers as needed
    private void configureLivesByDifficulty(Board.Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> maxLives = 10;
            case MEDIUM -> maxLives = 8;
            case HARD -> maxLives = 6;
        }
    }

    // --------------------------
    // New game
    // --------------------------
    public void startNewGame() {
        this.board = new Board(difficulty);
        this.score = 0;
        this.lives = maxLives;
        this.status = GameStatus.RUNNING;
    }

    // --------------------------
    // Getters for GUI/controller
    // --------------------------
    public Board getBoard()       { return board; }
    public int getScore()         { return score; }
    public int getLives()         { return lives; }
    public int getMaxLives()      { return maxLives; }
    public GameStatus getStatus() { return status; }

    // --------------------------
    // Reveal (left click)
    // --------------------------
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

        // board handles the actual reveal & cascade
        board.reveal(row, col);

        if (cell.isMine()) {
            // stepped on a mine → lose life + penalty
            loseLife();
            addPoints(MINE_PENALTY_POINTS);

            if (lives <= 0) {
                status = GameStatus.LOST;
                return RevealOutcome.GAME_LOST;
            }
            return RevealOutcome.MINE;

        } else {
            // safe cell (number or empty)
            addPoints(SAFE_REVEAL_POINTS);

            if (checkWinCondition()) {
                status = GameStatus.WON;
                return RevealOutcome.GAME_WON;
            }
            return RevealOutcome.SAFE;
        }
    }

    // --------------------------
    // Flag (right click)
    // --------------------------
    public void handleToggleFlag(int row, int col) {
        if (status != GameStatus.RUNNING) return;
        board.toggleFlag(row, col);
        // later you can also change score based on correct / wrong flags
    }

    // --------------------------
    // Internal helpers
    // --------------------------
    private void addPoints(int points) {
        this.score += points;
    }

    private void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    // all non-mine cells are revealed → win
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
}
