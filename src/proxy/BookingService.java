package proxy;

import model.Movie;

public interface BookingService {
    void bookTicket(Movie movie, int seats);
    void viewBookings();
    void cancelBooking(int bookingId);
}