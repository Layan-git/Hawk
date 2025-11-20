package model;

public class Cell {

    public enum CellType {
        MINE,
        NUMBER,
        EMPTY
    }

    public enum CellState {
        HIDDEN,
        REVEALED,
        FLAGGED
    }

    private final int row;
    private final int col;

    private CellType type;
    private CellState state;

    private int neighborMines; // 0–8

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.type = CellType.EMPTY; // default, Board will override
        this.state = CellState.HIDDEN;
        this.neighborMines = 0;
    }

    // -------------------------
    // Getters
    // -------------------------
    public int getRow() { return row; }
    public int getCol() { return col; }
    public CellType getType() { return type; }
    public CellState getState() { return state; }
    public int getNeighborMines() { return neighborMines; }

    // -------------------------
    // Setters (Board only)
    // -------------------------
    void setType(CellType type) {
        this.type = type;
    }

    void setNeighborMines(int count) {
        this.neighborMines = count;
        if (count > 0) {
            this.type = CellType.NUMBER;
        } else if (this.type != CellType.MINE) {
            this.type = CellType.EMPTY;
        }
    }

    public void setState(CellState newState) {
        this.state = newState;
    }

    // -------------------------
    // Helpers
    // -------------------------
    public boolean isMine() {
        return type == CellType.MINE;
    }

    public boolean isRevealed() {
        return state == CellState.REVEALED;
    }

    public boolean isFlagged() {
        return state == CellState.FLAGGED;
    }
}