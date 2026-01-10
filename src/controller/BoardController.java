package controller;

import view.GameBoardView;

public class BoardController implements IGameBoardController {
    
    private final GamePlayController gamePlayController;
    private GameBoardView gameBoardView;
    
    public BoardController(GamePlayController gamePlayController) {
        this.gamePlayController = gamePlayController;
    }
    
    public void setGameBoardView(GameBoardView gameBoardView) {
        this.gameBoardView = gameBoardView;
    }
    
    @Override
    public void onCellClick(int playerNum, int row, int col) {
        gamePlayController.handleCellClick(playerNum, row, col);
    }

    @Override
    public void onCellRightClick(int playerNum, int row, int col) {
        gamePlayController.handleCellRightClick(playerNum, row, col);
    }

    @Override
    public void pauseGame() {
        gamePlayController.pauseGame();
    }

    @Override
    public void quitToMenu() {
        gamePlayController.quitToMenu();
    }
}
