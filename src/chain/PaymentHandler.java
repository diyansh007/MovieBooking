package chain;

import model.Booking;

public class PaymentHandler
        extends AbstractBookingHandler {

    @Override
    public boolean handle(Booking booking) {

        System.out.println(
                "[2/3] Verifying payment...");

        if (booking.getTotalAmount() <= 0) {

            System.out.println(
                    "Payment failed.");

            return false;
        }

        System.out.println(
                "Payment verified successfully.");

        return handleNext(booking);
    }
}