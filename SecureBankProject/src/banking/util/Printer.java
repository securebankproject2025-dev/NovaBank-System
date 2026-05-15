package banking.util;

import banking.model.Account;
import banking.model.Customer;
import banking.model.Transaction;
import java.util.List;

/**
 * Printer.java
 *
 * Handles all display output for the CLI application.
 * Keeping all print formatting in one place means we only
 * need to change it here if we want to update how things look.
 *
 * Author: David
 */
public class Printer {

    // This class is a utility — it should never be instantiated.
    private Printer() { }

    // ── Divider lines for neat CLI output ────────────────────────

    public static void printDivider() {
        System.out.println("========================================");
    }

    public static void printThinDivider() {
        System.out.println("----------------------------------------");
    }

    // ── Customer display ─────────────────────────────────────────

    public static void printCustomer(Customer c) {
        printThinDivider();
        System.out.println("Customer ID : " + c.getCustomerId());
        System.out.println("Name        : " + c.getFullName());
        System.out.println("ID Number   : " + c.getIdNumber());
        System.out.println("Email       : " + c.getEmail());
        System.out.println("Phone       : " + c.getPhone());
        System.out.println("Address     : " + c.getAddress());
        printThinDivider();
    }

    public static void printCustomerList(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        printDivider();
        System.out.println("CUSTOMER LIST");
        printDivider();
        for (Customer c : customers) {
            System.out.println(c.getCustomerId() + ". "
                + c.getFullName()
                + " | " + c.getEmail()
                + " | " + c.getPhone());
        }
        printDivider();
    }

    // ── Account display ──────────────────────────────────────────

    public static void printAccount(Account a) {
        printThinDivider();
        System.out.println("Account No  : " + a.getAccountNumber());
        System.out.println("Type        : " + a.getAccountTypeName());
        System.out.println("Balance     : R" + a.getBalance());
        System.out.println("Status      : " + (a.isActive() ? "Open" : "Closed"));
        printThinDivider();
    }

    public static void printAccountList(List<Account> accounts) {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        printDivider();
        System.out.println("ACCOUNT LIST");
        printDivider();
        for (Account a : accounts) {
            System.out.println(a.getAccountNumber()
                + " | " + a.getAccountTypeName()
                + " | R" + a.getBalance()
                + " | " + (a.isActive() ? "Open" : "Closed"));
        }
        printDivider();
    }

    // ── Success and error messages ───────────────────────────────

    public static void printSuccess(String message) {
        System.out.println("SUCCESS: " + message);
    }

    public static void printError(String message) {
        System.out.println("ERROR: " + message);
    }

    public static void printInfo(String message) {
        System.out.println("INFO: " + message);
    }
}