
package foodorderingsystem;

/**
 *
 * @author lili
 */
public class FoodOrderingSystem {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DatabaseConnection.getConnection(); // هذا بيجرب الاتصال
        new LoginForm();
        new DatabaseConnection();
     
    }
    
}
