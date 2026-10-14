package controller;

import builder.BookingDirector;
import dao.BookingDAO;
import factory.MovieFactory;
import model.Booking;
import model.Movie;
import singleton.BookingSystem;
import strategy.PricingService;
import template.GroupBookingWorkflow;
import template.StandardBookingWorkflow;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingController {
    private final BookingDAO bookingDAO;
    private final BookingDirector bookingDirector;

    public BookingController(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
        this.bookingDirector = new BookingDirector();
    }

    public Map<String, Object> createBooking(int userId, String movie, String format, int seats, String strategy) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (userId <= 0) {
            response.put("error", "A logged-in user is required.");
            return response;
        }
        if (movie == null || movie.isBlank() || format == null || format.isBlank()) {
            response.put("error", "Movie and format are required.");
            return response;
        }
        if (seats <= 0 || seats > 60) {
            response.put("error", "Seat count must be between 1 and 60.");
            return response;
        }
        try {
            Movie movieObject = MovieFactory.createMovie(format, movie);
            String selectedStrategy = strategy == null || strategy.isBlank() ? "standard" : strategy.toLowerCase();
            double total = PricingService.calculateTotal(movieObject, seats, selectedStrategy, false);
            Booking booking = bookingDirector.constructStandardBooking(userId, movie, format, seats, total, selectedStrategy);
            boolean success = BookingSystem.getInstance().processBookingWithWorkflow(booking,
                    seats >= 4 ? new GroupBookingWorkflow(bookingDAO, BookingSystem.getInstance().getBookingSubject())
                            : new StandardBookingWorkflow(bookingDAO, BookingSystem.getInstance().getBookingSubject()));
            response.put("success", success);
            response.put("message", success ? "Your tickets are confirmed." : "We could not confirm this booking.");
            response.put("amount", total);
            response.put("strategy", selectedStrategy);
            return response;
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
            return response;
        }
    }

    public Map<String, Object> handleCancel(int bookingId, int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean changed = BookingSystem.getInstance().cancelBooking(bookingId, userId);
        response.put("success", changed);
        response.put("message", changed ? "Booking cancelled." : "Booking not found.");
        return response;
    }
}