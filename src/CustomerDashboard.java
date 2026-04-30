package foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ForgotPasswordForm extends JFrame {
    private JTextField emailField;
    private JButton resetPasswordButton;

    public ForgotPasswordForm() {
        setTitle("Forgot Password");
        setSize(350, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 2, 10, 10));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        resetPasswordButton = new JButton("Reset Password");

        add(emailLabel); add(emailField);
        add(new JLabel()); add(resetPasswordButton);

        resetPasswordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetPassword();
            }
        });

        setVisible(true);
    }

    private void resetPassword() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
             PreparedStatement ps = con.prepareStatement("SELECT * FROM Users WHERE email=?")) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");

                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    if (!isStrongPassword(newPassword)) {
                        JOptionPane.showMessageDialog(this, "Password must contain at least one uppercase letter, one digit, one special character, and be at least 8 characters long.");
                        return;
                    }

                    PreparedStatement updatePs = con.prepareStatement("UPDATE Users SET password=? WHERE email=?");
                    updatePs.setString(1, newPassword);
                    updatePs.setString(2, email);
                    updatePs.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Password reset successfully!");
                    dispose();
                    new LoginForm(); // إعادة توجيه المستخدم إلى صفحة تسجيل الدخول
                }
            } else {
                JOptionPane.showMessageDialog(this, "Email not found.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // دالة للتحقق من قوة كلمة المرور
    private boolean isStrongPassword(String password) {
        // يجب أن تحتوي على حرف كبير، رقم، ورمز خاص، وطولها 8 أحرف على الأقل
        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$");
    }

 
}
