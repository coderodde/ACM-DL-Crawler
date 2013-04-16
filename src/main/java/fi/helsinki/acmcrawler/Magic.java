package fi.helsinki.acmcrawler;

/**
 * Contains the main magic data.
 *
 * @author Rodion Efremov
 * @version I
 */
public class Magic {
    public static final String VERSION = "I";

    public static final String COMMAND_DUMP = "--dump";
    public static final String COMMAND_FILE = "--file";
    public static final String COMMAND_MAX = "--max";
    public static final String COMMAND_UNKNOWN = "unknown";

    public static final String DEFAULT_DB_FILE = "acm_graph.db";
    public static final long   DEFAULT_MAX = Long.MAX_VALUE;
    public static final int    DEFAULT_JAVASCRIPT_WAIT = 90;

    public static final String URL_BASE = "http://dl.acm.org";
    public static final String URL_AUTHOR_PAGE_SCRIPT_NAME = "author_page.cfm";
    public static final String URL_GET_ALL_ARGS = "&perpage=10000&start=1";
    public static final String URL_JOURNAL_LIST_PAGE = "pubs.cfm";


    public static final String HELP_MSG =
            "usage: java ... [" + COMMAND_DUMP + "] [" + COMMAND_MAX + " N] " +
                "[" + COMMAND_FILE + " FILE]\n" +
            "where:\n" +
            "  N        is the maximum amount of nodes to visit, " +
                DEFAULT_MAX + " if omitted\n" +
            "  FILE     is the name of a database file, \"" + DEFAULT_DB_FILE +
                "\" if omitted\n" +
            "  --dump do not crawl, dump the BibTeX data to stdout\n" +
            "         --file may be used to choose the file\n" +
            "         --max  may be used to limit the output";
}
