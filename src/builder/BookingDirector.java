package builder;

import model.Booking;

public class BookingDirector {
    public Booking constructStandardBooking(int userId, String movieName, String movieType, int seats, double totalAmount, String pricingStrategy) {
        return new ConcreteBookingBuilder()
                .userId(userId)
                .movieName(movieName)
                .movieType(movieType)
                .seats(seats)
                .totalAmount(totalAmount)
                .bookingStatus("PENDING")
                .pricingStrategy(pricingStrategy)
                .build();
    }
}