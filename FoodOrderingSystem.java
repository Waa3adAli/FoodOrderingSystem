package foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class CustomerOrderTrackingForm extends JFrame {
    private JLabel statusLabel;
    private int userId;
    private Timer timer;

    public CustomerOrderTrackingForm(int userId) {
        this.userId = userId;

        setTitle("Order Tracking");
        setSize(450, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Checking your order status...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(statusLabel, BorderLayout.CENTER);

        loadOrderStatus(); // initial load

        // Auto refresh every 30 seconds
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> loadOrderStatus());
            }
        }, 30000, 30000);

        setVisible(true);
    }

    private void loadOrderStatus() {
        String query = "SELECT status FROM Orders WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                String message = switch (status) {
                    case "pending" -> "Your order has been received.";
                    case "preparing" -> "Your order is being prepared.";
                    case "on_the_way" -> "Your order is on the way!";
                    case "delivered" -> "Your order has been delivered.";
                    case "cancelled" -> "Your order was cancelled.";
                    default -> "Unknown status.";
                };
                statusLabel.setText(message);
            } else {
                statusLabel.setText("No recent orders found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading status.");
        }
    }

    @Override
    public void dispose() {
        if (timer != null) timer.cancel();
        super.dispose();
    }

    public static void main(String[] args) {
        new CustomerOrderTrackingForm(1); // Replace 1 with actual logged-in user_id
    }
}

