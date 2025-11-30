package controller;

import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.Cell.CellState;
import model.GameManger;
import view.MainMenu;
import view.GameSetup;
import view.GameBoardView;

import java.awt.GridLayout;
import javax.swing.*;

public class Main {

    public static interface MainMenuController {
        void startGame();
        void openHistory();
        void openManageQuestions();
        void openHowToPlay();
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
    private Board board1;
    private Board board2;

    private String player1Name;
    private String player2Name;
    private int currentPlayer = 1;

    // ---------------- Controllers -------------------
    private final MainMenuController menuController = new MainMenuController() {
        public void startGame() { showSetup(); }
        public void openHistory() {}
        public void openManageQuestions() {}
        public void openHowToPlay() {}
        public void exit() { System.exit(0); }
    };

    private final GameSetupController setupController = new GameSetupController() {
        public void backToMenu() {
            if (gameSetup != null) gameSetup.close();
            mainMenu.show();
        }

        public void confirmStart(String p1, String p2, Difficulty d) {
            player1Name = p1;
            player2Name = p2;
            startGameBoard(p1, p2, d);
        }
    };

    // ---------------- New Effect Popup -------------------
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

    // ---------------- Core Game Controller -------------------
    private final GameBoardController boardController = new GameBoardController() {

        @Override
        public void onCellClick(int playerNum, int row, int col) {

            if (playerNum != currentPlayer) {
                gameBoardView.showMessage("Wrong Turn", "Wait for your turn!");
                return;
            }

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            if (cell.isFlagged() || cell.isRevealed())
                return;

            // ---------------- MINE -----------------
            if (cell.isMine()) {
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                gameManager.processMineHit();
                gameBoardView.updateLives(gameManager.getLives());
                gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());

                JOptionPane.showMessageDialog(
                        null,
                        "Boom! You stepped on a mine.\nYou lost 1 life.",
                        "Mine Hit",
                        JOptionPane.ERROR_MESSAGE
                );

                if (gameManager.getLives() <= 0) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Game Over! No lives left.",
                            "Game Over",
                            JOptionPane.ERROR_MESSAGE
                    );
                    quitToMenu();
                    return;
                }

                switchTurn();
                return;
            }

            // ---------------- QUESTION CELL -----------------
            if (cell.isQuestion()) {

                showQuestionChoiceDialog(playerNum, row, col);

                return;
            }

            // ---------------- SURPRISE CELL -----------------
            if (cell.isSurprise()) {
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                // Fake effect (iteration 1)
                boolean positive = Math.random() < 0.5;

                if (positive) gameManager.applyPositiveEffect();
                else gameManager.applyNegativeEffect();

                showEffectMessage(positive);
                gameBoardView.updateScore(gameManager.getScore());

                switchTurn();
                return;
            }

            // ---------------- SAFE CELL -----------------
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);
            gameManager.awardSafeCellPoint();
            gameBoardView.updateScore(gameManager.getScore());
            
            switchTurn();
        }

        @Override
        public void onCellRightClick(int playerNum, int row, int col) {

            if (playerNum != currentPlayer)
                return;

            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);

            // האם אנחנו מוסיפים דגל?
            boolean addingFlag = cell.isHidden();

            // הופכים דגל
            currentBoard.toggleFlag(row, col);

            // ניקוד רק כשמוסיפים דגל
            if (addingFlag) {

                switch (cell.getType()) {

                    case NUMBER -> gameManager.addPoints(-3);
                    case EMPTY  -> gameManager.addPoints(-3);
                    case MINE   -> gameManager.addPoints(+1);

                    default -> {} // QUESTION, SURPRISE — אין ניקוד באיטרציה זו
                }

                gameBoardView.updateScore(gameManager.getScore());
            }

            // עדכון תצוגה
            cell = currentBoard.getCell(row, col);
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            switchTurn();
        }


        @Override
        public void pauseGame() {
            gameBoardView.showMessage("Pause", "Game Paused");
        }

        @Override
        public void quitToMenu() {
            if (gameBoardView != null) gameBoardView.close();
            mainMenu.show();
        }
    };

    // ---------------- Constructor -------------------
    public Main() {
        mainMenu = new MainMenu(menuController);
        mainMenu.show();
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

        board1 = new Board(difficulty);
        board2 = new Board(difficulty);

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
        gameBoardView.updateCurrentTurn(p1);
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
    
    

    private void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        String cpName = (currentPlayer == 1) ? player1Name : player2Name;
        gameBoardView.updateCurrentTurn(cpName);
        gameBoardView.updateTurnVisuals(currentPlayer);
    }

    // ---------------- New "Question Cell" Popup -------------------
    private void showQuestionChoiceDialog(int playerNum, int row, int col) {

        JDialog dialog = new JDialog();
        dialog.setTitle("Question Cell");
        dialog.setModal(true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(3, 1));

        JLabel msg = new JLabel(
                "You uncovered a Question Cell!\nWhat do you want to do?",
                SwingConstants.CENTER
        );

        dialog.add(msg);

        JButton passBtn = new JButton("Pass Turn");
        JButton answerBtn = new JButton("Answer Question");

        passBtn.addActionListener(e -> {
            dialog.dispose();
            switchTurn();
        });

        answerBtn.addActionListener(e -> {
            dialog.dispose();

            // עכשיו חושפים את התא
            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);
            cell.setState(CellState.REVEALED);
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);

            // ועכשיו פותחים את השאלה 
            openQuestionDialog(playerNum);
        });


        dialog.add(passBtn);
        dialog.add(answerBtn);

        dialog.setVisible(true);
    }

    // ---------------- Question Dialog (iteration 1) -------------------
    private void openQuestionDialog(int playerNum) {

        JDialog dialog = new JDialog();
        dialog.setTitle("Question");
        dialog.setModal(true);
        dialog.setSize(400, 260);
        dialog.setLayout(new GridLayout(5, 1));
        dialog.setLocationRelativeTo(null);

        JLabel label = new JLabel(
                "Temporary question placeholder\n(Iteration 1 Simulation)",
                SwingConstants.CENTER
        );
        dialog.add(label);

        JButton a = new JButton("Answer A");
        JButton b = new JButton("Answer B");
        JButton c = new JButton("Answer C");
        JButton d = new JButton("Answer D");

        // Every answer gives temporary effect (iteration 1)
        a.addActionListener(e -> handleQuestionAnswer(dialog));
        b.addActionListener(e -> handleQuestionAnswer(dialog));
        c.addActionListener(e -> handleQuestionAnswer(dialog));
        d.addActionListener(e -> handleQuestionAnswer(dialog));

        dialog.add(a);
        dialog.add(b);
        dialog.add(c);
        dialog.add(d);

        dialog.setVisible(true);
    }

    private void handleQuestionAnswer(JDialog dialog) {

    	gameManager.applyOpenCost();
        // הפעלה זמנית לאיטרציה 1 — תמיד מיישם עלות לפי רמת הקושי
        boolean positive = Math.random() < 0.5;

        if (positive) {
            gameManager.applyPositiveEffect();
        } else {
            gameManager.applyNegativeEffect();
        }
        // ❗ זו תהיה ההודעה היחידה שקופצת
        showEffectMessage(positive);
        // עדכון ניקוד
        gameBoardView.updateScore(gameManager.getScore());
        // סגירת חלון השאלה
        dialog.dispose();
        // העברת תור
        switchTurn();
    }

}
