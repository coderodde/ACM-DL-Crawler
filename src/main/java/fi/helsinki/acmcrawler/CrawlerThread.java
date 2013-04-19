package fi.helsinki.acmcrawler;

import fi.helsinki.acmcrawler.domain.Node;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
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

    public void stopCrawling() {
        stop = true;
    }

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

//            if (visited.size() >= max) {
//                stop = true;
//            }

            if (stop) {
                return;
            }
        }

        stop = true; // in case queue.size() > 0 failed, not stop == true.
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
