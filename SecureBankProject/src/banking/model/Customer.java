package banking.model;

/**
 * Customer.java
 *
 * Represents a single customer in the banking system.
 * This class mirrors the 'customer' table in our database.
 * Each field matches a column in that table exactly.
 *
 * Author: David
 */
public class Customer {

    private int    customerId;
    private String firstName;
    private String lastName;
    private String idNumber;
    private String email;
    private String phone;
    private String address;
    private String createdAt;

    // Default constructor — needed when building object from database results
    public Customer() { }

   
// Constructor used when reading customer data from the database
public Customer(int customerId, String firstName, String lastName,
                String idNumber, String email, String phone, String address) {
    this.customerId = customerId;
    this.firstName  = firstName;
    this.lastName   = lastName;
    this.idNumber   = idNumber;
    this.email      = email;
    this.phone      = phone;
    this.address    = address;
}    
// Full constructor — used when registering a new customer
    public Customer(String firstName, String lastName, String idNumber,
                    String email, String phone, String address) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.idNumber  = idNumber;
        this.email     = email;
        this.phone     = phone;
        this.address   = address;
    }

    // ── Getters and Setters ──────────────────────────────────────

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Convenience method — returns full name in one call
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Useful for printing customer details to the CLI
    @Override
    public String toString() {
        return "Customer ID : " + customerId + "\n"
             + "Name        : " + getFullName() + "\n"
             + "ID Number   : " + idNumber + "\n"
             + "Email       : " + email + "\n"
             + "Phone       : " + phone + "\n"
             + "Address     : " + address;
    }
}