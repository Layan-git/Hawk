package controller;

import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.border.LineBorder;

import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.Cell.CellState;
import model.GameManger;
import model.Questions;
import model.SysData;
import model.History;
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
        void confirmStart(String player1, String player2, Difficulty difficulty);
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

    // login/admin
    private view.LoginView loginView;
    private boolean isAdmin = false;
    private String currentUser = null;

    // in Main class, add a field:
    private view.QuestionsManager questionsManager;
    private view.Settings settings;

    private Board board1;
    private Board board2;

    private String player1Name;
    private String player2Name;

    private int currentPlayer = 1;   // we start the game with player 1

    // remember which question cell is currently being answered
    private int lastQuestionPlayer = -1;
    private int lastQuestionRow = -1;
    private int lastQuestionCol = -1;

    // question difficulty chosen by player: 1 = Easy, 2 = Medium, 3 = Hard, 4 = Advanced
    private int currentQuestionDifficulty = 1;

    // stats for summary / history
    private int minesHit = 0;
    private int questionsAnswered = 0;
    private int correctQuestions = 0;
    private int wrongQuestions = 0;
    private int surprisesTriggered = 0;
    private int positiveSurprises = 0;
    private int negativeSurprises = 0;

    // controller for summary screen
    private class SummaryControllerImpl implements view.GameSummaryView.SummaryController {
        @Override
        public void onPlayAgain() {
            Difficulty d = board1.getDifficulty();
            startGameBoard(player1Name, player2Name, d);
        }

        @Override
        public void onBackToMenu() {
            mainMenu.show();
        }
    }

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
            if (!isAdmin) {
                JOptionPane.showMessageDialog(
                        null,
                        "Only admin can manage questions.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            if (questionsManager == null) {
                questionsManager = new view.QuestionsManager();
            }
            questionsManager.show();
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
        public void confirmStart(String p1, String p2, Difficulty d) {
            player1Name = p1;
            player2Name = p2;
            startGameBoard(p1, p2, d);
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
            if (playerNum != currentPlayer) {
                gameBoardView.showMessage("Wrong Turn", "Wait for your turn!");
                return;
            }

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            if (cell.isQuestion() && cell.isRevealed() && !cell.isQuestionAttempted()) {
                showQuestionChoiceDialog(playerNum, row, col);
                return;
            }

            if (cell.isSurprise() && cell.isRevealed() && !cell.isReadyForSurprise()) {
                showSurpriseChoiceDialog(playerNum, row, col);
                return;
            }

            if (cell.isFlagged())
                return;

            // ---------------- MINE -----------------
            if (cell.isMine() && cell.isHidden()) {
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                gameManager.processMineHit();
                minesHit++;  // stats
                gameBoardView.updateLives(gameManager.getLives());
                gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());

                JOptionPane.showMessageDialog(
                        null,
                        "Boom! You stepped on a mine.\nYou lost 1 life.",
                        "Mine Hit",
                        JOptionPane.ERROR_MESSAGE
                );

                if (gameManager.getLives() <= 0) {
                    endGameAndShowSummary(false);
                    return;
                }

                switchTurn();
                return;
            }

            // ---------------- QUESTION CELL -----------------
            if (cell.isQuestion() && cell.isHidden()) {
                showQuestionChoiceDialog(playerNum, row, col);
                return;
            }

            // ---------------- SURPRISE CELL -----------------
            if (cell.isSurprise() && cell.isHidden()) {
                showSurpriseChoiceDialog(playerNum, row, col);
                return;
            }

            // ---------------- SAFE CELL -----------------
            if (cell.isHidden() && cell.isSafe()) {
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                gameManager.awardSafeCellPoint();  // +1 point
                gameBoardView.updateScore(gameManager.getScore());
                switchTurn();
            }
        }

        @Override
        public void onCellRightClick(int playerNum, int row, int col) {
            if (playerNum != currentPlayer)
                return;

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            if (cell.isRevealed()) {
                return;
            }

            boolean addingFlag = cell.isHidden(); // true if trying to place a flag

            if (addingFlag && gameManager.getScore() <= 0) {
                gameBoardView.showMessage("Not Enough Points",
                        "You need a positive score to place a flag.");
                return;
            }

            if (addingFlag) {
                switch (cell.getType()) {
                    case NUMBER -> gameManager.addPoints(-3);
                    case EMPTY -> gameManager.addPoints(-3);
                    case SURPRISE -> gameManager.addPoints(-3);
                    case QUESTION -> gameManager.addPoints(-3);
                    case MINE -> {
                    }
                }

                if (cell.isMine()) {
                    gameManager.gainLifeOrPoints();
                    gameBoardView.updateLives(gameManager.getLives());
                    cell.setState(CellState.REVEALED);
                    gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                } else {
                    cell.setState(CellState.FLAGGED);
                }
            } else {
                cell.setState(CellState.HIDDEN);
            }

            gameBoardView.updateScore(gameManager.getScore());
            cell = currentBoard.getCell(row, col);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());
            switchTurn();
        }

        @Override
        public void pauseGame() {
            if (gameBoardView != null) {
                gameBoardView.pauseTimer();
            }
            gameBoardView.showMessage("Pause", "Game Paused");
            if (gameBoardView != null) {
                gameBoardView.resumeTimer();
            }
        }

        @Override
        public void quitToMenu() {
            if (gameBoardView != null) gameBoardView.close();
            mainMenu.show();
        }
    };

    // ---------------- Constructor -------------------

    public Main() {
        view.LoginView.LoginController loginController = new view.LoginView.LoginController() {
            @Override
            public void onLogin(String username, String password) {
                if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
                    isAdmin = true;
                    currentUser = "admin";
                } else {
                    isAdmin = false;
                    currentUser = username;
                }

                if (currentUser == null || currentUser.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Please enter a username.",
                            "Login",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                loginView.close();
                mainMenu = new MainMenu(menuController);
                mainMenu.show();
            }

            @Override
            public void onExit() {
                System.exit(0);
            }
        };

        loginView = new view.LoginView(loginController);
        loginView.show();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(Main::new);
    }

    // ---------------- Setup -------------------

    private void showSetup() {
        if (gameSetup == null)
            gameSetup = new GameSetup(setupController);
        mainMenu.close();
        gameSetup.show();
    }

    // ---------------- Start Game -------------------

    private void startGameBoard(String p1, String p2, Difficulty difficulty) {
        if (gameSetup != null) gameSetup.close();
        gameManager = new GameManger();
        gameManager.GameManager(difficulty);

        SysData.resetAskedQuestions();

        minesHit = 0;
        questionsAnswered = 0;
        correctQuestions = 0;
        wrongQuestions = 0;
        surprisesTriggered = 0;
        positiveSurprises = 0;
        negativeSurprises = 0;

        board1 = new Board(difficulty);
        board2 = new Board(difficulty);

        gameManager.setBoard(board1);

        int size = switch (difficulty) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };

        gameBoardView = new GameBoardView(boardController, p1, p2, size);

        updateBoardDisplay(1, board1);
        updateBoardDisplay(2, board2);

        gameBoardView.updateMinesLeft(1, board1.getTotalMines());
        gameBoardView.updateMinesLeft(2, board2.getTotalMines());

        currentPlayer = 1;
        gameBoardView.updateScore(0);
        gameBoardView.updateLives(gameManager.getMaxLives());
        gameBoardView.updateStatus("Game Started!");
        gameBoardView.updateTurnVisuals(1);
        gameBoardView.show();
    }

    // ---------------- Helpers -------------------

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

    private void endGameAndShowSummary(boolean winByBoardsFinished) {
        if (gameBoardView != null) {
            gameBoardView.stopTimer();
        }

        int durationSeconds = (gameBoardView != null) ? gameBoardView.getElapsedSeconds() : 0;
        int finalScore = gameManager.getScore();
        int livesRemaining = gameManager.getLives();
        Difficulty difficulty = board1.getDifficulty();

        LocalDateTime now = LocalDateTime.now();

        History history = new History(
                now,
                player1Name,
                player2Name,
                difficulty.name(),
                winByBoardsFinished,
                finalScore,
                durationSeconds,
                minesHit,
                questionsAnswered,
                correctQuestions,
                wrongQuestions,
                surprisesTriggered,
                positiveSurprises,
                negativeSurprises,
                livesRemaining,
                currentUser
        );

        SysData.addHistory(history);

        view.GameSummaryView summary = new view.GameSummaryView(
                new SummaryControllerImpl(),
                player1Name,
                player2Name,
                difficulty.name(),
                winByBoardsFinished,
                finalScore,
                durationSeconds,
                minesHit,
                questionsAnswered,
                correctQuestions,
                wrongQuestions,
                surprisesTriggered,
                positiveSurprises,
                negativeSurprises,
                livesRemaining
        );
        summary.show();
    }

    private void switchTurn() {
        boolean p1Done = board1.isFinished();
        boolean p2Done = board2.isFinished();

        if (p1Done && p2Done) {
            gameBoardView.updateStatus("Game Over! Both boards are fully revealed.");
            gameBoardView.updateTurnVisuals(0);
            endGameAndShowSummary(true);
            return;
        }

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

        passBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            cell.setState(CellState.REVEALED);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            switchTurn();
        });

        answerBtn.addActionListener(e -> {
            dialog.dispose();

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            cell.setState(CellState.REVEALED);
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);

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

            int openCost = gameManager.getBaseOpenCost();
            gameManager.applyOpenCost();

            boolean positive = Math.random() < 0.5;
            int effectPoints = positive
                    ? gameManager.getGoodEffectPoints()
                    : gameManager.getBadEffectPoints();

            surprisesTriggered++;
            if (positive) {
                positiveSurprises++;
            } else {
                negativeSurprises++;
            }

            if (positive) gameManager.applyPositiveEffect();
            else gameManager.applyNegativeEffect();

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

            switchTurn();
        });

        mainPanel.add(passBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(activateBtn);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // ---------------- Question Dialog -------------------

    private void openQuestionDialog(@SuppressWarnings("unused") int playerNum) {
        Questions q = SysData.getRandomQuestion(currentQuestionDifficulty);
        if (q == null) {
            JOptionPane.showMessageDialog(null,
                    "No questions left for this difficulty.",
                    "No Questions",
                    JOptionPane.INFORMATION_MESSAGE);
            handleQuestionAnswer(null, false);
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

        JLabel timerLabel = new JLabel("Time: 20", SwingConstants.CENTER);
        timerLabel.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 20));
        timerLabel.setForeground(new java.awt.Color(0, 200, 255));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(timerLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        final boolean[] answered = {false};

        final int[] timeRemaining = {20};
        Timer questionTimer = new Timer(1000, e -> {
            timeRemaining[0]--;
            timerLabel.setText("Time: " + timeRemaining[0]);

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
        int attemptCost = gameManager.getBaseOpenCost();
        gameManager.applyOpenCost();

        gameManager.setCurrentQuestionDifficulty(currentQuestionDifficulty);

        Board currentBoard = (lastQuestionPlayer == 1) ? board1 : board2;
        gameManager.setBoard(currentBoard);

        GameManger.QuestionResult result = gameManager.processQuestionAnswer(isCorrect);
        result.attemptCost = attemptCost;

        questionsAnswered++;
        if (result.isCorrect) {
            correctQuestions++;
        } else {
            wrongQuestions++;
        }

        showQuestionAnswerMessage(result);

        if (!result.cellsRevealed.isEmpty()) {
            updateBoardDisplay(lastQuestionPlayer, currentBoard);
        }

        gameBoardView.updateScore(gameManager.getScore());
        gameBoardView.updateLives(gameManager.getLives());

        if (dialog != null) {
            dialog.dispose();
        }

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

        switchTurn();
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