package model;

public class Cell {

    public enum CellType {
        MINE,      // M - Mine cell
        QUESTION,  // Q - Question cell
        SURPRISE,  // S - Surprise cell
        NUMBER,    // 1-8 numbered cells
        EMPTY      // Empty cell (0 neighbors)
    }

    public enum CellState {
        HIDDEN,   // Not yet revealed
        REVEALED, // Visible to player
        FLAGGED   // Marked with flag
    }

    private final int row;
    private final int col;

    private CellType type;
    private CellState state;
    private int neighborMines; // 0–8

    // question / surprise state
    private boolean readyForQuestion = false;   // later we can use this to know if question is active/ready
    private boolean readyForSurprise = false;   // true = already used surprise, so no more clicking it
    private boolean questionAttempted = false;  // used to darken Q after attempt in the UI
    private boolean surprisePassed = false;     // used to darken S after "pass turn" choice

    public boolean isReadyForQuestion() {
        return readyForQuestion;
    }

    public void setReadyForQuestion(boolean readyForQuestion) {
        this.readyForQuestion = readyForQuestion;
    }

    public boolean isReadyForSurprise() {
        return readyForSurprise;
    }

    public void setReadyForSurprise(boolean readyForSurprise) {
        this.readyForSurprise = readyForSurprise;
    }

    public boolean isQuestionAttempted() {
        return questionAttempted;
    }

    public void setQuestionAttempted(boolean questionAttempted) {
        this.questionAttempted = questionAttempted;
    }

    public boolean isSurprisePassed() {
        return surprisePassed;
    }

    public void setSurprisePassed(boolean surprisePassed) {
        this.surprisePassed = surprisePassed;
    }

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.type = CellType.EMPTY; // default, Board will override to mine / question / surprise / number
        this.state = CellState.HIDDEN; // at start all cells are hidden
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
        // here we decide if this cell should be NUMBER or EMPTY,
        // but we never override special types (mine / question / surprise)
        if (count > 0
                && this.type != CellType.MINE
                && this.type != CellType.QUESTION
                && this.type != CellType.SURPRISE) {
            this.type = CellType.NUMBER;   // has neighbors -> show the number
        } else if (this.type != CellType.MINE
                && this.type != CellType.QUESTION
                && this.type != CellType.SURPRISE) {
            this.type = CellType.EMPTY;    // no neighbors and not special -> plain empty cell
        }
    }

    public void setState(CellState newState) {
        // simple state change, UI will redraw based on this
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
     * so the board knows if it should draw M, Q, S or a number
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
     * helps when the board wants to treat them differently from normal numbers/empty
     */
    public boolean isSpecialCell() {
        return type == CellType.MINE
                || type == CellType.QUESTION
                || type == CellType.SURPRISE;
    }

    /**
     * Check if this cell is safe (not a mine)
     * used a lot when we decide if we can give points or cascade reveal
     */
    public boolean isSafe() {
        return type != CellType.MINE;
    }

    @Override
    public String toString() {
        // small debug string so we can print the cell in logs while testing
        return "Cell[" + row + "," + col + "] Type:" + type + " State:" + state;
    }
}
