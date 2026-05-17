package banking.dao;

import banking.config.DatabaseConnection;
import banking.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CustomerDAO.java
 *
 * Handles all database operations for customers.
 * Includes registering, viewing, finding and updating customers.
 *
 * Author: Benjamina
 */
public class CustomerDAO {

    // Helper method so we don't repeat the long connection code
    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Register a new customer into the database
    public int registerCustomer(Customer customer) {

        // PreparedStatement protects against SQL injection
        String sql = "INSERT INTO customer (first_name, last_name, id_number, email, phone, address) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        // try-with-resources closes the PreparedStatement automatically
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getIdNumber());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getPhone());
            ps.setString(6, customer.getAddress());

            ps.executeUpdate();

            // Getting the auto-generated customer ID back from the database
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            // This catches duplicate ID number or email
            System.out.println("Duplicate customer ID number or email.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return -1;
    }

    // View all customers in the database
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();

        // PreparedStatement keeps database queries safe
        String sql = "SELECT * FROM customer";

        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("id_number"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                );
                customers.add(customer);
            }

        } catch (SQLException e) {
            System.out.println("Error reading customers: " + e.getMessage());
        }

        return customers;
    }

    // Find one customer by their ID
    public Optional<Customer> findById(int customerId) {

        // PreparedStatement stops users from injecting bad SQL
        String sql = "SELECT * FROM customer WHERE customer_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("id_number"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                    );
                    return Optional.of(customer);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }

        // Returning Optional.empty() instead of null to avoid NullPointerException
        return Optional.empty();
    }

    // Update email and phone for an existing customer
    public boolean updateContactDetails(int customerId, String email, String phone) {

        // PreparedStatement secures the update query
        String sql = "UPDATE customer SET email = ?, phone = ? WHERE customer_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setInt(3, customerId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }

        return false;
    }
}