package uk.ac.aber.dcs.cs39440.tjamflowlines.tests;

import org.junit.Test;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Game;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Path;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.PuzzleGenerator;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class PuzzleGeneratorTest {

    @Test
    public void testCase_PuzzleGeneratorClass_getSmallestComponentFunction()
    {
        PuzzleGenerator pg = new PuzzleGenerator(6,4);

        Game.colours[Game.cellToId[2][0]] = 999;
        Game.colours[Game.cellToId[2][1]] = 999;
        Game.colours[Game.cellToId[2][2]] = 999;
        Game.colours[Game.cellToId[2][3]] = 999;
        Game.colours[Game.cellToId[3][1]] = 999;
        Game.colours[Game.cellToId[4][1]] = 999;
        Game.colours[Game.cellToId[5][1]] = 999;

        pg.calculateConnectedComponents(Game.cellToId[5][1]);

        assertEquals(3, pg.components.get(pg.getSmallestComponent()).size());
    }

    @Test
    public void testCase_PuzzleGeneratorClass_calculateConnectedComponentsFunction()
    {
        PuzzleGenerator pg = new PuzzleGenerator(5,4);

        Game.colours[Game.cellToId[2][0]] = 999;
        Game.colours[Game.cellToId[2][1]] = 999;
        Game.colours[Game.cellToId[2][2]] = 999;
        Game.colours[Game.cellToId[2][3]] = 999;
        Game.colours[Game.cellToId[3][1]] = 999;
        Game.colours[Game.cellToId[4][1]] = 999;

        assertTrue(pg.calculateConnectedComponents(Game.cellToId[4][1]));
        assertFalse(pg.calculateConnectedComponents(Game.cellToId[2][3]));
    }

    @Test
    public void testCase_PuzzleGeneratorClass_getValidNeighboursFunction()
    {
        PuzzleGenerator pg = new PuzzleGenerator(3,2);

        pg.paths.add(new Path());
        pg.addNode(
                Game.cellToId[1][0],
                pg.paths.peek().id,
                false
        );

        pg.unvisited.remove(Game.cellToId[1][1]);
        pg.unvisited.remove(Game.cellToId[2][0]);

        ArrayList<Integer> expected = new ArrayList<>();
        ArrayList<Integer> actual = new ArrayList<>();
        expected.add(Game.cellToId[0][0]);

        pg.getValidNeighbours(actual, Game.cellToId[1][0], Game.NULL_INT_VALUE);
        assertEquals(expected, actual);
    }

    @Test
    public void testCase_PuzzleGeneratorClass_addNodeFunction_plus_remodeNodeFunction()
    {
        PuzzleGenerator pg = new PuzzleGenerator(3,3);

        pg.paths.add(new Path());
        pg.addNode(
                Game.cellToId[0][0],
                pg.paths.peek().id,
                false
        );

        assertFalse(pg.unvisited.contains(Game.cellToId[0][0]));
        assertEquals(Game.cellToId[0][0], pg.paths.peek().peek());
        assertEquals(pg.paths.peek().id, Game.colours[Game.cellToId[0][0]]);

        pg.removeNode(
                Game.cellToId[0][0]
        );

        assertTrue(pg.unvisited.contains(Game.cellToId[0][0]));
        assertEquals(Path.NULL_VALUE, pg.paths.peek().peek());
        assertEquals(Game.NULL_INT_VALUE, Game.colours[Game.cellToId[0][0]]);
    }


}
