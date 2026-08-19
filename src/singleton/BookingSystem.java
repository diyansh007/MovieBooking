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
     * Processes a booking using Chain of Responsibility.
     */
    public boolean processBooking(Booking booking) {

        // Create handlers
        BookingHandler seatCheck =
                new SeatAvailabilityHandler();

        BookingHandler paymentCheck =
                new PaymentHandler();

        BookingHandler confirmation =
                new TicketConfirmationHandler();

        // Connect handlers
        seatCheck.setNext(paymentCheck);
        paymentCheck.setNext(confirmation);

        // Start the chain
        boolean success = seatCheck.handle(booking);

        if (!success) {
            return false;
        }

        // Save booking in database
        int bookingId =
                bookingDAO.insertBooking(booking);

        if (bookingId == -1) {
            System.out.println(
                    "Booking could not be saved.");
            return false;
        }

        /*
         * Set generated database ID.
         */
        Booking confirmedBooking = new Booking(
                bookingId,
                booking.getUserId(),
                booking.getMovieName(),
                booking.getMovieType(),
                booking.getSeats(),
                booking.getTotalAmount(),
                booking.getBookingStatus()
        );

        System.out.println();
        System.out.println("--- Notifications ---");

        // Notify Customer, Admin and Manager
        bookingSubject.notifyObservers(
                confirmedBooking);

        return true;
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

        return bookingDAO.updateBookingStatus(
                bookingId,
                userId,
                "CANCELLED");
    }
}