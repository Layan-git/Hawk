package controller;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.LineBorder;
import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.Cell.CellState;
import model.GameManger;
import model.Questions;
import model.SysData;
import view.GameBoardView;
import view.GameSetup;
import view.MainMenu;

public class Main {

    public static interface MainMenuController {
        void startGame();
        void openHistory();
        void openManageQuestions();
        void openHowToPlay();
        void openSettings();
        void showMainMenu();
        void exit();
    }

    public static interface GameSetupController {
        void backToMenu();
        void confirmStart(String player1, String player2, Difficulty difficulty, int player1CharIndex, int player2CharIndex);
    }

    public static interface GameBoardController {
        void onCellClick(int playerNum, int row, int col);
        void onCellRightClick(int playerNum, int row, int col);
        void pauseGame();
        void quitToMenu();
    }

    private MainMenu mainMenu;
    private GameSetup gameSetup;
    private GameBoardView gameBoardView;
    private GameManger gameManager;
    
    // User tracking
    private String currentUser = null;
    private boolean isAdmin = false;
    
 // in Main class, add a field:
    private view.QuestionsManager questionsManager;
    private view.Settings settings;

    private Board board1;
    private Board board2;

    @SuppressWarnings("unused")
    private String player1Name;
    @SuppressWarnings("unused")
    private String player2Name;

    private int currentPlayer = 1;   // we start the game with player 1

    // remember which question cell is currently being answered
    private int lastQuestionPlayer = -1;
    private int lastQuestionRow = -1;
    private int lastQuestionCol = -1;
    
    // Stabilizer usage tracking (once per game)
    private boolean stabilizerUsed = false;
    
 // question difficulty chosen by player: 1 = Easy, 2 = Medium, 3 = Hard, 4 = Advanced
    private int currentQuestionDifficulty = 1;


    // ---------------- Controllers -------------------


