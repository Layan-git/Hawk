package model;

import java.util.*;

public class Board {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private int rows;
    private int cols;
    private int totalMines;

    private Cell[][] cells;

    public Board(Difficulty difficulty) {
        configureDifficulty(difficulty);

        cells = new Cell[rows][cols];
        initEmptyBoard();
        placeMinesRandomly();
        calculateNumbers();
    }

    // -------------------------------------------------------------
    // Difficulty configuration according to assignment (Iteration 1)
    // -------------------------------------------------------------
    private void configureDifficulty(Difficulty difficulty) {

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

    // -------------------------------------------------------------
    // Basic setup
    // -------------------------------------------------------------
    private void initEmptyBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
    }

    // -------------------------------------------------------------
    // Place mines randomly
    // -------------------------------------------------------------
    private void placeMinesRandomly() {
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

    // -------------------------------------------------------------
    // Calculate neighbor numbers (1–8)
    // -------------------------------------------------------------
    private void calculateNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (cells[r][c].isMine()) continue;

                int count = countNeighborMines(r, c);
                cells[r][c].setNeighborMines(count);
            }
        }
    }

    private int countNeighborMines(int row, int col) {
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
    // if the cords are inside the game board 
    private boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // -------------------------------------------------------------
    // Reveal cell
    // -------------------------------------------------------------
    public void reveal(int row, int col) {

        Cell cell = cells[row][col];

        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.setState(Cell.CellState.REVEALED);

        // If empty, cascade
        if (cell.getType() == Cell.CellType.EMPTY) {
            cascadeReveal(row, col);
        }
    }

    // -------------------------------------------------------------
    // Cascade reveal (classic Minesweeper flood fill)
    // -------------------------------------------------------------
    private void cascadeReveal(int row, int col) {

        Queue<Cell> queue = new LinkedList<>();
        queue.add(cells[row][col]);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            int r = current.getRow();
            int c = current.getCol();
            
            // loop through 3x3 matrix around the current cell 
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {

                    if (dr == 0 && dc == 0) continue;

                    int nr = r + dr;
                    int nc = c + dc;

                    if (!isInside(nr, nc)) continue;

                    Cell neighbor = cells[nr][nc];

                    // Skip flagged or revealed or mines
                    if (neighbor.isRevealed() || neighbor.isFlagged() || neighbor.isMine()) continue;

                    neighbor.setState(Cell.CellState.REVEALED);

                    // Continue expanding empty cells
                    if (neighbor.getType() == Cell.CellType.EMPTY) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Flagging
    // -------------------------------------------------------------
    public void toggleFlag(int row, int col) {
        Cell cell = cells[row][col];

        if (cell.isRevealed()) return;

        if (cell.isFlagged()) {
            cell.setState(Cell.CellState.HIDDEN);
        } else {
            cell.setState(Cell.CellState.FLAGGED);
        }
    }

    // -------------------------------------------------------------
    // Basic Getters
    // -------------------------------------------------------------
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getCell(int r, int c) { return cells[r][c]; }
}