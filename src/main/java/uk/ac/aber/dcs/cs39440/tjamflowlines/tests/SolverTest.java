package uk.ac.aber.dcs.cs39440.tjamflowlines.tests;

import org.junit.Test;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Cell;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Game;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.PuzzleGenerator;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Solver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Stack;

import static org.junit.Assert.*;

public class SolverTest {

    @Test
    public void testCase_SolverClass_massSolverTestingOnGeneratedPuzzles()
    {
        PuzzleGenerator pg;
        Solver solver;
        boolean result;
        long startTime;
        try {
            for (int i = 0; i < 1000; i++) {
                pg = new PuzzleGenerator(10, 10);
                pg.generatePuzzle();
//                pg.emptyPaths();
                pg.postGeneration();
                solver = new Solver();
                startTime = System.nanoTime();
                result = solver.solve();
                System.out.println(i + ") solvable = " + result + ". ||| time = " + Duration.ofNanos(System.nanoTime() - startTime).toNanos());
                assertTrue(result);
            }
        }
        catch (StackOverflowError error) {
            System.out.println("Exception: StackOverflowError caught - increase stack memory");
        }
    }

    @Test
    public void testCase_SolverClass_SolvingExamplePuzzle_3()
    {
        boolean isSolvable;

        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        int width = 10;
        int height = 10;

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(1,8));

        startGoals.add(new Cell(1,1));
        endGoals.add(new Cell(7,2));

        startGoals.add(new Cell(2,3));
        endGoals.add(new Cell(1,9));

        startGoals.add(new Cell(1,4));
        endGoals.add(new Cell(4,3));

        startGoals.add(new Cell(2,4));
        endGoals.add(new Cell(5,3));

        startGoals.add(new Cell(3,7));
        endGoals.add(new Cell(8,4));

        startGoals.add(new Cell(3,8));
        endGoals.add(new Cell(7,6));

        startGoals.add(new Cell(4,7));
        endGoals.add(new Cell(9,0));

        startGoals.add(new Cell(9,1));
        endGoals.add(new Cell(9,9));

        Solver game = new Solver(
                width,
                height,
                startGoals,
                endGoals
        );

        isSolvable = game.solve();

        if (isSolvable)
        {
            System.out.println("Solution found.");
        }
        else
        {
            System.out.println("No solution");
        }

        assertTrue(isSolvable);
    }

    @Test
    public void testCase_SolverClass_goalsSetup()
    {
        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(5,3));

        startGoals.add(new Cell(5,0));
        endGoals.add(new Cell(2,2));
        Cell startCell; int startId;
        Cell endCell; int endId;

        Solver solver = new Solver(6,4,startGoals,endGoals);
        for (int colour = 0; colour < startGoals.size(); colour++)
        {
            startCell = startGoals.get(colour);
            endCell = endGoals.get(colour);
            startId = Game.cellToId[startCell.getCol()][startCell.getRow()];
            endId = Game.cellToId[endCell.getCol()][endCell.getRow()];

            assertEquals(startId, Game.startGoals[colour]);
            assertEquals(endId, Game.endGoals[colour]);
            assertEquals(colour, Game.colours[startId]);
            assertEquals(colour, Game.colours[endId]);
        }
    }

    @Test
    public void testCase_SolverClass_pathsInit()
    {
        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(5,5));

        Solver solver = new Solver(6,6,startGoals,endGoals);

        for (int colour = 0; colour < startGoals.size(); colour++)
        {
            assertEquals(1, solver.paths.get(colour).size());
            assertEquals(
                    Game.cellToId[startGoals.get(colour).getCol()][startGoals.get(colour).getRow()],
                    (int) solver.paths.get(colour).getFirst()
            );
        }
    }

    @Test
    public void testCase_SolverClass_isColourNotDoneFunction()
    {
        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(1,0));

        Solver solver = new Solver(2,2,startGoals,endGoals);

        assertTrue(solver.isColourNotDone(0));

        solver.paths.get(0).addFirst(Game.cellToId[endGoals.get(0).getCol()][endGoals.get(0).getRow()]);

        assertFalse(solver.isColourNotDone(0));
    }

    @Test
    public void testCase_SolverClass_getVisitOrder()
    {

        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(3,3));

        Solver solver = new Solver(4,4,startGoals,endGoals);
        Stack<Integer> actualNeighbours = new Stack<>();
        solver.paths.get(0).addFirst(Game.cellToId[0][1]);

        solver.getVisitOrder(actualNeighbours, Game.cellToId[0][1], Game.cellToId[0][2], 0);

        Stack<Integer> expectedNeighbours = new Stack<>();
        expectedNeighbours.add(Game.cellToId[1][1]);
        expectedNeighbours.add(Game.cellToId[0][2]);

        assertEquals(expectedNeighbours, actualNeighbours);
    }

    @Test
    public void testCase_SolverClass_findConnectedComponentsFunction_findingComponentsCorrectly()
    {

        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(1,0));
        endGoals.add(new Cell(1,1));

        Solver solver = new Solver(3,2,startGoals,endGoals);

        solver.findConnectedComponents(0);
        assertEquals(0, solver.nodeToComponent[Game.cellToId[0][0]]);
        assertEquals(1, solver.nodeToComponent[Game.cellToId[2][1]]);
        assertEquals(2, (int) solver.componentSizes.get(0));
        assertEquals(2, (int) solver.componentSizes.get(1));
        assertTrue(solver.componentGoals.get(0).contains(Game.cellToId[1][0]));
        assertTrue(solver.componentGoals.get(1).contains(Game.cellToId[1][1]));
        assertTrue(solver.componentColours.get(0).contains(0));
        assertTrue(solver.componentColours.get(1).contains(0));
    }

    @Test
    public void testCase_SolverClass_findConnectedComponentsFunction_goalsNotSharingCommonComponent()
    {

        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(1,0));
        endGoals.add(new Cell(1,1));

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(2,0));

        Solver solver = new Solver(3,2,startGoals,endGoals);

        solver.paths.get(0).addFirst(Game.cellToId[1][1]);

        assertFalse(solver.findConnectedComponents(0));
    }

    @Test
    public void testCase_SolverClass_findBottlenecks()
    {

        ArrayList<Cell> startGoals = new ArrayList<>();
        ArrayList<Cell> endGoals = new ArrayList<>();

        startGoals.add(new Cell(2,0));
        endGoals.add(new Cell(2,1));

        startGoals.add(new Cell(0,0));
        endGoals.add(new Cell(4,0));

        startGoals.add(new Cell(1,0));
        endGoals.add(new Cell(3,0));

        Solver solver = new Solver(5,3,startGoals,endGoals);

        solver.paths.get(0).addFirst(Game.cellToId[2][1]);
        solver.findConnectedComponents(0);

        assertTrue(solver.isBottleneckPossible(Game.cellToId[2][1]));
    }


}
