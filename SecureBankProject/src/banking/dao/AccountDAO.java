package banking.dao;

import banking.config.DatabaseConnection;
import banking.model.Account;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AccountDAO.java
 *
 * Handles all database operations for bank accounts.
 * Includes creating, finding, viewing and closing accounts.
 *
 * Author: George
 */
public class AccountDAO {

    // Accessing the shared database connection foundation
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Generates the next account number in format ACC0000001
    private String generateAccountNumber() {
        String sql = "SELECT COALESCE(MAX(account_id), 0) + 1 AS next_id FROM account";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("ACC%07d", rs.getInt("next_id"));
            }
        } catch (SQLException e) {
            System.err.println("Account number generation error: " + e.getMessage());
        }
        return "ACC0000001";
    }

    // Opens a new account for a customer with an auto-generated account number
    public boolean createAccount(int customerId, int typeId, BigDecimal initialDeposit) {
        // Generate account number using next available ID to prevent NOT NULL constraint error
        String accountNumber = generateAccountNumber();

        // Using PreparedStatement here to prevent SQL injection and secure our marks
        String sql = "INSERT INTO account (account_number, customer_id, account_type_id, balance) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setInt(2, customerId);
            ps.setInt(3, typeId);
            ps.setBigDecimal(4, initialDeposit); // BigDecimal for balance - never use double for money
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Create account error: " + e.getMessage());
            return false;
        }
    }

    // Finds a specific account by its ID
    // Returns Optional so the caller is forced to check if account exists
    public Optional<Account> findById(int accountId) {
        // Using PreparedStatement - account ID is user-supplied so must be a parameter
        String sql = "SELECT a.*, at.type_name FROM account a "
                   + "JOIN account_type at ON a.account_type_id = at.account_type_id "
                   + "WHERE a.account_id = ? AND a.is_active = 1";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setAccountNumber(rs.getString("account_number"));
                    acc.setBalance(rs.getBigDecimal("balance"));
                    acc.setAccountTypeName(rs.getString("type_name"));
                    acc.setActive(rs.getBoolean("is_active"));
                    return Optional.of(acc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return Optional.empty();
    }

    // Returns all active accounts for a given customer
    public List<Account> getAccountsByCustomer(int customerId) {
        List<Account> accounts = new ArrayList<>();

        // Joining account_type so we get the type name in one query
        String sql = "SELECT a.*, at.type_name FROM account a "
                   + "JOIN account_type at ON a.account_type_id = at.account_type_id "
                   + "WHERE a.customer_id = ? AND a.is_active = 1";

        // Using PreparedStatement to prevent SQL injection - customer_id is user-supplied
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setAccountNumber(rs.getString("account_number"));
                    acc.setBalance(rs.getBigDecimal("balance"));
                    acc.setAccountTypeName(rs.getString("type_name"));
                    acc.setActive(rs.getBoolean("is_active"));
                    accounts.add(acc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching accounts: " + e.getMessage());
        }
        return accounts;
    }

    // Soft-delete: we set is_active = 0 instead of deleting the row
    // Financial records must never be hard-deleted - we need them for the audit trail
    public boolean closeAccount(int accountId) {
        String sql = "UPDATE account SET is_active = 0, closed_at = CURRENT_TIMESTAMP "
                   + "WHERE account_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error closing account: " + e.getMessage());
            return false;
        }
    }

    // CRITICAL: accepts external Connection so Emmanuel's ACID transaction works
    // DO NOT call DatabaseConnection.getInstance() in here - must use the same connection
    public boolean updateBalance(int accountId, BigDecimal newBalance, Connection conn) {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ? AND is_active = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() == 1; // returns true if exactly one row was updated
        } catch (SQLException e) {
            System.err.println("Balance update error: " + e.getMessage());
            return false;
        }
    }
}
