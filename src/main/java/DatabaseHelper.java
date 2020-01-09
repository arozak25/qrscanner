import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    private static Connection conn;

    public static Connection getDBconnection() {
        try  {
            if (conn == null){

                String dbhost = "jdbc:mysql://127.0.0.1:3306/qrscanner?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
                String username = "root";
                String password = "";

                conn = DriverManager.getConnection(
                        dbhost, username, password);
            }
        } catch (SQLException e) {
            System.out.println("Cannot create database connection");
            e.printStackTrace();
        }
        return conn;
    }

}
