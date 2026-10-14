package template;

import dao.BookingDAO;
import model.Booking;
import observer.BookingSubject;

public class GroupBookingWorkflow extends BookingWorkflowTemplate {
    public GroupBookingWorkflow(BookingDAO bookingDAO, BookingSubject bookingSubject) {
        super(bookingDAO, bookingSubject);
    }

    @Override
    protected String getWorkflowName() {
        return "Group";
    }

    @Override
    protected boolean prepareBooking(Booking booking) {
        if (booking == null) {
            return false;
        }
        if (booking.getSeats() < 4) {
            System.out.println("[WORKFLOW] Group workflow is only eligible for 4 or more seats.");
            return false;
        }
        booking.setBookingStatus("PENDING");
        return true;
    }
}