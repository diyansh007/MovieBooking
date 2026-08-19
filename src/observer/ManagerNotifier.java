package observer;

import model.Booking;

public class ManagerNotifier implements Observer {

    @Override
    public void update(Booking booking) {

        System.out.println(
                "[THEATER MANAGER NOTIFICATION] " +
                booking.getSeats() +
                " seats booked for " +
                booking.getMovieName());
    }
}