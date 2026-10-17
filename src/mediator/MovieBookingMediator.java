package mediator;

import builder.BookingDirector;
import dao.BookingDAO;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public MovieBookingMediator(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
    }

    @Override
    public Map<String, Object> createBookingRequest(int userId, String movie, String format, int seats, String strategy, boolean groupBooking) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("accepted", false);
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
            if (!PricingService.getSupportedStrategies().containsKey(selectedStrategy)) {
                response.put("error", "Unsupported pricing strategy.");
                return response;
            }
            double total = PricingService.calculateTotal(movieObject, seats, selectedStrategy, false);
            Booking booking = bookingDirector.constructStandardBooking(userId, movie, format, seats, total, selectedStrategy);
            BookingWorkflowTemplate workflow = groupBooking && seats >= 4
                    ? new GroupBookingWorkflow(bookingDAO, BookingSystem.getInstance().getBookingSubject())
                    : new StandardBookingWorkflow(bookingDAO, BookingSystem.getInstance().getBookingSubject());
            boolean success = BookingSystem.getInstance().processBookingWithWorkflow(booking, workflow);
            response.put("accepted", success);
            response.put("bookingId", booking.getBookingId());
            response.put("total", total);
            response.put("strategy", selectedStrategy);
        } catch (IllegalArgumentException exception) {
            response.put("error", exception.getMessage());
        }
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
        BookingDraft prototype = new BookingDraft(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getMovieName(),
                booking.getMovieType(),
                booking.getSeats(),
                booking.getTotalAmount(),
                booking.getBookingStatus(),
                booking.getPricingStrategy());
        BookingDraft clonedDraft = prototype.clone();
        draft.put("sourceBookingId", booking.getBookingId());
        draft.put("movie", clonedDraft.getMovieName());
        draft.put("format", clonedDraft.getMovieType());
        draft.put("seats", clonedDraft.getSeats());
        draft.put("strategy", clonedDraft.getPricingStrategy());
        draft.put("amount", clonedDraft.getTotalAmount());
        draft.put("draft", true);
        return draft;
    }
}