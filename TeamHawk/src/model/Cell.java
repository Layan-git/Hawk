package model;

public class Cell {
    
    public enum CellType {
        MINE,        // M - Mine cell
        QUESTION,    // Q - Question cell
        SURPRISE,    // S - Surprise cell
        NUMBER,      // 1-8 numbered cells
        EMPTY        // Empty cell (0 neighbors)
    }
    
    public enum CellState {
        HIDDEN,      // Not yet revealed
        REVEALED,    // Visible to player
        FLAGGED      // Marked with flag
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
    public int getRow() { 
        return row; 
    }
    
    public int getCol() { 
        return col; 
    }
    
    public CellType getType() { 
        return type; 
    }
    
    public CellState getState() { 
        return state; 
    }
    
    public int getNeighborMines() { 
        return neighborMines; 
    }
    
    // -------------------------
    // Setters
    // -------------------------
    public void setType(CellType type) {
        this.type = type;
    }
    
    public void setNeighborMines(int count) {
        this.neighborMines = count;
        
        // Only update type if it's not already a special cell (mine, question, surprise)
        if (count > 0 
                && this.type != CellType.MINE 
                && this.type != CellType.QUESTION 
                && this.type != CellType.SURPRISE) {
            this.type = CellType.NUMBER;
        } else if (this.type != CellType.MINE 
                && this.type != CellType.QUESTION 
                && this.type != CellType.SURPRISE) {
            this.type = CellType.EMPTY;
        }
    }
    
    public void setState(CellState newState) {
        this.state = newState;
    }
    
    // -------------------------
    // Helper Methods
    // -------------------------
    public boolean isMine() { 
        return type == CellType.MINE; 
    }
    
    public boolean isQuestion() { 
        return type == CellType.QUESTION; 
    }
    
    public boolean isSurprise() { 
        return type == CellType.SURPRISE; 
    }
    
    public boolean isRevealed() { 
        return state == CellState.REVEALED; 
    }
    
    public boolean isFlagged() { 
        return state == CellState.FLAGGED; 
    }
    
    public boolean isHidden() {
        return state == CellState.HIDDEN;
    }
    
    /**
     * Get the display label for this cell type
     * Used by the view to show appropriate text on the cell
     * @return String representation: "M", "Q", "S", number, or empty
     */
    public String getDisplayLabel() {
        return switch (type) {
            case MINE -> "M";
            case QUESTION -> "Q";
            case SURPRISE -> "S";
            case NUMBER -> String.valueOf(neighborMines);
            case EMPTY -> "";
        };
    }
    
    /**
     * Check if this is a special cell (Question, Surprise, or Mine)
     * @return true if cell is Q, S, or M
     */
    public boolean isSpecialCell() {
        return type == CellType.MINE 
            || type == CellType.QUESTION 
            || type == CellType.SURPRISE;
    }
    
    /**
     * Check if this cell is safe (not a mine)
     * @return true if cell is not a mine
     */
    public boolean isSafe() {
        return type != CellType.MINE;
    }
    
    @Override
    public String toString() {
        return "Cell[" + row + "," + col + "] Type:" + type + " State:" + state;
    }
}
