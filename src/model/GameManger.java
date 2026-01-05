package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Board.Difficulty;

public class GameManger {

    public enum GameStatus {
        READY,
        RUNNING,
        WON,
        LOST
    }

    // Result object to track what happened when answering a question
    public static class QuestionResult {
        public boolean isCorrect;
        public int pointsChange;
        public int livesChange;
        public int attemptCost;  // cost to attempt (deducted before outcome)
        public String effectDescription; // description of special effect (e.g., "opened random mine")
        public List<int[]> cellsRevealed; // cells that were revealed as a special effect

        public QuestionResult(boolean isCorrect, int pointsChange, int livesChange, String effectDescription) {
            this.isCorrect = isCorrect;
            this.pointsChange = pointsChange;
            this.livesChange = livesChange;
            this.effectDescription = effectDescription;
            this.attemptCost = 0;  // default 0, will be set by caller
            this.cellsRevealed = new ArrayList<>();
        }

        public QuestionResult(boolean isCorrect, int pointsChange, int livesChange, int attemptCost, String effectDescription) {
            this.isCorrect = isCorrect;
            this.pointsChange = pointsChange;
            this.livesChange = livesChange;
            this.attemptCost = attemptCost;
            this.effectDescription = effectDescription;
            this.cellsRevealed = new ArrayList<>();
        }
    }

    private Difficulty difficulty;
    private Board board;
    private int score;
    private int lives;
    private int maxLives;
    private GameStatus status;
    private int currentQuestionDifficulty = 1; // tracks current question difficulty (1=Easy, 2=Medium, 3=Hard, 4=Advanced)
    private final Random random = new Random();
    
    // Momentum Multiplier system
    private int consecutiveSafeCells = 0; // tracks streak of safe clicks
    
    // Shop system
    private boolean safetyNetActive = false;    // one-time use protection
    private boolean metalDetectorActive = false; // 5-second mine detection
    private long metalDetectorEndTime = 0;      // timestamp when detector expires
    private int safetyNetPurchases = 0;         // times purchased (max 3)
    private int metalDetectorPurchases = 0;     // times purchased (max 3)

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

    // Set the board reference (called from Main.java after creating the game manager)
    public void setBoard(Board board) {
        this.board = board;
    }

    // when a mine is hit we only touch lives here (no score logic)
    public void processMineHit() {
        loseLife();
        resetMomentumMultiplier(); // reset streak when mine is hit
    }
    
    // Check if player is on last life (for Stabilizer mechanic)
    public boolean isOnLastLife() {
        return lives == 1;
    }
    
