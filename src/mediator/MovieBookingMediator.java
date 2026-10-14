package mediator;

import builder.BookingDirector;
import dao.BookingDAO;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import factory.MovieFactory;
import model.Booking;
import model.Movie;
import prototype.BookingDraft;
import singleton.BookingSystem;
import strategy.PricingService;
import template.BookingWorkflowTemplate;
import template.GroupBookingWorkflow;
import template.StandardBookingWorkflow;

public class MovieBookingMediator implements BookingMediator {
    private final BookingDAO bookingDAO;
    private final BookingDirector bookingDirector = new BookingDirector();
    private final Map<Integer, Deque<Map<String, Object>>> draftHistory = new ConcurrentHashMap<>();

    public MovieBookingMediator(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
    }

    @Override
    public Map<String, Object> createBookingRequest(int userId, String movie, String format, int seats, String strategy, boolean groupBooking) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("accepted", false);
        if (movie == null || movie.isBlank() || format == null || format.isBlank()) {
            response.put("error", "Movie and format are required.");
            return response;
        }
        if (seats <= 0 || seats > 60) {
            response.put("error", "Seat count must be between 1 and 60.");
            return response;
        }
        Movie movieObject = MovieFactory.createMovie(format, movie);
        String selectedStrategy = strategy == null || strategy.isBlank() ? "standard" : strategy.toLowerCase();
        double total = PricingService.calculateTotal(movieObject, seats, selectedStrategy, false);
        Booking booking = bookingDirector.constructStandardBooking(userId, movie, format, seats, total, selectedStrategy);
        BookingWorkflowTemplate workflow = groupBooking && seats >= 4 ? new GroupBookingWorkflow(bookingDAO, BookingSystem.getInstance().getBookingSubject()) : new StandardBookingWorkflow(bookingDAO, BookingSystem.getInstance().getBookingSubject());
        boolean success = BookingSystem.getInstance().processBookingWithWorkflow(booking, workflow);
        response.put("accepted", success);
        response.put("bookingId", booking.getBookingId());
        response.put("total", total);
        response.put("strategy", selectedStrategy);
        return response;
    }

    @Override
    public Map<String, Object> prepareBookAgainDraft(int bookingId, int userId) {
        Booking booking = bookingDAO.findBookingById(bookingId, userId);
        Map<String, Object> draft = new LinkedHashMap<>();
        if (booking == null) {
            draft.put("error", "Booking not found.");
            return draft;
        }
        draft.put("bookingId", booking.getBookingId());
        draft.put("movie", booking.getMovieName());
        draft.put("format", booking.getMovieType());
        draft.put("seats", booking.getSeats());
        draft.put("strategy", booking.getPricingStrategy());
        draft.put("amount", booking.getTotalAmount());
        return draft;
    }

    @Override
    public Map<String, Object> saveDraftState(int userId, Map<String, Object> draftState) {
        Deque<Map<String, Object>> history = draftHistory.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        if (draftState != null) {
            history.addLast(new LinkedHashMap<>(draftState));
            if (history.size() > 10) {
                history.pollFirst();
            }
        }
        return draftState == null ? new LinkedHashMap<>() : new LinkedHashMap<>(draftState);
    }

    @Override
    public Map<String, Object> undoLastDraft(int userId) {
        Deque<Map<String, Object>> history = draftHistory.get(userId);
        if (history == null || history.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("error", "No saved draft state to undo.");
            return empty;
        }
        Map<String, Object> previous = history.pollLast();
        if (previous == null) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("error", "No saved draft state to undo.");
            return empty;
        }
        return new LinkedHashMap<>(previous);
    }

    @Override
    public Booking getBookingFromDraft(int userId) {
        Deque<Map<String, Object>> history = draftHistory.get(userId);
        if (history == null || history.isEmpty()) {
            return null;
        }
        Map<String, Object> latest = history.peekLast();
        if (latest == null) {
            return null;
        }
        return new Booking(0, userId, (String) latest.getOrDefault("movie", ""), (String) latest.getOrDefault("format", ""), Integer.parseInt(String.valueOf(latest.getOrDefault("seats", 1))), Double.parseDouble(String.valueOf(latest.getOrDefault("amount", 0.0))), "PENDING", (String) latest.getOrDefault("strategy", "standard"));
    }
}