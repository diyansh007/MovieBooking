package mediator;

import java.util.Map;

public interface BookingMediator {
    Map<String, Object> createBookingRequest(int userId, String movie, String format, int seats, String strategy, boolean groupBooking);
    Map<String, Object> prepareBookAgainDraft(int bookingId, int userId);
}