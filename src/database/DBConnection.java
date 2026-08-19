package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:movie_booking.db";

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found.");
        }

        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public static void initializeDatabase() {

        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                )
                """;

        String createBookings = """
                CREATE TABLE IF NOT EXISTS bookings (
                    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    movie_name TEXT NOT NULL,
                    movie_type TEXT NOT NULL,
                    seats INTEGER NOT NULL,
                    total_amount REAL NOT NULL,
                    booking_status TEXT NOT NULL,
                    FOREIGN KEY (user_id)
                    REFERENCES users(user_id)
                    ON DELETE CASCADE
                )
                """;

        String insertStudent = """
                INSERT OR IGNORE INTO users(username, password)
                VALUES('student', 'pass123')
                """;

        String insertAdmin = """
                INSERT OR IGNORE INTO users(username, password)
                VALUES('admin', 'admin123')
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createUsers);
            statement.execute(createBookings);
            statement.execute(insertStudent);
            statement.execute(insertAdmin);

            System.out.println("SQLite database initialized successfully.");

        } catch (SQLException e) {
            System.out.println("Database initialization error: "
                    + e.getMessage());
        }
    }
}