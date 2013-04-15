package fi.helsinki.bibtex.crawler.domain.support;

import fi.helsinki.acmcrawler.Magic;
import fi.helsinki.bibtex.crawler.domain.Node;
import fi.helsinki.bibtex.crawler.storage.CollaborationGraphDB;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author rodionefremov
 */
public class AuthorNode extends Node<AuthorNode> {
    private String id;
    private CollaborationGraphDB db;

    public AuthorNode(String id) {
        super();

        if (id == null) {
            throw new IllegalArgumentException("'id' may not be null.");
        }

        this.id = id;
    }

    @Override
    public Iterator<AuthorNode> iterator() {
        return new NeighborIterator();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final AuthorNode other = (AuthorNode) obj;
        return this.id.equals(other.id);
    }

    @Override
    public String toString() {
        return "[Actor node: id=" + id + " name=\"" + name + "\"]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CollaborationGraphDB getDb() {
        return db;
    }

    public void setDb(CollaborationGraphDB db) {
        this.db = db;
    }

    private String getAuthorPageUrl() {
        return getAuthorPageUrlSimple() + Magic.URL_GET_ALL_ARGS;
    }

    private String getAuthorPageUrlSimple() {
        return Magic.URL_BASE
                + "/"
                + Magic.URL_AUTHOR_PAGE_SCRIPT_NAME
                + "?id="
                + id;
    }

    private class NeighborIterator implements Iterator<AuthorNode> {
        private static final String XPATH_COLLABORATORS_A =
                "//div[@class='abstract']/table/tbody/" +
                "tr[@valign='top']/td/div/a";

        private static final String TEXT_LINK_COLLEAGUES =
                "See all colleagues of this author";

        private static final String XPATH_PAPER_A =
                "//a[contains(@href,'citation.cfm')]";

        private Iterator<AuthorNode> iter;

        NeighborIterator() {
            List<AuthorNode> adj = new ArrayList<AuthorNode>();
            populate(adj, Magic.DEFAULT_JAVASCRIPT_WAIT);
            iter = adj.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public AuthorNode next() {
            return iter.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "remove() not implemented."
                    );
        }

        private void populate(List<AuthorNode> list, int timeoutSeconds) {
            HtmlUnitDriver driver = new HtmlUnitDriver(true);
            navigateToColleaguesPage(driver, timeoutSeconds);

            List<WebElement> aElements = driver.findElements(
                    By.xpath(XPATH_COLLABORATORS_A));

            processCoauthorList(aElements, list);

            navigateToPaperListPage(driver, timeoutSeconds);
        }

        private void navigateToColleaguesPage(HtmlUnitDriver driver,
                                              int timeoutSeconds) {
            driver.get(AuthorNode.this.getAuthorPageUrlSimple());

            WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);
            wait.until(ExpectedConditions
                    .presenceOfElementLocated(
                        By.linkText(TEXT_LINK_COLLEAGUES)));

            WebElement linkElement = driver.findElement(
                    By.linkText(TEXT_LINK_COLLEAGUES)
                    );

            linkElement.click();

            wait.until(ExpectedConditions
                       .presenceOfElementLocated(
                            By.xpath(XPATH_COLLABORATORS_A)));
        }

        private void processCoauthorList(List<WebElement> aElemList,
                                         List<AuthorNode> authors) {
            for (WebElement e : aElemList) {
                String href = e.getAttribute("href");

                if (href == null) {
                    continue;
                }

                int i1 = href.indexOf("id=");

                if (i1 < 0) {
                    continue;
                }

                int i2 = href.indexOf("&");

                if (i2 < 0 || i2 < i1) {
                    continue;
                }

                String id = href.substring(i1 + "id=".length(), i2);

                AuthorNode neighbor = new AuthorNode(id);
                neighbor.setName(e.getText());
                authors.add(neighbor);

                if (db != null) {
                    db.addAuthor(neighbor.getId(), neighbor.getName());
                }
            }
        }

        private void navigateToPaperListPage(HtmlUnitDriver driver,
                                             int timeoutSeconds) {
            driver.get(AuthorNode.this.getAuthorPageUrl());

            WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);
            wait.until(ExpectedConditions
                    .presenceOfElementLocated(
                        By.xpath(XPATH_PAPER_A)));
        }

        private void processPaperList(List<WebElement> aElements) {
            for (WebElement e : aElements) {
                
            }
        }
    }
}
