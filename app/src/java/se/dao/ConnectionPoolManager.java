package se.dao;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * A class that manages connections to the database. It also has a utility
 * method that close connections, statements and resultsets
 */
public class ConnectionPoolManager {

    private static final String PROPS_FILENAME = "/connection.properties";
    private static String dbUser;
    private static String dbPassword;
    private static String dbURL;
    private static ConnectionPoolManager connectionPoolManager;
    private BasicDataSource ds;

    private ConnectionPoolManager() throws IOException, SQLException, PropertyVetoException {
        if (!readOpenshiftDatabaseProperties()) {
            readLocalDatabaseProperties();
        }
        ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername(dbUser);
        ds.setPassword(dbPassword);
        ds.setUrl(dbURL);

        // the settings below are optional -- dbcp can work with defaults
        ds.setMinIdle(5);
        ds.setMaxIdle(20);
        ds.setMaxOpenPreparedStatements(180);

    }

    private static boolean readOpenshiftDatabaseProperties() {
        // grab environment variable
        String host = System.getenv("OPENSHIFT_MYSQL_DB_HOST");

        if (host == null) {
            return false;
        }
        // this is production environment
        // obtain database connection properties from environment variables
        String port = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
        String dbName = System.getenv("OPENSHIFT_APP_NAME");
        dbUser = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
        dbPassword = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");

        dbURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        return true;
    }

    private static void readLocalDatabaseProperties() {
        try {
            // Retrieve properties from connection.properties via the CLASSPATH
            // WEB-INF/classes is on the CLASSPATH
            InputStream is = ConnectionPoolManager.class.getResourceAsStream(PROPS_FILENAME);
            Properties props = new Properties();
            props.load(is);

            // load database connection details
            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String dbName = props.getProperty("db.name");
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");

            dbURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        } catch (Exception ex) {
            // unable to load properties file
            String message = "Unable to load '" + PROPS_FILENAME + "'.";

            System.out.println(message);
            Logger.getLogger(ConnectionPoolManager.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        }
    }

    /**
     *
     * @return ConnectionPoolManager an instance of ConnectionPoolManager
     * @throws IOException when there are input or output errors
     * @throws SQLException sql errors
     * @throws PropertyVetoException when a proposed change to a property represents an unacceptable value.
     */
    public static ConnectionPoolManager getInstance() throws IOException, SQLException, PropertyVetoException {
        if (connectionPoolManager == null) {
            connectionPoolManager = new ConnectionPoolManager();
            return connectionPoolManager;
        } else {
            return connectionPoolManager;
        }
    }

    /**
     *
     * @return Connection
     * @throws SQLException sql errors
     */
    public Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }
    
    /**
   * close the given connection, statement and resultset
   *
   * @param conn the connection object to be closed
   * @param stmt the statement object to be closed
   * @param rs the resultset object to be closed
   */
  public static void close(Connection conn, Statement stmt, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException ex) {
      Logger.getLogger(ConnectionPoolManager.class.getName()).log(Level.WARNING,
              "Unable to close ResultSet", ex);
    }
    try {
      if (stmt != null) {
        stmt.close();
      }
    } catch (SQLException ex) {
      Logger.getLogger(ConnectionPoolManager.class.getName()).log(Level.WARNING,
              "Unable to close Statement", ex);
    }
    try {
      if (conn != null) {
        conn.close();
      }
    } catch (SQLException ex) {
      Logger.getLogger(ConnectionPoolManager.class.getName()).log(Level.WARNING,
              "Unable to close Connection", ex);
    }
  }
  
    /**
     *
     * @param conn Connection
     * @param stmt PreparedStatement
     */
    public static void close(Connection conn, Statement stmt) {
    close(conn, stmt, null);
  }
  
    /**
     *
     * @param conn Connection
     */
    public static void close(Connection conn) {
    close(conn, null, null);
  }
}


