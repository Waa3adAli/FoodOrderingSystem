package foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.*;

public class OrderTrackingForm extends JFrame {
    private JTable ordersTable;
    private JButton refreshButton, updateStatusButton, deleteOrderButton;
    private DefaultTableModel tableModel;
    private Timer timer;
    private JTextField orderIdField;
    private JComboBox<String> statusComboBox;
    private int restaurantId;
    private Map<Integer, String> lastOrderStatuses = new HashMap<>();

    public OrderTrackingForm(int restaurantId) {
        this.restaurantId = restaurantId;

        setTitle("Order Tracking");
        setSize(800, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Order ID", "User ID", "Total Price", "Status", "Created At"}, 0);
        ordersTable = new JTable(tableModel);

        refreshButton = new JButton("Refresh Orders");
        refreshButton.addActionListener(e -> loadOrders(true));

        updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(e -> updateOrderStatus());

        deleteOrderButton = new JButton("Delete Order");
        deleteOrderButton.addActionListener(e -> deleteOrder());

        orderIdField = new JTextField(10);
        statusComboBox = new JComboBox<>(new String[]{"pending", "preparing", "on_the_way", "delivered", "cancelled"});

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Order ID:"));
        topPanel.add(orderIdField);
        topPanel.add(new JLabel("Status:"));
        topPanel.add(statusComboBox);
        topPanel.add(updateStatusButton);
        topPanel.add(deleteOrderButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadOrders(false);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadOrders(true));
            }
        }, 60000, 60000); // كل 60 ثانية

        setVisible(true);
    }

    private void loadOrders(boolean checkForChanges) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Orders WHERE restaurant_id = ?")) {

            System.out.println("Connected to the database successfully.");
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            Map<Integer, String> currentStatuses = new HashMap<>();
            tableModel.setRowCount(0);  // Reset table data before loading new data

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String status = rs.getString("status");
                currentStatuses.put(orderId, status);

                // إضافة البيانات إلى الجدول
                tableModel.addRow(new Object[]{
                        orderId,
                        rs.getInt("user_id"),
                        rs.getDouble("total_price"),
                        status,
                        rs.getTimestamp("created_at")
                });

                System.out.println("Order ID: " + orderId + ", Status: " + status);  // طباعة البيانات للتحقق

                if (checkForChanges) {
                    String lastStatus = lastOrderStatuses.get(orderId);
                    if (lastStatus != null && !lastStatus.equals(status)) {
                        showStatusChangeNotification(orderId, lastStatus, status);
                    }
                }
            }

            lastOrderStatuses = currentStatuses;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load orders: " + ex.getMessage());
        }
    }

    private void showStatusChangeNotification(int orderId, String oldStatus, String newStatus) {
        JOptionPane.showMessageDialog(this,
                "تم تحديث حالة الطلب #" + orderId + "\nمن: " + oldStatus + "\nإلى: " + newStatus,
                "تنبيه: تغيير حالة الطلب", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateOrderStatus() {
        String orderIdText = orderIdField.getText();
        String newStatus = (String) statusComboBox.getSelectedItem();

        if (orderIdText.isEmpty() || newStatus == null) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال رقم الطلب وتحديد الحالة الجديدة.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
             PreparedStatement stmt = con.prepareStatement("UPDATE Orders SET status = ? WHERE order_id = ? AND restaurant_id = ?")) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, Integer.parseInt(orderIdText));
            stmt.setInt(3, restaurantId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "تم تحديث حالة الطلب.");
                loadOrders(false);
            } else {
                JOptionPane.showMessageDialog(this, "لم يتم العثور على الطلب أو لا يتبع هذا المطعم.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "فشل في تحديث حالة الطلب: " + ex.getMessage());
        }
    }

    private void deleteOrder() {
        String orderIdText = orderIdField.getText();

        if (orderIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال رقم الطلب.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
             PreparedStatement stmt = con.prepareStatement("DELETE FROM Orders WHERE order_id = ? AND restaurant_id = ?")) {

            stmt.setInt(1, Integer.parseInt(orderIdText));
            stmt.setInt(2, restaurantId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "تم حذف الطلب.");
                loadOrders(false);
            } else {
                JOptionPane.showMessageDialog(this, "لم يتم العثور على الطلب أو لا يتبع هذا المطعم.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "فشل في حذف الطلب: " + ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (timer != null) timer.cancel();
        super.dispose();
    }

   
}
