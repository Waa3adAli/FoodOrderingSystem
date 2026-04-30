package foodorderingsystem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RestaurantListingForm extends JFrame {
    private JTable restaurantTable;
    private int userId;  // تعريف متغير userId

    // تعديل المُنشئ ليأخذ userId كمعامل
    public RestaurantListingForm(int userId) {
        this.userId = userId;  // تعيين قيمة userId

        setTitle("Restaurant Listing and Filtering");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        restaurantTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(restaurantTable);
        add(scrollPane, BorderLayout.CENTER);

        loadRestaurants();

        // التعامل مع الضغط على صف من الجدول
        restaurantTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = restaurantTable.getSelectedRow();
                if (row != -1) {
                    int restaurantId = (int) restaurantTable.getValueAt(row, 0);  // استخراج restaurantId
                    openDishListForm(restaurantId, userId);  // تمرير restaurantId و userId
                }
            }
        });

        setVisible(true);
    }

    private void loadRestaurants() {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Location", "Cuisine", "Rating"});

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoodOrderingSystem", "root", "Ll123456@");
            String sql = "SELECT r.restaurant_id, u.name, r.location, r.category, r.rating " +
                         "FROM Restaurants r JOIN Users u ON r.user_id = u.user_id";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("restaurant_id");
                String name = rs.getString("name");
                String location = rs.getString("location");
                String category = rs.getString("category");
                double rating = rs.getDouble("rating");

                model.addRow(new Object[]{id, name, location, category, rating});
            }

            restaurantTable.setModel(model);

            rs.close();
            ps.close();
            con.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading restaurants: " + ex.getMessage());
        }
    }

    private void openDishListForm(int restaurantId, int userId) {
        new DishListForm(restaurantId, userId);  // تمرير restaurantId و userId
    }

   
}
