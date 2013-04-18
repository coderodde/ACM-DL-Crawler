package fi.helsinki.acmcrawler;

import static fi.helsinki.acmcrawler.Magic.*;
import fi.helsinki.acmcrawler.domain.SeedFactory;
import fi.helsinki.acmcrawler.domain.support.AuthorNode;
import fi.helsinki.acmcrawler.domain.support.DefaultSeedFactory;
import fi.helsinki.acmcrawler.storage.CollaborationGraphDB;
import fi.helsinki.acmcrawler.storage.support.BibTexSQLiteDB;
import fi.helsinki.acmcrawler.storage.support.CollaborationSQLiteDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The top-level code.
 *
 * @author coderodde
 * @version 0.1
 */
public class App extends Thread {

    private CrawlerThread<?>[] crawlers;

    public App(CrawlerThread<?>[] crawlers) {
        this.crawlers = crawlers;
    }

    @Override
    public void run() {
        long total = 0L;

        for (CrawlerThread<?> t : crawlers) {
            t.stopCrawling();
        }

        for (CrawlerThread<?> t : crawlers) {
            try {
                t.join();
                total += t.getCrawlCount();
            } catch(InterruptedException e) {

            }
        }

        System.out.println("DONE: Crawled " + total + " authors.");
    }

    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println(HELP_MSG);
            System.exit(0);
        }

        Map<String, String> m = processCommandLine(args);

        for (Map.Entry<String, String> e : m.entrySet()) {
            System.out.println(e);
        }

        Long max = DEFAULT_MAX;
        Integer threads = DEFAULT_THREAD_COUNT;

        if (m.containsKey(COMMAND_MAX)) {
            try {
                max = Long.parseLong(m.get(COMMAND_MAX));
            } catch(NumberFormatException e) {
                System.err.println("Error: " + m.get(COMMAND_MAX) +
                        ": not an integer.");
                System.exit(1);
            }
        }

        if (m.containsKey(COMMAND_THREADS)) {
            try {
                threads = Integer.parseInt(m.get(COMMAND_THREADS));
            } catch(NumberFormatException e) {
                System.err.println("Error: " + m.get(COMMAND_THREADS) +
                        ": not an integer.");
                System.exit(1);
            }
        }

        if (m.containsKey(COMMAND_UNKNOWN)) {
            System.err.println("Error: unknown command: "
                    + m.get(COMMAND_UNKNOWN));
            System.exit(1);
        }

        if (m.containsKey(COMMAND_DUMP)) {
            // Do DB dumping to stdout.
            System.exit(dump(m.containsKey(COMMAND_FILE) ?
                    m.get(COMMAND_FILE) :
                    DEFAULT_DB_FILE));
        }

        // Try doing crawling.
        System.out.println(
                "Going to crawl at most " + max + " authors in " +
                threads + " threads."
                );

        CrawlerThread<?>[] crawlers = crawl(
                m.containsKey(COMMAND_FILE) ?
                    m.get(COMMAND_FILE) :
                    DEFAULT_DB_FILE,
                max,
                threads
                );

        if (crawlers == null) {
            System.exit(1);
        }

        long total = 0;

        for (CrawlerThread<?> t : crawlers) {
            try {
                t.join();
            } catch(InterruptedException e) {

            }

            total += t.getCrawlCount();
        }

        System.out.println("DONE: Crawled for " + total + " author nodes.");
        System.exit(0);
        //Runtime.getRuntime().addShutdownHook(new App(crawlers));
    }

    private static int dump(String dbFilename) {
        CollaborationGraphDB<AuthorNode> db = getDB(dbFilename);

        for (String s : db.listAllBibtexReferences()) {
            System.out.println(s);
            System.out.println();
        }

        return 0;
    }

    private static CrawlerThread<?>[]
            crawl(String dbFilename,
                  long max,
                  int threads) {
        if (max < 1) {
            return null;
        }

        if (threads < 1) {
            threads = 1;
        }

        CollaborationGraphDB<AuthorNode> db = getDB(dbFilename);

        if (db == null) {
            System.err.println("Error: cannot open the database at file \""
                    + dbFilename + "\"");
            return null;
        }

        SeedFactory<AuthorNode> seedFactory = new DefaultSeedFactory(db);
        return beginCrawl(max, threads, db, seedFactory);
    }

    private static CollaborationGraphDB<AuthorNode> getDB(String dbFilename) {
        try {
            return new CollaborationSQLiteDB(dbFilename);
        } catch(Exception e) {
            System.err.println(e);
            return null;
        }
    }

    private static CrawlerThread<?>[]
            beginCrawl(long maxNodes,
                       int threads,
                       CollaborationGraphDB<AuthorNode> db,
                       SeedFactory<AuthorNode> seedFactory) {
        List<AuthorNode> seedList = seedFactory.get(threads);
        CrawlerThread<?>[] crawlers = new CrawlerThread<?>[threads];
        ThreadSafeSet<AuthorNode> visitedSet = new ThreadSafeSet<AuthorNode>();

        for (int i = 0; i < threads; ++i) {
            List<AuthorNode> arg1 = new ArrayList<AuthorNode>();
            arg1.add(seedList.get(i));
            crawlers[i] = new CrawlerThread<AuthorNode>(
                    arg1,
                    visitedSet,
                    maxNodes
                    );
        }

        for (CrawlerThread<?> c : crawlers) {
            c.start();
        }

        return crawlers;
    }

