package controller;

import dao.BookingDAO;
import mediator.BookingMediator;
import mediator.MovieBookingMediator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Booking;

public class HistoryController {
    private final BookingDAO bookingDAO;
    private final BookingMediator bookingMediator;

    public HistoryController(BookingDAO bookingDAO) {
        this(bookingDAO, new MovieBookingMediator(bookingDAO));
    }

    public HistoryController(BookingDAO bookingDAO, BookingMediator bookingMediator) {
        this.bookingDAO = bookingDAO;
        this.bookingMediator = bookingMediator;
    }

    public Map<String, Object> listBookings(int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Booking> bookings = bookingDAO.getBookingsByUserId(userId);
        response.put("bookings", bookings);
        return response;
    }

    public Map<String, Object> createDraft(int bookingId, int userId) {
        return bookingMediator.prepareBookAgainDraft(bookingId, userId);
    }
}