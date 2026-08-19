package observer;

import model.Booking;

public class CustomerNotifier implements Observer {

    @Override
    public void update(Booking booking) {

        System.out.println(
                "[CUSTOMER NOTIFICATION] " +
                "Your ticket for " +
                booking.getMovieName() +
                " has been booked.");
    }
}