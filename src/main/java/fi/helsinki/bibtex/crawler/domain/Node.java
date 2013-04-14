package fi.helsinki.bibtex.crawler.domain;

import java.util.EnumMap;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public abstract class Node<T> implements Iterable<T> {
    protected String name;
    protected EnumMap<ActionType, Actor> map;

    public Node() {
        map = new EnumMap<ActionType, Actor>(ActionType.class);
    }

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

    public Object act(ActionType type) {
        if (!map.containsKey(type)) {
            return null;
        }

        return map.get(type).performAction();
    }

    public Actor mapAction(ActionType type, Actor actor) {
        return map.put(type, actor);
    }
}
