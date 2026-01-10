package controller;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.Cell.CellState;
import model.GameManger;
import model.History;
import model.Questions;
import model.ResourceLoader;
import model.SysData;
import view.GameBoardView;
import view.GifAnimationDialog;

/**
 * GamePlayController manages the actual gameplay mechanics:
 * - Cell click handling
 * - Turn management
 * - Game state updates
 * - Dialogs and user interactions during play
 */
public class GamePlayController {
    
    private GameFlowController flowController;
    private GameManger gameManager;
    private GameBoardView gameBoardView;
    
    private Board board1;
    private Board board2;
    
    private String player1Name;
    private String player2Name;
    
    private int currentPlayer = 1;
    
    // Question tracking
    private int lastQuestionPlayer = -1;
    private int lastQuestionRow = -1;
    private int lastQuestionCol = -1;
    
    // Stabilizer usage tracking
    private boolean stabilizerUsed = false;
    private boolean isStabilizerMode = false;
    private int stabilizerMineRow = -1;
    private int stabilizerMineCol = -1;
    private int currentQuestionDifficulty = 1;
    
    // Game history tracking
    private long gameStartTime = 0;
    
    public GamePlayController(GameFlowController flowController) {
        this.flowController = flowController;
    }
    
    public void initializeGame(String p1, String p2, Difficulty difficulty, int p1CharIndex, int p2CharIndex) {
        this.player1Name = p1;
        this.player2Name = p2;
        
        gameManager = new GameManger();
        gameManager.GameManager(difficulty);
        
        // Reset tracking variables
        stabilizerUsed = false;
        isStabilizerMode = false;
        stabilizerMineRow = -1;
        stabilizerMineCol = -1;
        currentQuestionDifficulty = 1;
        currentPlayer = 1;
        gameStartTime = System.currentTimeMillis();  // Track game start time for history
        
        // Reset questions asked counter for new game
        SysData.resetAskedQuestions();
        
        // Preload all sound effects for minimal delay
        model.AudioManager audioManager = model.AudioManager.getInstance();
        audioManager.preloadSoundEffect("good_effect.wav");
        audioManager.preloadSoundEffect("bad_effect.wav");
        audioManager.preloadSoundEffect("expolsion.wav");  // Note: filename has typo "expolsion"
        audioManager.preloadSoundEffect("winner_sound.wav");
        audioManager.preloadSoundEffect("loser_sound.wav");
        
        // Stop background music when game starts
        audioManager.stopBackgroundMusic();
        
        // Initialize boards
        board1 = new Board(difficulty);
        board2 = new Board(difficulty);
        
        gameManager.setBoard(board1);
        
        int size = switch (difficulty) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };
        
        // Create game board view
        gameBoardView = new GameBoardView(flowController.getBoardController(), p1, p2, size, p1CharIndex, p2CharIndex);
        gameBoardView.setBoards(board1, board2);
        
        // Register GameBoardView as an observer of GameManger (Observer pattern)
        gameManager.addObserver(gameBoardView);
        
        // Set up shop button listeners for purchasable items
        gameBoardView.setShopButtonListeners(
            () -> handleSafetyNetPurchase(),
            () -> handleMetalDetectorPurchase()
        );
        
        // Initial board display
        updateBoardDisplay(1, board1);
        updateBoardDisplay(2, board2);
        
