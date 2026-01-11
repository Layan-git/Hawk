package controller;

import model.Board;

/**
 * Interface for game setup controller operations
 */
public interface IGameSetupController {
    void backToMenu();
    void confirmStart(String player1, String player2, Board.Difficulty difficulty, int player1CharIndex, int player2CharIndex);
}
