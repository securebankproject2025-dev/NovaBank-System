package banking.util;

import java.math.BigDecimal;

/**
 * InputValidator.java
 *
 * Central validation class for all user input in the application.
 * Keeping validation in one place means we only have to fix a
 * bug or tighten a rule here — everywhere else just calls these methods.
 *
 * Author: David (Security Lead)
 */
public class InputValidator {

    // This class is a utility — it should never be instantiated.
    // All methods are static so callers don't need to create an object.
    private InputValidator() { }


    // ============================================================
    // NAME VALIDATION
    // ============================================================

    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2
            && trimmed.length() <= 60
            && trimmed.matches("[a-zA-Z\\s'\\-]+");
    }


    // ============================================================
    // SA ID NUMBER VALIDATION
    // ============================================================

    public static boolean isValidSaIdNumber(String idNumber) {
        // First check: must be exactly 13 numeric digits.
        if (idNumber == null || !idNumber.matches("\\d{13}")) {
            return false;
        }
        // Second check: Luhn algorithm verifies the number is valid.
        // Researched the Luhn algorithm to validate SA ID numbers
        // mathematically for extra security points.
        return luhnCheck(idNumber);
    }

    /**
     * The Luhn algorithm checks the validity of a numeric string
     * by calculating a checksum from its digits.
     * It is the same algorithm used to validate credit card numbers.
     */
    private static boolean luhnCheck(String number) {
        int total = 0;
        boolean shouldDouble = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';

            if (shouldDouble) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            total = total + digit;
            shouldDouble = !shouldDouble;
        }

        return total % 10 == 0;
    }


    // ============================================================
    // EMAIL VALIDATION
    // ============================================================

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.trim().matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
            && email.length() <= 120;
    }


    // ============================================================
    // PHONE VALIDATION
    // ============================================================

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.trim().matches("[0-9+\\-() ]{7,15}");
    }


    // ============================================================
    // ACCOUNT NUMBER VALIDATION
    // ============================================================

    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return accountNumber.trim().toUpperCase().matches("ACC\\d{7}");
    }


    // ============================================================
    // MONETARY AMOUNT VALIDATION
    // ============================================================

    /**
     * Validates that a string represents a valid transaction amount.
     * Must be positive, max 2 decimal places, and under R1 000 000.
     * We use BigDecimal for exact decimal parsing — never double.
     */
    public static boolean isValidAmount(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            BigDecimal amount = new BigDecimal(input.trim());

            boolean isPositive   = amount.compareTo(BigDecimal.ZERO) > 0;
            boolean maxTwoPlaces = amount.scale() <= 2;
            boolean withinLimit  = amount.compareTo(new BigDecimal("1000000")) <= 0;

            return isPositive && maxTwoPlaces && withinLimit;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static BigDecimal parseAmount(String input) {
        return new BigDecimal(input.trim()).setScale(2, java.math.RoundingMode.HALF_UP);
    }


    // ============================================================
    // MENU CHOICE VALIDATION
    // ============================================================

    public static boolean isValidMenuChoice(String input, int min, int max) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            int choice = Integer.parseInt(input.trim());
            return choice >= min && choice <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int parseMenuChoice(String input) {
        return Integer.parseInt(input.trim());
    }


    // ============================================================
    // POSITIVE INTEGER (for customer/account IDs)
    // ============================================================

    public static boolean isValidPositiveInt(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(input.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int parsePositiveInt(String input) {
        return Integer.parseInt(input.trim());
    }
}