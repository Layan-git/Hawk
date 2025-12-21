package model;

import model.Board;
import model.Board.Difficulty;
import model.Cell;
import model.GameManger;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyFirstTests {

    // J1 – EASY board size + mine count
    @Test
    public void easyBoardHasCorrectSizeAndMineCount() {
        Board b = new Board(Difficulty.EASY);

        assertEquals(9, b.getRows());
        assertEquals(9, b.getCols());
        assertEquals(10, b.getTotalMines());
    }

    // J2 – EASY board question + surprise counts
    @Test
    public void easyBoardHasCorrectQuestionAndSurpriseCounts() {
        Board b = new Board(Difficulty.EASY);

        int questions = 0;
        int surprises = 0;

        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                if (cell.isQuestion()) questions++;
                if (cell.isSurprise()) surprises++;
            }
        }

        assertEquals(6, questions); // EASY spec
        assertEquals(2, surprises); // EASY spec
    }

    // J3 – Cell default state
    @Test
    public void newCellIsEmptyHiddenAndSafe() {
        Cell cell = new Cell(0, 0);

        assertTrue(cell.isHidden());
        assertTrue(cell.isSafe());
        assertEquals(Cell.CellType.EMPTY, cell.getType());
        assertEquals("", cell.getDisplayLabel());
    }

    // J4 – GameManger negative effect on EASY
    @Test
    public void applyNegativeEffectOnEasyDecreasesScoreAndLives() {
        GameManger gm = new GameManger();
        gm.GameManager(Difficulty.EASY);   // init manager
        gm.addPoints(20);                  // start with 20 points
        int startLives = gm.getLives();

        gm.applyNegativeEffect();          // EASY: −8 points, −1 life

        assertEquals(12, gm.getScore());   // 20 − 8
        assertEquals(startLives - 1, gm.getLives());
    }
}
