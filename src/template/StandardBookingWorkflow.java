package template;

import dao.BookingDAO;
import model.Booking;
import observer.BookingSubject;

public class StandardBookingWorkflow extends BookingWorkflowTemplate {
    public StandardBookingWorkflow(BookingDAO bookingDAO, BookingSubject bookingSubject) {
        super(bookingDAO, bookingSubject);
    }

    @Override
    protected String getWorkflowName() {
        return "Standard";
    }

    @Override
    protected boolean prepareBooking(Booking booking) {
        if (booking == null) {
            return false;
        }
        if (booking.getTotalAmount() <= 0) {
            System.out.println("[WORKFLOW] Booking total must be positive before checkout.");
            return false;
        }
        booking.setBookingStatus("PENDING");
        return true;
    }
}