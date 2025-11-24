package controller;

import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.GameManger;
import view.MainMenu;
import view.GameSetup;
import view.GameBoardView;

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
    
    // Game state
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
            
            // Update the view
            updateBoardDisplay(playerNum, currentBoard);
            
            // Handle different cell types
            if (cell.isMine()) {
                gameBoardView.updateStatus("Hit a mine! -1 Life");
                gameBoardView.showMessage("Mine!", "You hit a MINE! Lost 1 life.");
            } else if (cell.isQuestion()) {
                gameBoardView.updateStatus("Question Cell! Answer to earn points.");
                gameBoardView.showMessage("Question!", "You found a QUESTION cell!");
            } else if (cell.isSurprise()) {
                gameBoardView.updateStatus("Surprise Cell! Special effect activated.");
                gameBoardView.showMessage("Surprise!", "You found a SURPRISE cell!");
            } else {
                gameBoardView.updateStatus("Safe cell revealed!");
            }
            
            // Switch turn
            switchTurn();
        }
        
        @Override
        public void onCellRightClick(int playerNum, int row, int col) {
            if (playerNum != currentPlayer) {
                return;
            }
            
            Board currentBoard = (playerNum == 1) ? board1 : board2;
            currentBoard.toggleFlag(row, col);
            
            Cell cell = currentBoard.getCell(row, col);
            String label = cell.getDisplayLabel();
            gameBoardView.updateCell(playerNum, row, col, cell, label);
            
            // Switch turn after flagging
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
        
        // Initialize game logic
        gameManager = new GameManger();
        gameManager.GameManager(difficulty);
        
        // Create two separate boards for each player
        board1 = new Board(difficulty);
        board2 = new Board(difficulty);
        
        int boardSize = switch (difficulty) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };
        
        // Create and show the game board view
        gameBoardView = new GameBoardView(boardController, p1, p2, boardSize);
        
        // Initialize both boards
        updateBoardDisplay(1, board1);
        updateBoardDisplay(2, board2);
        
        currentPlayer = 1;
        gameBoardView.updateCurrentTurn(p1);
        gameBoardView.updateScore(0);
        gameBoardView.updateLives(gameManager.getMaxLives());
        gameBoardView.updateStatus("Game Started!");
        
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
    }
}
