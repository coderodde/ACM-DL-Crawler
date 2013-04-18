package fi.helsinki.acmcrawler.domain;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public abstract class Node<T> implements Iterable<T> {
    protected String name;

    @Override
    abstract public int hashCode();

    @Override
    abstract public boolean equals(Object obj);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
