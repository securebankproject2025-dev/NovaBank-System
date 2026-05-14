package banking.config;

import java.sql.Connection;

/**
 * ConnectionTest.java
 *
 * Sanity check — run this once to confirm the database is
 * reachable before the team starts connecting.
 *
 * DELETE THIS FILE before pushing to GitHub.
 */
public class ConnectionTest {

    public static void main(String[] args) {

        System.out.println("Attempting to connect to MySQL...");
        System.out.println("  Host     : localhost:3306");
        System.out.println("  Database : banking_system");
        System.out.println("  User     : root");
        System.out.println("");

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            if (conn != null && conn.isValid(3)) {
                System.out.println("==================================");
                System.out.println("  Connection Successful!          ");
                System.out.println("  MySQL is running and reachable. ");
                System.out.println("==================================");
            } else {
                System.out.println("FAIL: Connection object is null or invalid.");
            }

        } catch (Exception e) {
            System.out.println("===== CONNECTION FAILED ============");
            System.out.println("Error: " + e.getMessage());
            System.out.println("");
            System.out.println("Common causes:");
            System.out.println("  1. MySQL Server is not running.");
            System.out.println("     Fix: Open Services, start MySQL80");
            System.out.println("");
            System.out.println("  2. Wrong password in DatabaseConnection.java");
            System.out.println("     Fix: Update the PASSWORD constant.");
            System.out.println("");
            System.out.println("  3. MySQL JAR not in project Libraries.");
            System.out.println("     Fix: Project Properties > Libraries > Add JAR");
            System.out.println("");
            System.out.println("  4. banking_system database does not exist yet.");
            System.out.println("     Fix: Run banking_system.sql in MySQL Workbench first.");
            System.out.println("====================================");
        } finally {
            DatabaseConnection.getInstance().closeConnection();
        }
    }
}