package controller;

import model.AudioManager;
import model.Board.Difficulty;
import view.GameBoardView;
import view.GameSetup;
import view.MainMenu;

/**
 * GameFlowController manages the high-level flow of the application:
 * - Showing/hiding views
 * - Transitioning between setup and gameplay
 * - Managing user sessions
 */
public class GameFlowController {
    
    private MainMenu mainMenu;
    private GameSetup gameSetup;
    private GameBoardView gameBoardView;
    private GamePlayController gamePlayController;
    private MenuController menuController;
    private SetupController setupController;
    private BoardController boardController;
    
    private String currentUser;
    private boolean isAdmin;
    
    public GameFlowController(String currentUser, boolean isAdmin) {
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;
        initializeControllers();
        startBackgroundMusic();
    }
    
    private void initializeControllers() {
        // Create the main controllers
        this.gamePlayController = new GamePlayController(this);
        this.menuController = new MenuController(this, currentUser, isAdmin);
        this.setupController = new SetupController(this);
        this.boardController = new BoardController(gamePlayController);
        
        // Initialize main menu
        this.mainMenu = new MainMenu(menuController);
        menuController.setMainMenu(mainMenu);
    }
    
    /**
     * Start background music
     */
    private void startBackgroundMusic() {
        AudioManager.getInstance().playBackgroundMusic("menu_music.wav");
    }
    
    public void showMainMenu() {
        if (mainMenu != null) {
            mainMenu.show();
        }
    }
    
    public void showSetup(MainMenu fromMenu) {
        if (fromMenu != null) fromMenu.close();
        gameSetup = new GameSetup(setupController);
        setupController.setGameSetup(gameSetup);
        gameSetup.show();
    }
    
    public void startGameBoard(String p1, String p2, Difficulty difficulty, int p1CharIndex, int p2CharIndex) {
        if (gameSetup != null) gameSetup.close();
        
        // Initialize gameplay
        gamePlayController.initializeGame(p1, p2, difficulty, p1CharIndex, p2CharIndex);
        
        // Get the game board view from gameplay controller
        gameBoardView = gamePlayController.getGameBoardView();
        boardController.setGameBoardView(gameBoardView);
        gameBoardView.show();
    }
    
    public void returnToMainMenu() {
        if (gameBoardView != null) gameBoardView.close();
        if (gameSetup != null) gameSetup.close();
        // restart music - was silent after game, now plays again
        startBackgroundMusic();
        showMainMenu();
    }
    
    public MenuController getMenuController() {
        return menuController;
    }
    
    public SetupController getSetupController() {
        return setupController;
    }
    
    public BoardController getBoardController() {
        return boardController;
    }
    
    public GamePlayController getGamePlayController() {
        return gamePlayController;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setUserInfo(String user, boolean admin) {
        this.currentUser = user;
        this.isAdmin = admin;
        menuController.setUserInfo(user, admin);
    }
}
