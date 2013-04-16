package fi.helsinki.acmcrawler.domain.support;

import fi.helsinki.acmcrawler.Magic;
import fi.helsinki.acmcrawler.domain.support.AuthorNode;
import fi.helsinki.acmcrawler.domain.SeedFactory;
import fi.helsinki.acmcrawler.storage.CollaborationGraphDB;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public class DefaultSeedFactory implements SeedFactory<AuthorNode>{

    private static final String XPATH_JOURNAL_A =
            "//a[contains(@href,'pub.cfm?id=')]";

    private static final String XPATH_TOP10_ARTICLE_A =
            "//div[@id='toShowTop10']/ol/li/a";

    private static final String XPATH_AUTHORS_A =
    "//a[contains(@href,'results.cfm?') and contains(@title, 'Search for')]";

    private CollaborationGraphDB<AuthorNode> db;
    private List<AuthorNode> list;
    private int max;

    public DefaultSeedFactory(CollaborationGraphDB<AuthorNode> db) {
        this.db = db;
    }

    @Override
    public List<AuthorNode> get(int max) {
        if (list != null) {
            return new ArrayList<AuthorNode>(list);
        }

        this.max = max;
        list = new ArrayList<AuthorNode>();
        populate(Magic.DEFAULT_JAVASCRIPT_WAIT);
        return list;
    }

    private void populate(int timeoutSeconds) {
        WebDriver driver = new HtmlUnitDriver();
        navigateToJournalListPage(driver, timeoutSeconds);
        List<WebElement> aList = getLinkElementsFromJournalListPage(driver);

        for (WebElement e : aList) {
            System.out.println(e.getText());
            populateFromJournal(e.getAttribute("href"), timeoutSeconds);

            if (max <= 0) {
                return;
            }
        }
    }

    private void navigateToJournalListPage(WebDriver driver, int timeoutSeconds) {
        driver.get(Magic.URL_BASE + "/" + Magic.URL_JOURNAL_LIST_PAGE);
    }

    private List<WebElement> getLinkElementsFromJournalListPage(WebDriver driver) {
        return driver.findElements(By.xpath(XPATH_JOURNAL_A));
    }

    private void populateFromJournal(String url, int timeoutSeconds) {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);
        wait.until(ExpectedConditions
                   .presenceOfElementLocated(By.xpath(XPATH_TOP10_ARTICLE_A)));

        List<WebElement> aList =
                driver.findElements(By.xpath(XPATH_TOP10_ARTICLE_A));

        for (WebElement e : aList) {
            System.out.println("  " + e.getText());
            // Returns the amount of authors succ. processed.
            max -= processPaper(e.getAttribute("href"), timeoutSeconds);

            if (max <= 0) {
                return;
            }
        }
    }

    private int processPaper(String url, int timeoutSeconds) {
        WebDriver driver = new HtmlUnitDriver(true);
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);
        wait.until(ExpectedConditions
                   .presenceOfElementLocated(By.xpath(XPATH_AUTHORS_A)));


        List<WebElement> aList = driver.findElements(By.xpath(XPATH_AUTHORS_A));

        for (WebElement e : aList) {
            System.out.println("    " + e.getText());
        }

        return aList.size();
    }
}
