import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBCheck {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hospital_db", "postgres", "29061612");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT full_name, password_hash FROM users WHERE bhyt_number = 'HN47900000001'");
        if (rs.next()) {
            System.out.println("Name: " + rs.getString(1));
            System.out.println("Hash in DB: " + rs.getString(2));
        } else {
            System.out.println("User not found!");
        }
        conn.close();
    }
}