        gameBoardView.setMaxLives(gameManager.getMaxLives());
        gameBoardView.updateScore(gameManager.getScore());
        gameBoardView.updateLives(gameManager.getLives());
        gameBoardView.updateMinesLeft(1, board1.getHiddenMineCount());
        gameBoardView.updateMinesLeft(2, board2.getHiddenMineCount());
        gameBoardView.updateShopButtons(gameManager.getScore(), 
                gameManager.isSafetyNetActive(), 
                gameManager.isMetalDetectorActive(),
                gameManager.getSafetyNetPurchases(),
                gameManager.getMetalDetectorPurchases());
        gameBoardView.updateTurnVisuals(currentPlayer);
    }
    
    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }
    
    public void handleCellClick(int playerNum, int row, int col) {
        // Ignore clicks from non-current player
        if (playerNum != currentPlayer) {
            gameBoardView.showMessage("Wrong Turn", "Wait for your turn!");
            return;
        }
        
        Board currentBoard = (playerNum == 1) ? board1 : board2;
        Cell cell = currentBoard.getCell(row, col);
        
        // Allow clicking revealed QUESTION cell again if not attempted
        if (cell.isQuestion() && cell.isRevealed() && !cell.isQuestionAttempted()) {
            showQuestionChoiceDialog(playerNum, row, col);
            return;
        }
        
        // Allow clicking revealed SURPRISE cell again if not yet used
        if (cell.isSurprise() && cell.isRevealed() && !cell.isReadyForSurprise()) {
            showSurpriseChoiceDialog(playerNum, row, col);
            return;
        }
        
        // Flagged cells are locked
        if (cell.isFlagged())
            return;
        
        // -------- MINE --------
        if (cell.isMine() && cell.isHidden()) {
            handleMineHit(playerNum, row, col, currentBoard);
            return;
        }
        
        // -------- QUESTION --------
        if (cell.isQuestion() && cell.isHidden()) {
            cell.setState(CellState.REVEALED);
            updateBoardDisplay(playerNum, currentBoard);
            showQuestionChoiceDialog(playerNum, row, col);
            return;
        }
        
        // -------- SURPRISE --------
        if (cell.isSurprise() && cell.isHidden()) {
            cell.setState(CellState.REVEALED);
            updateBoardDisplay(playerNum, currentBoard);
            showSurpriseChoiceDialog(playerNum, row, col);
            return;
        }
        
        // -------- SAFE CELL --------
        if (cell.isHidden() && cell.isSafe()) {
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);
            
            gameManager.awardSafeCellWithMomentum();
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
            gameBoardView.updateMomentumDisplay(
                    gameManager.getConsecutiveSafeCells(),
                    gameManager.getMomentumTierDescription()
            );
            
            // Check if player won
            if (checkWinCondition(currentBoard)) {
                showWinAnimation();
                saveGameHistory(true);  // True = win
                quitToMenu();
                return;
            }
            
            switchTurn();
            return;
        }
    }
    
    private void handleMineHit(int playerNum, int row, int col, Board currentBoard) {
        // Check Safety Net
        if (gameManager.consumeSafetyNet()) {
            Cell cell = currentBoard.getCell(row, col);
            cell.setState(CellState.FLAGGED);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());
            gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
            gameBoardView.updateShopStatus("Safety Net activated! Mine disabled.");
            
            JOptionPane.showMessageDialog(
                    null,
                    "Safety Net activated!\nThe mine was automatically flagged and disabled.",
                    "Safety Net",
                    JOptionPane.INFORMATION_MESSAGE
            );
            
            switchTurn();
            return;
        }
        
        // Check Stabilizer (once per game, on last life)
        if (gameManager.isOnLastLife() && !stabilizerUsed) {
            stabilizerUsed = true;
            showStabilizerQuestion(playerNum, row, col);
            return;
        }
        
        // Normal mine hit
        currentBoard.reveal(row, col);
        updateBoardDisplay(playerNum, currentBoard);
        
        int prevMomentum = gameManager.getConsecutiveSafeCells();
        gameManager.processMineHit();
        gameBoardView.updateLives(gameManager.getLives());
        gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
        gameBoardView.updateMomentumDisplay(
                gameManager.getConsecutiveSafeCells(),
                gameManager.getMomentumTierDescription()
        );
        
        // Play explosion sound effect
        model.AudioManager.getInstance().playSoundEffect("expolsion.wav");  // Note: filename has typo "expolsion"
        // Check for game over IMMEDIATELY before showing dialog
        if (gameManager.getLives() <= 0) {
            showLoseAnimation();
            saveGameHistory(false);  // False = loss
            quitToMenu();
            return;
        }
        
        // Only show momentum loss message if player had 5+ streak (Tier 1 or higher)
        String message = "Boom! You stepped on a mine.\nYou lost 1 life.";
        if (prevMomentum >= 5) {
            message += "\nMomentum multiplier reset!";
        }
        
        JOptionPane.showMessageDialog(
                null,
                message,
                "Mine Hit",
                JOptionPane.ERROR_MESSAGE
        );
        
        switchTurn();
    }
    
    public void handleCellRightClick(int playerNum, int row, int col) {
        if (playerNum != currentPlayer) {
            gameBoardView.showMessage("Wrong Turn", "Wait for your turn!");
            return;
        }
        
        Board currentBoard = (playerNum == 1) ? board1 : board2;
        Cell cell = currentBoard.getCell(row, col);
        
        // Only allow flagging hidden cells
        if (cell.isFlagged()) {
            // Unflag if already flagged
            cell.setState(CellState.HIDDEN);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());
        } else if (cell.isHidden()) {
            // Flag the cell
            cell.setState(CellState.FLAGGED);
            
            // If it's a mine, reveal it when flagged and award +1 point
            if (cell.isMine()) {
                cell.setState(CellState.REVEALED);
                gameManager.awardFlagBonus();
                gameBoardView.updateScore(gameManager.getScore());
            } else {
                // If it's NOT a mine, deduct 3 points
                gameManager.addPoints(-3);
                gameBoardView.updateScore(gameManager.getScore());
            }
            
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());
            gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
            
            // Check if board is fully revealed/flagged - if so, automatically pass turn
            if (currentBoard.isFinished()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Your board is fully revealed! Passing turn to other player.",
                        "Board Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
                switchTurn();
                return;
            }
            
            // Pass the turn after flagging
            switchTurn();
            return;
        }
    }
    
    private void switchTurn() {
        // Switch to the other player
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        
        // Check if the current player's board is complete
        Board currentBoard = (currentPlayer == 1) ? board1 : board2;
        if (isBoardComplete(currentBoard)) {
            // Current player's board is complete, skip their turn and switch back
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            
            // Check if the other player's board is also complete (both finished)
            Board otherBoard = (currentPlayer == 1) ? board1 : board2;
            if (isBoardComplete(otherBoard)) {
                // Both players finished - game ends in win
                showWinAnimation();
                saveGameHistory(true);
                quitToMenu();
                return;
            }
        }
        
        gameBoardView.updateTurnVisuals(currentPlayer);
    }
    
    private void updateBoardDisplay(int playerNum, Board board) {
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Cell cell = board.getCell(row, col);
                String label = cell.getDisplayLabel();
                gameBoardView.updateCell(playerNum, row, col, cell, label);
            }
        }
    }
    
    private void showQuestionChoiceDialog(int playerNum, int row, int col) {
        lastQuestionPlayer = playerNum;
        lastQuestionRow = row;
        lastQuestionCol = col;
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Question Cell");
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setModal(true);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel label = new JLabel("Answer question now or pass?");
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(new Color(100, 255, 100));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(label);
        mainPanel.add(Box.createVerticalStrut(20));
        
        Color greenColor = new Color(50, 150, 100);
        Color orangeColor = new Color(200, 120, 50);
        
        JButton answerBtn = new JButton("Answer Now");
        answerBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        answerBtn.setBackground(greenColor);
        answerBtn.setForeground(Color.WHITE);
        answerBtn.setFocusPainted(false);
        answerBtn.setMaximumSize(new Dimension(300, 40));
        answerBtn.setPreferredSize(new Dimension(300, 40));
        answerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        answerBtn.setHorizontalAlignment(SwingConstants.CENTER);
        answerBtn.addActionListener(e -> {
            dialog.dispose();
            showQuestionDifficultyDialog(playerNum);
        });
        
        JButton passBtn = new JButton("Pass");
        passBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        passBtn.setBackground(orangeColor);
        passBtn.setForeground(Color.WHITE);
        passBtn.setFocusPainted(false);
        passBtn.setMaximumSize(new Dimension(300, 40));
        passBtn.setPreferredSize(new Dimension(300, 40));
        passBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        passBtn.setHorizontalAlignment(SwingConstants.CENTER);
        passBtn.addActionListener(e -> {
            dialog.dispose();
            Board board = (playerNum == 1) ? board1 : board2;
            board.getCell(row, col).setState(CellState.REVEALED);
            gameBoardView.updateCell(playerNum, row, col, board.getCell(row, col), "");
            switchTurn();
        });
        
        mainPanel.add(answerBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(passBtn);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showSurpriseChoiceDialog(int playerNum, int row, int col) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Surprise Cell");
        dialog.setModal(true);
        dialog.setSize(400, 240);
        dialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel msg = new JLabel(
                "<html>You uncovered a Surprise Cell!<br/>What do you want to do?</html>",
                SwingConstants.CENTER
        );
        msg.setFont(new Font("Tahoma", Font.BOLD, 16));
        msg.setForeground(new Color(180, 100, 255));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(msg);
        mainPanel.add(Box.createVerticalStrut(20));

        Color purpleColor = new Color(180, 100, 255);
        Color orangeColor = new Color(200, 120, 50);
        
        JButton activateBtn = new JButton("Try Your Luck");
        JButton passBtn = new JButton("Pass");
        
        for (JButton btn : new JButton[]{activateBtn, passBtn}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(300, 40));
            btn.setPreferredSize(new Dimension(300, 40));
            btn.setFont(new Font("Tahoma", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        activateBtn.setBackground(purpleColor);
        activateBtn.setForeground(Color.WHITE);
        
        passBtn.setBackground(orangeColor);
        passBtn.setForeground(Color.WHITE);

        // pass: we reveal S, mark that we passed it so it becomes darker but can still be used later
        passBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            cell.setState(CellState.REVEALED);
            cell.setSurprisePassed(true);
            cell.setReadyForSurprise(false);

            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            switchTurn();
        });

        // activate: we pay open cost, apply random good/bad effect and lock this surprise
        activateBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            if (cell.isHidden()) {
                cell.setState(CellState.REVEALED);
                currentBoard.reveal(row, col);
            }

            cell.setSurprisePassed(false);
            cell.setReadyForSurprise(true);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            // Capture open cost before deducting it
            int openCost = gameManager.getBaseOpenCost();
            
            gameManager.applyOpenCost();
            boolean positive = Math.random() < 0.5;
            
            // Get the effect outcome before applying
            int effectPoints = positive 
                    ? gameManager.getGoodEffectPoints() 
                    : gameManager.getBadEffectPoints();
            
            if (positive) gameManager.applyPositiveEffect();
            else gameManager.applyNegativeEffect();

            // Play sound effect based on positive/negative surprise
            if (positive) {
                model.AudioManager.getInstance().playSoundEffect("good_effect.wav");
            } else {
                model.AudioManager.getInstance().playSoundEffect("bad_effect.wav");
            }

            // Create result object with full breakdown (open cost + effect outcome)
            int livesChange = positive ? 1 : -1;
            String effectDesc = positive ? "Good Surprise! Gained effect" : "Bad Surprise! Negative effect";
            GameManger.QuestionResult result = new GameManger.QuestionResult(
                    positive,
                    effectPoints, 
                    livesChange, 
                    openCost,
                    effectDesc
            );
            
            showQuestionAnswerMessage(result);
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateLives(gameManager.getLives());
            
            // Check if game is over due to lives reaching 0
            if (gameManager.getLives() <= 0) {
                showLoseAnimation();
                saveGameHistory(false);  // False = loss
                quitToMenu();
                return;
            }
            
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
            
            // Update surprise cell display to show it was activated
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            switchTurn();
        });

        mainPanel.add(activateBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(passBtn);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showStabilizerQuestion(int playerNum, int row, int col) {
        // Store stabilizer state
        isStabilizerMode = true;
        stabilizerMineRow = row;
        stabilizerMineCol = col;
        lastQuestionPlayer = playerNum;
        lastQuestionRow = row;
        lastQuestionCol = col;
        
        // Show warning dialog first
        JOptionPane.showMessageDialog(
                null,
                "This is your LAST LIFE!\n\nYou must answer this HARD question correctly or you will lose the game.\n\nThe mine will be disabled if you answer correctly.",
                "⚠️ STABILIZER ACTIVATED ⚠️",
                JOptionPane.WARNING_MESSAGE
        );
        
        // Automatically pick a hard question (difficulty 3)
        currentQuestionDifficulty = 3;
        gameManager.setCurrentQuestionDifficulty(3);
        openQuestionDialog(playerNum);
    }
    
    private void showQuestionDifficultyDialog(int playerNum) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Select Question Difficulty");
        dialog.setModal(true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel msg = new JLabel(
                "<html>Select a difficulty for your question:<br>"
                        + "Easy (1), Medium (2), Hard (3), or Advanced (4).</html>",
                SwingConstants.CENTER);
        msg.setFont(new Font("Tahoma", Font.BOLD, 14));
        msg.setForeground(new Color(100, 255, 100));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(msg);
        mainPanel.add(Box.createVerticalStrut(20));

        int remEasy = SysData.getRemainingQuestions(1);
        int totEasy = SysData.getTotalQuestions(1);
        int remMed = SysData.getRemainingQuestions(2);
        int totMed = SysData.getTotalQuestions(2);
        int remHard = SysData.getRemainingQuestions(3);
        int totHard = SysData.getTotalQuestions(3);
        int remAdv = SysData.getRemainingQuestions(4);
        int totAdv = SysData.getTotalQuestions(4);

        JButton easyBtn = new JButton("Easy  (" + remEasy + "/" + totEasy + ")");
        JButton mediumBtn = new JButton("Medium  (" + remMed + "/" + totMed + ")");
        JButton hardBtn = new JButton("Hard  (" + remHard + "/" + totHard + ")");
        JButton advancedBtn = new JButton("Advanced  (" + remAdv + "/" + totAdv + ")");

        easyBtn.setEnabled(remEasy > 0);
        mediumBtn.setEnabled(remMed > 0);
        hardBtn.setEnabled(remHard > 0);
        advancedBtn.setEnabled(remAdv > 0);
        
        // Style buttons with green color and consistent dimensions
        Color greenColor = new Color(50, 150, 100);
        Color disabledColor = new Color(80, 100, 90);
        for (JButton btn : new JButton[]{easyBtn, mediumBtn, hardBtn, advancedBtn}) {
            btn.setFont(new Font("Tahoma", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setMaximumSize(new Dimension(350, 40));
            btn.setPreferredSize(new Dimension(350, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            if (btn.isEnabled()) {
                btn.setBackground(greenColor);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(disabledColor);
                btn.setForeground(new Color(150, 150, 150));
            }
        }

        easyBtn.addActionListener(e -> {
            currentQuestionDifficulty = 1;
            gameManager.setCurrentQuestionDifficulty(1);
            dialog.dispose();
            openQuestionDialog(playerNum);
        });
        mediumBtn.addActionListener(e -> {
            currentQuestionDifficulty = 2;
            gameManager.setCurrentQuestionDifficulty(2);
            dialog.dispose();
            openQuestionDialog(playerNum);
        });
        hardBtn.addActionListener(e -> {
            currentQuestionDifficulty = 3;
            gameManager.setCurrentQuestionDifficulty(3);
            dialog.dispose();
            openQuestionDialog(playerNum);
        });
        advancedBtn.addActionListener(e -> {
            currentQuestionDifficulty = 4;
            gameManager.setCurrentQuestionDifficulty(4);
            dialog.dispose();
            openQuestionDialog(playerNum);
        });

        mainPanel.add(easyBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(mediumBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(hardBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(advancedBtn);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openQuestionDialog(int playerNum) {
        Questions q = SysData.getRandomQuestion(currentQuestionDifficulty);
        if (q == null) {
            JOptionPane.showMessageDialog(null,
                    "No questions left for this difficulty.",
                    "No Questions",
                    JOptionPane.INFORMATION_MESSAGE);
            switchTurn();
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Question (Difficulty " + currentQuestionDifficulty + ")");
        dialog.setModal(true);
        dialog.setSize(550, 420);
        dialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel("<html>" + q.getText() + "</html>", SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(new Color(100, 255, 100));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(label);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton a = new JButton("A) " + q.getOptA());
        JButton b = new JButton("B) " + q.getOptB());
        JButton c = new JButton("C) " + q.getOptC());
        JButton d = new JButton("D) " + q.getOptD());
        
        // Style all answer buttons consistently
        Color greenColor = new Color(50, 150, 100);
        for (JButton btn : new JButton[]{a, b, c, d}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(350, 40));
            btn.setPreferredSize(new Dimension(350, 40));
            btn.setFont(new Font("Tahoma", Font.BOLD, 13));
            btn.setBackground(greenColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // Timer label for countdown
        JLabel timerLabel = new JLabel("Time: 20", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        timerLabel.setForeground(new Color(100, 255, 100));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(timerLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Array to track if a button was clicked (to prevent multiple answers)
        final boolean[] answered = {false};

        // Countdown timer: 20 seconds
        final int[] timeRemaining = {20};
        Timer questionTimer = new Timer(1000, e -> {
            timeRemaining[0]--;
            timerLabel.setText("Time: " + timeRemaining[0]);

            // When time runs out, auto-fail the question
            if (timeRemaining[0] <= 0) {
                ((Timer) e.getSource()).stop();
                if (!answered[0]) {
                    answered[0] = true;
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Time's up! You failed to answer the question in time.",
                            "Time Out",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    handleQuestionAnswer(dialog, false);
                }
            }
        });
        questionTimer.start();

        a.addActionListener(e -> {
            if (!answered[0]) {
                answered[0] = true;
                questionTimer.stop();
                handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("A"));
            }
        });
        b.addActionListener(e -> {
            if (!answered[0]) {
                answered[0] = true;
                questionTimer.stop();
                handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("B"));
            }
        });
        c.addActionListener(e -> {
            if (!answered[0]) {
                answered[0] = true;
                questionTimer.stop();
                handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("C"));
            }
        });
        d.addActionListener(e -> {
            if (!answered[0]) {
                answered[0] = true;
                questionTimer.stop();
                handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("D"));
            }
        });

        // Store timer reference so we can stop it when dialog closes
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                questionTimer.stop();
            }
        });

        mainPanel.add(a);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(b);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(c);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(d);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void handleQuestionAnswer(JDialog dialog, boolean isCorrect) {
        Board currentBoard = (lastQuestionPlayer == 1) ? board1 : board2;
        gameManager.setBoard(currentBoard);
        gameManager.setCurrentQuestionDifficulty(currentQuestionDifficulty);

        GameManger.QuestionResult result;
        int attemptCost = 0;
        
        // Check if this is a stabilizer question
        if (isStabilizerMode) {
            result = gameManager.processStabilizerQuestion(isCorrect);
            
            // Play sound effect based on correct/incorrect
            if (isCorrect) {
                model.AudioManager.getInstance().playSoundEffect("good_effect.wav");
            } else {
                model.AudioManager.getInstance().playSoundEffect("bad_effect.wav");
            }
            
            // Show simple stabilizer result message
            if (isCorrect) {
                JOptionPane.showMessageDialog(
                        null,
                        "Correct! The mine has been disabled and flagged.\nYou can continue playing.",
                        "✓ Stabilizer Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                
                // Flag and disable the mine
                Cell mineCell = currentBoard.getCell(stabilizerMineRow, stabilizerMineCol);
                mineCell.setState(CellState.FLAGGED);
                updateBoardDisplay(lastQuestionPlayer, currentBoard);
                gameBoardView.updateMinesLeft(lastQuestionPlayer, currentBoard.getHiddenMineCount());
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Incorrect! You lost your last life.\nGame Over.",
                        "✗ Stabilizer Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            
            // Update lives display
            gameBoardView.updateLives(gameManager.getLives());
            
            // Check if game is over
            if (gameManager.getLives() <= 0) {
                if (dialog != null) {
                    dialog.dispose();
                }
                showLoseAnimation();
                saveGameHistory(false);
                quitToMenu();
                return;
            }
            
            // Reset stabilizer mode
            isStabilizerMode = false;
            stabilizerMineRow = -1;
            stabilizerMineCol = -1;
            
            if (dialog != null) {
                dialog.dispose();
            }
            switchTurn();
            return;
        } else {
            // Normal question - apply cost and process normally
            attemptCost = gameManager.getBaseOpenCost();
            gameManager.applyOpenCost();
            result = gameManager.processQuestionAnswer(isCorrect);
            result.attemptCost = attemptCost;
        }

        // Play sound effect based on correct/incorrect
        if (isCorrect) {
            model.AudioManager.getInstance().playSoundEffect("good_effect.wav");
        } else {
            model.AudioManager.getInstance().playSoundEffect("bad_effect.wav");
        }

        showQuestionAnswerMessage(result);

        if (!result.cellsRevealed.isEmpty()) {
            updateBoardDisplay(lastQuestionPlayer, currentBoard);
        }

        gameBoardView.updateScore(gameManager.getScore());
        gameBoardView.updateLives(gameManager.getLives());
        
        // Check if game is over due to lives reaching 0
        if (gameManager.getLives() <= 0) {
            showLoseAnimation();
            saveGameHistory(false);  // False = loss
            quitToMenu();
            return;
        }
        
        gameBoardView.updateShopButtons(gameManager.getScore(), 
                gameManager.isSafetyNetActive(), 
                gameManager.isMetalDetectorActive(),
                gameManager.getSafetyNetPurchases(),
                gameManager.getMetalDetectorPurchases());

        if (dialog != null) {
            dialog.dispose();
        }

        if (lastQuestionPlayer != -1 && lastQuestionRow != -1 && lastQuestionCol != -1) {
            Cell cell = currentBoard.getCell(lastQuestionRow, lastQuestionCol);
            if (cell.isQuestion()) {
                cell.setState(CellState.REVEALED);
                cell.setQuestionAttempted(true);
                // Update the cell display to show it as attempted (grayed out)
                gameBoardView.updateCell(lastQuestionPlayer, lastQuestionRow, lastQuestionCol, cell, cell.getDisplayLabel());
            }
        }

        switchTurn();
    }
    
    private void showQuestionAnswerMessage(GameManger.QuestionResult result) {
        // Determine if this is a surprise effect
        boolean isSurprise = result.effectDescription != null && result.effectDescription.contains("Surprise");
        
        // Create a custom styled dialog
        JDialog dialog = new JDialog();
        String titleText = isSurprise ? 
            (result.isCorrect ? "Positive Effect" : "Negative Effect") :
            (result.isCorrect ? "Correct" : "Incorrect");
        dialog.setTitle(titleText);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(null);
        
        // Main panel with dark background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title label - centered
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
        Color titleColor = result.isCorrect ? new Color(100, 255, 100) : new Color(255, 100, 100);
        titleLabel.setForeground(titleColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Add correct/incorrect icon image
        try {
            String iconName = result.isCorrect ? "/resources/correct.png" : "/resources/incorrect.png";
            java.net.URL iconUrl = GamePlayController.class.getResource(iconName);
            BufferedImage iconImg = null;
            if (iconUrl != null) {
                iconImg = ImageIO.read(iconUrl);
            } else {
                String iconPath = ResourceLoader.getResourcePath(iconName);
                if (iconPath != null && !iconPath.isEmpty()) {
                    iconImg = ImageIO.read(new File(iconPath));
                }
            }
            
            if (iconImg != null) {
                Image scaledIcon = iconImg.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                mainPanel.add(iconLabel);
                mainPanel.add(Box.createVerticalStrut(10));
            }
        } catch (Exception e) {
            System.err.println("Could not load result icon: " + e.getMessage());
        }
        
        mainPanel.add(Box.createVerticalStrut(5));
        
        // Content panel with scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Attempt Cost - centered
        JLabel costLabel = new JLabel("Attempt Cost: -" + result.attemptCost + " points");
        costLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        costLabel.setForeground(new Color(255, 150, 100));
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        costLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        costLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(costLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Attempt Outcome section - centered
        JLabel outcomeLabel = new JLabel("Attempt Outcome:");
        outcomeLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        outcomeLabel.setForeground(new Color(180, 200, 255));
        outcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        outcomeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        outcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(outcomeLabel);
        
        String outcomeText = "";
        if (result.pointsChange >= 0) {
            outcomeText += "+" + result.pointsChange + " points";
        } else {
            outcomeText += result.pointsChange + " points";
        }
        
        if (result.livesChange > 0) {
            outcomeText += " & +" + result.livesChange + " life";
            if (result.livesChange > 1) outcomeText += "s";
        } else if (result.livesChange < 0) {
            outcomeText += " & " + result.livesChange + " life";
            if (result.livesChange < -1) outcomeText += "s";
        }
        
        if (result.effectDescription != null && !result.effectDescription.isEmpty()) {
            outcomeText += "\n(" + result.effectDescription + ")";
        }
        
        JLabel outcomeValueLabel = new JLabel("<html><center>" + outcomeText.replace("\n", "<br>") + "</center></html>");
        outcomeValueLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        outcomeValueLabel.setForeground(new Color(200, 255, 200));
        outcomeValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        outcomeValueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        outcomeValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(outcomeValueLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Overall Result section - centered
        JLabel overallLabel = new JLabel("Overall Result:");
        overallLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        overallLabel.setForeground(new Color(180, 200, 255));
        overallLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        overallLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        overallLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(overallLabel);
        
        int overallPoints = result.pointsChange - result.attemptCost;
        String overallText = "";
        if (overallPoints >= 0) {
            overallText += "+" + overallPoints + " points";
        } else {
            overallText += overallPoints + " points";
        }
        
        if (result.livesChange > 0) {
            overallText += " & +" + result.livesChange + " life";
            if (result.livesChange > 1) overallText += "s";
        } else if (result.livesChange < 0) {
            overallText += " & " + result.livesChange + " life";
            if (result.livesChange < -1) overallText += "s";
        }
        
        Color overallColor = result.isCorrect ? new Color(100, 255, 100) : new Color(255, 120, 120);
        JLabel overallValueLabel = new JLabel("<html><center>" + overallText.replace("\n", "<br>") + "</center></html>");
        overallValueLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        overallValueLabel.setForeground(overallColor);
        overallValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        overallValueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        overallValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(overallValueLabel);
        
        // Add scrollable content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 150, 120);
            }
        });
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // OK button
        JButton okBtn = new JButton("OK");
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        okBtn.setBackground(new Color(0, 150, 120));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setPreferredSize(new Dimension(100, 35));
        okBtn.setMaximumSize(new Dimension(100, 35));
        okBtn.addActionListener(e -> dialog.dispose());
        mainPanel.add(okBtn);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void handleSafetyNetPurchase() {
        if (gameManager.purchaseSafetyNet()) {
            // Play icon picked sound effect
            model.AudioManager.getInstance().playSoundEffect("icon_picked.wav");
            
            JOptionPane.showMessageDialog(
                    null,
                    "Safety Net purchased!\nYou now have protection against the next mine.",
                    "Purchase Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Cannot purchase Safety Net.\nInsufficient points or maximum purchases reached.",
                    "Purchase Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    private void handleMetalDetectorPurchase() {
        if (gameManager.purchaseMetalDetector()) {
            // Play icon picked sound effect
            model.AudioManager.getInstance().playSoundEffect("icon_picked.wav");
            
            // Activate the metal detector (5 second timer)
            gameManager.startMetalDetector();
            gameBoardView.setMetalDetectorActive(true);
            
            JOptionPane.showMessageDialog(
                    null,
                    "Metal Detector purchased!\nYou can now detect nearby mines for 5 seconds.",
                    "Purchase Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
            
            // Start a timer to update the UI while detector is active
            Timer detectorTimer = new Timer(100, e -> {
                long timeRemaining = gameManager.getMetalDetectorTimeRemaining();
                if (timeRemaining > 0) {
                    gameBoardView.updateMetalDetectorTimer(timeRemaining / 1000.0);
                } else {
                    // Detector expired, stop the timer
                    ((Timer) e.getSource()).stop();
                    gameBoardView.updateMetalDetectorTimer(0);
                    gameBoardView.setMetalDetectorActive(false);
                }
                gameBoardView.updateShopButtons(gameManager.getScore(), 
                        gameManager.isSafetyNetActive(), 
                        gameManager.isMetalDetectorActive(),
                        gameManager.getSafetyNetPurchases(),
                        gameManager.getMetalDetectorPurchases());
            });
            detectorTimer.start();
            
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Cannot purchase Metal Detector.\nInsufficient points or already active.",
                    "Purchase Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    public void pauseGame() {
        if (gameBoardView != null) {
            gameBoardView.pauseTimer();
        }
        JOptionPane.showMessageDialog(null, "Game paused");
        if (gameBoardView != null) {
            gameBoardView.resumeTimer();
        }
    }
    
    public void quitToMenu() {
        if (gameBoardView != null) gameBoardView.close();
        flowController.returnToMainMenu();
    }
    
    /**
     * Save the finished game to history
     * @param win true if players won, false if they lost
     */
    private void saveGameHistory(boolean win) {
        if (gameManager == null) return;
        
        long gameDurationSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
        
        History history = new History(
                LocalDateTime.now(),
                player1Name,
                player2Name,
                gameManager.getDifficulty().toString(),
                win,
                gameManager.getScore(),
                gameDurationSeconds,
                0,  // minesHit - not tracked in detail
                0,  // questionsAnswered - not tracked
                0,  // correctQuestions - not tracked
                0,  // wrongQuestions - not tracked
                0,  // surprisesTriggered - not tracked
                0,  // positiveSurprises - not tracked
                0,  // negativeSurprises - not tracked
                gameManager.getLives(),  // livesRemaining at end of game
                "player"  // username - default player
        );
        
        SysData.addHistory(history);
    }
    
    /**
     * Check if BOTH players have revealed all non-mine cells on their boards (win condition)
     * Game only ends when both players uncover their whole boards, not when one uncovered all mines
     */
    private boolean checkWinCondition(Board board) {
        // Check if current player has revealed all non-mine cells
        boolean currentBoardComplete = isBoardComplete(board);
        if (!currentBoardComplete) {
            return false;
        }
        
        // Check if the other player's board is also complete
        Board otherBoard = (board == board1) ? board2 : board1;
        boolean otherBoardComplete = isBoardComplete(otherBoard);
        
        return otherBoardComplete;
    }
    
    private boolean isBoardComplete(Board board) {
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
    
    /**
     * Display win animation GIF and wait for it to complete
     */
    private void showWinAnimation() {
        try {
            // Play winner sound effect
            model.AudioManager.getInstance().playSoundEffect("winner_sound.wav");
            
            // Show win.gif centered, auto-close after 7 seconds
            GifAnimationDialog winDialog = new GifAnimationDialog("win.gif", 400, 300, null);
            winDialog.autoCloseAfter(7000);  // 7 seconds
            winDialog.setVisible(true);
            // Dialog is modal, so execution blocks until it closes
        } catch (Exception e) {
            System.err.println("Error displaying win animation: " + e.getMessage());
        }
    }
    
    /**
     * Display lose animation GIF and wait for it to complete
     */
    private void showLoseAnimation() {
        try {
            // Play loser sound effect
            model.AudioManager.getInstance().playSoundEffect("loser_sound.wav");
            
            // Hide the game timer before showing lose GIF
            gameBoardView.hideTimer();
            
            // Show lose.gif centered, auto-close after 7 seconds
            GifAnimationDialog loseDialog = new GifAnimationDialog("lose.gif", 400, 300, null);
            loseDialog.autoCloseAfter(7000);  // 7 seconds
            loseDialog.setVisible(true);
            // Dialog is modal, so execution blocks until it closes
        } catch (Exception e) {
            System.err.println("Error displaying lose animation: " + e.getMessage());
        }
    }
    
    /**
     * Display explosion animation GIF at mine cell location
     */
    private void showExplosionAnimation(int playerNum, int row, int col) {
        try {
            // Calculate cell position on screen
            // Get game board view's component bounds
            Component boardComponent = gameBoardView.getBoardPanel(playerNum);
            if (boardComponent == null) return;
            
            Point boardPos = boardComponent.getLocationOnScreen();
            
            // Calculate approximate cell position (these are rough estimates based on board layout)
            // This assumes cells are laid out in a grid
            int cellSize = boardComponent.getWidth() / (gameBoardView.getBoardSize());
            int explosionX = (int) (boardPos.x + (col * cellSize) + (cellSize / 4));
            int explosionY = (int) (boardPos.y + (row * cellSize) + (cellSize / 4));
            
            // Show explosion.gif at mine location, half size (150x125), auto-close after 1.5 seconds
            GifAnimationDialog explosionDialog = new GifAnimationDialog("explosion.gif", 150, 125, null, explosionX, explosionY);
            explosionDialog.autoCloseAfter(1500);  // 1.5 seconds
            explosionDialog.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error displaying explosion animation: " + e.getMessage());
        }
    }
}