    // inside menuController:
    private final MainMenuController menuController = new MainMenuController() {
        @Override
        public void startGame() { showSetup(); }

        @Override
        public void openHistory() {
            view.HistoryView historyView = new view.HistoryView(currentUser, isAdmin);
            historyView.show();
        }

        @Override
        public void openManageQuestions() {
            // Use array to hold reference so it can be modified in inner class
            final view.LoginView[] loginViewHolder = new view.LoginView[1];

            // Show login dialog to authenticate before allowing access
            view.LoginView.LoginController loginController = new view.LoginView.LoginController() {
                @Override
                public void onLogin(String username, String password) {
                    if (username == null || username.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Please enter a username.",
                                "Login",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // Simple auth: "admin" is required to manage questions
                    if (!"admin".equalsIgnoreCase(username)) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Only admin can manage questions.",
                                "Access Denied",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // Store current user info
                    isAdmin = true;
                    currentUser = username;

                    // Close the login dialog
                    if (loginViewHolder[0] != null) {
                        loginViewHolder[0].close();
                    }

                    // Show questions manager
                    if (questionsManager == null) {
                        questionsManager = new view.QuestionsManager();
                    }
                    questionsManager.show();
                }

                @Override
                public void onExit() {
                    // User cancelled login, just close the dialog and stay in menu
                    if (loginViewHolder[0] != null) {
                        loginViewHolder[0].close();
                    }
                }
            };

            // Create and store LoginView with proper controller
            loginViewHolder[0] = new view.LoginView(loginController);
            loginViewHolder[0].show();
        }

        @Override
        public void openHowToPlay() {}

        @Override
        public void openSettings() {
            if (settings == null) {
                settings = new view.Settings(this);
            }
            mainMenu.close();
            settings.show();
        }

        @Override
        public void showMainMenu() {
            mainMenu.show();
        }

        @Override
        public void exit() { System.exit(0); }
    };


    // this controller handles the setup screen, once both names + difficulty are chosen we start the game
    private final GameSetupController setupController = new GameSetupController() {
        @Override
        public void backToMenu() {
            if (gameSetup != null) gameSetup.close();
            mainMenu.show();
        }

        @Override
        public void confirmStart(String p1, String p2, Difficulty d, int p1CharIndex, int p2CharIndex) {
            player1Name = p1;
            player2Name = p2;
            startGameBoard(p1, p2, d, p1CharIndex, p2CharIndex);
        }
    };

    // ---------------- Effect Popup -------------------

    // here we show a small popup whenever a surprise / question effect is applied
    @SuppressWarnings("unused")
    private void showEffectMessage(boolean isPositive) {
        int points = isPositive
                ? gameManager.getGoodEffectPoints()
                : gameManager.getBadEffectPoints();

        String msg = isPositive
                ? "Good Effect! You earned +" + points + " points."
                : "Bad Effect! You lost " + Math.abs(points) + " points.";

        String title = isPositive ? "Positive Effect" : "Negative Effect";

        JOptionPane.showMessageDialog(
                null,
                msg,
                title,
                isPositive ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );
    }

    // Show popup message for question answer result with detailed breakdown
    private void showQuestionAnswerMessage(GameManger.QuestionResult result) {
        // Calculate overall result: outcomePoints - attemptCost (cost is already negative)
        int overallPoints = result.pointsChange - result.attemptCost;
        
        // Build the detailed message
        StringBuilder msg = new StringBuilder();
        msg.append("Attempt Cost: -").append(result.attemptCost).append(" points\n\n");
        msg.append("Attempt Outcome:\n");
        
        if (result.pointsChange >= 0) {
            msg.append("+").append(result.pointsChange).append(" points");
        } else {
            msg.append(result.pointsChange).append(" points");
        }
        
        if (result.livesChange > 0) {
            msg.append(" & +").append(result.livesChange).append(" life");
            if (result.livesChange > 1) msg.append("s");
        } else if (result.livesChange < 0) {
            msg.append(" & ").append(result.livesChange).append(" life");
            if (result.livesChange < -1) msg.append("s");
        }
        
        // Add effect description if present
        if (result.effectDescription != null && !result.effectDescription.isEmpty()) {
            msg.append(" (").append(result.effectDescription).append(")");
        }
        
        msg.append("\n\n");
        msg.append("Overall Result:\n");
        if (overallPoints >= 0) {
            msg.append("+").append(overallPoints).append(" points");
        } else {
            msg.append(overallPoints).append(" points");
        }
        
        if (result.livesChange > 0) {
            msg.append(" & +").append(result.livesChange).append(" life");
            if (result.livesChange > 1) msg.append("s");
        } else if (result.livesChange < 0) {
            msg.append(" & ").append(result.livesChange).append(" life");
            if (result.livesChange < -1) msg.append("s");
        }
        
        String title = result.isCorrect ? "Correct!" : "Incorrect!";
        int messageType = result.isCorrect ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;

        JOptionPane.showMessageDialog(
                null,
                msg.toString(),
                title,
                messageType
        );
    }

    // ---------------- Core Game Controller -------------------

    // this controller is the heart of the game, every click on a cell goes through here
    private final GameBoardController boardController = new GameBoardController() {

        @Override
        public void onCellClick(int playerNum, int row, int col) {
            // we ignore clicks from the wrong board so only the active player can play
            if (playerNum != currentPlayer) {
                gameBoardView.showMessage("Wrong Turn", "Wait for your turn!");
                return;
            }

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            // allow clicking revealed QUESTION cell again (if not attempted yet)
            // this is for the case where the player passed and later decides to answer it
            if (cell.isQuestion() && cell.isRevealed() && !cell.isQuestionAttempted()) {
                showQuestionChoiceDialog(playerNum, row, col);
                return;
            }

            // allow clicking revealed SURPRISE cell again (if not yet used)
            // same idea as question: you can come back later and activate the surprise
            if (cell.isSurprise() && cell.isRevealed() && !cell.isReadyForSurprise()) {
                showSurpriseChoiceDialog(playerNum, row, col);
                return;
            }

            // flagged cells are “locked”, left click does nothing here
            if (cell.isFlagged())
                return;

            // ---------------- MINE -----------------
            // if we hit a hidden mine we check for Safety Net, Stabilizer, or normal mine hit
            if (cell.isMine() && cell.isHidden()) {
                // Check if Safety Net is active
                if (gameManager.consumeSafetyNet()) {
                    // Safety Net activated: mine is auto-flagged, no damage
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
                
                // Check if player is on last life and Stabilizer hasn't been used yet
                if (gameManager.isOnLastLife() && !stabilizerUsed) {
                    // Trigger Stabilizer: present a question (once per game)
                    stabilizerUsed = true;
                    showStabilizerQuestion(playerNum, row, col);
                    return;
                }
                
                // Normal mine hit
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                gameManager.processMineHit();
                gameBoardView.updateLives(gameManager.getLives());
                gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                
                // Update momentum display (reset)
                gameBoardView.updateMomentumDisplay(
                        gameManager.getConsecutiveSafeCells(),
                        gameManager.getMomentumTierDescription()
                );

                JOptionPane.showMessageDialog(
                        null,
                        "Boom! You stepped on a mine.\nYou lost 1 life.\nMomentum multiplier reset!",
                        "Mine Hit",
                        JOptionPane.ERROR_MESSAGE
                );

                // if shared lives reach 0 we show game over and go back to menu
                if (gameManager.getLives() <= 0) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Game Over! No lives left.",
                            "Game Over",
                            JOptionPane.ERROR_MESSAGE
                    );
                    boardController.quitToMenu();
                    return;
                }

                // mine was handled, now we just pass the turn
                switchTurn();
                return;
            }

            // ---------------- QUESTION CELL -----------------
            // question cell gives the player a choice: answer now or pass turn and keep it for later
            if (cell.isQuestion() && cell.isHidden()) {
                showQuestionChoiceDialog(playerNum, row, col);
                return;
            }

            // ---------------- SURPRISE CELL -----------------
            // same idea for surprise: pass and mark it darker, or activate and get random effect
            if (cell.isSurprise() && cell.isHidden()) {
                showSurpriseChoiceDialog(playerNum, row, col);
                return;
            }

            // ---------------- SAFE CELL (EMPTY or NUMBER) -----------------
            // normal safe cell, we open it, update view, award points with momentum bonus
            if (cell.isHidden() && (cell.isSafe())) {
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                gameManager.awardSafeCellWithMomentum();
                gameBoardView.updateScore(gameManager.getScore());
                
                // Update momentum display
                gameBoardView.updateMomentumDisplay(
                        gameManager.getConsecutiveSafeCells(),
                        gameManager.getMomentumTierDescription()
                );
                
                // Update shop buttons in case score changed
                gameBoardView.updateShopButtons(gameManager.getScore(), 
                        gameManager.isSafetyNetActive(), 
                        gameManager.isMetalDetectorActive(),
                        gameManager.getSafetyNetPurchases(),
                        gameManager.getMetalDetectorPurchases());
                
                switchTurn();
            }
        }

        @Override
        public void onCellRightClick(int playerNum, int row, int col) {
            // right click also obeys turn rules, we dont let the other player flag your board
            if (playerNum != currentPlayer)
                return;

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            // if the cell is already revealed we ignore this flag click (and don't switch turn)
            if (cell.isRevealed()) {
                return;
            }

            boolean addingFlag = cell.isHidden(); // true if trying to place a flag

            // block placing new flag when score <= 0, but still allow removing existing flags
            if (addingFlag && gameManager.getScore() <= 0) {
                gameBoardView.showMessage("Not Enough Points",
                        "You need a positive score to place a flag.");
                return;
            }

            if (addingFlag) {
                // scoring for placing a flag
                switch (cell.getType()) {
                    case NUMBER -> gameManager.addPoints(-3);
                    case EMPTY -> gameManager.addPoints(-3);
                    case SURPRISE -> gameManager.addPoints(-3);
                    case QUESTION -> gameManager.addPoints(-3);
                    case MINE -> {
                        // no fixed +1 here anymore
                        // reward is handled by gainLifeOrPoints() below
                    }
                }

                if (cell.isMine()) {
                    // reward: if lives < max -> +1 life, else +open-cost points
                    gameManager.gainLifeOrPoints();

                    // update lives on UI since they may have changed
                    gameBoardView.updateLives(gameManager.getLives());

                    // reveal mine (no cascade) and update mines-left
                    cell.setState(CellState.REVEALED);
                    gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                } else {
                    // wrong flag: cell stays flagged (flag stays on top)
                    cell.setState(CellState.FLAGGED);
                }
            } else {
                // removing a flag: just hide, no score change
                cell.setState(CellState.HIDDEN);
            }


            // update score label
            gameBoardView.updateScore(gameManager.getScore());
            
            // Update shop buttons after score change
            gameBoardView.updateShopButtons(gameManager.getScore(), 
                    gameManager.isSafetyNetActive(), 
                    gameManager.isMetalDetectorActive(),
                    gameManager.getSafetyNetPurchases(),
                    gameManager.getMetalDetectorPurchases());

            // redraw this cell based on its new state/type
            cell = currentBoard.getCell(row, col); // re-fetch if needed
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            // after a flag attempt (right-click) we still pass the turn
            switchTurn();
        }




        @Override
        public void pauseGame() {
            // pause the main game timer
            if (gameBoardView != null) {
                gameBoardView.pauseTimer();
            }
            gameBoardView.showMessage("Pause", "Game Paused");
            // resume timer when dialog closes
            if (gameBoardView != null) {
                gameBoardView.resumeTimer();
            }
        }

        @Override
        public void quitToMenu() {
            // close the board window and go back to main menu screen
            if (gameBoardView != null) gameBoardView.close();
            mainMenu.show();
        }
    };

