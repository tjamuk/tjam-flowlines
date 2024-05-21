package uk.ac.aber.dcs.cs39440.tjamflowlines.model;

import java.util.Objects;

/**
 * A class representing a position in the grid.
 */
public class Cell {
    /**
     * The column that the cell is in.
     */
    private final int col;

    /**
     * The row that the cell is in.
     */
    private final int row;

    /**
     * Constructor for a cell
     * @param col - The column that the cell is in.
     * @param row - The row that the cell is in.
     */
    public Cell(int col, int row)
    {
        this.col = col;
        this.row = row;
    }

    /**
     * @return The column of the cell.
     */
    public int getCol()
    {
        return col;
    }

    /**
     * @return The row of the cell.
     */
    public int getRow()
    {
        return row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return getCol() == cell.getCol() && getRow() == cell.getRow();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCol(), getRow());
    }

    @Override
    public String toString() {

        return "{" + col + "," + row + "}" ;
    }
}
