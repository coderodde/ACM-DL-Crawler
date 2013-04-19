package fi.helsinki.acmcrawler;

import fi.helsinki.acmcrawler.domain.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author rodionefremov
 */
public class CrawlerThreadTest {

    private static final int THREADS = 2;
    private static final int NODE_AMOUNT = 10000;
    private static final float LF = 0.15f;

    private static List<GraphNode> graph;
    private static Random r;

    @BeforeClass
    public static void setUpClass() {
        r = new Random(313L);
        graph = createRandomGraph(NODE_AMOUNT, r, LF);
    }

    @Test
    public void mainTest() {
        System.out.println("--- main activity");
        List<GraphNode> oneSeed = new LinkedList<GraphNode>();
        ThreadSafeSet<GraphNode> visitedSet = new ThreadSafeSet<GraphNode>();

        oneSeed.add(graph.get(graph.size() / 2));

        CrawlerThread<GraphNode> ct = new CrawlerThread<GraphNode>(
                oneSeed,
                visitedSet,
                Long.MAX_VALUE
                );

        long ta = System.currentTimeMillis();
        ct.run();
        long tb = System.currentTimeMillis();

        System.out.println("Crawling in one thread took " + (tb - ta) + " ms.");
        System.out.println("Nodes: " + ct.getCrawlCount() + ", " + visitedSet.size());

        CrawlerThread<?>[] crawlers = new CrawlerThread<?>[THREADS];
        List<GraphNode> seeds = sampleSeeds(graph, r, THREADS);
        ThreadSafeSet<GraphNode> visitedSet2 =new ThreadSafeSet<GraphNode>();

        for (int i = 0; i < THREADS; ++i) {
            List<GraphNode> seedList = new ArrayList<GraphNode>(1);
            seedList.add(seeds.get(i));

            crawlers[i] = new CrawlerThread<GraphNode>(
                    seedList,
                    visitedSet2,
                    Long.MAX_VALUE
                    );
        }

        ta = System.currentTimeMillis();

        for (CrawlerThread<?> c : crawlers) {
            c.start();
        }

        for (CrawlerThread<?> c : crawlers) {
            try {
                c.join();
            } catch(InterruptedException e) {
                System.err.println(e);
            }
        }

        tb = System.currentTimeMillis();

        System.out.println("Crawling in " + THREADS + " threads took " +
                (tb - ta) + " ms."
                );

        long l = 0L;

        for (CrawlerThread<?> c : crawlers) {
            l += c.getCrawlCount();
        }

        System.out.println("Nodes: " + l + ", " + visitedSet2.size());

        assertEquals(visitedSet.size(), visitedSet2.size());
    }

    /**
     * Test of stopCrawling method, of class CrawlerThread.
     */
    @Test
    public void testStopCrawling() {
        System.out.println("--- stopCrawling()");

        CrawlerThread<?>[] crawlers = new CrawlerThread<?>[THREADS];
        List<GraphNode> seeds = sampleSeeds(graph, r, THREADS);
        ThreadSafeSet<GraphNode> visitedSet =new ThreadSafeSet<GraphNode>();

        for (int i = 0; i < THREADS; ++i) {
            List<GraphNode> seedList = new ArrayList<GraphNode>(1);
            seedList.add(seeds.get(i));

            crawlers[i] = new CrawlerThread<GraphNode>(
                    seedList,
                    visitedSet,
                    Long.MAX_VALUE
                    );
        }

        for (CrawlerThread<?> c : crawlers) {
            c.start();
        }

        for (CrawlerThread<?> c : crawlers) {
            c.stopCrawling();
        }

        for (CrawlerThread<?> c : crawlers) {
            try {
                c.join();
            } catch(InterruptedException e) {
                System.err.println(e);
            }
        }

        System.out.println("visitedSet.size(): " + visitedSet.size());

        // Didn't crawled the entire graph.
        assertTrue(visitedSet.size() < NODE_AMOUNT);


        long l = 0L;

        for (CrawlerThread<?> c : crawlers) {
            l += c.getCrawlCount();
        }

        assertEquals(l, visitedSet.size());
    }

    @Test
    public void testParallelCrawlingIsBoundable() {
        final int BOUND = 5000;

        System.out.println("--- bounded search");

        CrawlerThread<?>[] crawlers = new CrawlerThread<?>[THREADS];
        List<GraphNode> seeds = sampleSeeds(graph, r, THREADS);
        ThreadSafeSet<GraphNode> visitedSet =new ThreadSafeSet<GraphNode>();

        assertEquals(visitedSet.size(), 0);

        for (int i = 0; i < THREADS; ++i) {
            List<GraphNode> seedList = new ArrayList<GraphNode>(1);
            seedList.add(seeds.get(i));

            crawlers[i] = new CrawlerThread<GraphNode>(
                    seedList,
                    visitedSet,
                    BOUND
                    );
        }

        long ta = System.currentTimeMillis();

        for (CrawlerThread<?> c : crawlers) {
            c.start();
        }

        for (CrawlerThread<?> c : crawlers) {
            try {
                c.join();
            } catch(InterruptedException e) {
                System.err.println(e);
            }
        }

        long tb = System.currentTimeMillis();

        System.out.println("Crawling in " + THREADS + " threads took " +
                (tb - ta) + " ms."
                );

        System.out.println("Nodes: " + visitedSet.size() + ", bound: " + BOUND);

        assertEquals(visitedSet.size(), BOUND);

        long l = 0L;

        for (CrawlerThread<?> c : crawlers) {
            l += c.getCrawlCount();
        }

        System.out.println("Sum of crawl counters: " + l);

        assertEquals(l, BOUND);
    }

    private static List<GraphNode> createRandomGraph(int size,
                                                     Random r,
                                                     float lf) {
        List<GraphNode> V = new ArrayList<GraphNode>(size);

        for (int i = 0; i < size; ++i) {
            V.add(new GraphNode("" + i));
        }

        for (int i = 0; i < size - 1; ++i) {
            GraphNode u = V.get(i);

            for (int j = i + 1; j < size; ++j) {
                if (r.nextFloat() < lf) {
                    u.addNeighbor(V.get(j));
                }
            }
        }

        return V;
    }

    private static List<GraphNode> sampleSeeds(List<GraphNode> graph,
                                               Random r,
                                               int size) {
        if (size >= graph.size()) {
            return new ArrayList<GraphNode>(graph);
        }

        Set<Integer> s = new HashSet<Integer>();
        List<GraphNode> list = new ArrayList<GraphNode>(size);

        for (int i = 0; i < size; ++i) {
            int guess = r.nextInt(graph.size());

            if (s.contains(guess) == true) {
                --i;
                continue;
            } else {
                s.add(guess);
                list.add(graph.get(guess));
            }
        }

        return list;
    }

    private static class GraphNode extends Node<GraphNode> {

        private String id;
        private List<GraphNode> adj;

        GraphNode(String id) {
            if (id == null) {
                throw new IllegalArgumentException("id may not be null.");
            }

            this.id = id;
            this.adj = new ArrayList<GraphNode>();
        }

        public void addNeighbor(GraphNode u) {
            this.adj.add(u);
            u.adj.add(this);
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public Iterator<GraphNode> iterator() {
            return new NeighborIterator(adj.iterator());
        }

        private static class NeighborIterator implements Iterator<GraphNode> {
            private Iterator<GraphNode> iterator;

            NeighborIterator(Iterator<GraphNode> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public GraphNode next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                // Protect from modifications.
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }
}
