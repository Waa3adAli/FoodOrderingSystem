package foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.*;

public class AdminDashboard extends JFrame {
    private JButton viewUsersBtn, deleteUserBtn, updateUserBtn;
    private JButton viewRestaurantsBtn, deleteRestaurantBtn, updateRestaurantBtn;
    private JButton logoutButton, exitButton;
    private JTextArea outputArea;
    private JLabel adminLabel;
    private Logger logger = Logger.getLogger(AdminDashboard.class.getName());

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        adminLabel = new JLabel("Welcome, Admin", SwingConstants.CENTER);
        adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(adminLabel, BorderLayout.CENTER);

        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        logoutButton = new JButton("Logout");
        exitButton = new JButton("Exit");
        styleButton(logoutButton);
        styleButton(exitButton);

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        topButtonsPanel.add(logoutButton);
        topButtonsPanel.add(exitButton);
        headerPanel.add(topButtonsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Button panel (left side)
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        viewUsersBtn = new JButton("View Users");
        deleteUserBtn = new JButton("Delete User");
        updateUserBtn = new JButton("Update User");
        viewRestaurantsBtn = new JButton("View Restaurants");
        deleteRestaurantBtn = new JButton("Delete Restaurant");
        updateRestaurantBtn = new JButton("Update Restaurant");

        styleButton(viewUsersBtn);
        styleButton(deleteUserBtn);
        styleButton(updateUserBtn);
        styleButton(viewRestaurantsBtn);
        styleButton(deleteRestaurantBtn);
        styleButton(updateRestaurantBtn);

        buttonPanel.add(viewUsersBtn);
        buttonPanel.add(deleteUserBtn);
        buttonPanel.add(updateUserBtn);
        buttonPanel.add(viewRestaurantsBtn);
        buttonPanel.add(deleteRestaurantBtn);
        buttonPanel.add(updateRestaurantBtn);

        add(buttonPanel, BorderLayout.WEST);

        // Output area (center)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        viewUsersBtn.addActionListener(new ViewDataHandler("Users"));
        viewRestaurantsBtn.addActionListener(new ViewDataHandler("Restaurants"));
        deleteUserBtn.addActionListener(new DeleteHandler("Users", "user_id"));
        deleteRestaurantBtn.addActionListener(new DeleteHandler("Restaurants", "restaurant_id"));
        updateUserBtn.addActionListener(new UpdateHandler("Users", "user_id"));
        updateRestaurantBtn.addActionListener(new UpdateHandler("Restaurants", "restaurant_id"));

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
    }

    private class ViewDataHandler implements ActionListener {
        private String table;

        public ViewDataHandler(String table) {
            this.table = table;
        }

        public void actionPerformed(ActionEvent e) {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {

                outputArea.setText("");
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        outputArea.append(meta.getColumnName(i) + ": " + rs.getString(i) + " ");
                    }
                    outputArea.append("\n");
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Could not load data", ex);
                JOptionPane.showMessageDialog(AdminDashboard.this, "Could not load data: " + ex.getMessage());
            }
        }
    }

    private class DeleteHandler implements ActionListener {
        private String table, idColumn;

        public DeleteHandler(String table, String idColumn) {
            this.table = table;
            this.idColumn = idColumn;
        }

        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog(AdminDashboard.this, "Enter ID to delete:");
            if (id == null || id.isEmpty()) return;
            try (Connection conn = getConnection()) {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + idColumn + " = ?");
                checkStmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int confirm = JOptionPane.showConfirmDialog(AdminDashboard.this, "Are you sure you want to delete this record?");
                    if (confirm == JOptionPane.YES_OPTION) {
                        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM " + table + " WHERE " + idColumn + " = ?");
                        deleteStmt.setInt(1, Integer.parseInt(id));
                        deleteStmt.executeUpdate();
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Record deleted.");
                    }
                } else {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Record not found.");
                }
            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error: " + ex.getMessage());
            }
        }
    }

    private class UpdateHandler implements ActionListener {
        private String table, idColumn;

        public UpdateHandler(String table, String idColumn) {
            this.table = table;
            this.idColumn = idColumn;
        }

        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog(AdminDashboard.this, "Enter ID to update:");
            if (id == null || id.isEmpty()) return;

            String newName = JOptionPane.showInputDialog(AdminDashboard.this, "Enter new name:");
            if (newName == null || newName.isEmpty()) return;

            try (Connection conn = getConnection()) {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + idColumn + " = ?");
                checkStmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    PreparedStatement updateStmt = conn.prepareStatement("UPDATE " + table + " SET name = ? WHERE " + idColumn + " = ?");
                    updateStmt.setString(1, newName);
                    updateStmt.setInt(2, Integer.parseInt(id));
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Record updated.");
                } else {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Record not found.");
                }
            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}
