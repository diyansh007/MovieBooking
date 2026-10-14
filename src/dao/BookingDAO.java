package dao;

import database.DBConnection;
import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public int insertBooking(Booking booking) {

        String sql =
                "INSERT INTO bookings " +
                "(user_id, movie_name, movie_type, seats, " +
                "total_amount, booking_status, pricing_strategy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getUserId());
            statement.setString(2, booking.getMovieName());
            statement.setString(3, booking.getMovieType());
            statement.setInt(4, booking.getSeats());
            statement.setDouble(5, booking.getTotalAmount());
            statement.setString(6, booking.getBookingStatus());
            statement.setString(7, booking.getPricingStrategy());

            statement.executeUpdate();

            ResultSet result =
                    statement.getGeneratedKeys();

            if (result.next()) {
                int generatedId = result.getInt(1);
                booking.setBookingId(generatedId);
                return generatedId;
            }

        } catch (Exception e) {

            System.out.println(
                    "Booking insert error: " + e.getMessage());
        }

        return -1;
    }

    public List<Booking> getBookingsByUserId(int userId) {

        List<Booking> bookings = new ArrayList<>();

        String sql =
                "SELECT * FROM bookings WHERE user_id = ? ORDER BY booking_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result =
                    statement.executeQuery();

            while (result.next()) {

                bookings.add(new Booking(
                        result.getInt("booking_id"),
                        result.getInt("user_id"),
                        result.getString("movie_name"),
                        result.getString("movie_type"),
                        result.getInt("seats"),
                        result.getDouble("total_amount"),
                        result.getString("booking_status"),
                        result.getString("pricing_strategy")
                ));
            }

        } catch (Exception e) {

            System.out.println(
                    "Booking fetch error: " + e.getMessage());
        }

        return bookings;
    }

    public Booking findBookingById(int bookingId, int userId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ? AND user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            statement.setInt(2, userId);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new Booking(
                        result.getInt("booking_id"),
                        result.getInt("user_id"),
                        result.getString("movie_name"),
                        result.getString("movie_type"),
                        result.getInt("seats"),
                        result.getDouble("total_amount"),
                        result.getString("booking_status"),
                        result.getString("pricing_strategy")
                );
            }
        } catch (Exception e) {
            System.out.println("Booking lookup error: " + e.getMessage());
        }
        return null;
    }

    public boolean updateBookingStatus(
            int bookingId,
            int userId,
            String status) {

        String sql =
                "UPDATE bookings SET booking_status = ? " +
                "WHERE booking_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, bookingId);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            System.out.println(
                    "Booking update error: " + e.getMessage());
        }

        return false;
    }
}