package banking.ui;

import java.math.BigDecimal;
import banking.dao.CustomerDAO;
import banking.dao.AccountDAO;
import banking.dao.TransactionDAO;
import banking.util.InputValidator;
import banking.util.Printer;
import java.util.Scanner;
import java.util.Optional;
import java.util.List;
/**
 * BankingCLI.java
 *
 * Main entry point for the NovaBank Banking System.
 * This class controls the main menu and connects all
 * the different menus together in one place.
 * Each team member's menu is called from here.
 *
 * Author: David, Benjamina, George, Emmanuel
 */
public class BankingCLI {

    // Scanner declared once at the top — shared by all menus
    // so we never have two Scanners fighting over user input
    private final Scanner scanner = new Scanner(System.in);

    // DAOs declared once — each menu method uses these
    // to talk to the database
    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // Main method — creates ONE BankingCLI object and runs it
    public static void main(String[] args) {
        BankingCLI app = new BankingCLI();
        app.run();
    }

    // Main loop — keeps the app running until user selects 0
    private void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine();

            if (!InputValidator.isValidMenuChoice(choice, 0, 3)) {
                System.out.println("Invalid option. Try again.");
                continue;
            }

            switch (InputValidator.parseMenuChoice(choice)) {
                case 1 -> showCustomerMenu();    // Benjamina's
                case 2 -> showAccountMenu();     // George's
                case 3 -> showTransactionMenu(); // Emmanuel's
                case 0 -> running = false;
            }
        }
        System.out.println("Thank you for using NovaBank. Goodbye.");
    }

    // Main menu display
    private void printMainMenu() {
        System.out.println("\n========== NovaBank ==========");
        System.out.println("1. Customer Management");
        System.out.println("2. Account Management");
        System.out.println("3. Transactions");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }


    // ============================================================
// CUSTOMER MENU — Benjamina
// ============================================================
private void showCustomerMenu() {
    boolean running = true;
    while (running) {
        System.out.println("\n========== CUSTOMER MENU ==========");
        System.out.println("1. Register New Customer");
        System.out.println("2. View All Customers");
        System.out.println("3. Find Customer by ID");
        System.out.println("4. Update Contact Details");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine();

        if (!InputValidator.isValidMenuChoice(choice, 0, 4)) {
            System.out.println("Invalid choice. Try again.");
            continue;
        }

        switch (InputValidator.parseMenuChoice(choice)) {
            case 1 -> handleRegisterCustomer();
            case 2 -> handleViewAllCustomers();
            case 3 -> handleFindCustomer();
            case 4 -> handleUpdateCustomer();
            case 0 -> running = false;
        }
    }
}

private void handleRegisterCustomer() {
    System.out.println("\n--- Register New Customer ---");

    System.out.print("First Name: ");
    String firstName = scanner.nextLine();

    System.out.print("Last Name: ");
    String lastName = scanner.nextLine();

    System.out.print("ID Number: ");
    String idNumber = scanner.nextLine();

    // Validating SA ID number using Luhn algorithm
    if (!InputValidator.isValidSaIdNumber(idNumber)) {
        System.out.println("Invalid SA ID number. Please check and try again.");
        return;
    }

    System.out.print("Email: ");
    String email = scanner.nextLine();

    if (!InputValidator.isValidEmail(email)) {
        System.out.println("Invalid email address.");
        return;
    }

    System.out.print("Phone: ");
    String phone = scanner.nextLine();

    if (!InputValidator.isValidPhone(phone)) {
        System.out.println("Invalid phone number.");
        return;
    }

    System.out.print("Address: ");
    String address = scanner.nextLine();

    // Build Customer object and register
    banking.model.Customer customer = new banking.model.Customer(
        firstName, lastName, idNumber, email, phone, address
    );

    int newId = customerDAO.registerCustomer(customer);

    if (newId > 0) {
        Printer.printSuccess("Customer registered successfully! ID: " + newId);
    } else {
        Printer.printError("Registration failed. ID number or email may already exist.");
    }
}

