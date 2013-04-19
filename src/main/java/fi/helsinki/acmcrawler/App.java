package fi.helsinki.acmcrawler;

import static fi.helsinki.acmcrawler.Magic.*;
import fi.helsinki.acmcrawler.domain.SeedFactory;
import fi.helsinki.acmcrawler.domain.support.AuthorNode;
import fi.helsinki.acmcrawler.domain.support.DefaultSeedFactory;
import fi.helsinki.acmcrawler.storage.CollaborationGraphDB;
import fi.helsinki.acmcrawler.storage.support.CollaborationSQLiteDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The top-level code.
 *
 * @author coderodde
 * @version 0.1
 */
public class App extends Thread {

    private static final int SEED_COUNT = 1000;

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

//        long total = 0;
//
//        for (CrawlerThread<?> t : crawlers) {
//            try {
//                t.join();
//            } catch(InterruptedException e) {
//
//            }
//
//            total += t.getCrawlCount();
//        }

        App app = new App(crawlers);
        Runtime.getRuntime().addShutdownHook(app); // Just trying to be funky.
//        System.out.println("DONE: Crawled for " + total + " author nodes.");
//        System.exit(0);
    }

    public static <T> List<List<T>> trySplitEvenly(List<T> l, int parts) {
        List<List<T>> ret = new ArrayList<List<T>>(parts);

        for (int i = 0; i < parts; ++i) {
            ret.add(new ArrayList<T>(l.size() / parts + 1));
        }

        int i = 0;

        for (T element : l) {
            ret.get(i % parts).add(element);
            i++;
        }

        for (int j = ret.size() - 1; j >= 0; --j) {
            if (ret.get(j).isEmpty()) {
                ret.remove(j);
            }
        }

        return ret;
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
        // 3 seeds per each thread.
        List<AuthorNode> seedList =
                seedFactory.get(3 * threads);
                //seedFactory.get(threads);

        List<List<AuthorNode>> seedListPartition
                = trySplitEvenly(seedList, threads);

        if (threads > seedListPartition.size()) {
            System.out.println(
                    "Thread count adjusted from " + threads +
                    " to " + seedListPartition.size()
                    );

            threads = seedListPartition.size();
        }

        CrawlerThread<?>[] crawlers = new CrawlerThread<?>[threads];
        ThreadSafeSet<AuthorNode> visitedSet = new ThreadSafeSet<AuthorNode>();

        for (int i = 0; i < threads; ++i) {
            crawlers[i] = new CrawlerThread<AuthorNode>(
                    seedListPartition.get(i),
                    visitedSet,
                    maxNodes
                    );
        }

        for (CrawlerThread<?> c : crawlers) {
            c.start();
        }

        return crawlers;
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
