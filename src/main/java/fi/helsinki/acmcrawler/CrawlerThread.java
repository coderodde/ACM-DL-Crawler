package fi.helsinki.acmcrawler;

import fi.helsinki.bibtex.crawler.domain.ActionType;
import fi.helsinki.bibtex.crawler.domain.Node;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public class CrawlerThread<T extends Node<T>> extends Thread {

    private Deque<T>        queue;
    private Set<T>          visited;
    private boolean         stop;
    private long            crawled;
    private final long      max;
    private ActionType[]    onDiscovery;

    public CrawlerThread(
            List<T> seeds,
            Set<T> visited,
            long max,
            ActionType... onDiscovery) {
        this.max = max;
        this.queue = new LinkedList<T>();
        this.queue.addAll(seeds);
        this.visited = java.util.Collections.synchronizedSet(visited);
        markSeedsAsVisited(seeds, this.visited);
    }

    @Override
    public void run() {
        breadthFirstSearch();
    }

    public void stopCrawling() {
        stop = true;
    }

    private void breadthFirstSearch() {
        while (queue.size() > 0) {
            T node = queue.removeFirst();
            Expand(node);

            if (stop) {
                return;
            }
        }
    }

    private void Expand(T v) {
        for (T u : v) {
            if (crawled >= max) {
                stop = true;
                return;
            }

            // a stop requested by user code?
            if (stop) {
                return;
            }

            if (visited.contains(u) == false) {
                // one more node discovered.
                visited.add(u);
                queue.addLast(u);

                // apply actions to new node.
                for (ActionType action : onDiscovery) {
                    u.act(action);
                }

                ++crawled;
            }
        }
    }

    private void markSeedsAsVisited(List<T> seeds, Set<T> visited) {
        for (T seed : seeds) {
            visited.add(seed);
        }
    }
}
