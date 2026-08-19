package chain;

import model.Booking;

public class SeatAvailabilityHandler
        extends AbstractBookingHandler {

    private static final int MAX_SEATS = 60;

    @Override
    public boolean handle(Booking booking) {

        System.out.println(
                "[1/3] Checking seat availability...");

        if (booking.getSeats() <= 0) {

            System.out.println(
                    "Booking failed: Number of seats must be greater than 0.");

            return false;
        }

        if (booking.getSeats() > MAX_SEATS) {

            System.out.println(
                    "Booking failed: Maximum 60 seats allowed.");

            return false;
        }

        System.out.println("Seats are available.");

        return handleNext(booking);
    }
}