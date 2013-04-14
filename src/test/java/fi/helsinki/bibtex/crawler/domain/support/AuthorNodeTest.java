/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.bibtex.crawler.domain.support;

import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rodionefremov
 */
public class AuthorNodeTest {

    private static AuthorNode root;

    @BeforeClass
    public static void setUpClass() {
        root = new AuthorNode("81100552573");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of iterator method, of class AuthorNode.
     */
    @Test
    public void testIterator() {
        System.out.println("iterator():");
        int neighbors = 0;

        for (AuthorNode u : root) {
            System.out.println(u);
            neighbors++;
        }

        assertTrue(neighbors > 0);
    }

    /**
     * Test of toString method, of class AuthorNode.
     */
    @Test
    public void testToString() {
        System.out.println("toString(): " + root);
    }
}