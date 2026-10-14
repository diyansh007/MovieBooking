package mediator;

import java.util.Map;
import model.Booking;

public interface BookingMediator {
    Map<String, Object> createBookingRequest(int userId, String movie, String format, int seats, String strategy, boolean groupBooking);
    Map<String, Object> prepareBookAgainDraft(int bookingId, int userId);
    Map<String, Object> saveDraftState(int userId, Map<String, Object> draftState);
    Map<String, Object> undoLastDraft(int userId);
    Booking getBookingFromDraft(int userId);
}