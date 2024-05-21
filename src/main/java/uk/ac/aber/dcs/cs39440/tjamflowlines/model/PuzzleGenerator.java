package uk.ac.aber.dcs.cs39440.tjamflowlines.model;

import java.util.*;

/**
 * The class for generating puzzles.
 */
public class PuzzleGenerator extends Game
{
    /**
     * The probability that a path will turn.
     */
    public final static double TURN_PROBABILITY = 0.1;

    public final static Random RANDOM_GENERATOR = new Random();

    /**
     * A set representing all unvisited nodes in the graph.
     */
    public Set<Integer> unvisited;

    /**
     * Represents all the paths.
     */
    public Stack<Path> paths;

    /**
     * Simply a debug value to check which iteration of model.PuzzleGenerator the code is in.
     */
    public int counter;

    /**
     * Used for finding connected components where each element represents whether a node has been visited by the dfs.
     */
    public int[] visited;

    /**
     * An ArrayList where each Set<Integer> is a connected component and each Integer of that set is a node.
     */
    public ArrayList<Set<Integer>> components;

    /**
     * A variable for when checking for squares created by paths in the grid. If true then it has found a gap in the square.
     */
    public boolean foundGap;

    /**
     * For every addend (direction), it's either a change horizontally or vertically.
     * Left, right = horizontal.
     * Up, down = vertical.
     * So this holds for each addend (direction), the indexes of the addends of the other addends that are a different change.
     * For example left (index 1) is horizontal, so holds the vertical directions: down (index 2) and up (index 3)
     */
    public final static int[][] addendToAddends = {
            {2,3}, //right -> down, up
            {2,3}, //left -> down, up
            {0,1}, //down -> right, left
            {0,1} //up -> right, left
    };

    /**
     * The number of directions a path can take (left, right, up, down)
     */
    public final static int DIRECTION_COUNT = 4;

    /**
     * When trying to find squares created by an L shape containing coloured nodes or boundaries of the grid.
     * cells[0] = end of L shape.
     * cells[1] = corner of L shape
     * //cells[2] = other end of L shape
     */
    public static Cell[] cells;

    /**
     * The constructor for model.PuzzleGenerator.
     * @param width - the number of columns.
     * @param height - the number of rows.
     */
    public PuzzleGenerator(int width, int height)
    {
        super(width, height, true);

        colours = new int[Game.size];
        restart();
    }

    /**
     * If the number of possible paths can be represented as a decision tree, it would be exponentially large.
     * So if a mistake was made early in the decision tree, all descendants would have to be explored before backtracking back to there. As there can be an exponential number of descendants, it can take an exponential amount of time.
     * But if you realise this kind of mistake has been made, the algorithm is much better off restarting from the beginning. This function basically does this by resetting all relevant variables.
     */
    public void restart()
    {
        Path.count = 0;

        Arrays.fill(colours, NULL_INT_VALUE);

        counter = 0;

        paths = new Stack<>();
        unvisited = new HashSet<>(Game.size);

        for (int id = 0; id < Game.size; id++)
        {
            unvisited.add(id);
        }

        cells = new Cell[3];
    }

