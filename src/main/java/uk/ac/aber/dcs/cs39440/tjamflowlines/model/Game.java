package uk.ac.aber.dcs.cs39440.tjamflowlines.model;

import java.util.*;

public class Game {

    /**
     * For the primitive int data type there is no null value, this is the assigned value to represent it.
     */
    public final static int NULL_INT_VALUE = -1;

    /**
     * Iterated over to find every potential neighbour of a node.
     * e.g. node = (5,5) ---> (5,5) + (-1,0) = (4,5) -> (5,5) + (1,0) = (6,5) -> ...
     */
    public static final Cell[] ADDENDS_TO_FIND_NEIGHBOURS = {
            new Cell(1, 0), //right
            new Cell(-1, 0), //left
            new Cell(0, 1), //down
            new Cell(0, -1) //up
    };

    /**
     * An element's index = colour index.
     * An element's value = IDs of colour's start goal node
     * So the {start, end} goal pair of colour 0 would be equal to {startGoals[0], endGoals[0]}
     */
    public static int[] startGoals;

    /**
     * An element's index = colour index.
     * An element's value = IDs of colour's end goal node
     * So the {start, end} goal pair of colour 0 would be equal to {startGoals[0], endGoals[0]}
     */
    public static int[] endGoals;

    /**
     * An array where each element represents the colour held by a node.
     */
    public static int[] colours;

    /**
     * The number of columns.
     */
    public static int width;

    /**
     * The number of rows.
     */
    public static int height;

    /**
     * The total number of nodes (width*height)
     */
    public static int size;

    /**
     * Each element index = id of node
     * Each element value = the cell in the grid that the node represents.
     * So for {x,y} = idToCell[6]. {x,y} is the position (cell) of the node (with id 6) in the grid
     */
    public static Cell[] idToCell;

    /**
     * The value held at cellToId[x][y] is the id of the equivalent node (which occupies {x,y} on the grid)
     */
    public static int[][] cellToId;

    //

    /**
     * An old variable that holds the edges in the graph via adjacency list representation.
     * Each element (in ArrayList) index = node id.
     * Each element value = the ids of nodes connected to that node.
     * @deprecated
     */
    public static ArrayList<Set<Integer>> edges;

    /**
     * @deprecated
     * Finds the neighbouring nodes of every node.
     */
    public void addEdges() {

        Game.edges = new ArrayList<>(size);
        Cell cell;
        int neighbourCol;
        int neighbourRow;

        for (int id = 0; id < size; id++)
        {
            Game.edges.add(new HashSet<>());
            cell = Game.idToCell[id];
            for (Cell addendPair : ADDENDS_TO_FIND_NEIGHBOURS)
            {
                neighbourCol = cell.getCol() + addendPair.getCol();
                neighbourRow = cell.getRow() + addendPair.getRow();

                if (isNodeInGrid(neighbourCol, neighbourRow))
                {
                    Game.edges.get(id).add(Game.cellToId[neighbourCol][neighbourRow]); //if valid neighbour, add to hashset.
                }
            }
        }
    }

    /**
     * For a given node, finds and returns a collection of all of its neighbouring nodes.
     * @param id - the id of the given node.
     * @return node's neighbours
     */
    public static ArrayList<Integer> getEdges(int id)
    {
        ArrayList<Integer> neighbours = new ArrayList<>();
        int neighbourCol;
        int neighbourRow;
        Cell cell = idToCell[id];

        for (Cell addendPair : ADDENDS_TO_FIND_NEIGHBOURS)
        {
            neighbourCol = cell.getCol() + addendPair.getCol();
            neighbourRow = cell.getRow() + addendPair.getRow();

            if (isNodeInGrid(neighbourCol, neighbourRow))
            {
                neighbours.add(Game.cellToId[neighbourCol][neighbourRow]); //if valid neighbour, add to hashset.
            }
        }
        return neighbours;
    }

    /**
     * Constructor.
     * @param width the number of columns in the grid.
     * @param height the number of rows in the grid.
     * @param isShuffling - if idToCell will be shuffled (happens for puzzle generation)
     */
    public Game(int width, int height, boolean isShuffling)
    {
        Game.width = width;
        Game.height = height;
        Game.size = width*height;

        Game.idToCell = new Cell[Game.size];
        Game.cellToId = new int[width][height];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Game.idToCell[row*width+col] = new Cell(col, row);
            }
        }

        if (isShuffling)
        {
            Collections.shuffle(Arrays.asList(Game.idToCell));
        }

        Cell cell;
        for (int id = 0; id < Game.size; id++) {
            cell = Game.idToCell[id];
            Game.cellToId[cell.getCol()][cell.getRow()] = id;
        }
    }

    public Game() {}

    /**
     * Given 2 adjacent cells, finds the next cell going in the same direction.
     * @param first - the front of a path
     * @param second - the 2nd front (behind first) of the path
     * @return the straight on node.
     */
    public int getStraightOnNode(Cell first, Cell second)
    {
        int col = 2*first.getCol() - second.getCol();
        int row = 2*first.getRow() - second.getRow();

        return (isNodeInGrid(col, row))? Game.cellToId[col][row] : PuzzleGenerator.NULL_INT_VALUE;
    }

    /**
     * Checks whether a given cell is in the grid.
     * @param col - column of the node in the grid
     * @param row - row of the node in the grid
     * @return true = node is in the grid.
     */
    public static boolean isNodeInGrid(int col, int row) {
        return (
                col >= 0 &&
                        col < width &&
                        row >= 0 &&
                        row < height
        );
    }
}
