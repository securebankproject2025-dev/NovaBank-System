package banking.model;

import java.math.BigDecimal;

/**
 * Transaction.java
 *
 * Represents a single financial transaction in the system.
 * This class mirrors the 'transaction' table in our database.
 * Transactions are immutable records — once created they
 * are never updated or deleted. This is our audit trail.
 *
 * Author: David
 */
public class Transaction {

    private int        transactionId;
    private int        accountId;
    private int        transactionTypeId;
    private String     transactionTypeName;
    private BigDecimal amount;
    private Integer    transferToAccountId;
    private String     description;
    private String     transactedAt;

    // Default constructor — needed when building object from database results
    public Transaction() { }

    // ── Getters and Setters ──────────────────────────────────────

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(int transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Integer instead of int because this can be null
    // null means it was not a transfer
    public Integer getTransferToAccountId() {
        return transferToAccountId;
    }

    public void setTransferToAccountId(Integer transferToAccountId) {
        this.transferToAccountId = transferToAccountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactedAt() {
        return transactedAt;
    }

    public void setTransactedAt(String transactedAt) {
        this.transactedAt = transactedAt;
    }

    // Useful for printing transaction details to the CLI
    @Override
    public String toString() {
        return "Transaction ID : " + transactionId + "\n"
             + "Type           : " + transactionTypeName + "\n"
             + "Amount         : R" + amount + "\n"
             + "Description    : " + description + "\n"
             + "Date           : " + transactedAt;
    }
}