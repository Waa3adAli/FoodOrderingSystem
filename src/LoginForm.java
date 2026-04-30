package foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartForm extends JFrame {
    private int userId;
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private List<CartItem> cartItems;
    private int cartId;

    public CartForm(int userId) {
        this.userId = userId;
        this.cartItems = new ArrayList<>();
        this.cartId = getOrCreateActiveCartId(); // ✅ استخدام سلة موجودة أو إنشاء جديدة

        setTitle("Your Cart");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Item", "Price", "Quantity"}, 0);
        cartTable = new JTable(tableModel);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: $0.00");
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update Quantity");
        JButton deleteButton = new JButton("Delete Item");
        JButton paymentButton = new JButton("Payment");
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(paymentButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        updateButton.addActionListener(e -> updateQuantity());
        deleteButton.addActionListener(e -> deleteItem());
        paymentButton.addActionListener(e -> processPayment());

        loadCart();
        setVisible(true);
    }

    // ✅ الحصول على سلة موجودة أو إنشاء جديدة
    private int getOrCreateActiveCartId() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT cart_id FROM Cart WHERE user_id = ? AND status = 'active'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("cart_id");

            // لا يوجد سلة؟ أنشئ واحدة
            String insertSql = "INSERT INTO Cart (user_id, status) VALUES (?, 'active')";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, userId);
            insertStmt.executeUpdate();
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) return generatedKeys.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadCart() {
        cartItems.clear();
        tableModel.setRowCount(0);
        double total = 0.0;

        try (Connection conn = getConnection()) {
            String sql = "SELECT d.name, d.price, ci.quantity, ci.dish_id FROM CartItems ci " +
                         "JOIN Dishes d ON ci.dish_id = d.dish_id WHERE ci.cart_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cartId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                int dishId = rs.getInt("dish_id");
                cartItems.add(new CartItem(dishId, name, price, quantity));
                tableModel.addRow(new Object[]{name, price, quantity});
                total += price * quantity;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    private void updateQuantity() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0) {
            String newQtyStr = JOptionPane.showInputDialog(this, "Enter new quantity:");
            if (newQtyStr != null && !newQtyStr.trim().isEmpty()) {
                try {
                    int newQty = Integer.parseInt(newQtyStr);
                    if (newQty <= 0) throw new NumberFormatException();

                    CartItem item = cartItems.get(selectedRow);
                    try (Connection conn = getConnection()) {
                        String sql = "UPDATE CartItems SET quantity = ? WHERE cart_id = ? AND dish_id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, newQty);
                        stmt.setInt(2, cartId);
                        stmt.setInt(3, item.getDishId());
                        stmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(this, "Quantity updated.");
                    loadCart();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select an item first.");
        }
    }

    private void deleteItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0) {
            CartItem item = cartItems.get(selectedRow);
            try (Connection conn = getConnection()) {
                String sql = "DELETE FROM CartItems WHERE cart_id = ? AND dish_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, cartId);
                stmt.setInt(2, item.getDishId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Item removed.");
            loadCart();
        } else {
            JOptionPane.showMessageDialog(this, "Select an item first.");
        }
    }

    // ✅ الدفع وتحديث السلة
    private void processPayment() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        try (Connection conn = getConnection()) {
        String sql = "UPDATE Cart SET status = 'completed' WHERE cart_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cartId);
            int updatedRows = stmt.executeUpdate();

            if (updatedRows > 0) {
                double totalAmount = 0;
                int restaurantId = -1;

                for (CartItem item : cartItems) {
                    totalAmount += item.getPrice() * item.getQuantity();
                    if (restaurantId == -1) {
                        String resSql = "SELECT restaurant_id FROM Dishes WHERE dish_id = ?";
                        PreparedStatement resStmt = conn.prepareStatement(resSql);
                        resStmt.setInt(1, item.getDishId());
                        ResultSet resRs = resStmt.executeQuery();
                        if (resRs.next()) {
                            restaurantId = resRs.getInt("restaurant_id");
                        }
                    }
                }

                String orderSql = "INSERT INTO Orders (user_id, restaurant_id, total_price, status) VALUES (?, ?, ?, 'pending')";
                PreparedStatement orderStmt = conn.prepareStatement(orderSql);
                orderStmt.setInt(1, userId);
                orderStmt.setInt(2, restaurantId);
                orderStmt.setDouble(3, totalAmount);
                orderStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Payment successful! Order placed.");

                // ✅ فتح سلة جديدة تلقائيًا بعد الدفع
                new CartForm(userId);
                dispose();

                // ✅ فتح نافذة الدفع
                PaymentForm paymentForm =new PaymentForm(totalAmount, userId);
                paymentForm.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update cart status.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment failed.");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
    }
}

// ✅ كلاس مساعد داخل نفس الملف
class CartItem {
    private int dishId;
    private String name;
    private double price;
    private int quantity;

    public CartItem(int dishId, String name, double price, int quantity) {
        this.dishId = dishId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getDishId() { return dishId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
