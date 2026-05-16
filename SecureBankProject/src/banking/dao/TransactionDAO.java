package banking.dao;

import banking.config.DatabaseConnection;
import banking.model.Account;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * TransactionDAO.java
 *
 * Handles all financial operations: deposits, withdrawals, and transfers.
 *
 * Key concept — ACID Compliance:
 * Every operation that touches money uses explicit transaction management.
 * This means: disable auto-commit, run all the steps, then commit if
 * everything worked, or roll back if anything failed.
 * This prevents money from disappearing into a half-finished state.
 *
 * Author: Emmanuel
 */
public class TransactionDAO {

    // These IDs match the transaction_type table in the database.
    // Using constants so we never accidentally type the wrong number.
    private static final int TYPE_DEPOSIT    = 1;
    private static final int TYPE_WITHDRAWAL = 2;
    private static final int TYPE_TRANSFER   = 3;

    // We use AccountDAO to read and update balances.
    // TransactionDAO never writes to the account table directly.
    private final AccountDAO accountDAO = new AccountDAO();

    // Convenience method to keep code clean
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }


    // ============================================================
    // DEPOSIT
    // Credits an amount to a single account.
    // ============================================================
    public boolean deposit(int accountId, BigDecimal amount, String description) {

        Connection conn = getConn();

        try {
            // Turning off auto-commit so we can group the balance update
            // and the ledger insert together. If one fails, we roll back
            // both to prevent money appearing without a record.
            conn.setAutoCommit(false);

            // Step 1: Make sure the account exists and is open
            Optional<Account> accountOpt = accountDAO.findById(accountId);

            if (accountOpt.isEmpty() || !accountOpt.get().isActive()) {
                System.err.println("ERROR: Account not found or is closed.");
                conn.rollback();
                return false;
            }

            Account account = accountOpt.get();

            // Step 2: Add the deposit amount to the current balance
            // Using BigDecimal.add() for exact arithmetic — never use
            // double here because floating point loses precision with money
            BigDecimal newBalance = account.getBalance().add(amount);

            // Step 3: Write the new balance to the database
            // We pass our connection (conn) to George's method so that
            // this update is part of OUR transaction, not a separate one
            boolean balanceUpdated = accountDAO.updateBalance(accountId, newBalance, conn);

            if (!balanceUpdated) {
                conn.rollback();
                return false;
            }

            // Step 4: Write the transaction record to the ledger
            insertTransaction(conn, accountId, TYPE_DEPOSIT, amount, null, description);

            // Step 5: Both steps succeeded — make it permanent
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Something went wrong — undo everything
            rollbackQuietly(conn);
            System.err.println("ERROR - deposit failed: " + e.getMessage());

        } finally {
            // Always restore auto-commit whether we succeeded or failed.
            // The connection is shared — leaving it in manual-commit mode
            // would break every other database call that follows.
            resetAutoCommit(conn);
        }

        return false;
    }


    // ============================================================
    // WITHDRAWAL
    // Debits an amount from a single account.
    // ============================================================
    public boolean withdraw(int accountId, BigDecimal amount, String description) {

        Connection conn = getConn();

        try {
            // Turning off auto-commit so the balance update and ledger
            // insert happen together — if one fails we roll back both
            conn.setAutoCommit(false);

            Optional<Account> accountOpt = accountDAO.findById(accountId);

            if (accountOpt.isEmpty() || !accountOpt.get().isActive()) {
                System.err.println("ERROR: Account not found or is closed.");
                conn.rollback();
                return false;
            }

            Account account = accountOpt.get();

            // Check the account has enough money before withdrawing
            // compareTo returns negative if balance is less than amount
            if (account.getBalance().compareTo(amount) < 0) {
                System.err.println("ERROR: Insufficient funds.");
                System.err.println("  Available: R" + account.getBalance());
                System.err.println("  Requested: R" + amount);
                conn.rollback();
                return false;
            }

            // Subtract the amount — using BigDecimal for exact arithmetic
            BigDecimal newBalance = account.getBalance().subtract(amount);

            boolean balanceUpdated = accountDAO.updateBalance(accountId, newBalance, conn);

            if (!balanceUpdated) {
                conn.rollback();
                return false;
            }

            insertTransaction(conn, accountId, TYPE_WITHDRAWAL, amount, null, description);

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            System.err.println("ERROR - withdrawal failed: " + e.getMessage());

        } finally {
            resetAutoCommit(conn);
        }

        return false;
    }


    // ============================================================
    // TRANSFER
    // Moves money from one account to another atomically.
    // Both the debit and credit happen in ONE database transaction.
    // ============================================================
    public boolean transfer(int fromAccountId, int toAccountId,
                            BigDecimal amount, String description) {

        // Guard: you cannot transfer money to the same account
        if (fromAccountId == toAccountId) {
            System.err.println("ERROR: Source and destination cannot be the same account.");
            return false;
        }

        Connection conn = getConn();

        try {
            // Turning off auto-commit so we can group the withdrawal and deposit
            // together. If one fails, we roll back both to prevent losing money.
            conn.setAutoCommit(false);

            // Step 1: Load both accounts and verify they are open
            Optional<Account> fromOpt = accountDAO.findById(fromAccountId);
            Optional<Account> toOpt   = accountDAO.findById(toAccountId);

            if (fromOpt.isEmpty() || !fromOpt.get().isActive()) {
                System.err.println("ERROR: Source account not found or closed.");
                conn.rollback();
                return false;
            }

            if (toOpt.isEmpty() || !toOpt.get().isActive()) {
                System.err.println("ERROR: Destination account not found or closed.");
                conn.rollback();
                return false;
            }

            Account fromAccount = fromOpt.get();
            Account toAccount   = toOpt.get();

            // Step 2: Check the source account has enough money
            // compareTo returns negative if balance is less than amount
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                System.err.println("ERROR: Insufficient funds.");
                System.err.println("  Available: R" + fromAccount.getBalance());
                System.err.println("  Requested: R" + amount);
                conn.rollback();
                return false;
            }

            // Step 3: Debit the source account
            BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
            boolean debitOk = accountDAO.updateBalance(fromAccountId, newFromBalance, conn);

            // Step 4: Credit the destination account
            BigDecimal newToBalance = toAccount.getBalance().add(amount);
            boolean creditOk = accountDAO.updateBalance(toAccountId, newToBalance, conn);

            if (!debitOk || !creditOk) {
                // One of the balance updates failed — undo everything
                conn.rollback();
                return false;
            }

            // Step 5: Record the transfer in the ledger
            // We store the destination account ID so we can trace where money went
            insertTransaction(conn, fromAccountId, TYPE_TRANSFER,
                              amount, toAccountId, description);

            // Step 6: All steps passed — commit permanently
            conn.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            System.err.println("ERROR - transfer failed: " + e.getMessage());

        } finally {
            resetAutoCommit(conn);
        }

        return false;
    }


    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    // Writes one record to the transaction ledger table.
    // Called by deposit, withdraw and transfer after balance updates succeed.
    private void insertTransaction(Connection conn,
                                   int accountId,
                                   int typeId,
                                   BigDecimal amount,
                                   Integer transferToAccountId,
                                   String description) throws SQLException {

        String sql = "INSERT INTO transaction "
                   + "(account_id, transaction_type_id, amount, "
                   + " transfer_to_account_id, description) "
                   + "VALUES (?, ?, ?, ?, ?)";

        // Using PreparedStatement to prevent SQL injection
        // All values go in as parameters — never concatenated
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, typeId);
            ps.setBigDecimal(3, amount);

            // transfer_to_account_id is null for deposits and withdrawals
            // We must use setNull with the SQL type — passing null directly causes errors
            if (transferToAccountId != null) {
                ps.setInt(4, transferToAccountId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, description);
            ps.executeUpdate();
        }
    }

    // Rolls back the current transaction without throwing a new exception
    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("WARN: Rollback failed: " + e.getMessage());
        }
    }

    // Restores auto-commit to true after a manual transaction.
    // Always called in the finally block so it never gets skipped.
    // Forgetting this leaves the shared connection in manual-commit mode
    // and breaks all subsequent database calls.
    private void resetAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("WARN: Could not reset auto-commit: " + e.getMessage());
        }
    }
}