    // ---------------- Constructor -------------------

    // when the app starts we only show the main menu, game is created later from setup
    public Main() {
        mainMenu = new MainMenu(menuController);
        mainMenu.show();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(Main::new);
    }

    // ---------------- Setup -------------------

    // this shows the setup window where we choose names and difficulty
    private void showSetup() {
        if (gameSetup == null)
            gameSetup = new GameSetup(setupController);
        mainMenu.close();
        gameSetup.show();
    }

    // ---------------- Start Game -------------------

    // here we actually create the 2 boards and the game manager and open the game window
    private void startGameBoard(String p1, String p2, Difficulty difficulty, int p1CharIndex, int p2CharIndex) {
        if (gameSetup != null) gameSetup.close();
        gameManager = new GameManger();
        gameManager.GameManager(difficulty);

        // NEW: reset used question tracking for a fresh game
        model.SysData.resetAskedQuestions();
        
        // Reset Stabilizer usage for new game
        stabilizerUsed = false;

        board1 = new Board(difficulty);
        board2 = new Board(difficulty);

        // Set the boards in the game manager so it can reveal cells
        gameManager.setBoard(board1);  // We'll switch boards when needed

        int size = switch (difficulty) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };

        gameBoardView = new GameBoardView(boardController, p1, p2, size, p1CharIndex, p2CharIndex);
        
