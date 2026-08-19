package proxy;

import model.Booking;
import model.Movie;
import model.User;
import singleton.BookingSystem;

import java.util.List;

/**
 * Proxy Pattern: Controls access by verifying user session state.
 */
public class BookingProxy implements BookingService {
    private final BookingSystem bookingSystem;
    private User currentUser;

    public BookingProxy() {
        this.bookingSystem = BookingSystem.getInstance();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }

    @Override
    public void bookTicket(Movie movie, int seats) {
        if (!isAuthenticated()) {
            System.err.println("ACCESS DENIED: Please log in first to book tickets.");
            return;
        }

        double totalAmount = movie.calculatePrice(seats);
        Booking booking = new Booking(0, currentUser.getUserId(), movie.getTitle(), movie.getFormatType(), seats, totalAmount, "PENDING");
        
        bookingSystem.processBooking(booking);
    }

    @Override
    public void viewBookings() {
        if (!isAuthenticated()) {
            System.err.println("ACCESS DENIED: Please log in first to view bookings.");
            return;
        }

        List<Booking> bookings = bookingSystem.getUserBookings(currentUser.getUserId());
        if (bookings.isEmpty()) {
            System.out.println("No booking history found.");
            return;
        }

        System.out.println("\n================ Your Bookings ================");
        System.out.printf("%-10s %-15s %-8s %-6s %-10s %-10s\n", "ID", "Movie", "Type", "Seats", "Amount", "Status");
        System.out.println("---------------------------------------------------------------");
        for (Booking b : bookings) {
            System.out.printf("%-10d %-15s %-8s %-6d Rs.%-7.2f %-10s\n", 
                    b.getBookingId(), b.getMovieName(), b.getMovieType(), b.getSeats(), b.getTotalAmount(), b.getBookingStatus());
        }
    }

    @Override
    public void cancelBooking(int bookingId) {
        if (!isAuthenticated()) {
            System.err.println("ACCESS DENIED: Please log in first to cancel bookings.");
            return;
        }

        boolean updated = bookingSystem.cancelBooking(bookingId, currentUser.getUserId());
        if (updated) {
            System.out.println("Booking #" + bookingId + " status changed to CANCELLED.");
        } else {
            System.err.println("Failed to cancel booking ID #" + bookingId + ". Check if ID is valid.");
        }
    }
}