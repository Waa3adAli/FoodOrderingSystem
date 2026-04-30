
package foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ReviewAndTrackingForm extends JFrame {
    private JLabel[] starLabels = new JLabel[5];
    private int selectedRating = 0;
    private JTextArea commentArea;
    private JButton submitButton;
    private JPanel reviewsContainer;

    public ReviewAndTrackingForm() {
        setTitle("Submit Review");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Stars Panel
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ratingLabel = new JLabel("Rating:");
        starsPanel.add(ratingLabel);

        Font starFont = new Font("SansSerif", Font.PLAIN, 30);
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            starLabels[i] = new JLabel("☆");
            starLabels[i].setFont(starFont);
            starLabels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            starLabels[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    setRating(rating);
                }
            });
            starsPanel.add(starLabels[i]);
        }

        // Comment area
        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.add(new JLabel("Comment (optional):"), BorderLayout.NORTH);
        commentArea = new JTextArea(3, 40);
        commentPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

        // Submit button
        submitButton = new JButton("Submit Review");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> dispose());
        buttonPanel.add(exitButton);

        // Reviews display container
        reviewsContainer = new JPanel();
        reviewsContainer.setLayout(new BoxLayout(reviewsContainer, BoxLayout.Y_AXIS));
        JScrollPane reviewsScroll = new JScrollPane(reviewsContainer);
        reviewsScroll.setPreferredSize(new Dimension(550, 250));

        // Add components to main panel
        mainPanel.add(starsPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(commentPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JLabel("All Reviews:"));
        mainPanel.add(reviewsScroll);

        add(mainPanel, BorderLayout.CENTER);

        // تحميل التقييمات القديمة
        loadReviewsFromDatabase();

        // إرسال تقييم جديد
        submitButton.addActionListener(e -> {
            if (selectedRating == 0) {
                JOptionPane.showMessageDialog(null, "Please select a star rating.");
            } else {
                String comment = commentArea.getText().trim();
                saveReviewToDatabase(selectedRating, comment.isEmpty() ? "No comment" : comment);
                commentArea.setText("");
                setRating(0);
                reviewsContainer.removeAll(); // إعادة تحميل
                loadReviewsFromDatabase();
            }
        });

        setVisible(true);
    }

    private void setRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < 5; i++) {
            starLabels[i].setText(i < rating ? "★" : "☆");
        }
    }

    private void addReviewCard(int rating, String comment) {
        JPanel reviewCard = new JPanel();
        reviewCard.setLayout(new BoxLayout(reviewCard, BoxLayout.Y_AXIS));
        reviewCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        reviewCard.setBackground(Color.WHITE);
        reviewCard.setMaximumSize(new Dimension(500, 100));
        reviewCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ratingLabel = new JLabel("Rating: " + rating + " Stars");
        JLabel commentLabel = new JLabel("<html><body style='width: 450px;'>Comment: " + comment + "</body></html>");

        reviewCard.add(Box.createVerticalStrut(5));
        reviewCard.add(ratingLabel);
        reviewCard.add(commentLabel);
        reviewCard.add(Box.createVerticalStrut(5));

        reviewsContainer.add(Box.createVerticalStrut(10));
        reviewsContainer.add(reviewCard);
    }

    private void saveReviewToDatabase(int rating, String comment) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@"
            );
            String sql = "INSERT INTO Reviews (user_id, restaurant_id, rating, comment) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, 1); // مؤقتاً user_id = 1
            stmt.setInt(2, 1); // مؤقتاً restaurant_id = 1
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void loadReviewsFromDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@"
            );
            String sql = "SELECT rating, comment FROM Reviews ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");
                addReviewCard(rating, comment);
            }
            rs.close();
            stmt.close();
            conn.close();
            reviewsContainer.revalidate();
            reviewsContainer.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

   
}
