package fi.helsinki.acmcrawler;

import fi.helsinki.acmcrawler.domain.Node;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a crawling thread effectively utilizing the
 * breadth-first strategy.
 *
 * @author Rodion Efremov
 * @version I
 */
public class CrawlerThread<T extends Node<T>> extends Thread {

    private Deque<T>         queue;
    private ThreadSafeSet<T> visited;
    private volatile boolean stop;
    private volatile long    crawled;
    private final long       max;

    /**
     * Constructs a new crawling thread.
     *
     * @param seeds the nodes this crawling thread will start from.
     * @param visited a thread safe set for storing the visited nodes.
     * @param max the maximum amount of nodes to <strong>reach</strong>.
     */
    public CrawlerThread(List<T> seeds, ThreadSafeSet<T> visited, long max) {
        this.max = max;
        this.queue = new LinkedList<T>();
        this.visited = visited;
        markSeedsAsVisited(seeds, this.visited);
        this.queue.addAll(seeds);
    }

    @Override
    public void run() {
        System.out.println(">> Thread " + this.getName() + " is starting.");
        breadthFirstSearch();
        System.out.println(
                ">> Thread " + this.getName() + " is shutting down."
                );
    }

    /**
     * Asks this thread to stop crawling prematurely.
     */
    public void stopCrawling() {
        stop = true;
    }

    /**
     * Returns the amount of nodes this thread reached.
     *
     * @return the amount of nodes reached.
     */
    public long getCrawlCount() {
        if (stop == false) {
            throw new IllegalStateException(
                    "CrawlerThread should be stopped first before calling " +
                    "this operation."
                    );
        }

        return crawled;
    }

    private void breadthFirstSearch() {
        while (queue.size() > 0) {
            T node = queue.removeFirst();
            Expand(node);

            if (stop) {
                return;
            }
        }

        stop = true; // so that getCrawlCount() works.
    }

    private void Expand(T v) {
        for (T u : v) {
            if (visited.size() >= max) {
                stop = true;
                return;
            }

            // a stop requested by user code?
            if (stop) {
                return;
            }

            if (visited.containsAndAddBounded(u, max) == false) {
                // one more node discovered.
                queue.addLast(u);
                ++crawled;
            }
        }
    }

    private void markSeedsAsVisited(List<T> seeds, ThreadSafeSet<T> visited) {
        Iterator<T> iter = seeds.iterator();

        while (iter.hasNext()) {
            T seed = iter.next();

            if (crawled >= max) {
                iter.remove();
                continue;
            }

            visited.add(seed);
            ++crawled;
        }
    }
}
