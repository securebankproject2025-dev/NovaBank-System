package banking.model;

import java.math.BigDecimal;

/**
 * Account.java
 *
 * Represents a single bank account in the system.
 * This class mirrors the 'account' table in our database.
 * Balance is stored as BigDecimal never double or float
 * because those lose precision when dealing with money.
 *
 * Author: David
 */
public class Account {

    private int        accountId;
    private String     accountNumber;
    private int        customerId;
    private int        accountTypeId;
    private String     accountTypeName;
    private BigDecimal balance;
    private boolean    isActive;
    private String     openedAt;
    private String     closedAt;

    // Default constructor — needed when building object from database results
    public Account() { }

    // ── Getters and Setters ──────────────────────────────────────

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(int accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    // Useful for printing account details to the CLI
    @Override
    public String toString() {
        return "Account No  : " + accountNumber + "\n"
             + "Type        : " + accountTypeName + "\n"
             + "Balance     : R" + balance + "\n"
             + "Status      : " + (isActive ? "Open" : "Closed");
    }
}