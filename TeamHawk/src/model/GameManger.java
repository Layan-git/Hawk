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

    // this is acting like an init method (not a real constructor) for now
    public void GameManager(Difficulty difficulty) {
        this.difficulty = difficulty;             // keep current difficulty so we know rules
        configureLivesByDifficulty(difficulty);   // set starting hearts based on difficulty
        startNewGame();                           // create a fresh board + reset score/lives
    }

    // set how many lives the players share, depends on difficulty
    private void configureLivesByDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> maxLives = 10;   // easy starts with 10 hearts
            case MEDIUM -> maxLives = 8;  // medium: 8 hearts
            case HARD -> maxLives = 6;    // hard: 6 hearts
        }
    }

    // create a new board and reset score/lives/status
    public void startNewGame() {
        this.board = new Board(difficulty);
        this.score = 0;
        this.lives = maxLives;
        this.status = GameStatus.RUNNING;
    }

    // when a mine is hit we only touch lives here (no score logic)
    public void processMineHit() {
        loseLife();
    }

    // -------------------------------
    // Surprise / Question costs & effects
    // -------------------------------

    // how many points it costs to activate a question/surprise cell
    public int getBaseOpenCost() {
        return switch (difficulty) {
            case EASY -> 5;   // easy: 5 points cost
            case MEDIUM -> 8; // medium: 8 points cost
            case HARD -> 12;  // hard: 12 points cost
        };
    }

    // actually pay the activation cost
    public void applyOpenCost() {
        addPoints(-getBaseOpenCost());
    }

    // how many points we get on a good effect (before life change)
    public int getGoodEffectPoints() {
        return switch (difficulty) {
            case EASY -> 8;   // good surprise easy: +8 pts
            case MEDIUM -> 12;// medium: +12 pts
            case HARD -> 16;  // hard: +16 pts
        };
    }

    // how many points we lose on a bad effect
    public int getBadEffectPoints() {
        return switch (difficulty) {
            case EASY -> -8;   // easy: -8 pts
            case MEDIUM -> -12;// medium: -12 pts
            case HARD -> -16;  // hard: -16 pts
        };
    }

    // apply “good effect”: add points and give life (or points if already full)
    public void applyPositiveEffect() {
        addPoints(getGoodEffectPoints());
        gainLifeOrPoints();
    }

    // apply “bad effect”: remove points and 1 life
    public void applyNegativeEffect() {
        addPoints(getBadEffectPoints());
        loseLife();
    }

    // +1 point for each safe revealed cell (normal click on empty/number)
    public void awardSafeCellPoint() {
        addPoints(1);
    }

    // -------------------------------
    // Generic reveal handler (not main path used by UI yet)
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

        // if it was a mine we lose life and maybe end the game
        if (cell.isMine()) {
            loseLife();
            if (lives <= 0) {
                status = GameStatus.LOST;
                return RevealOutcome.GAME_LOST;
            }
            return RevealOutcome.MINE;
        }

        // if every non-mine is open, we mark game as won
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
        this.score += points;   // we allow negative values here too
    }

    private void loseLife() {
        if (lives > 0) {
            lives--;            // just drop one heart, caller checks for game over
        }
    }

    // +1 life up to max; if already full, convert the “extra life” into points
    private void gainLifeOrPoints() {
        if (lives < maxLives) {
            lives++;
        } else {
            // extra life converted to points: same value as the open cost
            addPoints(getBaseOpenCost());
        }
    }

    // win = all non-mine cells are revealed (we ignore flags here)
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

    public Board getBoard() { return board; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getMaxLives() { return maxLives; }
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
