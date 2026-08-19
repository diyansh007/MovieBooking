package observer;

import model.Booking;

public class AdminNotifier implements Observer {

    @Override
    public void update(Booking booking) {

        System.out.println(
                "[ADMIN NOTIFICATION] " +
                "New booking ID: " +
                booking.getBookingId());
    }
}