//    private static int doCrawl(BibTexSQLiteDB db, WebDriver wd, long max) {
//        wd.get(URL_BASE + "/" + Magic.URL_JOURNAL_LIST_PAGE);
//        List<WebElement> elems = wd.findElements(
//                By.xpath("html/body/div/table/tbody/tr/td[2]/a")
//                );
//
//        for (WebElement we : elems) {
//            System.out.println(we.getText());
//            HtmlUnitDriver journalDriver = new HtmlUnitDriver(true);
//            journalDriver.get(we.getAttribute("href"));
//
//            WebDriverWait wait = new WebDriverWait(journalDriver, 60);
//            wait.until(ExpectedConditions
//                       .presenceOfElementLocated(By.id("toShowTop10")));
//
//            max -= crawlJournal(db, journalDriver, max);
//
//            if (max <= 0) {
//                return 0;
//            }
//        }
//
//        return 0;
//    }

    private static long crawlJournal(BibTexSQLiteDB db, WebDriver wd, long max) {
        if (max < 1) {
            return 0;
        }

        List<WebElement> aElements = wd.findElements(By.xpath(
                "//div[@id='toShowTop10']/ol/li/a"));

        long crawled = 0;

        if (aElements.size() > 0) {
            for (WebElement we : aElements) {
                processPaperPage(we.getAttribute("href"), db);
                ++crawled;

                if (crawled >= max) {
                    return crawled;
                }
            }
        }

        return 0;
    }

    private static void processPaperPage(String url, BibTexSQLiteDB db) {
        HtmlUnitDriver driver = new HtmlUnitDriver(true); // enable JS.
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[text()=\"BibTeX\"]")
                ));

        WebElement a = driver.findElement(By.xpath("//a[text()=\"BibTeX\"]"));
        String href = a.getAttribute("href");
        String bibtexStr = "bibtex";

        int i1 = href.indexOf("exportformats.cfm?id=");

        if (i1 >= 0) {
            int i2 = href.indexOf(bibtexStr);

            if (i2 >= 0 && i2 > i1) {
                String bibtexUrl = URL_BASE + "/" +
                        href.substring(i1, i2 + bibtexStr.length());

                getBibTeXReference(bibtexUrl, db);
            }
        }
    }

    public static void getBibTeXReference(String url, BibTexSQLiteDB db) {
        WebDriver driver = new HtmlUnitDriver();
        driver.get(url);
        WebElement pre = driver.findElement(By.tagName("pre"));

        if (pre == null) {
            return;
        }

        String ref = pre.getText();
        String name = parseNameOfBibtexRefText(ref);

        if (!db.contains(name)) {
            db.add(name, ref);
            System.out.println("Added: " + ref);
        }
    }

    private static String parseNameOfBibtexRefText(String s) {
        int i1 = s.indexOf("{");

        if (i1 < 0) {
            return null;
        }

        int i2 = s.indexOf(",");

        if (i2 < 0 || i1 > i2) {
            return null;
        }

        String name = s.substring(i1, i2);
        return name.isEmpty() ? null : name;
    }

    private static Map<String, String> processCommandLine(String... args) {
        Map<String, String> m = new TreeMap<String, String>();

        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals(COMMAND_DUMP)) {
                m.put(COMMAND_DUMP, null);
            } else if (args[i].equals(COMMAND_MAX)) {
                if (i + 1 < args.length) {
                    m.put(COMMAND_MAX, args[i + 1]);
                    i++; // omit the parameter.
                } else {
                    m.put(COMMAND_MAX, null);
                }
            } else if (args[i].equals(COMMAND_FILE)) {
                if (i + 1 < args.length) {
                    m.put(COMMAND_FILE, args[i + 1]);
                    ++i; // omit the parameter.
                } else {
                    m.put(COMMAND_FILE, null);
                }
            } else if (args[i].equals(COMMAND_THREADS)) {
                if (i + 1 < args.length) {
                    m.put(COMMAND_THREADS, args[i + 1]);
                    ++i; // omit the parameter.
                } else {
                    m.put(COMMAND_THREADS, null);
                }
            } else {
                m.put(COMMAND_UNKNOWN, args[i]);
                return m;
            }
        }

        return m;
    }
}
