package builder;

import model.Booking;

public class ConcreteBookingBuilder implements BookingBuilder {
    private int bookingId;
    private int userId;
    private String movieName;
    private String movieType;
    private int seats;
    private double totalAmount;
    private String bookingStatus = "PENDING";
    private String pricingStrategy = "standard";

    public ConcreteBookingBuilder() {
    }

    public ConcreteBookingBuilder bookingId(int bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    @Override
    public BookingBuilder userId(int userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public BookingBuilder movieName(String movieName) {
        this.movieName = movieName;
        return this;
    }

    @Override
    public BookingBuilder movieType(String movieType) {
        this.movieType = movieType;
        return this;
    }

    @Override
    public BookingBuilder seats(int seats) {
        this.seats = seats;
        return this;
    }

    @Override
    public BookingBuilder totalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    @Override
    public BookingBuilder bookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
        return this;
    }

    @Override
    public BookingBuilder pricingStrategy(String pricingStrategy) {
        this.pricingStrategy = pricingStrategy == null || pricingStrategy.isBlank() ? "standard" : pricingStrategy;
        return this;
    }

    @Override
    public Booking build() {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (movieName == null || movieName.isBlank()) {
            throw new IllegalArgumentException("Movie title is required.");
        }
        if (movieType == null || movieType.isBlank()) {
            throw new IllegalArgumentException("Movie format is required.");
        }
        if (seats <= 0 || seats > 60) {
            throw new IllegalArgumentException("Seat count must be between 1 and 60.");
        }
        return new Booking(bookingId, userId, movieName, movieType, seats, totalAmount, bookingStatus, pricingStrategy);
    }
}