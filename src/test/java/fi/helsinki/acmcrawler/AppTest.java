package fi.helsinki.acmcrawler;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public class AppTest {

    /**
     * Test of trySplitEvenly method, of class App.
     */
    @Test
    public void testTrySplitEvenly() {
        List<Integer> shit = new ArrayList<Integer>();

        for (int i = 1; i <= 7; ++i) {
            shit.add(i);
        }

        List<List<Integer>> partition = App.trySplitEvenly(shit, 4);

        assertNotNull(partition);
        assertEquals(partition.size(), 4);

        for (int i = 0; i < 3; ++i) {
            assertNotNull(partition.get(i));
            assertEquals(partition.get(i).size(), 2);
        }

        assertNotNull(partition.get(3));
        assertEquals(partition.get(3).size(), 1);

        // Test pruning.
        shit = new ArrayList<Integer>();

        for (int i = 0; i < 3; ++i) {
            shit.add(i);
        }

        partition = App.trySplitEvenly(shit, 4);

        assertNotNull(partition);
        assertEquals(partition.size(), 3);

        for (int i = 0; i < 3; ++i) {
            assertNotNull(partition.get(i));
            assertEquals(partition.get(i).size(), 1);
        }
    }
}
