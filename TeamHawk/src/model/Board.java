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
        placeSpecialCells();  // NEW: Place Question and Surprise cells
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
    // NEW: Place Question and Surprise cells randomly
    // Based on project specifications for each difficulty level
    // -------------------------------------------------------------
    private void placeSpecialCells() {
        Random random = new Random();
        
        // Determine number of special cells based on difficulty
        int questionCells = 0;
        int surpriseCells = 0;
        
        switch (difficulty) {
            case EASY:
                questionCells = 6;   // Easy: 6 question cells
                surpriseCells = 2;   // Easy: 2 surprise cells
                break;
            case MEDIUM:
                questionCells = 7;   // Medium: 7 question cells
                surpriseCells = 3;   // Medium: 3 surprise cells
                break;
            case HARD:
                questionCells = 11;  // Hard: 11 question cells
                surpriseCells = 4;   // Hard: 4 surprise cells
                break;
        }
        
        // Place Question cells (Q)
        int qCount = 0;
        while (qCount < questionCells) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            // Only place on cells that are not mines
            if (!cells[r][c].isMine() && cells[r][c].getType() == Cell.CellType.EMPTY) {
                cells[r][c].setType(Cell.CellType.QUESTION);
                qCount++;
            }
        }
        
        // Place Surprise cells (S)
        int sCount = 0;
        while (sCount < surpriseCells) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            // Only place on cells that are not mines or questions
            if (!cells[r][c].isMine() 
                    && !cells[r][c].isQuestion() 
                    && cells[r][c].getType() == Cell.CellType.EMPTY) {
                cells[r][c].setType(Cell.CellType.SURPRISE);
                sCount++;
            }
        }
    }

    // -------------------------------------------------------------
    // Calculate neighbor numbers (1–8)
    // Updated to skip Question and Surprise cells
    // -------------------------------------------------------------
    private void calculateNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Skip mines, questions, and surprises
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

    // if the cords are inside the game board 
    private boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // -------------------------------------------------------------
    // Reveal cell
    // Updated to handle Question and Surprise cells
    // -------------------------------------------------------------
    public void reveal(int row, int col) {
        Cell cell = cells[row][col];

        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.setState(Cell.CellState.REVEALED);

        // If empty (not mine, question, surprise, or number), cascade
        if (cell.getType() == Cell.CellType.EMPTY) {
            cascadeReveal(row, col);
        }
        
        // Question and Surprise cells don't cascade - they just reveal
    }

    // -------------------------------------------------------------
    // Cascade reveal (classic Minesweeper flood fill)
    // Updated to stop at Question and Surprise cells
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

                    // Skip flagged, revealed, mines, questions, and surprises
                    if (neighbor.isRevealed() 
                            || neighbor.isFlagged() 
                            || neighbor.isMine() 
                            || neighbor.isQuestion() 
                            || neighbor.isSurprise()) {
                        continue;
                    }

                    neighbor.setState(Cell.CellState.REVEALED);

                    // Continue expanding empty cells only
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
}