    /**
     * A recursive backtracking algorithm that builds a random solvable puzzle path by path.
     * @return true = was able to generate a puzzle; false = was not able.
     */
    public boolean generatePuzzle()
    {
        counter++;
        Path path;
        int second;
        int first;

        if (!unvisited.isEmpty()) //if there are some uncoloured nodes...
        {
            if (paths.isEmpty())
            {
                paths.push(new Path()); //if there are no paths, need to create one.
            }
            path = paths.peek();
            if (path.isFull() || path.isAtDeadend) //basically if the most recent path is finished, create a new path.
            {
                paths.push(new Path());
                path = paths.peek();
            }
            if (path.isEmpty()) //if path is empty, try initialising the path with all unvisited nodes.
            {
                //need to calculate components

                if (path.id > Game.size) //look at javadoc for restart() to see why this is here. So this condition tests for if that kind of mistake has been made.
                {
                    do {
                        restart();
                    } while (!generatePuzzle()); //basically keep restarting and trying until a solution is found.
                    return true;
                }

                calculateConnectedComponents(0);
                for (int node : components.get(getSmallestComponent()).parallelStream().toList()) //start on the smallest component (errors most likely to be made in the smallest component)
                {
                    addNode(node, path.id, false); //isTurn meaningless here as
                    if (checkForSquares(idToCell[node]) && calculateConnectedComponents(node))
                    {
                        if (generatePuzzle())
                        {
                            return true; //solution has been found
                        }
                    }
                    removeNode(node); //for the node that was added, no solution can be found from this point so remove it (and try another)
                }
                paths.pop(); //no solution can be found, one of the last paths has made it impossible for a solution to be found so need to backtrack.
                return false;
            }
            else //path not empty.
            {
                first = path.peek(); //the front of the path.
                second = path.peekSecond(); //2nd front of the path (behind first)
                ArrayList<Integer> neighbours = new ArrayList<>(); //an ordered sequence of neighbours to visit

                int straightOnNode = getValidNeighbours(neighbours, first, second); //gets the neighbours to visit and returns the straight on node (if path keeps going in same direction, node = next in path)

                for (int neighbour : neighbours)
                {
                    addNode(neighbour, path.id, straightOnNode!=neighbour); // add neighbour to path.

                    if (path.areDirectionsValid() && checkForSquares(idToCell[neighbour]) && calculateConnectedComponents(neighbour)) //path validity checks.
                    {
                        if (generatePuzzle())
                        {
                            return true; //solution has been found.
                        }
                        removeNode(neighbour); //for the node that was added, no solution can be found from this point so remove it (and try another)
                        path.isAtDeadend = false;
                    }
                    else
                    {
                        removeNode(neighbour); //node added makes it invalid.
                    }
                }

                //no more neighbours to add (at a deadend) so...
                if (path.isTooSmall()) //if path is too small, backtrack.
                {
                    return false;
                }
                else //if the path is of sufficient size, will call generatePuzzle again where it will create a new path.
                {
                    path.isAtDeadend = true;
                    return generatePuzzle();
                }
            }
        }
        else //when there are no cells left to fill, check if last path added is of sufficient size.
        {
            return isLastPathSufficientlyLarge();
        }
    }

    /**
     * Called when the grid has been completely filled and need to check if last added path is of sufficient size.
     * @return true (solution has been found, last path of sufficient length). | false (it's not a solution, last path is too small).
     */
    public boolean isLastPathSufficientlyLarge()
    {
        if (!paths.isEmpty())
        {
            return !paths.peek().isTooSmall();
        }
        else
        {
            return false;
        }
    }

    /**
     * In order, gets the valid neighbours of the front of the most recent path.
     * @param visitOrder - the order in which to visit the valid neighbours.
     * @param first - front of the most recent path.
     * @param second - 2nd to the front of the most recent path/
     * @return the next node if the path keeps going in the same direction.
     */
    public int getValidNeighbours(ArrayList<Integer> visitOrder, int first, int second)
    {
        Set<Integer> neighbours = new HashSet<>(Game.getEdges(first));

        if (second != Path.NULL_VALUE)
        {
            neighbours.remove(second); //if there is a second, it will be a neighbour to the first so remove (as it's already been visited)
        }

        boolean hasFoundRedundantNode;

        for (int neighbour : neighbours)
        {
            if (unvisited.contains(neighbour))
            {
                hasFoundRedundantNode = false;

                //finds redundant nodes where for each neighbour, need to check if it could've been visited earlier in the path.
                for (int nn : Game.getEdges(neighbour)) //Game.edges.get(neighbour).parallelStream().toList()
                {
                    if (nn != first && paths.peek().contains(nn))
                    {
                        hasFoundRedundantNode = true;
                        break;
                    }
                }
                if (!hasFoundRedundantNode)
                {
                    visitOrder.add(neighbour);
                }
            }
        }

        return reorderVisitOrder(visitOrder, first, second);
    }

