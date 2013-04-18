/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.acmcrawler.storage.support;

import fi.helsinki.acmcrawler.domain.support.AuthorNode;
import fi.helsinki.acmcrawler.storage.CollaborationGraphDB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author rodionefremov
 */
public class CollaborationSQLiteDB implements CollaborationGraphDB<AuthorNode> {
    private Connection conn;

    /**
     * Constructs a new DB-implementation using SQLite.
     *
     * @param filename the database file.
     */
    public CollaborationSQLiteDB(String filename) throws ClassNotFoundException,
                                            SQLException {
        Class.forName("org.sqlite.JDBC");
        open(filename);

        /* Initalize the opened DB if uninitialized. */
        if (!checkDB()) {
            initialize();
        }
    }

    @Override
    public boolean addAuthor(String id, String name) {
        try {
            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO Authors (id, name) VALUES (?, ?)"
                    );
            st.setString(1, id);
            st.setString(2, name);
            st.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println(
                    "Error: addAuthor(" + id + ", " + name + "): " + e
                    );
            
            return false;
        }
    }

    @Override
    public boolean addPaper(String id, String name) {
        try {
            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO Papers (id, name) VALUES (?, ?)"
                    );
            st.setString(1, id);
            st.setString(2, name);
            st.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println(
                    "Error: addPaper(" + id + ", " + name + "): " + e
                    );

            return false;
        }
    }

    @Override
    public boolean addBibtexToPaper(String id, String bibtex) {
        try {
            PreparedStatement st = conn.prepareStatement(
                    "UPDATE Papers SET bibtex=? WHERE id=?"
                    );
            st.setString(1, bibtex);
            st.setString(2, id);
            st.executeUpdate();
            conn.commit();
            return true;
        } catch(SQLException e) {

            System.err.println(
                    "Error: addBibtexToPaper(" + id + ", " +
                    bibtex + "): "+ e);
            return false;
        }
    }

    @Override
    public boolean associate(String authorId, String paperId) {
        try {
            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO Contribs (authorId, paperId) VALUES (?, ?)"
                    );
            st.setString(1, authorId);
            st.setString(2, paperId);
            st.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println(
                    "Error: associate(" + authorId + ", " + paperId + "): "
                    + e);
            return false;
        }
    }

    @Override
    public List<AuthorNode> listAllAuthors() {
        List<AuthorNode> list = new ArrayList<AuthorNode>();

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Authors;");

            while (rs.next()) {
                AuthorNode node = new AuthorNode(rs.getString("id"));
                node.setName(rs.getString("name"));
                list.add(node);
            }
        } catch(SQLException e) {
            System.err.println(e);
        }

        return list;
    }

    @Override
    public List<String> listAllBibtexReferences() {
        List<String> list = new ArrayList<String>();

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT bibtex FROM Papers " +
                    "WHERE bibtex IS NOT NULL;"
                    );

            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch(SQLException e) {
           System.err.println(e);
        }

        return list;
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
            st.execute("SELECT * FROM Authors LIMIT 1;");
            st.execute("SELECT * FROM Papers LIMIT 1;");
            st.execute("SELECT * FROM Contribs LIMIT 1;");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Creates the tables in this <code>DB</code>.
     */
    private void initialize() throws SQLException {
        Statement st = conn.createStatement();

        st.executeUpdate(
                "CREATE TABLE Authors(" +
                "   id   TEXT NOT NULL PRIMARY KEY," +
                "   name TEXT NOT NULL" +
                ");"
                );

        st.executeUpdate(
                "CREATE TABLE Papers(" +
                "   id   TEXT NOT NULL PRIMARY KEY," +
                "   name TEXT NOT NULL," +
                "   bibtex TEXT" +
                ");"
                );

        st.executeUpdate(
                "CREATE TABLE Contribs(" +
                "   authorId TEXT NOT NULL," +
                "   paperId TEXT NOT NULL," +
                "   PRIMARY KEY (authorId, paperId)," +
                "   FOREIGN KEY (authorId) REFERENCES Authors(id)," +
                "   FOREIGN KEY (paperId)  REFERENCES Papers(id)" +
                ");"
                );

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