private void handleViewAllCustomers() {
    System.out.println("\n--- All Customers ---");
    List<banking.model.Customer> customers = customerDAO.getAllCustomers();

    if (customers.isEmpty()) {
        System.out.println("No customers found.");
        return;
    }

    for (banking.model.Customer c : customers) {
        System.out.println("----------------------------------------");
        System.out.println(c);
    }
    System.out.println("----------------------------------------");
}

private void handleFindCustomer() {
    System.out.print("Enter Customer ID: ");
    String input = scanner.nextLine();

    if (!InputValidator.isValidPositiveInt(input)) {
        System.out.println("Invalid ID.");
        return;
    }

    int id = InputValidator.parsePositiveInt(input);
    Optional<banking.model.Customer> found = customerDAO.findById(id);

    if (found.isPresent()) {
        System.out.println("\n--- Customer Found ---");
        System.out.println(found.get());
    } else {
        System.out.println("No customer found with ID: " + id);
    }
}

private void handleUpdateCustomer() {
    System.out.print("Enter Customer ID: ");
    String input = scanner.nextLine();

    if (!InputValidator.isValidPositiveInt(input)) {
        System.out.println("Invalid ID.");
        return;
    }
    int id = InputValidator.parsePositiveInt(input);

    System.out.print("New Email: ");
    String email = scanner.nextLine();

    if (!InputValidator.isValidEmail(email)) {
        System.out.println("Invalid email address.");
        return;
    }

    System.out.print("New Phone: ");
    String phone = scanner.nextLine();

    if (!InputValidator.isValidPhone(phone)) {
        System.out.println("Invalid phone number.");
        return;
    }

    boolean updated = customerDAO.updateContactDetails(id, email, phone);

    if (updated) {
        Printer.printSuccess("Customer updated successfully.");
    } else {
        Printer.printError("Update failed. Customer ID may not exist.");
    }
}

    // ============================================================
    // ACCOUNT MENU — George
    // ============================================================
    private void showAccountMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== Account Menu ==========");
            System.out.println("1. Open New Account");
            System.out.println("2. View My Accounts");
            System.out.println("3. Close Account");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            if (!InputValidator.isValidMenuChoice(choice, 0, 3)) {
                System.out.println("Invalid option. Try again.");
                continue;
            }

            switch (InputValidator.parseMenuChoice(choice)) {
                case 1 -> handleOpenAccount();
                case 2 -> handleViewAccounts();
                case 3 -> handleCloseAccount();
                case 0 -> running = false;
            }
        }
    }

    private void handleOpenAccount() {
        // Get customer ID
        System.out.print("Enter Customer ID: ");
        String custInput = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(custInput)) {
            System.out.println("Invalid Customer ID.");
            return;
        }
        int customerId = InputValidator.parsePositiveInt(custInput);

        // Show account types
        System.out.println("Account Types:");
        System.out.println("1. Cheque");
        System.out.println("2. Savings");
        System.out.println("3. Fixed Deposit");
        System.out.println("4. Business");
        System.out.println("5. Student");
        System.out.print("Choose type: ");

        String typeInput = scanner.nextLine();
        if (!InputValidator.isValidMenuChoice(typeInput, 1, 5)) {
            System.out.println("Invalid account type.");
            return;
        }
        int typeId = InputValidator.parseMenuChoice(typeInput);

        // Get initial deposit
        System.out.print("Enter initial deposit amount: ");
        String amountInput = scanner.nextLine();

        if (!InputValidator.isValidAmount(amountInput)) {
            System.out.println("Invalid amount.");
            return;
        }
        BigDecimal initialDeposit = InputValidator.parseAmount(amountInput);

        // Create the account
        boolean success = accountDAO.createAccount(customerId, typeId, initialDeposit);
        if (success) {
            Printer.printSuccess("Account opened successfully!");
        } else {
            Printer.printError("Failed to open account. Please try again.");
        }
    }

    private void handleViewAccounts() {
        System.out.print("Enter Customer ID: ");
        String custInput = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(custInput)) {
            System.out.println("Invalid Customer ID.");
            return;
        }
        int customerId = InputValidator.parsePositiveInt(custInput);

        // Get accounts and display them
        Printer.printAccountList(accountDAO.getAccountsByCustomer(customerId));
    }

    private void handleCloseAccount() {
        System.out.print("Enter Account ID to close: ");
        String input = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(input)) {
            System.out.println("Invalid Account ID.");
            return;
        }
        int accountId = InputValidator.parsePositiveInt(input);

        boolean success = accountDAO.closeAccount(accountId);
        if (success) {
            Printer.printSuccess("Account closed successfully.");
        } else {
            Printer.printError("Failed to close account.");
        }
    }


    // ============================================================
    // TRANSACTION MENU — Emmanuel
    // ============================================================
    public void showTransactionMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== Transaction Menu ==========");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            if (!InputValidator.isValidMenuChoice(choice, 0, 3)) {
                System.out.println("Invalid option. Try again.");
                continue;
            }

            switch (InputValidator.parseMenuChoice(choice)) {
                case 1 -> handleDeposit();
                case 2 -> handleWithdraw();
                case 3 -> handleTransfer();
                case 0 -> running = false;
            }
        }
    }

    private void handleDeposit() {
        // Prompt user for account ID
        System.out.print("Enter account ID: ");
        String idInput = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(idInput)) {
            System.out.println("Invalid account ID.");
            return;
        }
        int accountId = InputValidator.parsePositiveInt(idInput);

        // Prompt user for amount and validate it
        System.out.print("Enter amount: ");
        String amountInput = scanner.nextLine();

        // Validate the amount using InputValidator — if invalid, print error and return
        if (!InputValidator.isValidAmount(amountInput)) {
            System.out.println("Invalid amount.");
            return;
        }

        // Convert the valid amount string to BigDecimal
        BigDecimal amount = InputValidator.parseAmount(amountInput);

        // Prompt user for a description
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        // Call transactionDAO.deposit() and store the result
        boolean success = transactionDAO.deposit(accountId, amount, description);

        if (success) {
            Printer.printSuccess("Deposit successful!");
        } else {
            Printer.printError("Deposit failed. Please try again.");
        }
    }

    private void handleWithdraw() {
        // Prompt user for account ID
        System.out.print("Enter account ID: ");
        String idInput = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(idInput)) {
            System.out.println("Invalid account ID.");
            return;
        }
        int accountId = InputValidator.parsePositiveInt(idInput);

        // Prompt user for amount and validate it
        System.out.print("Enter amount: ");
        String amountInput = scanner.nextLine();

        // Validate the amount using InputValidator — if invalid, print error and return
        if (!InputValidator.isValidAmount(amountInput)) {
            System.out.println("Invalid amount.");
            return;
        }

        // Convert the valid amount string to BigDecimal
        BigDecimal amount = InputValidator.parseAmount(amountInput);

        // Prompt user for a description
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        // Call transactionDAO.withdraw() and store the result
        boolean success = transactionDAO.withdraw(accountId, amount, description);

        if (success) {
            Printer.printSuccess("Withdrawal successful!");
        } else {
            Printer.printError("Withdrawal failed. Please try again.");
        }
    }

    private void handleTransfer() {
        // Prompt user for source account ID
        System.out.print("Enter your account ID: ");
        String fromInput = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(fromInput)) {
            System.out.println("Invalid account ID.");
            return;
        }
        int accountId = InputValidator.parsePositiveInt(fromInput);

        // Prompt user for destination account ID
        System.out.print("Enter destination account ID: ");
        String toInput = scanner.nextLine();

        if (!InputValidator.isValidPositiveInt(toInput)) {
            System.out.println("Invalid destination account ID.");
            return;
        }
        int destAccountId = InputValidator.parsePositiveInt(toInput);

        // Prompt user for amount and validate it
        System.out.print("Enter amount: ");
        String amountInput = scanner.nextLine();

        // Validate the amount using InputValidator — if invalid, print error and return
        if (!InputValidator.isValidAmount(amountInput)) {
            System.out.println("Invalid amount.");
            return;
        }

        // Convert the valid amount string to BigDecimal
        BigDecimal amount = InputValidator.parseAmount(amountInput);

        // Prompt user for a description
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        // Call transactionDAO.transfer() with both account IDs
        boolean success = transactionDAO.transfer(accountId, destAccountId, amount, description);

        if (success) {
            Printer.printSuccess("Transfer successful!");
        } else {
            Printer.printError("Transfer failed. Please try again.");
        }
    }
}