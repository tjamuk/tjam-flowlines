package uk.ac.aber.dcs.cs39440.tjamflowlines.tests;

import org.junit.Test;
import uk.ac.aber.dcs.cs39440.tjamflowlines.model.Path;

import static org.junit.Assert.*;

public class PathTest {

    @Test
    public void testCase_PathClass_addFunction()
    {
        Path path = new Path();
        path.add(0, false);

        assertEquals(0, path.peek());
        assertTrue(path.set.contains(0));
        assertTrue(path.listOfTurns.isEmpty());

        path.add(1, false);
        path.add(2, true);

        assertEquals(1, path.listOfTurns.size());
        assertTrue(path.listOfTurns.getFirst());
    }

    @Test
    public void testCase_PathClass_removeFunction()
    {
        Path path = new Path();
        path.add(0, false);
        path.add(1, false);
        path.add(2, true);
        path.add(3, false);

        assertEquals(3, path.peek());
        assertFalse(path.listOfTurns.getFirst());

        path.remove();

        assertEquals(2, path.peek());
        assertTrue(path.listOfTurns.getFirst());

        path.remove();

        assertEquals(1, path.peek());
        assertTrue(path.listOfTurns.isEmpty());

        assertTrue(path.set.contains(0));
        assertTrue(path.set.contains(1));
        assertFalse(path.set.contains(2));
        assertFalse(path.set.contains(3));
    }

    @Test
    public void testCase_PathClass_getTurnCountFunction()
    {
        Path path = new Path();
        path.add(0, false);
        path.add(1, false);

        path.add(2, true);
        path.add(3, false);
        path.add(4, true);
        path.add(5, false);
        path.add(6, true);

        assertEquals(3, path.getTurnCount());
    }

    @Test
    public void testCase_PathClass_areDirectionsValidFunction()
    {
        Path path = new Path();
        path.add(0, false);
        path.add(1, false);

        assertTrue(path.areDirectionsValid());

        path.add(2, false);
        path.add(3, false);
        path.add(4, true);
        path.add(5, true);

        assertTrue(path.areDirectionsValid());

        path.add(6, true);
        assertFalse(path.areDirectionsValid());
    }

    @Test
    public void testCase_PathClass_containsFunction()
    {
        Path path = new Path();
        assertFalse(path.contains(0));
        path.add(101, false);
        path.add(909, false);
        assertTrue(path.contains(101));
        assertTrue(path.contains(909));
    }

    @Test
    public void testCase_PathClass_isEmptyFunction()
    {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.add(0, false);
        assertFalse(path.isEmpty());
        path.remove();
        assertTrue(path.isEmpty());
    }

    @Test
    public void testCase_PathClass_peekFunction()
    {
        Path path = new Path();
        assertEquals(Path.NULL_VALUE, path.peek());
        path.add(0, false);
        assertEquals(0, path.peek());
    }

    @Test
    public void testCase_PathClass_peekSecondFunction()
    {
        Path path = new Path();
        assertEquals(Path.NULL_VALUE, path.peekSecond());

        path.add(0, false);
        assertEquals(Path.NULL_VALUE, path.peekSecond());

        path.add(1, false);
        assertEquals(0, path.peekSecond());

        path.add(2, false);
        assertEquals(1, path.peekSecond());
    }

    @Test
    public void testCase_PathClass_peekLastFunction()
    {
        Path path = new Path();
        assertEquals(Path.NULL_VALUE, path.peekLast());

        path.add(0, false);
        assertEquals(0, path.peekLast());

        path.add(1, false);
        assertEquals(0, path.peekLast());

        path.add(2, false);
        assertEquals(0, path.peekLast());
    }

    @Test
    public void testCase_PathClass_isTooSmallFunction()
    {
        Path path = new Path();

        path.add(0, false);

        path.add(1, false);
        assertTrue(path.isTooSmall());

        path.add(2, false);
        assertFalse(path.isTooSmall());
    }
}
