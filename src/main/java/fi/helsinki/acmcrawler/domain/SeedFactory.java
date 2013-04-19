package fi.helsinki.acmcrawler.domain;

import java.util.List;

/**
 * Defines the API for seed selection and retrieval.
 *
 * @author Rodion Efremov
 * @version I
 */
public interface SeedFactory<T extends Node<T>> {
    List<T> get(int max);
}
