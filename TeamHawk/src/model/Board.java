package model;

import java.util.*;

public class Board {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private Difficulty difficulty;

    private int rows;
    private int cols;
    private int totalMines;

    private Cell[][] cells;

    public Board(Difficulty difficulty) {
        this.difficulty = difficulty;
        configureDifficulty(difficulty);  // set board size + mine count by difficulty
        cells = new Cell[rows][cols];
        initEmptyBoard();                  // start with all EMPTY cells
        placeMinesRandomly();              // drop mines on random cells
        placeSpecialCells();               // add questions + surprises on empty cells
        calculateNumbers();                // set number values around mines
    }

    private void configureDifficulty(Difficulty difficulty) {
        // here we decide how big the board is and how many mines for each level
        switch (difficulty) {
            case EASY:
                rows = 9;
                cols = 9;
                totalMines = 10;
                break;
            case MEDIUM:
                rows = 13;
                cols = 13;
                totalMines = 26;
                break;
            case HARD:
                rows = 16;
                cols = 16;
                totalMines = 44;
                break;
        }
    }

    private void initEmptyBoard() {
        // create all cells as EMPTY (no mines / specials yet)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
    }

    private void placeMinesRandomly() {
        // randomly place the exact number of mines, skip cells that already have one
        Random random = new Random();
        int count = 0;
        while (count < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!cells[r][c].isMine()) {
                cells[r][c].setType(Cell.CellType.MINE);
                count++;
            }
        }
    }

    private void placeSpecialCells() {
        // place question and surprise cells on EMPTY, non-mine cells
        Random random = new Random();
        int questionCells = 0;
        int surpriseCells = 0;

        // how many specials we want for each difficulty
        switch (difficulty) {
            case EASY:
                questionCells = 6;
                surpriseCells = 2;
                break;
            case MEDIUM:
                questionCells = 7;
                surpriseCells = 3;
                break;
            case HARD:
                questionCells = 11;
                surpriseCells = 4;
                break;
        }

        // place question cells
        int qCount = 0;
        while (qCount < questionCells) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!cells[r][c].isMine() && cells[r][c].getType() == Cell.CellType.EMPTY) {
                cells[r][c].setType(Cell.CellType.QUESTION);
                qCount++;
            }
        }

        // place surprise cells
        int sCount = 0;
        while (sCount < surpriseCells) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!cells[r][c].isMine()
                    && !cells[r][c].isQuestion()
                    && cells[r][c].getType() == Cell.CellType.EMPTY) {
                cells[r][c].setType(Cell.CellType.SURPRISE);
                sCount++;
            }
        }
    }

    private void calculateNumbers() {
        // go over all non-mine / non-special cells and count how many mines around them
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine()
                        || cells[r][c].isQuestion()
                        || cells[r][c].isSurprise()) {
                    continue;
                }

                int count = countNeighborMines(r, c);
                cells[r][c].setNeighborMines(count);
            }
        }
    }

    private int countNeighborMines(int row, int col) {
        // standard 8-neighbor check around a cell
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (isInside(nr, nc) && cells[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isInside(int r, int c) {
        // helper to make sure we stay inside the board
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public void reveal(int row, int col) {
        // reveals a single cell; if it’s empty we start the flood fill
        Cell cell = cells[row][col];
        if (cell.isRevealed() || cell.isFlagged()) return;
        cell.setState(Cell.CellState.REVEALED);
        if (cell.getType() == Cell.CellType.EMPTY) {
            cascadeReveal(row, col);
        }
    }

    private void cascadeReveal(int row, int col) {
        // when we reveal an empty cell, we want to uncover all touching empty cells in every direction
        Queue<Cell> queue = new LinkedList<>();
        queue.add(cells[row][col]);
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            int r = current.getRow();
            int c = current.getCol();

            // look at all 8 neighbors around this cell (including diagonals)
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue; // skip itself, only want neighbors
                    int nr = r + dr;
                    int nc = c + dc;
                    // out of bounds? skip this neighbor
                    if (!isInside(nr, nc)) continue;

                    Cell neighbor = cells[nr][nc];

                    // skip anything that's already open or flagged (already seen or locked by the player)
                    if (neighbor.isRevealed() || neighbor.isFlagged()) continue;

                    // mines we skip, never auto-reveal
                    if (neighbor.isMine()) continue;
                    // question and surprise cells don't auto-reveal either, player must choose those
                    if (neighbor.isQuestion() || neighbor.isSurprise()) {
                        continue;
                    }

                    // reveal this cell now
                    neighbor.setState(Cell.CellState.REVEALED);

                    // only empty cells keep the wave going,
                    // numbered cells stop the spread (boundary), so we don't recurse into them
                    if (neighbor.getType() == Cell.CellType.EMPTY) {
                        queue.add(neighbor); // queue up the next round for empty cells
                    }
                }
            }
        }
    }


    public void toggleFlag(int row, int col) {
        // simple flag toggle, ui decides how to score it
        Cell cell = cells[row][col];
        if (cell.isRevealed()) return;
        if (cell.isFlagged()) {
            cell.setState(Cell.CellState.HIDDEN);
        } else {
            cell.setState(Cell.CellState.FLAGGED);
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getCell(int r, int c) { return cells[r][c]; }
    public Difficulty getDifficulty() { return difficulty; }

    public int getTotalMines() {
        return totalMines;
    }

    public int getHiddenMineCount() {
        // used to update “mines left” counter on the ui
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine() && !cells[r][c].isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * true when this board has no more hidden cells (everything is revealed or flagged)
     */
    public boolean isFinished() {
        // once no cell is HIDDEN, this board is done
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isHidden()) {
                    return false;
                }
            }
        }
        return true;
    }
}
