package fi.helsinki.acmcrawler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * This class models a simple generic hash set with only two major operations:
 * <ul>
 *  <li><code>add(T)</code> adds an element if not present,
 *  <li><code>containsAndAdd(T)</code> atomic test and add.
 * </ul>
 *
 * All access is synchronized.
 *
 * @param T the type of elements.
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

    /*
    //// THIS IS SLOOOOW ON MAC
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
    }*/

    public synchronized boolean add(T element) {
        return set.add(element);
    }

    public synchronized boolean containsAndAdd(T element) {
        boolean contains = set.contains(element);

        if (contains == false) {
            set.add(element);
        }

        return contains;
    }

    public synchronized int size() {
        return set.size();
    }
}
