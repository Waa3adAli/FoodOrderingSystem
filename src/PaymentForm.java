package foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DishListForm extends JFrame implements ActionListener {
    private DefaultTableModel tableModel;
    private JTable dishTable;
    private JButton addToCartButton;
    private JButton goToCartButton;
    private int restaurantId;
    private int userId;

    public DishListForm(int restaurantId, int userId) {
        this.restaurantId = restaurantId;
        this.userId = userId;

        setTitle("Dish List");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Dish ID", "Dish Name", "Description", "Price"}, 0);
        dishTable = new JTable(tableModel);
        add(new JScrollPane(dishTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        addToCartButton = new JButton("Add to Cart");
        goToCartButton = new JButton("Go to Cart");

        addToCartButton.addActionListener(this);
        goToCartButton.addActionListener(this);

        bottomPanel.add(addToCartButton);
        bottomPanel.add(goToCartButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadDishes();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addToCartButton) {
            addSelectedDishToCart();
        } else if (e.getSource() == goToCartButton) {
            goToCart();
        }
    }

    private void loadDishes() {
        try (
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Dishes WHERE restaurant_id = ?")
        ) {
            ps.setInt(1, restaurantId);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0); // مسح البيانات القديمة
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("dish_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load dishes: " + ex.getMessage());
        }
    }

    private void addSelectedDishToCart() {
        int selectedRow = dishTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a dish first.");
            return;
        }

        int dishId = (int) tableModel.getValueAt(selectedRow, 0);
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (quantityStr == null) return;

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) throw new NumberFormatException();

            int cartId = getOrCreateActiveCartId();

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@")) {
                String checkSql = "SELECT quantity FROM CartItems WHERE cart_id = ? AND dish_id = ?";
                PreparedStatement checkStmt = con.prepareStatement(checkSql);
                checkStmt.setInt(1, cartId);
                checkStmt.setInt(2, dishId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int existingQty = rs.getInt("quantity");
                    String updateSql = "UPDATE CartItems SET quantity = ? WHERE cart_id = ? AND dish_id = ?";
                    PreparedStatement updateStmt = con.prepareStatement(updateSql);
                    updateStmt.setInt(1, existingQty + quantity);
                    updateStmt.setInt(2, cartId);
                    updateStmt.setInt(3, dishId);
                    updateStmt.executeUpdate();
                } else {
                    String insertSql = "INSERT INTO CartItems (cart_id, dish_id, quantity) VALUES (?, ?, ?)";
                    PreparedStatement insertStmt = con.prepareStatement(insertSql);
                    insertStmt.setInt(1, cartId);
                    insertStmt.setInt(2, dishId);
                    insertStmt.setInt(3, quantity);
                    insertStmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Dish added to cart.");

            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void goToCart() {
        dispose();  // إغلاق نافذة DishListForm
        new CartForm(userId);  // فتح نافذة CartForm
    }

    private int getOrCreateActiveCartId() throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@")) {
            String checkSql = "SELECT cart_id FROM Cart WHERE user_id = ? AND status = 'active'";
            PreparedStatement stmt = con.prepareStatement(checkSql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("cart_id");
            } else {
                String createSql = "INSERT INTO Cart (user_id) VALUES (?)";
                PreparedStatement insertStmt = con.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setInt(1, userId);
                insertStmt.executeUpdate();
                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        }

        throw new SQLException("Failed to get or create cart.");
    }
}
