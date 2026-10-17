package controller;

import dao.BookingDAO;
import mediator.BookingMediator;
import mediator.MovieBookingMediator;
import singleton.BookingSystem;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingController {
    private final BookingMediator bookingMediator;

    public BookingController(BookingDAO bookingDAO) {
        this(bookingDAO, new MovieBookingMediator(bookingDAO));
    }

    public BookingController(BookingDAO bookingDAO, BookingMediator bookingMediator) {
        this.bookingMediator = bookingMediator;
    }

    public Map<String, Object> createBooking(int userId, String movie, String format, int seats, String strategy) {
        Map<String, Object> mediated = bookingMediator.createBookingRequest(userId, movie, format, seats, strategy, seats >= 4);
        if (mediated.containsKey("error")) {
            return mediated;
        }
        boolean success = Boolean.TRUE.equals(mediated.get("accepted"));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("message", success ? "Your tickets are confirmed." : "We could not confirm this booking.");
        response.put("amount", mediated.get("total"));
        response.put("strategy", mediated.get("strategy"));
        response.put("bookingId", mediated.get("bookingId"));
        return response;
    }

    public Map<String, Object> handleCancel(int bookingId, int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean changed = BookingSystem.getInstance().cancelBooking(bookingId, userId);
        response.put("success", changed);
        response.put("message", changed ? "Booking cancelled." : "Booking not found.");
        return response;
    }
}