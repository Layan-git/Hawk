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
        configureDifficulty(difficulty);

        cells = new Cell[rows][cols];
        initEmptyBoard();
        placeMinesRandomly();
        placeSpecialCells();
        calculateNumbers();
    }

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

    private void initEmptyBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
    }

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

    private void placeSpecialCells() {
        Random random = new Random();
        
        int questionCells = 0;
        int surpriseCells = 0;
        
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
        
        int qCount = 0;
        while (qCount < questionCells) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            if (!cells[r][c].isMine() && cells[r][c].getType() == Cell.CellType.EMPTY) {
                cells[r][c].setType(Cell.CellType.QUESTION);
                qCount++;
            }
        }
        
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
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public void reveal(int row, int col) {
        Cell cell = cells[row][col];

        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.setState(Cell.CellState.REVEALED);

        if (cell.getType() == Cell.CellType.EMPTY) {
            cascadeReveal(row, col);
        }
    }

    private void cascadeReveal(int row, int col) {
        Queue<Cell> queue = new LinkedList<>();
        queue.add(cells[row][col]);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            int r = current.getRow();
            int c = current.getCol();
            
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;

                    int nr = r + dr;
                    int nc = c + dc;

                    if (!isInside(nr, nc)) continue;

                    Cell neighbor = cells[nr][nc];

                    if (neighbor.isRevealed() 
                            || neighbor.isFlagged() 
                            || neighbor.isMine() 
                            || neighbor.isQuestion() 
                            || neighbor.isSurprise()) {
                        continue;
                    }

                    neighbor.setState(Cell.CellState.REVEALED);

                    if (neighbor.getType() == Cell.CellType.EMPTY) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    public void toggleFlag(int row, int col) {
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
    
    /**
     * Getter for the total number of initial mines.
     */
    public int getTotalMines() {
        return totalMines;
    }

    /**
     * Calculates the number of mines that are still hidden (not revealed).
     * This is used to update the "Mines Left" counter when a mine is hit.
     */
    public int getHiddenMineCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // If it is a mine and it is NOT revealed, it is still "hidden/left"
                if (cells[r][c].isMine() && !cells[r][c].isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }
}