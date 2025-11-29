package controller;

import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.GameManger;
import view.MainMenu;
import view.GameSetup;
import view.GameBoardView;
import javax.swing.JOptionPane;

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
    
    private final GameBoardController boardController = new GameBoardController() {
        @Override
        public void onCellClick(int playerNum, int row, int col) {
            if (playerNum != currentPlayer) {
                gameBoardView.showMessage("Not Your Turn", "Wait for your turn!");
                return;
            }
            
            Board currentBoard = (playerNum == 1) ? board1 : board2;
            Cell cell = currentBoard.getCell(row, col);
            
            if (cell.isRevealed() || cell.isFlagged()) {
                return;
            }
            
            // Reveal the cell
            currentBoard.reveal(row, col);
            updateBoardDisplay(playerNum, currentBoard);
            
            if (cell.isMine()) {
                // 1. Process damage
                gameManager.processMineHit();
                
                // 2. Update view stats
                gameBoardView.updateLives(gameManager.getLives());
                gameBoardView.updateScore(gameManager.getScore());
                
                // 3. Update Mines Left Counter (Decrements because a mine was revealed)
                gameBoardView.updateMinesLeft(playerNum, currentBoard.getHiddenMineCount());
                
                // 4. CHECK FOR GAME OVER
                if (gameManager.getLives() <= 0) {
                    // Show standard message, then the Game Over popup
                    gameBoardView.updateStatus("Game Over!");
                    
                    // This popup blocks until OK is pressed
                    JOptionPane.showMessageDialog(null, 
                        "Game Over! You ran out of lives.", 
                        "Game Over", 
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Return to menu immediately after OK
                    quitToMenu();
                    return; // Stop execution here
                }
                
                gameBoardView.updateStatus("Hit a mine! -1 Life");
                gameBoardView.showMessage("Mine!", "You hit a MINE! Lost 1 life.");
                
            } else if (cell.isQuestion()) {
                gameManager.processSafeReveal();
                gameBoardView.updateScore(gameManager.getScore());
                gameBoardView.updateStatus("Question Cell! Answer to earn points.");
                gameBoardView.showMessage("Question!", "You found a QUESTION cell!");
            } else if (cell.isSurprise()) {
                gameManager.processSafeReveal();
                gameBoardView.updateScore(gameManager.getScore());
                gameBoardView.updateStatus("Surprise Cell! Special effect activated.");
                gameBoardView.showMessage("Surprise!", "You found a SURPRISE cell!");
            } else {
                gameManager.processSafeReveal();
                gameBoardView.updateScore(gameManager.getScore());
                gameBoardView.updateStatus("Safe cell revealed!");
            }
            
            // Switch turn only if game is not over
            switchTurn();
        }
        
        @Override
        public void onCellRightClick(int playerNum, int row, int col) {
            if (playerNum != currentPlayer) return;
            
            Board currentBoard = (playerNum == 1) ? board1 : board2;
            currentBoard.toggleFlag(row, col);
            
            Cell cell = currentBoard.getCell(row, col);
            String label = cell.getDisplayLabel();
            gameBoardView.updateCell(playerNum, row, col, cell, label);
            
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
    
    public Main() {
        mainMenu = new MainMenu(menuController);
        mainMenu.show();
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(Main::new);
    }
    
    private void showSetup() {
        if (gameSetup == null) {
            gameSetup = new GameSetup(setupController);
        }
        mainMenu.close();
        gameSetup.show();
    }
    
    private void startGameBoard(String p1, String p2, Difficulty difficulty) {
        if (gameSetup != null) gameSetup.close();
        
        gameManager = new GameManger();
        gameManager.GameManager(difficulty);
        
        board1 = new Board(difficulty);
        board2 = new Board(difficulty);
        
        int boardSize = switch (difficulty) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };
        
        gameBoardView = new GameBoardView(boardController, p1, p2, boardSize);
        
        updateBoardDisplay(1, board1);
        updateBoardDisplay(2, board2);
        
        // Initialize Mine Counts
        gameBoardView.updateMinesLeft(1, board1.getTotalMines());
        gameBoardView.updateMinesLeft(2, board2.getTotalMines());
        
        currentPlayer = 1;
        gameBoardView.updateCurrentTurn(p1);
        gameBoardView.updateScore(0);
        gameBoardView.updateLives(gameManager.getMaxLives());
        gameBoardView.updateStatus("Game Started!");
        gameBoardView.updateTurnVisuals(currentPlayer);
        
        gameBoardView.show();
    }
    
    private void updateBoardDisplay(int playerNum, Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                String label = cell.getDisplayLabel();
                gameBoardView.updateCell(playerNum, r, c, cell, label);
            }
        }
    }
    
    private void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        String currentPlayerName = (currentPlayer == 1) ? player1Name : player2Name;
        gameBoardView.updateCurrentTurn(currentPlayerName);
        gameBoardView.updateTurnVisuals(currentPlayer);
    }
}