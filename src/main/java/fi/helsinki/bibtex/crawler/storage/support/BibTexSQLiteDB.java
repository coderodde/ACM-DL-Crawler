package fi.helsinki.bibtex.crawler.storage.support;

import fi.helsinki.bibtex.crawler.storage.BibTexDB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class implements <code>DB</code>-interface by means of a SQLite-
 * database system.
 */
public class BibTexSQLiteDB implements BibTexDB {
    private Connection conn;

    /**
     * Constructs a new DB-implementation using SQLite.
     *
     * @param filename the database file.
     */
    public BibTexSQLiteDB(String filename) throws ClassNotFoundException,
                                            SQLException {
        Class.forName("org.sqlite.JDBC");
        open(filename);

        /* Initalize the opened DB if uninitialized. */
        if (!checkDB()) {
            initialize();
        }
    }

    /**
     * Add a new BibTeX-reference.
     *
     * @param name the name of the reference; must be unique.
     * @param bibtexRef the actual text of a BibTeX-reference.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */
    @Override
    public boolean add(String name, String bibtexRef) {
        if (bibtexRef == null) {
            throw new IllegalArgumentException("Entry may not be null.");
        }

        try {
            PreparedStatement st = conn.prepareStatement(
                "INSERT INTO ReferenceTable (name, reftext) VALUES (?, ?)");
            st.setString(1, name);
            st.setString(2, bibtexRef);
            st.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    /**
     * Checks whether this <code>DB</code> contains a reference with the name
     * <code>name</code>.
     *
     * @param name the name of a reference to query.
     *
     * @return <code>true</code> if and only if a reference with name
     * <code>name</code> is present in this database.
     */
    @Override
    public boolean contains(String name) {
        try {
            PreparedStatement st = conn.prepareStatement(
                    "SELECT name FROM ReferenceTable WHERE name = ?");
            st.setString(1, name);
            return st.executeQuery().next();
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    /**
     * Returns an iterator object of this <code>DB</code>.
     *
     * @return a new iterator of this database.
     */
    @Override
    public Iterator<String> iterator() {
        ResultSet rs = null;

        try {
            Statement st = conn.createStatement();
            rs = st.executeQuery("SELECT reftext FROM ReferenceTable;");
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }

        return new DBIterator(rs);
    }

    /**
     * Opens a connection in this <code>DB</code>.
     *
     * @param filename the filename or path for the binary database file.
     */
    private void open(String filename) throws SQLException {
        if (filename == null) {
            throw new IllegalArgumentException("Filename may not be null");
        }

        conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
        conn.setAutoCommit(false);
    }

    /**
     * Check whether this <code>DB</code>-implementation is initialized.
     */
    private boolean checkDB() {
        try {
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM ReferenceTable;");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Creates the tables in this <code>DB</code>.
     */
    private void initialize() throws SQLException {
        conn.createStatement().executeUpdate(
            "CREATE TABLE ReferenceTable(" +
            "    name TEXT NOT NULL PRIMARY KEY," +
            "    reftext TEXT NOT NULL" +
            ");");
        conn.commit();
    }

    /**
     * This class comprises the iterator object for <code>SQLiteDB</code>.
     */
    private class DBIterator implements Iterator<String> {
        private boolean nextp = true;
        private ResultSet rs;

        DBIterator(ResultSet rs) {
            try {
                nextp = rs.next();
            } catch (SQLException ex) {
                nextp = false;
            }

            this.rs = rs;
        }

        public boolean hasNext() {
            return nextp;
        }

        public String next() {
            if (!nextp) {
                throw new NoSuchElementException("Iteration overflow.");
            }

            try {
                String bibtexRef = rs.getString("reftext");
                nextp = rs.next();
                return bibtexRef;
            } catch (SQLException e) {
                nextp = false;
            }

            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "Iterator's remove() not implemented."
                    );
        }
    }
}
