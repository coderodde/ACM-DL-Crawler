package fi.helsinki.acmcrawler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;


/**
 *
 * @author Rodion Efremov
 * @version I
 */
public class ThreadSafeSet<T> {
    private static final int    DEFAULT_SIZE = 20;
    private static final float  DEFAULT_LOAD_FACTOR = 1.05f;

    private Semaphore mutex;
    private Set<T>    set;

    public ThreadSafeSet(int size, float loadFactor) {
        set     = new HashSet<T>(size, loadFactor);
        mutex   = new Semaphore(1, true);
    }

    public ThreadSafeSet(int size) {
        this(size, DEFAULT_LOAD_FACTOR);
    }

    public ThreadSafeSet() {
        this(DEFAULT_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public boolean add(T element) {
        mutex.acquireUninterruptibly();
        boolean ret = set.add(element);
        mutex.release();
        return ret;
    }

    public boolean containsAndAdd(T element) {
        mutex.acquireUninterruptibly();
        boolean contains = set.contains(element);

        if (contains == false) {
            set.add(element);
        }

        mutex.release();
        return contains;
    }

    public int size() {
        mutex.acquireUninterruptibly();
        int sz = set.size();
        mutex.release();
        return sz;
    }
}
