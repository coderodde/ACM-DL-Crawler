package fi.helsinki.bibtex.crawler.storage;

import fi.helsinki.bibtex.crawler.domain.Node;
import java.util.List;

/**
 *
 * @author rodionefremov
 */
public interface CollaborationGraphDB<T extends Node<T>> {
    boolean addAuthor(String id, String name);
    boolean addPaper(String id, String name);
    boolean associate(String authorId, String paperId);

    List<T> listAllAuthors();
}
