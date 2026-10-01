
package parkflowmanagement;
public class ParkFlowManagement {

   public static void main(String[] args) {
    DatabaseUtil.initializeDatabase();
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            new LoginUI().setVisible(true);
        }
    });
   }}