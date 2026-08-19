package chain;

import model.Booking;

public interface BookingHandler {

    void setNext(BookingHandler nextHandler);

    boolean handle(Booking booking);
}