        // Set board references for metal detector
        gameBoardView.setBoards(board1, board2);

        // at start we draw all cells as hidden for both players
        updateBoardDisplay(1, board1);
        updateBoardDisplay(2, board2);

        // mines-left starts with total mines for each board
        gameBoardView.updateMinesLeft(1, board1.getTotalMines());
        gameBoardView.updateMinesLeft(2, board2.getTotalMines());

        // shared score and lives also start here
        currentPlayer = 1;
        gameBoardView.updateScore(0);
        gameBoardView.updateLives(gameManager.getMaxLives());
        gameBoardView.updateStatus("Game Started!");
        gameBoardView.updateTurnVisuals(1);
        
        // Initialize shop UI
        gameBoardView.updateMomentumDisplay(0, "No bonus (5 more for Tier 1)");
        gameBoardView.updateShopButtons(0, false, false, 0, 0);
        gameBoardView.updateShopStatus("");
        
        // Set shop button listeners
        gameBoardView.setShopButtonListeners(
            // Safety Net purchase
            () -> {
                if (gameManager.purchaseSafetyNet()) {
                    gameBoardView.updateScore(gameManager.getScore());
                    gameBoardView.updateShopButtons(gameManager.getScore(), 
                            gameManager.isSafetyNetActive(), 
                            gameManager.isMetalDetectorActive(),
                            gameManager.getSafetyNetPurchases(),
                            gameManager.getMetalDetectorPurchases());
                    gameBoardView.updateShopStatus("Safety Net purchased! Next mine will be disabled.");
                    JOptionPane.showMessageDialog(null,
                            "Safety Net purchased!\nThe next mine you click will be automatically flagged.",
                            "Purchase Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Cannot purchase Safety Net.\nRequires 10 points and not already active.",
                            "Purchase Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            },
            // Metal Detector purchase
            () -> {
                if (gameManager.purchaseMetalDetector()) {
                    gameBoardView.updateScore(gameManager.getScore());
                    gameBoardView.updateShopButtons(gameManager.getScore(), 
                            gameManager.isSafetyNetActive(), 
                            gameManager.isMetalDetectorActive(),
                            gameManager.getSafetyNetPurchases(),
                            gameManager.getMetalDetectorPurchases());
                    
                    JOptionPane.showMessageDialog(null,
                            "Metal Detector purchased!\nHover over cells - cursor will change color over mines.",
                            "Purchase Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    // Start timer AFTER user clicks OK
                    gameManager.startMetalDetector();
                    gameBoardView.setMetalDetectorActive(true);
                    gameBoardView.updateShopStatus("Metal Detector active!");
                    
                    // Start a timer to update the countdown
                    Timer detectorTimer = new Timer(100, null);
                    detectorTimer.addActionListener(e -> {
                        long remaining = gameManager.getMetalDetectorTimeRemaining();
                        if (remaining > 0) {
                            double seconds = remaining / 1000.0;
                            gameBoardView.updateMetalDetectorTimer(seconds);
                        } else {
                            gameBoardView.updateMetalDetectorTimer(0);
                            gameBoardView.updateShopStatus("");
                            gameBoardView.setMetalDetectorActive(false);
                            gameBoardView.updateShopButtons(gameManager.getScore(), 
                                    gameManager.isSafetyNetActive(), 
                                    gameManager.isMetalDetectorActive(),
                                    gameManager.getSafetyNetPurchases(),
                                    gameManager.getMetalDetectorPurchases());
                            detectorTimer.stop();
                        }
                    });
                    detectorTimer.start();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Cannot purchase Metal Detector.\nRequires 15 points.",
                            "Purchase Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        );
        
        gameBoardView.show();
    }

    // ---------------- Helpers -------------------

    // helper to redraw a whole board, used after flood fill or any big reveal
    private void updateBoardDisplay(int playerNum, Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                gameBoardView.updateCell(
                        playerNum,
                        r,
                        c,
                        cell,
                        cell.getDisplayLabel()
                );
            }
        }
    }

    // switch to the other player, but also check if one or both boards are already fully done
    private void switchTurn() {
        // if both boards finished -> end game and show a win message
        boolean p1Done = board1.isFinished();
        boolean p2Done = board2.isFinished();

        if (p1Done && p2Done) {
            gameBoardView.updateStatus("Game Over! Both boards are fully revealed.");
            gameBoardView.updateTurnVisuals(0);
            
            // Show game summary
            if (gameBoardView != null) {
                gameBoardView.stopTimer();
            }

            int durationSeconds = (gameBoardView != null) ? gameBoardView.getElapsedSeconds() : 0;
            int finalScore = gameManager.getScore();
            int livesRemaining = gameManager.getLives();
            String difficulty = board1.getDifficulty().name();

            // Save to history
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            model.History history = new model.History(
                    now,
                    player1Name,
                    player2Name,
                    difficulty,
                    true,  // they won
                    finalScore,
                    durationSeconds,
                    0,  // minesHit - not tracked in this version
                    0,  // questionsAnswered - not tracked in this version
                    0,  // correctQuestions - not tracked in this version
                    0,  // wrongQuestions - not tracked
                    0,  // surprisesTriggered - not tracked
                    0,  // positiveSurprises - not tracked
                    0,  // negativeSurprises - not tracked
                    livesRemaining,
                    currentUser
            );
            model.SysData.addHistory(history);

            // Create and show summary
            view.GameSummaryView summary = new view.GameSummaryView(
                    new view.GameSummaryView.SummaryController() {
                        @Override
                        public void onPlayAgain() {
                            if (gameBoardView != null) gameBoardView.close();
                            showSetup();
                        }

                        @Override
                        public void onBackToMenu() {
                            if (gameBoardView != null) gameBoardView.close();
                            mainMenu.show();
                        }
                    },
                    player1Name,
                    player2Name,
                    difficulty,
                    true,  // win
                    finalScore,
                    durationSeconds,
                    0,  // minesHit
                    0,  // questionsAnswered
                    0,  // correctQuestions
                    0,  // wrongQuestions
                    0,  // surprisesTriggered
                    0,  // positiveSurprises
                    0,  // negativeSurprises
                    livesRemaining
            );
            summary.show();
            
            if (gameBoardView != null) gameBoardView.close();
            return;
        }

        // normal turn switch, but skip finished board so we dont give turns to a solved board
        int next = currentPlayer == 1 ? 2 : 1;
        if (next == 1 && p1Done) {
            next = 2;
        } else if (next == 2 && p2Done) {
            next = 1;
        }

        currentPlayer = next;

        gameBoardView.updateTurnVisuals(currentPlayer);
    }


    // ---------------- "Question Cell" popup -------------------

    // Helper method to style dialogs and buttons to match the game theme
    private void styleDialog(JDialog dialog) {
        dialog.setBackground(new Color(15, 25, 30));
        dialog.getContentPane().setBackground(new Color(15, 25, 30));
    }

    private JButton createThemedButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(new Color(220, 235, 230));
        btn.setFont(new Font("Tahoma", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(20, 80, 60));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 102, 51), 2, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(30, 120, 80));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(20, 80, 60));
            }
        });
        return btn;
    }

    // this popup is shown when we open a question cell and the player must pick pass or answer
    private void showQuestionChoiceDialog(int playerNum, int row, int col) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Question Cell");
        dialog.setModal(true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(null);
        styleDialog(dialog);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel msg = new JLabel(
                "<html>You uncovered a Question Cell!<br/>What do you want to do?</html>",
                SwingConstants.CENTER
        );
        msg.setFont(new Font("Tahoma", Font.BOLD, 16));
        msg.setForeground(new Color(255, 215, 0));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(msg);
        mainPanel.add(Box.createVerticalStrut(15));

        JButton passBtn = createThemedButton("Pass Turn");
        JButton answerBtn = createThemedButton("Answer Question");
        passBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        answerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // pass turn: we just reveal the Q, paint it yellow and give the next player the turn
        passBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            cell.setState(CellState.REVEALED);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            switchTurn();
        });

        // answer: we reveal the cell and also run flood fill if needed, then open the question dialog
        answerBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            cell.setState(CellState.REVEALED);
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);

            // we save which question this was so later we can mark it as “attempted”
            lastQuestionPlayer = playerNum;
            lastQuestionRow = row;
            lastQuestionCol = col;

            showQuestionDifficultyDialog(playerNum);
        });

        mainPanel.add(passBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(answerBtn);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // ---------------- Surprise Cell popup -------------------

    // this popup is for surprise cells, same idea but with random effect instead of a question
    private void showSurpriseChoiceDialog(int playerNum, int row, int col) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Surprise Cell");
        dialog.setModal(true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(null);
        styleDialog(dialog);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel msg = new JLabel(
                "<html>You uncovered a Surprise Cell!<br/>What do you want to do?</html>",
                SwingConstants.CENTER
        );
        msg.setFont(new Font("Tahoma", Font.BOLD, 16));
        msg.setForeground(new Color(180, 100, 255));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(msg);
        mainPanel.add(Box.createVerticalStrut(15));

        JButton passBtn = createThemedButton("Pass Turn");
        JButton activateBtn = createThemedButton("Try Your Luck");
        passBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        activateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // pass: we reveal S, mark that we passed it so it becomes darker but can still be used later
        passBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            cell.setState(CellState.REVEALED);
            cell.setSurprisePassed(true);      // mark as passed so we paint it darker and keep it active
            cell.setReadyForSurprise(false);   // not used yet

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
            cell.setReadyForSurprise(true);    // used, no more clicking
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

            // Create result object with full breakdown (open cost + effect outcome)
            int livesChange = positive ? 1 : -1;
            String effectDesc = positive ? "Good Surprise! Gained effect" : "Bad Surprise! Negative effect";
            GameManger.QuestionResult result = new GameManger.QuestionResult(
                    positive,  // isCorrect = true for positive, false for negative
                    effectPoints, 
                    livesChange, 
                    openCost,
                    effectDesc
            );
            
            showQuestionAnswerMessage(result);
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateLives(gameManager.getLives());

            switchTurn();
        });

        mainPanel.add(passBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(activateBtn);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // ---------------- Question Dialog (placeholder) -------------------

    private void openQuestionDialog(@SuppressWarnings("unused") int playerNum) {
        Questions q = SysData.getRandomQuestion(currentQuestionDifficulty);
        if (q == null) {
            JOptionPane.showMessageDialog(null,
                    "No questions left for this difficulty.",
                    "No Questions",
                    JOptionPane.INFORMATION_MESSAGE);
            // still switch turn after “answering nothing”
            handleQuestionAnswer(null, false); // or just switchTurn()
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Question (Difficulty " + currentQuestionDifficulty + ")");
        dialog.setModal(true);
        dialog.setSize(550, 420);
        dialog.setLocationRelativeTo(null);
        styleDialog(dialog);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel("<html>" + q.getText() + "</html>", SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(new Color(0, 200, 255));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(label);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton a = createThemedButton("A) " + q.getOptA());
        JButton b = createThemedButton("B) " + q.getOptB());
        JButton c = createThemedButton("C) " + q.getOptC());
        JButton d = createThemedButton("D) " + q.getOptD());
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Timer label for countdown
        JLabel timerLabel = new JLabel("Time: 20", SwingConstants.CENTER);
        timerLabel.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 20));
        timerLabel.setForeground(new java.awt.Color(0, 200, 255));
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
                    handleQuestionAnswer(dialog, false); // fail the question
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
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
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


    // after answering a question we pay the open cost, apply effect and mark the Q as attempted
    private void handleQuestionAnswer(JDialog dialog, boolean isCorrect) {
        // Record the attempt cost before deducting
        int attemptCost = gameManager.getBaseOpenCost();
        gameManager.applyOpenCost();

        // Set the question difficulty in the game manager
        gameManager.setCurrentQuestionDifficulty(currentQuestionDifficulty);

        // Set the current board so GameManager can reveal cells if needed
        Board currentBoard = (lastQuestionPlayer == 1) ? board1 : board2;
        gameManager.setBoard(currentBoard);

        // Process the question answer and get the result
        GameManger.QuestionResult result = gameManager.processQuestionAnswer(isCorrect);
        
        // Set the attempt cost in the result
        result.attemptCost = attemptCost;

        // Show message about the answer result with detailed breakdown
        showQuestionAnswerMessage(result);

        // Update the board display if any cells were revealed
        if (!result.cellsRevealed.isEmpty()) {
            updateBoardDisplay(lastQuestionPlayer, currentBoard);
        }

        gameBoardView.updateScore(gameManager.getScore());
        gameBoardView.updateLives(gameManager.getLives());
        
        // Update shop buttons after score/lives change
        gameBoardView.updateShopButtons(gameManager.getScore(), 
                gameManager.isSafetyNetActive(), 
                gameManager.isMetalDetectorActive(),
                gameManager.getSafetyNetPurchases(),
                gameManager.getMetalDetectorPurchases());

        if (dialog != null) {
            dialog.dispose();
        }

        // if we still remember which question this was, we paint it as attempted (darker Q, disabled)
        if (lastQuestionPlayer != -1 &&
                lastQuestionRow != -1 &&
                lastQuestionCol != -1) {
            Cell cell = currentBoard.getCell(lastQuestionRow, lastQuestionCol);
            if (cell.isQuestion()) {
                cell.setQuestionAttempted(true);
                gameBoardView.markQuestionAttempted(
                        lastQuestionPlayer,
                        lastQuestionRow,
                        lastQuestionCol,
                        cell
                );
            }
        }

        // after handling the question we pass the turn to the other player
        switchTurn();
    }
    
    // ---------------- Stabilizer Question (Last Life) -------------------
    
    private void showStabilizerQuestion(int playerNum, int row, int col) {
        gameBoardView.pauseTimer();
        
        // Get a random easy/medium question for stabilizer
        Questions q = SysData.getRandomQuestion(1); // Easy question
        if (q == null) {
            // No question available, mine explodes
            Board currentBoard = (playerNum == 1) ? board1 : board2;
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);
            
            gameManager.processMineHit();
            gameBoardView.updateLives(gameManager.getLives());
            gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
            
            JOptionPane.showMessageDialog(null,
                    "Stabilizer Failed! No question available.\nMine exploded!",
                    "Stabilizer Failed",
                    JOptionPane.ERROR_MESSAGE);
            
            if (gameManager.getLives() <= 0) {
                boardController.quitToMenu();
                return;
            }
            
            switchTurn();
            return;
        }
        
        JDialog dialog = new JDialog();
        dialog.setTitle("⚠️ STABILIZER ACTIVATED - LAST LIFE! ⚠️");
        dialog.setModal(true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(null);
        styleDialog(dialog);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(15, 25, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel warning = new JLabel("<html><center>⚠️ YOU ARE ON YOUR LAST LIFE! ⚠️<br>"
                + "Answer this question to disable the mine!</center></html>", SwingConstants.CENTER);
        warning.setFont(new Font("Tahoma", Font.BOLD, 16));
        warning.setForeground(new Color(255, 50, 50));
        warning.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(warning);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel label = new JLabel("<html>" + q.getText() + "</html>", SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(new Color(0, 200, 255));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(label);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton a = createThemedButton("A) " + q.getOptA());
        JButton b = createThemedButton("B) " + q.getOptB());
        JButton c = createThemedButton("C) " + q.getOptC());
        JButton d = createThemedButton("D) " + q.getOptD());
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Timer for 20 seconds
        JLabel timerLabel = new JLabel("Time: 20", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        timerLabel.setForeground(new Color(255, 100, 100));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(timerLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        final boolean[] answered = {false};
        Timer countdownTimer = new Timer(1000, null);
        final int[] timeLeft = {20};

        ActionListener answerListener = e -> {
            if (answered[0]) return;
            answered[0] = true;
            countdownTimer.stop();

            JButton clicked = (JButton) e.getSource();
            String choice = clicked.getText().substring(0, 1).toUpperCase();
            boolean isCorrect = choice.equals(q.getCorrectAnswer());

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            gameManager.processStabilizerQuestion(isCorrect);

            if (isCorrect) {
                // Success: flag the mine
                Cell cell = currentBoard.getCell(row, col);
                cell.setState(CellState.FLAGGED);
                gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());
                gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                
                JOptionPane.showMessageDialog(null,
                        "Correct! The mine has been disabled and flagged.\nYou survived!",
                        "Stabilizer Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Failure: mine explodes
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);
                gameBoardView.updateLives(gameManager.getLives());
                gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                
                // Update momentum display (reset)
                gameBoardView.updateMomentumDisplay(
                        gameManager.getConsecutiveSafeCells(),
                        gameManager.getMomentumTierDescription()
                );
                
                JOptionPane.showMessageDialog(null,
                        "Wrong answer! The mine exploded.\nGame Over!",
                        "Stabilizer Failed",
                        JOptionPane.ERROR_MESSAGE);
                
                if (gameManager.getLives() <= 0) {
                    dialog.dispose();
                    gameBoardView.resumeTimer();
                    boardController.quitToMenu();
                    return;
                }
            }

            dialog.dispose();
            gameBoardView.resumeTimer();
            switchTurn();
        };

        a.addActionListener(answerListener);
        b.addActionListener(answerListener);
        c.addActionListener(answerListener);
        d.addActionListener(answerListener);

        countdownTimer.addActionListener(e -> {
            timeLeft[0]--;
            timerLabel.setText("Time: " + timeLeft[0]);
            
            if (timeLeft[0] <= 0) {
                countdownTimer.stop();
                if (!answered[0]) {
                    answered[0] = true;
                    
                    // Timeout = failure
                    Board currentBoard = (playerNum == 1) ? board1 : board2;
                    gameManager.processStabilizerQuestion(false);
                    
                    currentBoard.reveal(row, col);
                    updateBoardDisplay(playerNum, currentBoard);
                    gameBoardView.updateLives(gameManager.getLives());
                    gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                    
                    // Update momentum display (reset)
                    gameBoardView.updateMomentumDisplay(
                            gameManager.getConsecutiveSafeCells(),
                            gameManager.getMomentumTierDescription()
                    );
                    
                    JOptionPane.showMessageDialog(null,
                            "Time's up! The mine exploded.\nGame Over!",
                            "Stabilizer Failed",
                            JOptionPane.ERROR_MESSAGE);
                    
                    dialog.dispose();
                    gameBoardView.resumeTimer();
                    
                    if (gameManager.getLives() <= 0) {
                        boardController.quitToMenu();
                        return;
                    }
                    
                    switchTurn();
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
        countdownTimer.start();
        dialog.setVisible(true);
    }
    
    private void showQuestionDifficultyDialog(int playerNum) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Select Question Difficulty");
        dialog.setModal(true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(5, 1, 5, 5));
        styleDialog(dialog);

        JLabel msg = new JLabel(
                "<html>Select a difficulty for your question:<br>"
                        + "Easy, Medium, Hard, or Advanced.</html>",
                SwingConstants.CENTER);
        msg.setFont(new Font("Tahoma", Font.BOLD, 14));
        msg.setForeground(new Color(255, 215, 0));
        dialog.add(msg);

        int remEasy = SysData.getRemainingQuestions(1);
        int totEasy = SysData.getTotalQuestions(1);
        int remMed = SysData.getRemainingQuestions(2);
        int totMed = SysData.getTotalQuestions(2);
        int remHard = SysData.getRemainingQuestions(3);
        int totHard = SysData.getTotalQuestions(3);
        int remAdv = SysData.getRemainingQuestions(4);
        int totAdv = SysData.getTotalQuestions(4);

        JButton easyBtn = createThemedButton("Easy  (questions left " + remEasy + "/" + totEasy + ")");
        JButton mediumBtn = createThemedButton("Medium  (questions left " + remMed + "/" + totMed + ")");
        JButton hardBtn = createThemedButton("Hard  (questions left " + remHard + "/" + totHard + ")");
        JButton advancedBtn = createThemedButton("Advanced  (questions left " + remAdv + "/" + totAdv + ")");

        easyBtn.setEnabled(remEasy > 0);
        mediumBtn.setEnabled(remMed > 0);
        hardBtn.setEnabled(remHard > 0);
        advancedBtn.setEnabled(remAdv > 0);

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

        dialog.add(easyBtn);
        dialog.add(mediumBtn);
        dialog.add(hardBtn);
        dialog.add(advancedBtn);

        dialog.setVisible(true);
    }
    



}
