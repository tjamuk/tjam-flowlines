package uk.ac.aber.dcs.cs39440.tjamflowlines.model;

import java.util.*;

public class Solver extends Game {

    /**
     * The number of coloured goal pairs.
     */
    int colourCount;

    /**
     * The number of connected components.
     */
    public int componentCount;

    /**
     * Each element index represents a node's id. | Each element's value = the connected component id in which the node resides.
     */
    public int[] nodeToComponent;

    /**
     * Each element index represents a component's id. | Each element's value = a set of goals that are adjacent to that component.
     */
    public ArrayList<Set<Integer>> componentGoals;

    /**
     * Each element index represents a component's id. | Each element's value = the size of that component.
     */
    public ArrayList<Integer> componentSizes;

    /**
     * Each element index represents a component's id. | Each element's value = a set of colours whose goals are adjacent to that component.
     */
    public ArrayList<Set<Integer>> componentColours;

    /**
     * Each element index represents a colour's id. | Each element's value = whether the front of a colour's path and its end goal are possible to connect (i.e. share an adjacent component)
     */
    public boolean[] areColoursPossibleToConnect;

    /**
     * Each element index represents a node's id. | Each element's value = if there is a goal at the node, value = colour of goal. Else NULL_INT_VALUE
     */
    public int[] goalColours;

    /**
     * Each element index represents a node's id. | Each element's value = whether they are possible to connect.
     */
    public boolean[] visited;

    /**
     * Each element index represents a new component's id. | Each element's value = a set of colour ids. If a colour is present, then the front of the path of that colour is in that new component.
     */
    public ArrayList<Set<Integer>> starts;

    /**
     * Each element index represents a new component's id. | Each element's value = a set of colour ids. If a colour is present, then the end goal of that colour is in that new component.
     */
    public ArrayList<Set<Integer>> ends;

    /**
     * Each element index represents a new component's id. | Each element's value = size of new component.
     */
    public ArrayList<Integer> sizes;

    /**
     * The number of new components.
     */
    public int newComponentCount;

    /**
     * The paths of the colours.
     */
    public ArrayList<LinkedList<Integer>> paths;

    /**
     * Only constructor for model.Game that initialises everything.
     *
     * @param width the number of columns in the grid.
     * @param height the number of rows in the grid.
//     * @param goals An array list of start,end goal node pairs. Where each node is a column,row pair.
     */
    public Solver(int width, int height, ArrayList<Cell> startGoalsList, ArrayList<Cell> endGoalsList)
    {
        super(width, height, false);

        colours = new int[width*height];

        colourCount = startGoalsList.size(); //the number of coloured pairs of goals.

        Cell cell;

        startGoals = new int[colourCount];
        endGoals = new int[colourCount];

        for (int colour = 0; colour < colourCount; colour++) //setups the start and end goals for all colours.
        {
            cell = startGoalsList.get(colour);
            startGoals[colour] = Game.cellToId[cell.getCol()][cell.getRow()];

            cell = endGoalsList.get(colour);
            endGoals[colour] = Game.cellToId[cell.getCol()][cell.getRow()];
        }

        Arrays.fill(colours, NULL_INT_VALUE); //all cells in the grid are unfilled.

        addGoalsToGrid(); //adds goals to the grid (fills in the cells with the colours of the goals)
        initialisePaths(); //adds the start goal to the paths.
    }

    /**
     * A constructor for when there are already goals, width and height set.
     */
    public Solver()
    {
        colourCount = Game.startGoals.length;

        colours = new int[width*height];
        Arrays.fill(colours, NULL_INT_VALUE);
        addGoalsToGrid();
        initialisePaths();
    }

    /**
     * Adds the coloured goals (found in int[] startGoals, endGoals) to Colour[] colours.
     */
    public void addGoalsToGrid()
    {
        for (int goalPairIndex = 0; goalPairIndex < colourCount; goalPairIndex++) //for every pair of goals...
        {
            colours[startGoals[goalPairIndex]] = goalPairIndex;
            colours[endGoals[goalPairIndex]] = goalPairIndex;
        }
        goalColours = colours.clone();
    }