    /**
     * Reorders the visit order of nodes.
     *
     * Decides if the path will be going straight or not and then reorders it accordingly.
     * @param visitOrder - the order in which to visit the valid neighbours.
     * @param first - front of the most recent path.
     * @param second - 2nd to the front of the most recent path.
     */
    public int reorderVisitOrder(ArrayList<Integer> visitOrder, int first, int second)
    {
        if (first != Path.NULL_VALUE && second != Path.NULL_VALUE)
        {
            boolean isGoingStraight = isGoingStraight();
            int straightOnNode = getStraightOnNode(Game.idToCell[first], Game.idToCell[second]);
            int lastIndex = visitOrder.size()-1;

            if (lastIndex > -1 && straightOnNode != NULL_INT_VALUE)
            {
                if (visitOrder.get(0) == straightOnNode)
                {
                    if (!isGoingStraight)
                    {
                        if (lastIndex > 0) //in case it's just the straightOnNode in the neighbours.
                        {
                            visitOrder.set(0, visitOrder.get(lastIndex));
                            visitOrder.set(lastIndex, straightOnNode);
                        }
                    }
                    //else do nothing
                }
                else
                {
                    if (isGoingStraight)
                    {
                        //swap to front
                        for (int index = 1; index < lastIndex; index++)
                        {
                            if (visitOrder.get(index) == straightOnNode)
                            {
                                visitOrder.set(index, visitOrder.get(0));
                                visitOrder.set(0, straightOnNode);
                            }
                        }
                    }
                    //else do nothing
                }
            }
            return straightOnNode;
        }
        return NULL_INT_VALUE;
    }

    /**
     * Removes a node.
     * @param node = the node to be removed.
     */
    public void removeNode(int node)
    {
        paths.peek().remove();
        unvisited.add(node);
        colours[node] = NULL_INT_VALUE;
    }

    /**
     * Add a node.
     * @param node = the node to be added.
     */
    public void addNode(int node, int pathId, boolean isTurn)
    {
        paths.peek().add(node, isTurn);
        unvisited.remove(node);
        colours[node] = pathId;
    }

    /**
     * Decides randomly if the path is going straight or not.
     * @return true = path going straight.
     */
    public boolean isGoingStraight()
    {
        return RANDOM_GENERATOR.nextDouble() > TURN_PROBABILITY;
    }

    /**
     * Finds the index of the smallest component.
     * @return the index of the smallest component.
     */
    public int getSmallestComponent()
    {
        if (components.isEmpty())
        {
            return 0;
        }
        else
        {
            int smallestIndex = 0;
            int smallestSize = components.get(0).size();

            for (int componentIndex = 1; componentIndex < components.size(); componentIndex++)
            {
                if (components.get(componentIndex).size() < smallestSize)
                {
                    smallestIndex = componentIndex;
                    smallestSize = components.get(componentIndex).size();
                }
            }

            return smallestIndex;
        }
    }

    /**
     * Finds the connected components.
     * @param latestNode - the most recently added node.
     * @return true = All components are of sufficient size OR they're adjacent to the most recent node.
     */
    public boolean calculateConnectedComponents(int latestNode)
    {
        components = new ArrayList<>();
        visited = new int[Game.size];

//        if (paths.isEmpty())
//        {
//            components.add(new HashSet<>());
//            for (int node = 0; node < Game.size; node++)
//            {
//                components.get(0).add(node);
//            }
//            Arrays.fill(visited, 0);
//        }
//        else
//        {
            Set<Integer> latestNodeNeighbours = new HashSet<>(Game.getEdges(latestNode));
            int lastComponentIndex = 0;
            boolean isGood;

            Arrays.fill(visited, NULL_INT_VALUE);

            for (int vertex = 0; vertex < size; vertex++)
            {
                if (visited[vertex] == NULL_INT_VALUE && colours[vertex] == NULL_INT_VALUE)
                {
                    isGood = false;
                    components.add(new HashSet<>());
                    dfs(vertex, lastComponentIndex);

                    if (components.get(lastComponentIndex).size() < 3) //size
                    {
                        //todo bruh just
                        for (int node : components.get(lastComponentIndex)) //if component too small, check if its adjacent to the latest node.
                        {
                            if (latestNodeNeighbours.contains(node))
                            {
                                isGood = true;
                                break;
                            }
                        }
                        if (!isGood) {return false;}
                    }
                    lastComponentIndex++;
                }
            }
//        }
        return true;
    }

