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
    private boolean gameSaved = false;  // Prevent duplicate history saves
    
    // Statistics tracking for history
    private int totalSurprisesTriggered = 0;
    private int totalPositiveSurprises = 0;
    private int totalNegativeSurprises = 0;
    private int totalQuestionsAnswered = 0;
    private int totalCorrectQuestions = 0;
    private int totalWrongQuestions = 0;
    private int totalMinesHit = 0;
    
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
        gameSaved = false;  // Reset save flag for new game
        
        // Reset statistics tracking
        totalSurprisesTriggered = 0;
        totalPositiveSurprises = 0;
        totalNegativeSurprises = 0;
        totalQuestionsAnswered = 0;
        totalCorrectQuestions = 0;
        totalWrongQuestions = 0;
        totalMinesHit = 0;
        
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
            case EXTREME -> 13;
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
            // Show styled "not your turn" dialog
            JDialog dialog = new JDialog();
            dialog.setTitle("Not Your Turn");
            dialog.setModal(true);
            dialog.setSize(500, 280);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    Color c1 = new Color(8, 45, 40);
                    Color c2 = new Color(5, 80, 60);
                    GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            // Title with icon
            JLabel titleLabel = new JLabel("WAIT FOR YOUR TURN", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
            titleLabel.setForeground(new Color(255, 150, 0));
            java.awt.image.BufferedImage waitIcon = model.ResourceLoader.loadImage("/resources/hourglass.png");
            if (waitIcon != null) {
                Image scaledIcon = waitIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                titleLabel.setIcon(new ImageIcon(scaledIcon));
                titleLabel.setIconTextGap(15);
            }
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(titleLabel);
            mainPanel.add(Box.createVerticalStrut(20));
            
            // Message
            JLabel messageLabel = new JLabel("<html><center>It's not your turn yet.<br>Please wait for the other player to finish their move.</center></html>", SwingConstants.CENTER);
            messageLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            mainPanel.add(messageLabel);
            mainPanel.add(Box.createVerticalStrut(20));
            
            // OK Button
            JButton okBtn = new JButton("OK");
            okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            okBtn.setMaximumSize(new Dimension(120, 40));
            okBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
            okBtn.setBackground(new Color(50, 150, 100));
            okBtn.setForeground(Color.WHITE);
            okBtn.setFocusPainted(false);
            okBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 150), 2));
            okBtn.addActionListener(e -> dialog.dispose());
            mainPanel.add(okBtn);
            
            dialog.add(mainPanel);
            dialog.setVisible(true);
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
            
            // Momentum only for EXTREME difficulty
            if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
                gameManager.awardSafeCellWithMomentum();
            } else {
                gameManager.awardSafeCell();
            }
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
            
            // Only show momentum display for EXTREME
            if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
                gameBoardView.updateMomentumDisplay(
                        gameManager.getConsecutiveSafeCells(),
                        gameManager.getMomentumTierDescription()
                );
            }
            
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
            model.AudioManager.getInstance().playSoundEffect("good_effect.wav");
            
            showStyledInfoDialog("Safety Net Activated!",
                    "The mine was automatically flagged and disabled.\nYour protection saved you!",
                    new Color(50, 180, 80), "/resources/net.png");
            
            switchTurn();
            return;
        }
        
        // Check Stabilizer (once per game, on last life) - EXTREME DIFFICULTY ONLY
        if (gameManager.getDifficulty() == Board.Difficulty.EXTREME &&
                gameManager.isOnLastLife() && !stabilizerUsed) {
            stabilizerUsed = true;
            gameBoardView.setStabilizerUsed();  // Update UI to show stabilizer is used
            showStabilizerQuestion(playerNum, row, col);
            return;
        }
        
        // Normal mine hit
        currentBoard.reveal(row, col);
        updateBoardDisplay(playerNum, currentBoard);
        
        int prevMomentum = gameManager.getConsecutiveSafeCells();
        gameManager.processMineHit();
        totalMinesHit++;  // Track mine hit for history
        gameBoardView.updateLives(gameManager.getLives());
        gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
        
        // Only show momentum display for EXTREME
        if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
            gameBoardView.updateMomentumDisplay(
                    gameManager.getConsecutiveSafeCells(),
                    gameManager.getMomentumTierDescription()
            );
        }
        
        // Play explosion sound effect
        model.AudioManager.getInstance().playSoundEffect("expolsion.wav");  // Note: filename has typo "expolsion"
        // Check for game over IMMEDIATELY before showing dialog
        if (gameManager.getLives() <= 0) {
            showLoseAnimation();
            saveGameHistory(false);  // False = loss
            quitToMenu();
            return;
        }
        
        // Only show momentum loss message for EXTREME if player had 5+ streak (Tier 1 or higher)
        String message = "You stepped on a mine.\nYou lost 1 life.";
        if (gameManager.getDifficulty() == Board.Difficulty.EXTREME && prevMomentum >= 5) {
            message += "\nMomentum multiplier reset!";
        }
        
        showStyledErrorDialog("Mine Hit!", message, "/resources/bomb.png");
        
        switchTurn();
    }
    
    public void handleCellRightClick(int playerNum, int row, int col) {
        if (playerNum != currentPlayer) {
            // Show styled "not your turn" dialog
            JDialog dialog = new JDialog();
            dialog.setTitle("Not Your Turn");
            dialog.setModal(true);
            dialog.setSize(500, 280);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    Color c1 = new Color(8, 45, 40);
                    Color c2 = new Color(5, 80, 60);
                    GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            // Title with icon
            JLabel titleLabel = new JLabel("WAIT FOR YOUR TURN", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
            titleLabel.setForeground(new Color(255, 150, 0));
            java.awt.image.BufferedImage waitIcon = model.ResourceLoader.loadImage("/resources/hourglass.png");
            if (waitIcon != null) {
                Image scaledIcon = waitIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                titleLabel.setIcon(new ImageIcon(scaledIcon));
                titleLabel.setIconTextGap(15);
            }
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(titleLabel);
            mainPanel.add(Box.createVerticalStrut(20));
            
            // Message
            JLabel messageLabel = new JLabel("<html><center>It's not your turn yet.<br>Please wait for the other player to finish their move.</center></html>", SwingConstants.CENTER);
            messageLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            mainPanel.add(messageLabel);
            mainPanel.add(Box.createVerticalStrut(20));
            
            // OK Button
            JButton okBtn = new JButton("OK");
            okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            okBtn.setMaximumSize(new Dimension(120, 40));
            okBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
            okBtn.setBackground(new Color(50, 150, 100));
            okBtn.setForeground(Color.WHITE);
            okBtn.setFocusPainted(false);
            okBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 150), 2));
            okBtn.addActionListener(e -> dialog.dispose());
            mainPanel.add(okBtn);
            
            dialog.add(mainPanel);
            dialog.setVisible(true);
            return;
        }
        
        Board currentBoard = (playerNum == 1) ? board1 : board2;
        Cell cell = currentBoard.getCell(row, col);
        
        // Only allow flagging hidden cells
        if (cell.isFlagged()) {
            // Unflag if already flagged
            cell.setState(CellState.HIDDEN);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());
            // Unflagging is a move, so switch turn
            switchTurn();
            return;
        } else if (cell.isHidden()) {
            // Check if player has enough points to flag (flagging costs points)
            if (gameManager.getScore() <= 0) {
                showStyledErrorDialog("Insufficient Points",
                        "You cannot flag cells without points.\nEarn more points first!",
                        "/resources/gears.png");
                return;
            }
            
            // Flag the cell
            cell.setState(CellState.FLAGGED);
            
            // If it's a mine, reveal it when flagged and award +1 point
            if (cell.isMine()) {
                cell.setState(CellState.REVEALED);
                gameManager.awardFlagBonus();
                model.AudioManager.getInstance().playSoundEffect("good_effect.wav");
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
        
        // Check if all mines are revealed in both boards - if so, player wins
        if (board1.getHiddenMineCount() == 0 && board2.getHiddenMineCount() == 0) {
            // All mines revealed - game ends in win
            showWinAnimation();
            saveGameHistory(true);
            quitToMenu();
            return;
        }
        
        // Check if the current player's board is complete (old logic for any other auto-complete scenario)
        Board currentBoard = (currentPlayer == 1) ? board1 : board2;
        if (isBoardComplete(currentBoard)) {
            // Current player's board is complete, skip their turn and switch back
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            
            // Check if the other player's board is also complete (both finished)
            Board otherBoard = (currentPlayer == 1) ? board1 : board2;
            if (isBoardComplete(otherBoard)) {
                // Both players finished - game ends in win
                showWinAnimation();
                // Note: History already saved at line 295, don't save again
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
            
            // Track momentum for question attempt (whether attempting or passing)
            if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
                gameManager.awardSafeCellWithMomentum();
                gameBoardView.updateMomentumDisplay(
                        gameManager.getConsecutiveSafeCells(),
                        gameManager.getMomentumTierDescription()
                );
            }
            
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
            
            // Track momentum for surprise attempt (passing counts toward streak)
            if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
                gameManager.awardSafeCellWithMomentum();
                gameBoardView.updateMomentumDisplay(
                        gameManager.getConsecutiveSafeCells(),
                        gameManager.getMomentumTierDescription()
                );
            }

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
            
            // Track momentum for surprise attempt (activating counts toward streak)
            if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
                gameManager.awardSafeCellWithMomentum();
            }

            // Capture open cost before deducting it
            int openCost = gameManager.getBaseOpenCost();
            
            gameManager.applyOpenCost();
            boolean positive = Math.random() < 0.5;
            
            // Track surprise statistics
            totalSurprisesTriggered++;
            if (positive) {
                totalPositiveSurprises++;
            } else {
                totalNegativeSurprises++;
            }
            
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
        
        // Show styled warning dialog first
        JDialog dialog = new JDialog();
        dialog.setTitle("Stabilizer Activated");
        dialog.setModal(true);
        dialog.setSize(500, 280);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        
        // Handle X button click as incorrect answer
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Treat closing without answering as incorrect
                if (isStabilizerMode) {
                    isStabilizerMode = false;
                    stabilizerMineRow = -1;
                    stabilizerMineCol = -1;
                    
                    // Deduct a life (same as mine hit)
                    gameManager.processMineHit();
                    showStyledErrorDialog("Stabilizer Failed", "You closed the question without answering! You lost your last life. Game Over.", "/resources/bomb.png");
                    
                    // Update lives display
                    gameBoardView.updateLives(gameManager.getLives());
                    
                    // Check if game is over
                    if (gameManager.getLives() <= 0) {
                        showLoseAnimation();
                        saveGameHistory(false);
                        quitToMenu();
                        return;
                    }
                    
                    switchTurn();
                }
            }
        });
        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Title with icon
        JLabel titleLabel = new JLabel("STABILIZER ACTIVATED", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 200, 0));
        java.awt.image.BufferedImage warningIcon = model.ResourceLoader.loadImage("/resources/defibrillator.png");
        if (warningIcon != null) {
            Image scaledIcon = warningIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledIcon));
            titleLabel.setIconTextGap(15);
        }
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Message
        JLabel messageLabel = new JLabel("<html><center>This is your LAST LIFE!<br><br>You must answer this HARD question<br>correctly or you will lose the game.<br><br>The mine will be disabled if<br>you answer correctly.</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // OK Button
        JButton okBtn = new JButton("Understood");
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 16));
        okBtn.setBackground(new Color(200, 100, 0));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 150, 0), 2));
        okBtn.addActionListener(e -> dialog.dispose());
        mainPanel.add(okBtn);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
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
                SwingConstants.CENTER
        );
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
        dialog.setSize(800, 420);
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
        
        // Calculate the maximum width needed for any answer option
        FontMetrics fm = a.getFontMetrics(new Font("Tahoma", Font.BOLD, 13));
        int maxWidth = 350; // minimum default
        String[] options = {a.getText(), b.getText(), c.getText(), d.getText()};
        for (String option : options) {
            int width = fm.stringWidth(option) + 30; // add padding
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        
        // Style all answer buttons consistently with calculated width
        Color greenColor = new Color(50, 150, 100);
        for (JButton btn : new JButton[]{a, b, c, d}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFont(new Font("Tahoma", Font.BOLD, 13));
            btn.setBackground(greenColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setPreferredSize(new Dimension(maxWidth, 40));
            btn.setMaximumSize(new Dimension(maxWidth, 40));
        }

        // Timer label for countdown
        JLabel timerLabel = new JLabel("Time: 30");
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        timerLabel.setForeground(new Color(100, 255, 100));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(timerLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Array to track if a button was clicked (to prevent multiple answers)
        final boolean[] answered = {false};

        // Countdown timer: 30 seconds
        final int[] timeRemaining = {30};
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
                // If in stabilizer mode and haven't answered yet, treat close as incorrect
                if (isStabilizerMode && !answered[0]) {
                    answered[0] = true;
                    handleQuestionAnswer(dialog, false);
                }
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
            
            // Show styled stabilizer result message
            if (isCorrect) {
                showStyledSuccessDialog("Stabilizer Success", "Correct! The mine has been disabled and flagged. You can continue playing.", "/resources/correct.png");
                model.AudioManager.getInstance().playSoundEffect("good_effect.wav");
                
                // Flag and disable the mine
                Cell mineCell = currentBoard.getCell(stabilizerMineRow, stabilizerMineCol);
                mineCell.setState(CellState.FLAGGED);
                updateBoardDisplay(lastQuestionPlayer, currentBoard);
                gameBoardView.updateMinesLeft(lastQuestionPlayer, currentBoard.getHiddenMineCount());
            } else {
                showStyledErrorDialog("Stabilizer Failed", "Incorrect! You lost your last life. Game Over.", "/resources/bomb.png");
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

        // Track question statistics
        totalQuestionsAnswered++;
        if (isCorrect) {
            totalCorrectQuestions++;
        } else {
            totalWrongQuestions++;
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
        
        // Track momentum for question attempt (answering counts toward streak)
        if (gameManager.getDifficulty() == Board.Difficulty.EXTREME) {
            gameManager.awardSafeCellWithMomentum();
            gameBoardView.updateMomentumDisplay(
                    gameManager.getConsecutiveSafeCells(),
                    gameManager.getMomentumTierDescription()
            );
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
            
            showStyledSuccessDialog("Safety Net Purchased!",
                    "Protection activated!\nYou now have protection against the next mine.",
                    "/resources/net.png");
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
        } else {
            showStyledErrorDialog("Purchase Failed",
                    "Cannot purchase Safety Net.\\nInsufficient points or maximum purchases reached.",
                    "/resources/gears.png");  // TODO: Need error/cross icon
        }
    }
    
    private void handleMetalDetectorPurchase() {
        if (gameManager.purchaseMetalDetector()) {
            // Play icon picked sound effect
            model.AudioManager.getInstance().playSoundEffect("icon_picked.wav");
            
            // Activate the metal detector (5 second timer)
            gameManager.startMetalDetector();
            gameBoardView.setMetalDetectorActive(true);
            
            // Pass callback to start timer when user clicks OK on dialog
            showStyledSuccessDialog("Metal Detector Purchased!",
                    "Detection mode activated!\nYou can now detect nearby mines for 5 seconds.",
                    "/resources/metaldetector.png",
                    () -> {
                        // Timer starts ONLY after user clicks OK
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
                    });
            
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());
        } else {
            showStyledErrorDialog("Purchase Failed",
                    "Cannot purchase Metal Detector.\\nInsufficient points or already active.",
                    "/resources/gears.png");  // TODO: Need error/cross icon
        }
    }
    
    public void pauseGame() {
        if (gameBoardView != null) {
            gameBoardView.pauseTimer();
        }
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Game Paused");
        dialog.setModal(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        // Background with gradient
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(bg);
        
        // Title
        JLabel title = new JLabel("Game Paused", SwingConstants.CENTER);
        title.setForeground(new Color(255, 200, 100));
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        
        // Try to load pause icon
        java.awt.image.BufferedImage pauseIcon = model.ResourceLoader.loadImage("/resources/pause-button.png");
        if (pauseIcon != null) {
            Image scaledIcon = pauseIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            title.setIcon(new ImageIcon(scaledIcon));
            title.setIconTextGap(10);
        }
        bg.add(title, BorderLayout.NORTH);
        
        // Message
        JLabel msg = new JLabel("<html><center>Game is paused. Choose an action to continue.</center></html>");
        msg.setFont(new Font("Tahoma", Font.PLAIN, 14));
        msg.setForeground(Color.WHITE);
        bg.add(msg, BorderLayout.CENTER);
        
        // Buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        JButton resumeBtn = new JButton("Resume");
        resumeBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        resumeBtn.setBackground(new Color(50, 150, 100));
        resumeBtn.setForeground(Color.WHITE);
        resumeBtn.setFocusPainted(false);
        resumeBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 150), 2));
        resumeBtn.setPreferredSize(new Dimension(100, 35));
        resumeBtn.addActionListener(e -> {
            dialog.dispose();
            if (gameBoardView != null) {
                gameBoardView.resumeTimer();
            }
        });
        
        btnPanel.add(resumeBtn);
        bg.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
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
        if (gameManager == null || gameSaved) return;  // Prevent duplicate saves
        gameSaved = true;
        
        long gameDurationSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
        
        History history = new History(
                LocalDateTime.now(),
                player1Name,
                player2Name,
                gameManager.getDifficulty().toString(),
                win,
                gameManager.getScore(),
                gameDurationSeconds,
                totalMinesHit,
                totalQuestionsAnswered,
                totalCorrectQuestions,
                totalWrongQuestions,
                totalSurprisesTriggered,
                totalPositiveSurprises,
                totalNegativeSurprises,
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
    
    /**
     * Display a styled success dialog with custom text and icon
     */
    private void showStyledSuccessDialog(String title, String message, String iconPath) {
        showStyledSuccessDialog(title, message, iconPath, null);
    }
    
    private void showStyledSuccessDialog(String title, String message, String iconPath, Runnable onOkClicked) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(450, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(bg);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(50, 200, 100));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        
        // Try to load and display icon
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadImage(iconPath);
        if (icon != null) {
            Image scaledIcon = icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledIcon));
            titleLabel.setIconTextGap(10);
        }
        bg.add(titleLabel, BorderLayout.NORTH);
        
        JLabel msgLabel = new JLabel("<html>" + message + "</html>", SwingConstants.CENTER);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        bg.add(msgLabel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        okBtn.setBackground(new Color(50, 150, 100));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 150), 2));
        okBtn.setPreferredSize(new Dimension(90, 35));
        okBtn.addActionListener(e -> {
            dialog.dispose();
            if (onOkClicked != null) {
                onOkClicked.run();
            }
        });
        btnPanel.add(okBtn);
        bg.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    /**
     * Display a styled error dialog with custom text and icon
     */
    private void showStyledErrorDialog(String title, String message, String iconPath) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(450, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(bg);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(255, 100, 100));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        
        // Try to load and display icon
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadImage(iconPath);
        if (icon != null) {
            Image scaledIcon = icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledIcon));
            titleLabel.setIconTextGap(10);
        }
        bg.add(titleLabel, BorderLayout.NORTH);
        
        JLabel msgLabel = new JLabel("<html>" + message + "</html>", SwingConstants.CENTER);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        bg.add(msgLabel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        okBtn.setBackground(new Color(180, 50, 50));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 100, 100), 2));
        okBtn.setPreferredSize(new Dimension(90, 35));
        okBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(okBtn);
        bg.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    /**
     * Display a styled info dialog with custom color and icon
     */
    private void showStyledInfoDialog(String title, String message, Color titleColor, String iconPath) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(450, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color c1 = new Color(8, 45, 40);
                Color c2 = new Color(5, 80, 60);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(bg);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(titleColor);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        
        // Try to load and display icon
        java.awt.image.BufferedImage icon = model.ResourceLoader.loadImage(iconPath);
        if (icon != null) {
            Image scaledIcon = icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            titleLabel.setIcon(new ImageIcon(scaledIcon));
            titleLabel.setIconTextGap(10);
        }
        bg.add(titleLabel, BorderLayout.NORTH);
        
        JLabel msgLabel = new JLabel("<html>" + message + "</html>", SwingConstants.CENTER);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        bg.add(msgLabel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        okBtn.setBackground(titleColor);
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createLineBorder(titleColor.brighter(), 2));
        okBtn.setPreferredSize(new Dimension(90, 35));
        okBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(okBtn);
        bg.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}

