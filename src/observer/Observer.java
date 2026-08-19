package observer;

import model.Booking;

public interface Observer {

    void update(Booking booking);
}