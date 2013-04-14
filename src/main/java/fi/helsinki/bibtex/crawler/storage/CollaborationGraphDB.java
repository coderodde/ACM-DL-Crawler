package fi.helsinki.bibtex.crawler.storage;

/**
 *
 * @author rodionefremov
 */
public interface CollaborationGraphDB {
    boolean addAuthor(String id, String name);
    boolean addPaper(String id, String name);
    boolean associate(String authorId, String paperId);
}
