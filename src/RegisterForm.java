package foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;

public class RegisterForm extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeBox;
    private JButton registerButton;

    public RegisterForm() {
        setTitle("User Registration");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel typeLabel = new JLabel("User Role:");
        String[] userRoles = {"customer", "restaurant", "admin"};
        userTypeBox = new JComboBox<>(userRoles);

        registerButton = new JButton("Register");

        add(nameLabel); add(nameField);
        add(emailLabel); add(emailField);
        add(passwordLabel); add(passwordField);
        add(typeLabel); add(userTypeBox);
        add(new JLabel()); add(registerButton);

        registerButton.addActionListener(e -> registerUser());

        setVisible(true);
    }

    private void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword()).trim();
        String role = userTypeBox.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }

        if (!isStrongPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters, include uppercase, number, and special character.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
             PreparedStatement ps = con.prepareStatement("INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                JOptionPane.showMessageDialog(this, "User registered successfully!");

                if (role.equals("restaurant")) {
                    String[] types = {"Italian", "Indian", "American", "Saudi", "International", "Asian", "Marine", "Arab", "French"};
                    String[] locations = {"Riyadh", "Qassim", "Jeddah", "Mecca", "Khobar", "Dammam"};

                    JComboBox<String> typeBox = new JComboBox<>(types);
                    JComboBox<String> locationBox = new JComboBox<>(locations);

                    JPanel panel = new JPanel(new GridLayout(2, 2));
                    panel.add(new JLabel("Restaurant Category:"));
                    panel.add(typeBox);
                    panel.add(new JLabel("Location:"));
                    panel.add(locationBox);

                    int result = JOptionPane.showConfirmDialog(this, panel, "Enter Restaurant Details", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        String category = (String) typeBox.getSelectedItem();
                        String location = (String) locationBox.getSelectedItem();

                        PreparedStatement ps3 = con.prepareStatement(
                            "INSERT INTO Restaurants (name, location, category, user_id) VALUES (?, ?, ?, ?)"
                        );
                        ps3.setString(1, name);
                        ps3.setString(2, location);
                        ps3.setString(3, category);
                        ps3.setInt(4, userId);
                        ps3.executeUpdate();
                    }
                }

                dispose();
                new LoginForm();
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Email already registered.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$");
    }

    public static void main(String[] args) {
        new RegisterForm();
    }
}
