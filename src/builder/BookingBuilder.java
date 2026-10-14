package builder;

import model.Booking;

public interface BookingBuilder {
    BookingBuilder userId(int userId);
    BookingBuilder movieName(String movieName);
    BookingBuilder movieType(String movieType);
    BookingBuilder seats(int seats);
    BookingBuilder totalAmount(double totalAmount);
    BookingBuilder bookingStatus(String bookingStatus);
    BookingBuilder pricingStrategy(String pricingStrategy);
    Booking build();
}