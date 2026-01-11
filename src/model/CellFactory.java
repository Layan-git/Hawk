package model;

/**
 * Factory class for creating Cell objects.
 * Centralizes cell creation logic using the Factory design pattern.
 */
public class CellFactory {

    /**
     * Creates a cell with the specified type at the given position.
     * 
     * @param type the type of cell to create (EMPTY, MINE, QUESTION, SURPRISE, NUMBER)
     * @param row the row position
     * @param col the column position
     * @return a new Cell instance with the specified type
     */
    public static Cell createCell(Cell.CellType type, int row, int col) {
        Cell cell = new Cell(row, col);
        cell.setType(type);
        return cell;
    }
}
