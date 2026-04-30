package foodorderingsystem;

import javax.swing.*;
import java.awt.*;

public class PaymentForm extends JFrame {
    private double totalAmount;
    private int userId;

    public PaymentForm(double totalAmount, int userId) {
        this.totalAmount = totalAmount;
        this.userId = userId;

        setTitle("Payment");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Total to Pay: $" + String.format("%.2f", totalAmount), SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        add(label, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Card Number:"));
        panel.add(new JTextField());
        panel.add(new JLabel("Expiry Date:"));
        panel.add(new JTextField());
        panel.add(new JLabel("CVV:"));
        panel.add(new JTextField());
        add(panel, BorderLayout.CENTER);

        JButton payButton = new JButton("Pay Now");
        payButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Payment Successful!");

            // ✅ بعد الدفع، فتح Cart جديدة تلقائيًا
            new CartForm(userId);
            dispose(); // إغلاق نافذة الدفع
        });
        add(payButton, BorderLayout.SOUTH);

        setVisible(true);
    }
}
