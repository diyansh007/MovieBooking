package chain;

import model.Booking;

public class TicketConfirmationHandler
        extends AbstractBookingHandler {

    @Override
    public boolean handle(Booking booking) {

        System.out.println(
                "[3/3] Confirming ticket...");

        booking.setBookingStatus("CONFIRMED");

        System.out.println(
                "Ticket confirmed successfully.");

        return handleNext(booking);
    }
}