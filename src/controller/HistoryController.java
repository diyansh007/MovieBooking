package controller;

import dao.BookingDAO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Booking;

public class HistoryController {
    private final BookingDAO bookingDAO;

    public HistoryController(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
    }

    public Map<String, Object> listBookings(int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Booking> bookings = bookingDAO.getBookingsByUserId(userId);
        response.put("bookings", bookings);
        return response;
    }

    public Map<String, Object> createDraft(int bookingId, int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        Booking booking = bookingDAO.findBookingById(bookingId, userId);
        if (booking == null) {
            response.put("error", "Booking not found.");
            return response;
        }
        response.put("movie", booking.getMovieName());
        response.put("format", booking.getMovieType());
        response.put("seats", booking.getSeats());
        response.put("strategy", booking.getPricingStrategy());
        response.put("amount", booking.getTotalAmount());
        return response;
    }
}