package model;

import java.util.*;

public class Board {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private final Difficulty difficulty;
    private int rows;
    private int cols;
    private int totalMines;
    private final Cell[][] cells;

    public Board(Difficulty difficulty) {
        this.difficulty = difficulty;
        configureDifficulty(difficulty); // set board size + mine count by difficulty
        cells = new Cell[rows][cols];

        initEmptyBoard();      // start with all EMPTY cells
        placeMinesRandomly();  // drop mines on random cells
        placeSpecialCells();   // add questions + surprises on empty cells (respect 3x3 no-mine rule)
        calculateNumbers();    // set number values around mines
    }

    private void configureDifficulty(Difficulty difficulty) {
        // here we decide how big the board is and how many mines for each level
        switch (difficulty) {
            case EASY -> {
                rows = 9;
                cols = 9;
                totalMines = 10;
            }
            case MEDIUM -> {
                rows = 13;
                cols = 13;
                totalMines = 26;
            }
            case HARD -> {
                rows = 16;
                cols = 16;
                totalMines = 44;
            }
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

    /**
     * Helper: can we place a special cell (Q/S) at (row, col)?
     * Rules:
     * - Cell must currently be EMPTY.
     * - The entire 3x3 area centered on (row, col) must contain NO mines.
     * - Q and S are allowed next to each other.
     */
    private boolean canPlaceSpecialAt(int row, int col) {
        // must be exactly EMPTY (no number, no special, no mine)
        if (cells[row][col].getType() != Cell.CellType.EMPTY) {
            return false;
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                if (!isInside(nr, nc)) {
                    continue;
                }
                if (cells[nr][nc].isMine()) {
                    return false; // disallow any mine in the 3x3 area
                }
            }
        }
        return true;
    }

    private void placeSpecialCells() {
        // place question and surprise cells on EMPTY cells that have no mines in their 3x3 area
        Random random = new Random();
        int questionCells = 0;
        int surpriseCells = 0;

        // how many specials we want for each difficulty
        switch (difficulty) {
            case EASY -> {
                questionCells = 6;
                surpriseCells = 2;
            }
            case MEDIUM -> {
                questionCells = 7;
                surpriseCells = 3;
            }
            case HARD -> {
                questionCells = 11;
                surpriseCells = 4;
            }
        }

        // safety cap to avoid infinite loops on very constrained boards
        int maxAttempts = rows * cols * 10;

        // place question cells
        int qCount = 0;
        int attempts = 0;
        while (qCount < questionCells && attempts < maxAttempts) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            attempts++;

            if (canPlaceSpecialAt(r, c)) {
                cells[r][c].setType(Cell.CellType.QUESTION);
                qCount++;
            }
        }

        // place surprise cells
        int sCount = 0;
        attempts = 0;
        while (sCount < surpriseCells && attempts < maxAttempts) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            attempts++;

            // Q/S are allowed next to each other; only the 3x3 no-mine rule matters.
            if (canPlaceSpecialAt(r, c)) {
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
        // reveals a single cell; if it’s empty or a special (Q/S) we start the flood fill
        Cell cell = cells[row][col];
        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.setState(Cell.CellState.REVEALED);

        // treat QUESTION and SURPRISE as “empty-like” for flood-fill purposes
        if (cell.getType() == Cell.CellType.EMPTY
                || cell.isQuestion()
                || cell.isSurprise()) {
            cascadeReveal(row, col);
        }
    }

    private void cascadeReveal(int row, int col) {
        // flood fill starting from (row, col)
        Queue<Cell> queue = new LinkedList<>();
        queue.add(cells[row][col]);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            int r = current.getRow();
            int c = current.getCol();

            // look at all 8 neighbors around this cell (including diagonals)
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue; // skip itself

                    int nr = r + dr;
                    int nc = c + dc;

                    // out of bounds? skip this neighbor
                    if (!isInside(nr, nc)) continue;

                    Cell neighbor = cells[nr][nc];

                    // skip anything that's already open or flagged
                    if (neighbor.isRevealed() || neighbor.isFlagged()) continue;

                    // mines we skip, never auto-reveal
                    if (neighbor.isMine()) continue;

                    // reveal this cell now (EMPTY, NUMBER, QUESTION, SURPRISE are all safe)
                    neighbor.setState(Cell.CellState.REVEALED);

                    // decide which types keep the wave going:
                    //    EMPTY, QUESTION, SURPRISE all act like empty for flood-fill
                    if (neighbor.getType() == Cell.CellType.EMPTY
                            || neighbor.isQuestion()
                            || neighbor.isSurprise()) {
                        queue.add(neighbor);
                    }
                    // NUMBER cells are revealed but do not propagate
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

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell getCell(int r, int c) {
        return cells[r][c];
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

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
