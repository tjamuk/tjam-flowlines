package uk.ac.aber.dcs.cs39440.tjamflowlines.model;

import java.util.*;

public class Path {
    /**
     * A constant representing a null value for an integer.
     */
    public final static int NULL_VALUE = -1;

    /**
     * The max number of consecutive turns a path can take.
     */
    public final static int MAX_ADJACENT_TURNS = 2;

    /**
     * The minimum path length (number of nodes in path)
     */
    public final static int MIN_PATH_LENGTH = 3;

    /**
     * A class variable showing the number of paths that have been created. This is helpful for assigning a unique id.
     */
    public static int count = 0;

    public static int upperBound; //todo remove
    public static int lowerBound = 4; //todo remove
    public static Random randomGenerator = new Random(); //todo remove
    public int maxLength; //todo remove

    /**
     * Contains all the elements of the path within a HashSet. This variable is great for checking if a given element is in the path (constant average case time)
     */
    public Set<Integer> set;

    /**
     * Contains an ordered list of elements in the path.
     */
    public LinkedList<Integer> sequence;

    /**
     * This variable goes hand in hand with variable 'sequence'.
     * When adding onto the path, it can either go straight or turn. This variable records whether the path goes straight (elem = false) or turns (elem = true)
     */
    public LinkedList<Boolean> listOfTurns;

    /**
     * This records whether the path is at a deadend (no more nodes to visit) or not
     */
    public boolean isAtDeadend;

    /**
     * A unique path id.
     */
    public final int id;


    /**
     * Constructor
     */
    public Path()
    {
        set = new HashSet<>();
        sequence = new LinkedList<>();
        listOfTurns = new LinkedList<>();
        isAtDeadend = false;
        id = ++Path.count;
        maxLength = 50;
    }

    /**
     * Adds a node onto the path
     * @param id - the id of the node
     * @param isTurn - whether the path is turning or not.
     */
    public void add(int id, boolean isTurn)
    {
        set.add(id);
        sequence.addFirst(id);
        if (sequence.size() > 2) //if seq = { 1-->2 }, cannot tell if path going straight or turning. Whereas if seq = { 1 --> 2 --> 3 } if 2-->3 same direction as 1-->2, then the path is going straight (else turning). This means listOfTurns's size = seq size - 2. As for first two elements of seq. can't tell if turning or not.
        {
            listOfTurns.addFirst(isTurn);
        }
    }

    /**
     * Gets the number of turns within the path.
     */
    public int getTurnCount()
    {
        int counter = 0;
        for (boolean isTurn : listOfTurns)
        {
            if (isTurn) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Removes the first/front node of the path.
     */
    public void remove()
    {
        set.remove(sequence.removeFirst());
        if (!listOfTurns.isEmpty())
        {
            listOfTurns.removeFirst();
        }
    }

    /**
     * If there are 3 (MAX_ADJACENT_TURNS + 1) consecutive turns in the path, return false (too many turns, creates bad path). Else return true.
     */
    public boolean areDirectionsValid()
    {
        if (listOfTurns.size() <= MAX_ADJACENT_TURNS)
        {
            return true;
        }

        Iterator<Boolean> iterator = listOfTurns.iterator();

        for (int i = 0; i <= MAX_ADJACENT_TURNS; i++) //checks the first 3 (MAX_ADJACENT_TURNS + 1) directions in listOfTurns
        {
            if (!iterator.next()) //if next element is not a turn, it's valid so return true.
            {
                return true;
            }
        }
        return false; //if here then it means the first 3 directions in listOfTurns were all turns
    }

    /**
     * Checks if there is a given node in the path.
     * @param id - the unique id of the node.
     * @return true (node is in the path). | false (node not in path)
     */
    public boolean contains(int id)
    {
        return set.contains(id);
    }

    /**
     * Returns whether the path is empty or not (has elements)
     * @return true (path is empty). | false (path not empty)
     */
    public boolean isEmpty()
    {
        return sequence.isEmpty();
    }

    /**
     * Gets the first / front node of the path.
     * @return The unique id of the front node.
     */
    public int peek()
    {
        return (sequence.isEmpty())? NULL_VALUE : sequence.peekFirst();
    }

    /**
     * Gets the second node of the path (i.e. 2nd to the front)
     * @return The unique id of the second node.
     */
    public int peekSecond()
    {
        return (sequence.size() > 1)? sequence.get(1) : NULL_VALUE;
    }

    /**
     * Gets the last / back node of the path.
     * @return The unique id of the last node.
     */
    public int peekLast()
    {
        return (!sequence.isEmpty())? sequence.getLast() : NULL_VALUE;
    }

    public void setRandomLength()
    {
        maxLength = randomGenerator.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public boolean isFull()
    {
        return sequence.size()==maxLength;
    }

    /**
     * Returns whether the path is too small, if so then it's an invalid path (if it also cannot add nodes on)
     * @return true (too small). | false (sufficient size)
     */
    public boolean isTooSmall()
    {
        return sequence.size()<MIN_PATH_LENGTH;
    }

    /**
     * Returns the path as an ordered list.
     */
    public LinkedList<Integer> getSequence()
    {
        return sequence;
    }
}
