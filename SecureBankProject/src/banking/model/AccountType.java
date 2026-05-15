package banking.model;

import java.math.BigDecimal;

/**
 * AccountType.java
 *
 * Represents an account type in the system.
 * This class mirrors the 'account_type table in our database.
 * We separated account types into their own table to follow 3NF —
 * the interest rate belongs to the type, not to each account.
 *
 * Author: David
 */
public class AccountType {

    private int        accountTypeId;
    private String     typeName;
    private BigDecimal interestRate;
    private String     description;

    // Default constructor — needed when building object from database results
    public AccountType() { }

    // Full constructor
    public AccountType(int accountTypeId, String typeName,
                       BigDecimal interestRate, String description) {
        this.accountTypeId = accountTypeId;
        this.typeName      = typeName;
        this.interestRate  = interestRate;
        this.description   = description;
    }

    // ── Getters and Setters ──────────────────────────────────────

    public int getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(int accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Useful for displaying account types in the CLI menu
    @Override
    public String toString() {
        return accountTypeId + ". " + typeName
             + " — Interest: " + interestRate + "%"
             + " | " + description;
    }
}