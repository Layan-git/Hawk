package controller;

/**
 * Interface for game board controller operations
 */
public interface IGameBoardController {
    void onCellClick(int playerNum, int row, int col);
    void onCellRightClick(int playerNum, int row, int col);
    void pauseGame();
    void quitToMenu();
}
