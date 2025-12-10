package controller;

import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.Cell.CellState;
import model.GameManger;
import model.Questions;
import model.SysData;
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
    
 // in Main class, add a field:
    private view.QuestionsManager questionsManager;

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


    // ---------------- Controllers -------------------


    // inside menuController:
    private final MainMenuController menuController = new MainMenuController() {
        @Override
        public void startGame() { showSetup(); }

        @Override
        public void openHistory() {}

        @Override
        public void openManageQuestions() {
            if (questionsManager == null) {
                questionsManager = new view.QuestionsManager();
            }
            questionsManager.show();
        }

        @Override
        public void openHowToPlay() {}

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
            // if we hit a hidden mine we reveal it, take a life and maybe end the game
            if (cell.isMine() && cell.isHidden()) {
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

                // if shared lives reach 0 we show game over and go back to menu
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
            // normal safe cell, we open it, update view, give +1 point and move to the other player
            if (cell.isHidden() && (cell.isSafe())) {
                currentBoard.reveal(row, col);
                updateBoardDisplay(playerNum, currentBoard);

                gameManager.awardSafeCellPoint();  // +1 point
                gameBoardView.updateScore(gameManager.getScore());
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
                    // wrong flag: cell stays hidden (no flag)
                    cell.setState(CellState.HIDDEN);
                }
            } else {
                // removing a flag: just hide, no score change
                cell.setState(CellState.HIDDEN);
            }


            // update score label
            gameBoardView.updateScore(gameManager.getScore());

            // redraw this cell based on its new state/type
            cell = currentBoard.getCell(row, col); // re-fetch if needed
            gameBoardView.updateCell(playerNum, row, col, cell, cell.getDisplayLabel());

            // after a flag attempt (right-click) we still pass the turn
            switchTurn();
        }




        @Override
        public void pauseGame() {
            // simple pause popup, we still keep the current state and turn
            gameBoardView.showMessage("Pause", "Game Paused");
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
    private void startGameBoard(String p1, String p2, Difficulty difficulty) {
        if (gameSetup != null) gameSetup.close();
        gameManager = new GameManger();
        gameManager.GameManager(difficulty);

        // NEW: reset used question tracking for a fresh game
        model.SysData.resetAskedQuestions();

        board1 = new Board(difficulty);
        board2 = new Board(difficulty);

        int size = switch (difficulty) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };

        gameBoardView = new GameBoardView(boardController, p1, p2, size);

        // at start we draw all cells as hidden for both players
        updateBoardDisplay(1, board1);
        updateBoardDisplay(2, board2);

        // mines-left starts with total mines for each board
        gameBoardView.updateMinesLeft(1, board1.getTotalMines());
        gameBoardView.updateMinesLeft(2, board2.getTotalMines());

        // shared score and lives also start here
        currentPlayer = 1;
        gameBoardView.updateCurrentTurn(p1);
        gameBoardView.updateScore(0);
        gameBoardView.updateLives(gameManager.getMaxLives());
        gameBoardView.updateStatus("Game Started!");
        gameBoardView.updateTurnVisuals(1);
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
            JOptionPane.showMessageDialog(
                    null,
                    "Game Over! All cells on both boards are revealed.You Have Won!!",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE
            );
            // back to main menu
            if (gameBoardView != null) gameBoardView.close();
            mainMenu.show();
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

        String cpName = (currentPlayer == 1) ? player1Name : player2Name;
        gameBoardView.updateCurrentTurn(cpName);
        gameBoardView.updateTurnVisuals(currentPlayer);
    }


    // ---------------- "Question Cell" popup -------------------

    // this popup is shown when we open a question cell and the player must pick pass or answer
    private void showQuestionChoiceDialog(int playerNum, int row, int col) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Question Cell");
        dialog.setModal(true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(3, 1));

        JLabel msg = new JLabel(
                "<html>You uncovered a Question Cell!<br/>What do you want to do?</html>",
                SwingConstants.CENTER
        );
        dialog.add(msg);

        JButton passBtn = new JButton("Pass Turn");
        JButton answerBtn = new JButton("Answer Question");

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

        dialog.add(passBtn);
        dialog.add(answerBtn);
        dialog.setVisible(true);
    }

    // ---------------- Surprise Cell popup -------------------

    // this popup is for surprise cells, same idea but with random effect instead of a question
    private void showSurpriseChoiceDialog(int playerNum, int row, int col) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Surprise Cell");
        dialog.setModal(true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(3, 1));

        JLabel msg = new JLabel(
                "<html>You uncovered a Surprise Cell!<br/>What do you want to do?</html>",
                SwingConstants.CENTER
        );
        dialog.add(msg);

        JButton passBtn = new JButton("Pass Turn");
        JButton activateBtn = new JButton("Try Your Luck");

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

            gameManager.applyOpenCost();
            boolean positive = Math.random() < 0.5;
            if (positive) gameManager.applyPositiveEffect();
            else gameManager.applyNegativeEffect();

            showEffectMessage(positive);
            gameBoardView.updateScore(gameManager.getScore());
            gameBoardView.updateLives(gameManager.getLives());

            switchTurn();
        });

        dialog.add(passBtn);
        dialog.add(activateBtn);
        dialog.setVisible(true);
    }

    // ---------------- Question Dialog (placeholder) -------------------

    private void openQuestionDialog(int playerNum) {
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
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(5, 1));

        JLabel label = new JLabel("<html>" + q.getText() + "</html>", SwingConstants.CENTER);
        dialog.add(label);

        JButton a = new JButton("A) " + q.getOptA());
        JButton b = new JButton("B) " + q.getOptB());
        JButton c = new JButton("C) " + q.getOptC());
        JButton d = new JButton("D) " + q.getOptD());

        a.addActionListener(e -> handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("A")));
        b.addActionListener(e -> handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("B")));
        c.addActionListener(e -> handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("C")));
        d.addActionListener(e -> handleQuestionAnswer(dialog, q.getCorrectAnswer().equalsIgnoreCase("D")));

        dialog.add(a);
        dialog.add(b);
        dialog.add(c);
        dialog.add(d);
        dialog.setVisible(true);
    }


    // after answering a question we pay the open cost, apply effect and mark the Q as attempted
    private void handleQuestionAnswer(JDialog dialog, boolean isCorrect) {
        gameManager.applyOpenCost();

        boolean positive = isCorrect || Math.random() < 0.5;
        if (positive) {
            gameManager.applyPositiveEffect();
        } else {
            gameManager.applyNegativeEffect();
        }

        showEffectMessage(positive);
        gameBoardView.updateScore(gameManager.getScore());
        gameBoardView.updateLives(gameManager.getLives());

        dialog.dispose();

        // if we still remember which question this was, we paint it as attempted (darker Q, disabled)
        if (lastQuestionPlayer != -1 &&
                lastQuestionRow != -1 &&
                lastQuestionCol != -1) {
            Board currentBoard = (lastQuestionPlayer == 1) ? board1 : board2;
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
    private void showQuestionDifficultyDialog(int playerNum) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Select Question Difficulty");
        dialog.setModal(true);
        dialog.setSize(380, 260);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(5, 1));

        JLabel msg = new JLabel(
                "<html>Select a difficulty for your question:<br>"
                        + "Easy, Medium, Hard, or Advanced.</html>",
                SwingConstants.CENTER);
        dialog.add(msg);

        int remEasy = SysData.getRemainingQuestions(1);
        int totEasy = SysData.getTotalQuestions(1);
        int remMed = SysData.getRemainingQuestions(2);
        int totMed = SysData.getTotalQuestions(2);
        int remHard = SysData.getRemainingQuestions(3);
        int totHard = SysData.getTotalQuestions(3);
        int remAdv = SysData.getRemainingQuestions(4);
        int totAdv = SysData.getTotalQuestions(4);

        JButton easyBtn = new JButton("Easy  (questions left " + remEasy + "/" + totEasy + ")");
        JButton mediumBtn = new JButton("Medium  (questions left " + remMed + "/" + totMed + ")");
        JButton hardBtn = new JButton("Hard  (questions left " + remHard + "/" + totHard + ")");
        JButton advancedBtn = new JButton("Advanced  (questions left " + remAdv + "/" + totAdv + ")");

        easyBtn.setEnabled(remEasy > 0);
        mediumBtn.setEnabled(remMed > 0);
        hardBtn.setEnabled(remHard > 0);
        advancedBtn.setEnabled(remAdv > 0);

        easyBtn.addActionListener(e -> {
            currentQuestionDifficulty = 1;
            dialog.dispose();
            openQuestionDialog(playerNum);
        });
        mediumBtn.addActionListener(e -> {
            currentQuestionDifficulty = 2;
            dialog.dispose();
            openQuestionDialog(playerNum);
        });
        hardBtn.addActionListener(e -> {
            currentQuestionDifficulty = 3;
            dialog.dispose();
            openQuestionDialog(playerNum);
        });
        advancedBtn.addActionListener(e -> {
            currentQuestionDifficulty = 4;
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
