package fi.helsinki.acmcrawler.domain;

import java.util.List;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public interface SeedFactory<T extends Node<T>> {
    List<T> get(int max);
}
