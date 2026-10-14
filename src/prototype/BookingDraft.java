package prototype;

import model.Booking;

public class BookingDraft extends Booking implements BookingPrototype<BookingDraft> {
    private String pricingPolicy;

    public BookingDraft(int bookingId, int userId, String movieName, String movieType, int seats, double totalAmount, String bookingStatus, String pricingStrategy) {
        super(bookingId, userId, movieName, movieType, seats, totalAmount, bookingStatus, pricingStrategy);
        this.pricingPolicy = pricingStrategy;
    }

    public String getPricingPolicy() {
        return pricingPolicy;
    }

    public void setPricingPolicy(String pricingPolicy) {
        this.pricingPolicy = pricingPolicy == null || pricingPolicy.isBlank() ? "standard" : pricingPolicy;
        super.setPricingStrategy(this.pricingPolicy);
    }

    @Override
    public BookingDraft clone() {
        BookingDraft draft = new BookingDraft(
                this.getBookingId(),
                this.getUserId(),
                this.getMovieName(),
                this.getMovieType(),
                this.getSeats(),
                this.getTotalAmount(),
                this.getBookingStatus(),
                this.getPricingStrategy());
        draft.setPricingPolicy(this.getPricingStrategy());
        return draft;
    }
}