    /**
     * Depth first search for finding connected components.
     * @param vertexId = the node.
     * @param componentIndex = the component index.
     */
    public void dfs(int vertexId, int componentIndex)
    {
        visited[vertexId] = componentIndex;
        components.get(componentIndex).add(vertexId);

        for (int neighbour : Game.getEdges(vertexId))
        {
            if (colours[neighbour] == NULL_INT_VALUE)
            {
                if (visited[neighbour] == NULL_INT_VALUE)
                {
                    dfs(neighbour, componentIndex);
                }
            }
        }
    }

    // ________ Here is a crude example  of a square. # = path.
    // |  #   |
    // |  #   |
    // | ##   |
    // |      |
    // --------
    /**
     * When adding onto a path, it's possible to make a square shape of free cells.
     * If a square shape is made then it is likely to be impossible to find a solution from this point.
     * This was much more relevant to when the minimum path length was 4 (now it's 3), but it's been kept as it took 1-2 weeks of work.
     * ----
     * @param newNode - the cell that was added onto a path.
     * @return true (valid, no squares found). | false (invalid, square found)
     */
    public boolean checkForSquares(Cell newNode)
    {
        cells[0] = newNode;

        //for a square to be made, there must be some sort of L shape formed by paths or/and boundary of the grid.
        //cells[0] = end of L shape
        //cells[1] = corner of L shape.
        //cells[2] = before corner of L shape.
        //Each element of the L shape must either be a neighbour that's not in the grid (showing that it's a boundary) or a coloured neighbour

        //the following loop tries to find different combinations of L shapes (where new node is the end of it).
        //When an L shape is found, it's tested to see if an L shape is made out of it.
        for (int direction = 0; direction < DIRECTION_COUNT; direction++) //for every direction
        {
            cells[1] = addCells(ADDENDS_TO_FIND_NEIGHBOURS[direction], newNode); //trying to find different corners of the L shape.

            if (!isNodeInGrid(cells[1].getCol(), cells[1].getRow()) || colours[cellToId[cells[1].getCol()][cells[1].getRow()]] != NULL_INT_VALUE) //if neighbour is NOT in grid OR is coloured
            {
                //so currently have found this ## (end of L shape -> corner of L shape)
                //                          #
                //need to find this: ## or ##
                //                    #
                for (int addend : addendToAddends[direction])
                {
                    cells[2] = addCells(ADDENDS_TO_FIND_NEIGHBOURS[addend], cells[1]); //gets the adjacent corner to the corner.
                    if (!isNodeInGrid(cells[2].getCol(), cells[2].getRow()) || colours[cellToId[cells[2].getCol()][cells[2].getRow()]] != NULL_INT_VALUE) //if neighbour is NOT in grid OR is coloured
                    {
                        if (!checkIfMakesSquare()) //if a square is found, return false (invalid)
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return true; //no squares found, return true (valid)
    }

    /**
     * Called from checkForSquares() when an L shape has been found, this function sees if the L shape makes a square.
     * @return true (no squares found). | false (square found)
     */
    public boolean checkIfMakesSquare()
    {
        //# <--if have found an L shape, need to calculate the closest corner (of the possible square formed by it) to it
        //##
        Cell closestCorner = addCells(  subtractCells(cells[0], cells[1])  ,  cells[2]);

        if (!Game.isNodeInGrid(closestCorner.getCol(), closestCorner.getRow()) || colours[cellToId[closestCorner.getCol()][closestCorner.getRow()]] != NULL_INT_VALUE)
        {
            return true; //the closest corner needs to be a node in the grid AND uncoloured. If it's not either, then a square has not been formed.
        }

        Set<Integer> corners = new HashSet<>(4); //the 4 corners of the square of free cells.
        corners.add(cellToId[closestCorner.getCol()][closestCorner.getRow()]);
        int x; int y;

        //this loop calculates the other corners of the square.
        //for example, corner of L shape --> closest corner has a direction. closest corner + direction = opposite corner of square. Because the direction will be a diagonal one.
        for (Cell cell : cells)
        {
            x = 2 * closestCorner.getCol() - cell.getCol();
            y = 2 * closestCorner.getRow() - cell.getRow();

            if (!Game.isNodeInGrid(x,y) || colours[cellToId[x][y]] != NULL_INT_VALUE)
            {
                return true; //if the corner is coloured or not a node in the grid, square has not been formed.
            }

            corners.add(cellToId[x][y]);
        }

        foundGap = false;

        //when you have L shape and a square of free nodes.
        //Need to check for other L shape
        //The square of free nodes doesn't have to be fully enclosed.
        //It can have a maximum of 1 gap in the edges of the square WHICH connects to other free cells (not in the square)
        for (int corner : corners)
        {
            if (checkCorner(corner, corners)) //returns whether this corner is adjacent to a free cell which is adjacent to other free cells.
            {
                if (foundGap) //there is more than 1 gap in the edges of the square WHICH connects to other cells --> it's NOT a square, so return true.
                {
                    return true;
                }
                else
                {
                    foundGap = true;
                }
            }
        }
        return false; //there is only 1 gap in the edges of the square WHICH connects to other free cells (not in the square)
    }

    /**
     * Adds cell a and cell b together
     * @param a - the first cell
     * @param b - the second cell
     * @return the cell made by adding cell a and b together
     */
    public Cell addCells(Cell a, Cell b)
    {
        return new Cell(
                a.getCol() + b.getCol(),
                a.getRow() + b.getRow()
        );
    }

    /**
     * Subtracts cell b FROM cell a
     * @param a - the cell being subtracted FROM
     * @param b - the cell being subtracted
     * @return the cell made by the subtraction
     */
    public Cell subtractCells(Cell a, Cell b)
    {
        return new Cell(
                a.getCol() - b.getCol(),
                a.getRow() - b.getRow()
        );
    }

    /**
     * Returns whether this corner is adjacent to a free cell which is adjacent to other free cells.
     * @param corner - the corner being inspected for adjacent free cells.
     * @param corners - the other corners
     * @return true (corner is adjacent to a free cell which is adjacent to other free cells.)
     */
    public boolean checkCorner(int corner, Set<Integer> corners)
    {

        boolean found = false;
        ArrayList<Integer> neighbours = getFreeNeighbours(corner); //gets the free, uncoloured neighbours of the corner.

        //Iterates the free neighbours of the corner
        for (int neighbour : neighbours)
        {
            if (!corners.contains(neighbour)) //excludes free neighbours that are other corners in the square.
            {
                if (getFreeNeighbours(neighbour).size() > 1) //the free neighbour has 1 neighbour for sure (the corner), if it has more than it's adjacent other free cells.
                {
                    if (found || foundGap) //if more than 1 free cell found which is adjacent to other free cells  -> not a square.
                    {
                        foundGap = true;
                        return true;
                    }
                    else
                    {
                        found = true; //found only 1 free cell which is adjacent other free cells.
                    }
                }
            }
        }
        return found;
    }

    /**
     * Gets the unfilled neighbours of a given node.
     * @param id - the unique id of the given node.
     * @return The free neighbours.
     */
    public ArrayList<Integer> getFreeNeighbours(int id)
    {
        ArrayList<Integer> neighbours = new ArrayList<>();
        int neighbourCol;
        int neighbourRow;
        Cell cell = idToCell[id];

        for (Cell addendPair : ADDENDS_TO_FIND_NEIGHBOURS)
        {
            neighbourCol = cell.getCol() + addendPair.getCol();
            neighbourRow = cell.getRow() + addendPair.getRow();

            if (isNodeInGrid(neighbourCol, neighbourRow) && colours[Game.cellToId[neighbourCol][neighbourRow]] == NULL_INT_VALUE)
            {
                neighbours.add(Game.cellToId[neighbourCol][neighbourRow]); //if valid neighbour, add to hashset.
            }
        }
        return neighbours;
    }

    /**
     * When a completed puzzle has been generated, this function finds the goal pairs in the puzzle and returns the difficulty of the puzzle.
     * @return the difficulty of the puzzle.
     */
    public float postGeneration()
    {
        float turnCount = 0;
        Path p;
        final int pathCount = paths.size();
        Game.startGoals = new int[pathCount];
        Game.endGoals = new int[pathCount];

        for (int i = 0; i < pathCount; i++)
        {
            p = paths.get(i);
            Game.startGoals[i] = p.peek();
            Game.endGoals[i] = p.peekLast();

            turnCount += p.getTurnCount();
        }

        return turnCount/pathCount;
    }
}
