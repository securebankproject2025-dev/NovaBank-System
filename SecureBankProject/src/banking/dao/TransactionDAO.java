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
 * Handles all financial operations: deposits, withdrawals, transfers.
 *
 * Key concept — ACID Compliance:
 * Every operation that touches money uses explicit transaction management.
 * Disable auto-commit, run all steps, then commit if everything worked,
 * or roll back if anything failed.
 *
 * Author: Emmanuel (ACID reference by David)
 */
public class TransactionDAO {

    // These IDs match the transaction_type table in the database.
    private static final int TYPE_DEPOSIT    = 1;
    private static final int TYPE_WITHDRAWAL = 2;
    private static final int TYPE_TRANSFER   = 3;

    private final AccountDAO accountDAO = new AccountDAO();

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }


    // ============================================================
    // DEPOSIT
    // ============================================================

    public boolean deposit(int accountId, BigDecimal amount, String description) {

        Connection conn = getConn();

        try {
            // Turning off auto-commit so we can group the balance update
            // and the ledger insert together. If one fails, we roll back
            // both to prevent money appearing without a record.
            conn.setAutoCommit(false);

            Optional<Account> accountOpt = accountDAO.findById(accountId);

            if (accountOpt.isEmpty() || !accountOpt.get().isActive()) {
                System.err.println("ERROR: Account not found or is closed.");
                conn.rollback();
                return false;
            }

            Account account = accountOpt.get();

            // Using BigDecimal.add() for exact arithmetic — never use
            // double here because floating point loses precision with money.
            BigDecimal newBalance = account.getBalance().add(amount);

            // Pass our connection to George's method so that this update
            // is part of OUR transaction, not a separate one.
            boolean balanceUpdated = accountDAO.updateBalance(accountId, newBalance, conn);

            if (!balanceUpdated) {
                conn.rollback();
                return false;
            }

            insertTransaction(conn, accountId, TYPE_DEPOSIT, amount, null, description);

            conn.commit();
            return true;

        } catch (SQLException e) {
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
    // TRANSFER
    // ============================================================

    public boolean transfer(int fromAccountId, int toAccountId,
                            BigDecimal amount, String description) {

        if (fromAccountId == toAccountId) {
            System.err.println("ERROR: Source and destination cannot be the same account.");
            return false;
        }

        Connection conn = getConn();

        try {
            // Turning off auto-commit so we can group the withdrawal
            // and deposit together. If one fails, we roll back both
            // to prevent losing money.
            conn.setAutoCommit(false);

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

            // compareTo returns negative if balance is less than amount.
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                System.err.println("ERROR: Insufficient funds.");
                System.err.println("  Available: R" + fromAccount.getBalance());
                System.err.println("  Requested: R" + amount);
                conn.rollback();
                return false;
            }

            BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
            boolean debitOk = accountDAO.updateBalance(fromAccountId, newFromBalance, conn);

            BigDecimal newToBalance = toAccount.getBalance().add(amount);
            boolean creditOk = accountDAO.updateBalance(toAccountId, newToBalance, conn);

            if (!debitOk || !creditOk) {
                conn.rollback();
                return false;
            }

            insertTransaction(conn, fromAccountId, TYPE_TRANSFER,
                              amount, toAccountId, description);

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

        // Using PreparedStatement to prevent SQL injection.
        // All values go in as parameters — never concatenated.
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, typeId);
            ps.setBigDecimal(3, amount);

            // transfer_to_account_id is null for deposits and withdrawals.
            // We must use setNull with the SQL type — passing null directly causes errors.
            if (transferToAccountId != null) {
                ps.setInt(4, transferToAccountId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, description);
            ps.executeUpdate();
        }
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("WARN: Rollback failed: " + e.getMessage());
        }
    }

    private void resetAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("WARN: Could not reset auto-commit: " + e.getMessage());
        }
    }
}