    /**
     * Adds the start goals to the paths.
     *
     * For each colour's path, add the colour's start goal to it.
     */
    public void initialisePaths()
    {
        paths = new ArrayList<>();

        for (int index = 0; index < colourCount; index++) //for each colour's path, add the colour's start goal to it.
        {
            paths.add(new LinkedList<>());
            paths.get(index).addFirst(startGoals[index]);
        }
    }

    /**
     * Returns whether there is a path connecting a colour's start and end goals.
     */
    public boolean isColourNotDone(int colour)
    {
        return !paths.get(colour).getFirst().equals(endGoals[colour]);
    }

    /**
     * A recursive backtracking algorithm that solves the puzzle.
     * @return true = was able to find a solution; false = was not able.
     */
    public boolean solve()
    {
        int current; //the head/front of the most recent path.
        int prev; //the 2nd front of the most recent path.
        int next; //the next node to add to the path.
        Stack<Integer> neighbours;

        for (int colour = 0; colour < colourCount; colour++) //for every colour, check if it's path is done.
        {
            if (isColourNotDone(colour)) //if colour's path not completed.
            {

                current = paths.get(colour).getFirst(); //1st front of colour's path.
                prev = getInPath(colour, 1); //2nd front of colour's path
                next = (prev == NULL_INT_VALUE)? NULL_INT_VALUE : getStraightOnNode(idToCell[current], idToCell[prev]); //if there is a previous front in path, assign 'next' to the straight on node. ELSE set to null.
                neighbours = new Stack<>();

                //if the new front of the path is next to the endGoal, can move onto the next colour.
                if (Game.getEdges(current).contains(endGoals[colour]))
                {
                    paths.get(colour).addFirst(endGoals[colour]); //add end goal onto path (completing it)
                    if (solve()) //move onto next colour. IF solution from this point -> return true. ELSE (no solution), remove end goal and return false (backtrack)
                    {
                        return true;
                    }
                    else
                    {
                        paths.get(colour).removeFirst();
                        return false;
                    }
                }

                getVisitOrder(neighbours, current, next, colour); //get an ordered stack of neighbours to visit.

                while (!neighbours.isEmpty()) //add every neighbour.
                {
                    next = neighbours.pop();

                    paths.get(colour).addFirst(next); //add to path.
                    colours[next] = colour;

                    if (findConnectedComponents(colour) && solve())
                    {
                        return true;
                    }
                    else
                    {
                        paths.get(colour).removeFirst();
                        colours[next] = NULL_INT_VALUE;
                    }
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the order in which to visit the neighbours of the front of the current.
     * @param neighbours - the visit order stack.
     * @param frontNodeOfPath - the front/1st of the current path.
     * @param straightOnNode - the next node when going straight from front of path.
     * @param colour - the current colour path being worked on.
     */
    public void getVisitOrder(Stack<Integer> neighbours, int frontNodeOfPath, int straightOnNode, int colour)
    {
        int third = getInPath(colour, 2);

        //For every neighbour of the front of the path...
        for (int node : Game.getEdges(frontNodeOfPath))
        {
            if (colours[node] == NULL_INT_VALUE && node != straightOnNode) //If the node is not filled NOR the straightOn node -> add.
            {
                if (third != NULL_INT_VALUE) //check to see if redundant node.
                {
                    if (Game.getEdges(third).contains(node)) //check to see that node is redundant.
                    {
                        third = NULL_INT_VALUE;
                        continue;
                    }
                }
                neighbours.add(node);
            }
        }

        //After all other neighbours have been added, if straightOn node exists and is unfilled, add (to front of stack)
        if (straightOnNode != NULL_INT_VALUE)
        {
            if (colours[straightOnNode] == NULL_INT_VALUE)
            {
                neighbours.add(straightOnNode);
            }
        }
    }

    /**
     * Finds every component and returns whether the resulting components are all valid.
     * @param latestNodeColour - the colour of the node most recently added.
     * @return whether all resulting components are valid (true = valid, false = invalid)
     */
    public boolean findConnectedComponents(int latestNodeColour) {

        nodeToComponent = new int[Game.size]; //each element's index represents what node it is, the value represents what component that node is a part of.
        Arrays.fill(nodeToComponent, NULL_INT_VALUE);
        areColoursPossibleToConnect = new boolean[colourCount]; //each element represents whether for a colour, it's start and end goals share an adjacent component. If they don't, a path is not possible to connect them.
        componentCount = 0; //number of components.
        componentGoals = new ArrayList<>(); //each element's index = component. ||| Elem value = what goals are in that component.
        componentColours = new ArrayList<>(); //each element's index = component. ||| Elem value = what colours are in that component.
        componentSizes = new ArrayList<>();

        for (int node = 0; node < Game.size; node++)
        {
            if (nodeToComponent[node] == NULL_INT_VALUE && colours[node] == NULL_INT_VALUE) //if it has NOT been visited AND it has no colour (unfilled) -> in new component.
            {
                //in new component
                componentGoals.add(new HashSet<>());
                componentColours.add(new HashSet<>());
                componentSizes.add(0);
                dfs(node);
                if (!isComponentValid(latestNodeColour, componentCount))
                {
                    return false;
                }
                componentCount++;
            }
        }
        return areComponentsValid(latestNodeColour);
    }

    /**
     * depth-first-search for finding components.
     * @param node - the node being visited.
     */
    public void dfs(int node)
    {
        nodeToComponent[node] = componentCount;
        componentSizes.set(componentCount, componentSizes.get(componentCount)+1);
        for (int neighbour : Game.getEdges(node))
        {
            if (colours[neighbour] == NULL_INT_VALUE) //if neighbour unfilled (no colour)
            {
                if (nodeToComponent[neighbour] == NULL_INT_VALUE) //if neighbour hasn't been visited
                {
                    dfs(neighbour); //explore neighbour
                }
            }
            else //neighbour filled (coloured)
            {
                int goalColour = goalColours[neighbour];
                if (goalColour != NULL_INT_VALUE) //if it is a goal
                {
                    componentColours.get(componentCount).add(goalColour); //add colour to component.
                    componentGoals.get(componentCount).add(neighbour); //add goal to component.
                }
            }
        }
    }

    /**
     * Checks if a single component is valid.
     * @param latestNodeColour - the colour of the latest coloured node (most recently added to a path)
     * @param componentId - the index of the component.
     * @return true = component is valid. | false = component is invalid.
     */
    public boolean isComponentValid(int latestNodeColour, int componentId)
    {
        if (componentColours.get(componentId).isEmpty()) //if the component has does not have an end goal nor start goal, return false.
        {
            return false;
        }

        boolean atLeastOneColourPossibleToConnect = false;

        for (int colour : componentColours.get(componentId)) //for every colour in component
        {
            if (
                    componentGoals.get(componentId).contains(startGoals[colour])
                    &&
                    componentGoals.get(componentId).contains(endGoals[colour])
            )
            {
                atLeastOneColourPossibleToConnect = true; //if both goals are in component, they are possible to connect.
                areColoursPossibleToConnect[colour] = true; //there is at least 1 possible connection.
            }
        }
        if (atLeastOneColourPossibleToConnect)
        {
            return true;
        }
        else //if it's the component where the most recently added to path is working towards its end goal.
        {
            return componentColours.get(componentId).contains(latestNodeColour);
        }
    }

    /**
     * Called after finding components. Checks for all colours if its start and end goals share a component (i.e. possible to connect.)
     * @param latestNodeColour - the colour of the latest coloured node (most recently added to a path)
     * @return true - all colours either finished or possible to have goals connected together. | false - else.
     */
    public boolean areComponentsValid(int latestNodeColour)
    {
        for (int colour = 0; colour < colourCount; colour++)
        {
            if (isColourNotDone(colour) && !areColoursPossibleToConnect[colour])
            {
                if (colour != latestNodeColour)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks for any bottlenecks created when a node is added to a path.
     * @param node - the node added to a path.
     * @return true = there is a bottleneck. | false = there is not a bottleneck.
     */
    public boolean isBottleneckPossible(int node)
    {

        int adj_component = -999;
        //checks to see if the node is adjacent to ONLY 1 adj_component.
        for (int neighbour : Game.getEdges(node))
        {
            if (nodeToComponent[neighbour] != -1)
            {
                if (adj_component == -999)
                {
                    adj_component = nodeToComponent[neighbour];
                }
                else if (adj_component != nodeToComponent[neighbour])
                {
                    return false;
                }
            }
        }
        if (adj_component == -999) {return false;} //if adjacent to no components, return false

        int count = 0;
        boolean isAddedNodeOnlyInAdjComponent;
        //finds how many goal pairs it has that are both in only 1 adj_component.
        for (int colour = 0; colour < colourCount; colour++)
        {
            if (
                    isColourNotDone(colour) &&
                    componentGoals.get(adj_component).contains(startGoals[colour]) &&
                    componentGoals.get(adj_component).contains(endGoals[colour])
            ) //if colour path is not complete and adj_component contains both goals of the colour.
            {
                isAddedNodeOnlyInAdjComponent = true;
                for (int neighbour : Game.getEdges(paths.get(colour).getFirst())) //check if path's end is adjacent to only the added node's adj_component
                {
                    if (!(nodeToComponent[neighbour] == adj_component || nodeToComponent[neighbour] == -1))
                    {
                        isAddedNodeOnlyInAdjComponent = false;
                        break;
                    }
                }
                if (isAddedNodeOnlyInAdjComponent)
                {
                    count++;
                }
            }
        }
        if (count > 1)
        {
            return doesNodeCreateBottleneck(node, adj_component);
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks for any bottlenecks created when a node is added to a path.
     * @param node - the node added to a path.
     * @param component - the component the node is in./
     * @return true = there is a bottleneck. | false = there is not a bottleneck.
     */
    public boolean doesNodeCreateBottleneck(int node, int component)
    {
        //smaller component must have 1 goal.
        starts = new ArrayList<>(); //Each index represents a subcomponent of component. ||| Each element is a set of colour indexes showing that the front of those colours' paths are present in that subcomponent.
        ends = new ArrayList<>(); //Each index represents a subcomponent of component. ||| Each element is a set of colour indexes showing that the end goals of those colours are present in that subcomponent.
        sizes = new ArrayList<>(); //Each index represents a subcomponent of component. ||| Each element is the size of the subcomponent.
        int count;
        newComponentCount = 0;
        for (int neighbour : Game.getEdges(node)) //for every neighbour (possible bottleneck) of newly node
        {
            if (colours[neighbour] == NULL_INT_VALUE) //identify a neighbour that is unfilled, this will be checked to be the bottleneck.
            {
                visited = new boolean[size];
                visited[neighbour] = true; //remove the possible bottleneck from the grid.
                for (int start : Game.getEdges(neighbour))
                {

                    //finds an unfilled start point for finding a new component created by removing the bottleneck.
                    //if the new component found is equal in size to the original component, return false.
                    //Else, a new component has been created -> check to  see if the new component is valid.

                    if (colours[start] == NULL_INT_VALUE)
                    {
                        starts.add(new HashSet<>());
                        ends.add(new HashSet<>());
                        count = dfsForFindingNewComponents(start);
                        if (count != componentSizes.get(component)-1)
                        {
                            sizes.add(count);
                            newComponentCount++;
                            return isNewComponentInvalid(component, neighbour, start);
                        }
                        else
                        {
                            starts.remove(newComponentCount);
                            ends.remove(newComponentCount);
                        }
                        break; //once an unfilled start point is found, won't search anymore.
                    }
                }
            }
        }
        return false;
    }

    public int getOtherNewComponents(int bottleneckNode, int startNode)
    {
        int chosenComponent = 0;
        int chosenComponentSize = starts.get(0).size() + ends.get(0).size(); //it will be faster to search the component with the fewest goals.
        int goalAndFrontCount;

        //There could be up to 3 components created by removing this bottleneck.
        //Because there are up to 3 neighbours of the bottleneck node (the 4th would be the most recently added node)
        //These neighbours are the start search points for the components.
        for (int node : Game.getEdges(bottleneckNode))
        {
            if (colours[node] == NULL_INT_VALUE && node != startNode)
            {
                starts.add(new HashSet<>());
                ends.add(new HashSet<>());
                sizes.add(dfsForFindingNewComponents(node));
                goalAndFrontCount = starts.get(newComponentCount).size() + ends.get(newComponentCount).size();
                if (goalAndFrontCount < chosenComponentSize)
                {
                    chosenComponentSize = goalAndFrontCount;
                    chosenComponent = starts.size()-1;
                }
                newComponentCount++;
            }
        }
        return chosenComponent;
    }

    /**
     * Checks to see if the resulting new component from removing a potential bottleneck is invalid.
      * @param originalComponent - the component that the new component was created from.
     * @param bottleneckNode - the bottleneck node whose removal created the new component.
     * @param startNode - a neighbour of the bottleneck node which was the start point of the search.
     * @return true = new component invalid. | false = new component valid.
     */
    public boolean isNewComponentInvalid(int originalComponent, int bottleneckNode, int startNode)
    {
        int chosenComponent = getOtherNewComponents(bottleneckNode, startNode);

        int count = 0;

        count+= getUniqueGoalPairCount(true, chosenComponent, originalComponent);
        if (count>1) {return true;}

        count+= getUniqueGoalPairCount(false, chosenComponent, originalComponent);

        return (count>1);
    }

    /**
     * Gets the number of unique goal pairs (adjacent to no other component) in the new component.
     * @param isStart - whether to check in ArrayList<Set<Integer>> 'starts' (true) or 'ends' (false)
     * @param smallestComponent - the new component with the fewest number of fronts and end goals.
     * @param originalComponent - the component from which the new components were created.
     * @return the number of unique goal pairs.
     */
    public int getUniqueGoalPairCount(boolean isStart, int smallestComponent, int originalComponent)
    {
        boolean isNodeOnlyInOneComponent; boolean isOnlyInOneNewComponent; int count = 0;
        for (int colour : ((isStart)? starts : ends).get(smallestComponent))
        {
            //find whether colour is only adjacent to original component.
            isNodeOnlyInOneComponent = true;
            for (int neighbour : Game.getEdges((isStart)? paths.get(colour).getFirst() : endGoals[colour]))
            {
                if (nodeToComponent[neighbour] != NULL_INT_VALUE && nodeToComponent[neighbour] != originalComponent)
                {
                    isNodeOnlyInOneComponent = false;
                    break;
                }
            }

            if (isNodeOnlyInOneComponent)
            {
                if (componentGoals.get(originalComponent).contains((isStart)? endGoals[colour] : paths.get(colour).getFirst())) //if the original component contained the opposite end (e.g. for front of path -> end goal)
                {
                    if ( !(((isStart)? ends : starts).get(smallestComponent)).remove(colour) ) //if it's opposite end is in the new component, remove from opposite end.
                    {
                        isOnlyInOneNewComponent = true;
                        //it may not be a bottleneck if available in other created new components so check for so.
                        for (int i = 0; i < ((isStart)? starts : ends).size(); i++) //for every new component
                        {
                            if (i != smallestComponent && (((isStart)? starts : ends).get(i).contains(colour) && ((isStart)? ends : starts).get(i).contains(colour)))
                            {
                                isOnlyInOneNewComponent = false;
                                break;
                            }
                        }
                        if (isOnlyInOneNewComponent)
                        {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * depth first search for finding new components
     * @param node - the node being visited
     * @return the number of nodes searched.
     */
    public int dfsForFindingNewComponents(int node)
    {
        visited[node] = true;
        int count = 1;
        for (int neighbour : Game.getEdges(node))
        {
            if (colours[neighbour] == NULL_INT_VALUE)
            {
                if (!visited[neighbour])
                {
                    count += dfsForFindingNewComponents(neighbour);
                }
            }
            else
            {
                int colour = colours[neighbour];
                if (colour != 999) //debug
                {

                    if (neighbour == paths.get(colour).getFirst())
                    {
                        starts.get(newComponentCount).add(colour);
                    }
                    else if (neighbour == endGoals[colour])
                    {
                        ends.get(newComponentCount).add(colour);
                    }
                }
            }
        }
        return count;
    }

    /**
     * Outputs the current state of the grid onto the console.
     */
    public void printGrid()
    {
        int node;
        for (int row = 0; row < height; row++)
        {
            for (int col = 0; col < width; col++)
            {
                node = cellToId[col][row];
                if (colours[node] == NULL_INT_VALUE)
                {
                    System.out.print("\u001B[0m   ");
                }
                else
                {
                    System.out.print(Colour.getBackgroundFromOrdinal(colours[node]) + "   ");
                }
            }
            System.out.println("\u001B[0m");
        }
    }

    public int getInPath(int colour, int index)
    {
        return (paths.get(colour).size() > index) ? paths.get(colour).get(index) : NULL_INT_VALUE;
    }
}
