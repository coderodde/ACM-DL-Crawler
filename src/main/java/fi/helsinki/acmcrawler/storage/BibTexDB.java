package fi.helsinki.acmcrawler.storage;

public interface BibTexDB extends Iterable<String> {
    boolean contains(String referenceName);
    boolean add(String referenceName, String referenceText);
}
