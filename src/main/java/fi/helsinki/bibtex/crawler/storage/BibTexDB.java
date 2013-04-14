package fi.helsinki.bibtex.crawler.storage;

public interface BibTexDB extends Iterable<String> {
    boolean contains(String referenceName);
    boolean add(String referenceName, String referenceText);
}
