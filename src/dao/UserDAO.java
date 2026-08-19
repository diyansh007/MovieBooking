package dao;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User authenticate(String username, String password) {

        String sql =
                "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                return new User(
                        result.getInt("user_id"),
                        result.getString("username"),
                        result.getString("password")
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Login database error: " + e.getMessage());
        }

        return null;
    }
}