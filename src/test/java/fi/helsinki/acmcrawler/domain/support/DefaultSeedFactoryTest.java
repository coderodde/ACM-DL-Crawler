package fi.helsinki.acmcrawler.domain.support;

import fi.helsinki.acmcrawler.storage.support.CollaborationSQLiteDB;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author rodionefremov
 */
public class DefaultSeedFactoryTest {

    private static final String DB_FILE = ":memory:";
    private static final int MAX_SEEDS = 4;
    private static DefaultSeedFactory factory;

    @BeforeClass
    public static void setUpClass() {
        try {
            factory = new DefaultSeedFactory(new CollaborationSQLiteDB(DB_FILE));
        } catch(Exception e) {
            System.err.println(e);
            fail("DB did not open.");
        }
    }

    /**
     * Test of get method, of class DefaultSeedFactory.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        List<AuthorNode> list = factory.get(MAX_SEEDS);
        assertEquals(list.size(), MAX_SEEDS);

        for (AuthorNode a : list) {
            System.out.println(a);
        }
    }
}
