package uk.ac.aber.dcs.cs39440.tjamflowlines.tests;

import org.junit.Test;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Cell;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Game;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void testCase_GameClass_getNeighboursFunction()
    {
        Game game = new Game(6,6,true);

        Set<Integer> expected = new HashSet<>(Set.of(
                Game.cellToId[2][3],
                Game.cellToId[2][5],
                Game.cellToId[1][4],
                Game.cellToId[3][4]
        ));

        assertEquals(expected, new HashSet<>(Game.getEdges(Game.cellToId[2][4])));
    }

    @Test
    public void testCase_GameClass_getStraightOnNodeFunction()
    {
        Game game = new Game(6,6,false);

        assertEquals(
                Game.cellToId[2][0],
                game.getStraightOnNode(new Cell(1,0), new Cell(0,0))
        );

        assertEquals(
                Game.NULL_INT_VALUE,
                game.getStraightOnNode(new Cell(0,0), new Cell(1,0))
        );
    }

    @Test
    public void testCase_GameClass_isNodeInGridFunction()
    {
        Game game = new Game(11,11,false);

        assertTrue(Game.isNodeInGrid(0,0));
        assertTrue(Game.isNodeInGrid(10,10));
        assertTrue(Game.isNodeInGrid(10,0));

        assertTrue(Game.isNodeInGrid(5,5));

        assertFalse(Game.isNodeInGrid(-1,0));
        assertFalse(Game.isNodeInGrid(10,11));
        assertFalse(Game.isNodeInGrid(11,-1));
    }

}