    // Process Stabilizer question result (when player is on last life and clicks mine)
    public QuestionResult processStabilizerQuestion(boolean isCorrect) {
        if (isCorrect) {
            // Success: mine is flagged/disabled, game continues
            // No points or lives change, just survival
            return new QuestionResult(true, 0, 0, "Stabilizer Success! Mine disabled.");
        } else {
            // Failure: mine explodes, lose life, reset multiplier
            loseLife();
            resetMomentumMultiplier();
            return new QuestionResult(false, 0, -1, "Stabilizer Failed! Mine exploded.");
        }
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

    // set current question difficulty (1=Easy, 2=Medium, 3=Hard, 4=Advanced)
    public void setCurrentQuestionDifficulty(int difficulty) {
        this.currentQuestionDifficulty = difficulty;
    }

    public int getCurrentQuestionDifficulty() {
        return currentQuestionDifficulty;
    }

    // -------- QUESTION ANSWER SCORING --------
    // Returns a QuestionResult object with all the details about what happened
    
    public QuestionResult processQuestionAnswer(boolean isCorrect) {
        return switch (difficulty) {
            case EASY -> // EASY GAME MODE RULES
                switch (currentQuestionDifficulty) {
                    case 1 -> processEasyGameEasyQuestion(isCorrect);
                    case 2 -> processEasyGameMediumQuestion(isCorrect);
                    case 3 -> processEasyGameHardQuestion(isCorrect);
                    case 4 -> processEasyGameAdvancedQuestion(isCorrect);
                    default -> new QuestionResult(isCorrect, 0, 0, "Unknown question type");
                };
            case MEDIUM -> // MEDIUM GAME MODE RULES
                switch (currentQuestionDifficulty) {
                    case 1 -> processMediumGameEasyQuestion(isCorrect);
                    case 2 -> processMediumGameMediumQuestion(isCorrect);
                    case 3 -> processMediumGameHardQuestion(isCorrect);
                    case 4 -> processMediumGameAdvancedQuestion(isCorrect);
                    default -> new QuestionResult(isCorrect, 0, 0, "Unknown question type");
                };
            case HARD -> // HARD GAME MODE RULES
                switch (currentQuestionDifficulty) {
                    case 1 -> processHardGameEasyQuestion(isCorrect);
                    case 2 -> processHardGameMediumQuestion(isCorrect);
                    case 3 -> processHardGameHardQuestion(isCorrect);
                    case 4 -> processHardGameAdvancedQuestion(isCorrect);
                    default -> new QuestionResult(isCorrect, 0, 0, "Unknown question type");
                };
        };
    }

    // ========== EASY GAME MODE METHODS ==========
    
    // EASY GAME MODE - Easy Question: Correct: +3pts & +1life | Incorrect: -3pts OR nothing (50/50)
    private QuestionResult processEasyGameEasyQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(3);
            gainLifeOrPoints();
            return new QuestionResult(true, 3, 1, "Correct! +3 points & +1 life");
        } else {
            // 50/50 chance: either lose 3 points or nothing
            if (random.nextBoolean()) {
                addPoints(-3);
                return new QuestionResult(false, -3, 0, "Incorrect! Lost 3 points");
            } else {
                return new QuestionResult(false, 0, 0, "Incorrect! Lucky - nothing happened");
            }
        }
    }

    // EASY GAME MODE - Medium Question: Correct: open random mine + 6pts | Incorrect: -6pts OR nothing (50/50)
    private QuestionResult processEasyGameMediumQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(6);
            // Open a random hidden mine
            int[] mineCell = openRandomMine();
            QuestionResult result = new QuestionResult(true, 6, 0, "Correct! Revealed random mine & +6 points");
            if (mineCell != null) {
                result.cellsRevealed.add(mineCell);
            }
            return result;
        } else {
            // 50/50 chance: either lose 6 points or nothing
            if (random.nextBoolean()) {
                addPoints(-6);
                return new QuestionResult(false, -6, 0, "Incorrect! Lost 6 points");
            } else {
                return new QuestionResult(false, 0, 0, "Incorrect! Lucky - nothing happened");
            }
        }
    }

    // EASY GAME MODE - Hard Question: Correct: open random 3x3 cells + 10pts | Incorrect: -10pts
    private QuestionResult processEasyGameHardQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(10);
            // Open a random 3x3 area
            List<int[]> revealed = openRandom3x3Area();
            QuestionResult result = new QuestionResult(true, 10, 0, "Correct! Revealed random 3x3 area & +10 points");
            result.cellsRevealed.addAll(revealed);
            return result;
        } else {
            addPoints(-10);
            return new QuestionResult(false, -10, 0, "Incorrect! Lost 10 points");
        }
    }

    // EASY GAME MODE - Advanced Question: Correct: +2lifes & +15pts | Incorrect: -15pts & -1life
    private QuestionResult processEasyGameAdvancedQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(15);
            gainLife();
            gainLife();
            return new QuestionResult(true, 15, 2, "Correct! +15 points & +2 lives");
        } else {
            addPoints(-15);
            loseLife();
            return new QuestionResult(false, -15, -1, "Incorrect! Lost 15 points & 1 life");
        }
    }

    // ========== MEDIUM GAME MODE METHODS ==========

    // MEDIUM GAME MODE - Easy Question: Correct: +8pts & +1life | Incorrect: -8pts
    private QuestionResult processMediumGameEasyQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(8);
            gainLifeOrPoints();
            return new QuestionResult(true, 8, 1, "Correct! +8 points & +1 life");
        } else {
            addPoints(-8);
            return new QuestionResult(false, -8, 0, "Incorrect! Lost 8 points");
        }
    }

    // MEDIUM GAME MODE - Medium Question: Correct: +1life & +10pts | Incorrect: (-10pts & -1life) OR nothing (50/50)
    private QuestionResult processMediumGameMediumQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(10);
            gainLifeOrPoints();
            return new QuestionResult(true, 10, 1, "Correct! +10 points & +1 life");
        } else {
            // 50/50 chance: either lose 10 points & 1 life or nothing
            if (random.nextBoolean()) {
                addPoints(-10);
                loseLife();
                return new QuestionResult(false, -10, -1, "Incorrect! Lost 10 points & 1 life");
            } else {
                return new QuestionResult(false, 0, 0, "Incorrect! Lucky - nothing happened");
            }
        }
    }

    // MEDIUM GAME MODE - Hard Question: Correct: +1life & +15pts | Incorrect: -15pts & -1life
    private QuestionResult processMediumGameHardQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(15);
            gainLifeOrPoints();
            return new QuestionResult(true, 15, 1, "Correct! +15 points & +1 life");
        } else {
            addPoints(-15);
            loseLife();
            return new QuestionResult(false, -15, -1, "Incorrect! Lost 15 points & 1 life");
        }
    }

    // MEDIUM GAME MODE - Advanced Question: Correct: +2lifes & +20pts | Incorrect: (-20pts & -1life) OR (-20pts & -2lifes) (50/50)
    private QuestionResult processMediumGameAdvancedQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(20);
            gainLife();
            gainLife();
            return new QuestionResult(true, 20, 2, "Correct! +20 points & +2 lives");
        } else {
            // 50/50 chance: either (-20 pts & -1 life) or (-20 pts & -2 lifes)
            addPoints(-20);
            if (random.nextBoolean()) {
                loseLife();
                return new QuestionResult(false, -20, -1, "Incorrect! Lost 20 points & 1 life");
            } else {
                loseLife();
                loseLife();
                return new QuestionResult(false, -20, -2, "Incorrect! Lost 20 points & 2 lives");
            }
        }
    }

    // ========== HARD GAME MODE METHODS ==========

    // HARD GAME MODE - Easy Question: Correct: +10pts & +1life | Incorrect: -10pts & -1life
    private QuestionResult processHardGameEasyQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(10);
            gainLifeOrPoints();
            return new QuestionResult(true, 10, 1, "Correct! +10 points & +1 life");
        } else {
            addPoints(-10);
            loseLife();
            return new QuestionResult(false, -10, -1, "Incorrect! Lost 10 points & 1 life");
        }
    }

    // HARD GAME MODE - Medium Question: Correct: (+1life & +15pts) OR (+15points & +2lifes) (50/50) | Incorrect: (-15pts & -1life) OR (-15pts & -2lifes) (50/50)
    private QuestionResult processHardGameMediumQuestion(boolean isCorrect) {
        if (isCorrect) {
            // 50/50 chance: either (+1 life & +15 pts) or (+15 pts & +2 lifes)
            if (random.nextBoolean()) {
                addPoints(15);
                gainLifeOrPoints();
                return new QuestionResult(true, 15, 1, "Correct! +15 points & +1 life");
            } else {
                addPoints(15);
                gainLife();
                gainLife();
                return new QuestionResult(true, 15, 2, "Correct! +15 points & +2 lives");
            }
        } else {
            // 50/50 chance: either (-15 pts & -1 life) or (-15 pts & -2 lifes)
            addPoints(-15);
            if (random.nextBoolean()) {
                loseLife();
                return new QuestionResult(false, -15, -1, "Incorrect! Lost 15 points & 1 life");
            } else {
                loseLife();
                loseLife();
                return new QuestionResult(false, -15, -2, "Incorrect! Lost 15 points & 2 lives");
            }
        }
    }

    // HARD GAME MODE - Hard Question: Correct: +2lifes & +20pts | Incorrect: -20pts & -2lifes
    private QuestionResult processHardGameHardQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(20);
            gainLife();
            gainLife();
            return new QuestionResult(true, 20, 2, "Correct! +20 points & +2 lives");
        } else {
            addPoints(-20);
            loseLife();
            loseLife();
            return new QuestionResult(false, -20, -2, "Incorrect! Lost 20 points & 2 lives");
        }
    }

    // HARD GAME MODE - Advanced Question: Correct: +3lifes & +40pts | Incorrect: -40pts & -3lifes
    private QuestionResult processHardGameAdvancedQuestion(boolean isCorrect) {
        if (isCorrect) {
            addPoints(40);
            gainLife();
            gainLife();
            gainLife();
            return new QuestionResult(true, 40, 3, "Correct! +40 points & +3 lives");
        } else {
            addPoints(-40);
            loseLife();
            loseLife();
            loseLife();
            return new QuestionResult(false, -40, -3, "Incorrect! Lost 40 points & 3 lives");
        }
    }
    private int[] openRandomMine() {
        if (board == null) return null;

        List<int[]> hiddenMines = new ArrayList<>();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine() && cell.isHidden()) {
                    hiddenMines.add(new int[]{r, c});
                }
            }
        }

        if (hiddenMines.isEmpty()) return null;

        int[] mineCell = hiddenMines.get(random.nextInt(hiddenMines.size()));
        board.reveal(mineCell[0], mineCell[1]);
        return mineCell;
    }

    // Helper: Open a random 3x3 area and return all revealed cells
    private List<int[]> openRandom3x3Area() {
        List<int[]> revealed = new ArrayList<>();
        if (board == null) return revealed;

        // Pick a random center point for the 3x3 area
        int centerRow = random.nextInt(Math.max(1, board.getRows() - 2));
        int centerCol = random.nextInt(Math.max(1, board.getCols() - 2));

        // Reveal all cells in the 3x3 area
        for (int r = centerRow; r < centerRow + 3 && r < board.getRows(); r++) {
            for (int c = centerCol; c < centerCol + 3 && c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isHidden()) {
                    board.reveal(r, c);
                    revealed.add(new int[]{r, c});
                }
            }
        }

        return revealed;
    }

    // Helper to gain one life
    private void gainLife() {
        if (lives < maxLives) {
            lives++;
        } else {
            // Extra life converted to points: same value as the open cost
            addPoints(getBaseOpenCost());
        }
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
    public void gainLifeOrPoints() {
        if (lives < maxLives) {
            lives++;  // give a life until we reach maxLives
        } else {
            // extra life converted to points: same value as the open cost
            addPoints(getBaseOpenCost());   // EASY: 5, MEDIUM: 8, HARD: 12
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
    
    // -------------------------------
    // Momentum Multiplier Methods
    // -------------------------------
    
    // Called when a safe cell is clicked
    public int awardSafeCellWithMomentum() {
        consecutiveSafeCells++;
        
        // Calculate bonus based on tier
        int basePoints = 1; // base point for safe cell
        int bonusPoints = 0;
        
        if (consecutiveSafeCells >= 15) {
            // Tier 2: +2 bonus points per safe cell
            bonusPoints = 2;
        } else if (consecutiveSafeCells >= 5) {
            // Tier 1: +1 bonus point per safe cell
            bonusPoints = 1;
        }
        
        int totalPoints = basePoints + bonusPoints;
        addPoints(totalPoints);
        
        return totalPoints; // return for UI display
    }
    
    // Called when a mine is clicked - resets multiplier
    public void resetMomentumMultiplier() {
        consecutiveSafeCells = 0;
    }
    
    public int getConsecutiveSafeCells() {
        return consecutiveSafeCells;
    }
    
    public String getMomentumTierDescription() {
        if (consecutiveSafeCells >= 15) {
            return "Tier 2 (+2 bonus points)";
        } else if (consecutiveSafeCells >= 5) {
            return "Tier 1 (+1 bonus point)";
        } else {
            return "No bonus (" + (5 - consecutiveSafeCells) + " more for Tier 1)";
        }
    }
    
    // -------------------------------
    // Shop System Methods
    // -------------------------------
    
    // Purchase Safety Net (10 points, max 3 times)
    public boolean purchaseSafetyNet() {
        if (score >= 10 && !safetyNetActive && safetyNetPurchases < 3) {
            addPoints(-10);
            safetyNetActive = true;
            safetyNetPurchases++;
            return true;
        }
        return false;
    }
    
    // Purchase Metal Detector (15 points, 5 seconds, max 3 times)
    public boolean purchaseMetalDetector() {
        if (score >= 15 && metalDetectorPurchases < 3) {
            addPoints(-15);
            metalDetectorPurchases++;
            return true;
        }
        return false;
    }
    
    // Start the metal detector timer (call after user confirms purchase)
    public void startMetalDetector() {
        metalDetectorActive = true;
        metalDetectorEndTime = System.currentTimeMillis() + 5000; // 5 seconds
    }
    
    // Check if Safety Net is active and consume it
    public boolean consumeSafetyNet() {
        if (safetyNetActive) {
            safetyNetActive = false;
            return true;
        }
        return false;
    }
    
    // Check if Metal Detector is currently active
    public boolean isMetalDetectorActive() {
        if (metalDetectorActive && System.currentTimeMillis() > metalDetectorEndTime) {
            metalDetectorActive = false; // expire
        }
        return metalDetectorActive;
    }
    
    public boolean isSafetyNetActive() {
        return safetyNetActive;
    }
    
    public long getMetalDetectorTimeRemaining() {
        if (!metalDetectorActive) return 0;
        long remaining = metalDetectorEndTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public int getSafetyNetPurchases() {
        return safetyNetPurchases;
    }
    
    public int getMetalDetectorPurchases() {
        return metalDetectorPurchases;
    }

    public enum RevealOutcome {
        INVALID,
        ALREADY_REVEALED,
        SAFE,
        MINE,
        GAME_WON,
        GAME_LOST
    }
}
