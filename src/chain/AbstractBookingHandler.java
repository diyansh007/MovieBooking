package chain;

import model.Booking;

public abstract class AbstractBookingHandler
        implements BookingHandler {

    private BookingHandler next;

    @Override
    public void setNext(BookingHandler nextHandler) {
        this.next = nextHandler;
    }

    protected boolean handleNext(Booking booking) {

        if (next == null) {
            return true;
        }

        return next.handle(booking);
    }
}