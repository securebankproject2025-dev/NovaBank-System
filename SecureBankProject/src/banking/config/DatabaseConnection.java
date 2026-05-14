package banking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection.java
 *
 * Manages a single shared connection to the MySQL database.
 * We use the Singleton design pattern here so that only ONE
 * connection is ever open at a time. This prevents resource
 * exhaustion and keeps the application lightweight.
 *
 * Author: David
 */
public class DatabaseConnection {

    // The database URL tells JDBC where to find our MySQL server.
    // useSSL=false is fine for a local development environment.
    // serverTimezone avoids a common MySQL 8 timezone error.
    // allowPublicKeyRetrieval=true is needed for MySQL 8 authentication.
    private static final String URL =
        "jdbc:mysql://localhost:3306/banking_system"
        + "?useSSL=false&serverTimezone=Africa/Johannesburg&allowPublicKeyRetrieval=true";

    // Standard database credentials for local development.
    private static final String USERNAME = "root";
    private static final String PASSWORD = "SecureBank@2025!";

    // The single shared instance — starts as null until first use.
    private static DatabaseConnection instance = null;

    // The actual JDBC connection object we reuse across the app.
    private Connection connection;

    // Private constructor — no one outside this class can create
    // a new DatabaseConnection. They must go through getInstance().
    private DatabaseConnection() {
        try {
            // Explicitly load the MySQL JDBC driver.
            // This is needed to ensure the driver registers itself
            // with the DriverManager before we connect.
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open the connection using our credentials above.
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (ClassNotFoundException e) {
            // This happens if the MySQL JAR is not on the classpath.
            // Fix: add mysql-connector-j.jar to NetBeans Libraries.
            System.err.println("ERROR: MySQL driver not found.");
            System.err.println("Make sure mysql-connector-j.jar is in your project Libraries.");
            throw new RuntimeException(e);

        } catch (SQLException e) {
            // This happens if MySQL is not running or credentials are wrong.
            System.err.println("ERROR: Cannot connect to the database.");
            System.err.println("Details: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the single shared instance of DatabaseConnection.
     * Creates it on the first call, then reuses it every time after.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the live JDBC Connection object.
     * If the connection dropped (MySQL timeout), it reconnects.
     */
    public Connection getConnection() {
        try {
            // isValid(2) pings the database with a 2-second timeout.
            // If the connection is stale, we open a fresh one.
            if (connection == null || !connection.isValid(2)) {
                System.out.println("INFO: Reconnecting to database...");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Reconnection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return connection;
    }

    /**
     * Cleanly closes the database connection.
     * Call this once when the application is exiting.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("INFO: Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("WARN: Error closing connection: " + e.getMessage());
        }
    }
}