package fi.helsinki.bibtex.crawler.storage.support;

import fi.helsinki.bibtex.crawler.storage.BibTexDB;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLiteDBTest {

    private static final String FILENAME = "Funky.dat";
    private static BibTexDB db;

    @BeforeClass
    public static void setUpClass() throws Exception {
        db = new BibTexSQLiteDB(FILENAME);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        File f = new File(FILENAME);
        f.delete();
    }

    @Test
    public void testAll() {
        try {
            assertTrue(db.add("kirja", "@book{book1, author = {Thor}}"));
            assertTrue(db.add("artikla", "@article{art1, author = {Frosty}}"));
            assertTrue(db.contains("kirja"));
            assertTrue(db.contains("artikla"));
            List<String> list = new ArrayList<String>();

            for (String ref : db) {
                list.add(ref);
            }

            assertTrue(list.size() >= 2);
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }
}
