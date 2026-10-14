package template;

import chain.AbstractBookingHandler;
import chain.BookingHandler;
import chain.PaymentHandler;
import chain.SeatAvailabilityHandler;
import chain.TicketConfirmationHandler;
import dao.BookingDAO;
import model.Booking;
import observer.BookingSubject;

public abstract class BookingWorkflowTemplate {
    protected final BookingDAO bookingDAO;
    protected final BookingSubject bookingSubject;

    protected BookingWorkflowTemplate(BookingDAO bookingDAO, BookingSubject bookingSubject) {
        this.bookingDAO = bookingDAO;
        this.bookingSubject = bookingSubject;
    }

    public final boolean execute(Booking booking) {
        System.out.println("[WORKFLOW] Starting " + getWorkflowName() + " booking flow.");
        if (!prepareBooking(booking)) {
            return false;
        }

        BookingHandler chain = createHandlerChain();
        if (!chain.handle(booking)) {
            System.out.println("[WORKFLOW] Validation chain rejected the booking before persistence.");
            return false;
        }

        if (!persistBooking(booking)) {
            return false;
        }

        if (!notifyObservers(booking)) {
            return false;
        }

        return finalizeBooking(booking);
    }

    protected abstract String getWorkflowName();

    protected boolean prepareBooking(Booking booking) {
        return booking != null && booking.getSeats() > 0;
    }

    protected BookingHandler createHandlerChain() {
        BookingHandler seatCheck = new SeatAvailabilityHandler();
        BookingHandler paymentCheck = new PaymentHandler();
        BookingHandler confirmation = new TicketConfirmationHandler();
        seatCheck.setNext(paymentCheck);
        paymentCheck.setNext(confirmation);
        return seatCheck;
    }

    protected boolean persistBooking(Booking booking) {
        int bookingId = bookingDAO.insertBooking(booking);
        if (bookingId == -1) {
            System.out.println("Booking could not be saved.");
            return false;
        }
        booking.setBookingStatus("CONFIRMED");
        return true;
    }

    protected boolean notifyObservers(Booking booking) {
        System.out.println();
        System.out.println("--- Notifications ---");
        bookingSubject.notifyObservers(booking);
        return true;
    }

    protected boolean finalizeBooking(Booking booking) {
        System.out.println("[WORKFLOW] " + getWorkflowName() + " completed successfully.");
        return true;
    }
}