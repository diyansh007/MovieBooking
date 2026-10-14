package singleton;

import chain.BookingHandler;
import chain.PaymentHandler;
import chain.SeatAvailabilityHandler;
import chain.TicketConfirmationHandler;
import dao.BookingDAO;
import model.Booking;
import observer.AdminNotifier;
import observer.BookingSubject;
import observer.CustomerNotifier;
import observer.ManagerNotifier;
import template.BookingWorkflowTemplate;
import template.StandardBookingWorkflow;

import java.util.List;

/*
 * Singleton Pattern
 *
 * Only one BookingSystem object is created.
 * It controls the main booking process.
 */
public class BookingSystem {

    private static BookingSystem instance;

    private final BookingDAO bookingDAO;
    private final BookingSubject bookingSubject;

    /*
     * Private constructor prevents other classes
     * from creating BookingSystem objects directly.
     */
    private BookingSystem() {

        bookingDAO = new BookingDAO();

        bookingSubject = new BookingSubject();

        // Register observers
        bookingSubject.attach(new CustomerNotifier());
        bookingSubject.attach(new AdminNotifier());
        bookingSubject.attach(new ManagerNotifier());
    }

    /*
     * Returns the single BookingSystem object.
     */
    public static synchronized BookingSystem getInstance() {

        if (instance == null) {
            instance = new BookingSystem();
        }

        return instance;
    }

    /*
     * Processes a booking using Template Method + Chain of Responsibility.
     */
    public boolean processBooking(Booking booking) {
        BookingWorkflowTemplate workflow = new StandardBookingWorkflow(bookingDAO, bookingSubject);
        return workflow.execute(booking);
    }

    public boolean processBookingWithWorkflow(Booking booking, BookingWorkflowTemplate workflow) {
        if (workflow == null) {
            return processBooking(booking);
        }
        return workflow.execute(booking);
    }

    /*
     * Returns bookings of the logged-in user.
     */
    public List<Booking> getUserBookings(int userId) {

        return bookingDAO.getBookingsByUserId(userId);
    }

    /*
     * Cancels a booking.
     */
    public boolean cancelBooking(
            int bookingId,
            int userId) {

        Booking existing = bookingDAO.findBookingById(bookingId, userId);
        boolean updated = bookingDAO.updateBookingStatus(
                bookingId,
                userId,
                "CANCELLED");

        if (updated && existing != null) {
            Booking cancelledBooking = new Booking(
                    existing.getBookingId(),
                    existing.getUserId(),
                    existing.getMovieName(),
                    existing.getMovieType(),
                    existing.getSeats(),
                    existing.getTotalAmount(),
                    "CANCELLED",
                    existing.getPricingStrategy());
            bookingSubject.notifyObservers(cancelledBooking);
        }

        return updated;
    }

    public BookingSubject getBookingSubject() {
        return bookingSubject;
    }
}