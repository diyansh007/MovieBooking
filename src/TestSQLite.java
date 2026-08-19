import java.sql.Connection;
import java.sql.DriverManager;

public class TestSQLite {

    public static void main(String[] args) {

        String url = "jdbc:sqlite:movie_booking.db";

        try {
            Class.forName("org.sqlite.JDBC");

            Connection connection = DriverManager.getConnection(url);

            System.out.println("SQLite connected successfully!");

            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}