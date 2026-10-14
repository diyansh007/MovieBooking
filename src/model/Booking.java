package model;

public class Booking {

    private int bookingId;
    private int userId;
    private String movieName;
    private String movieType;
    private int seats;
    private double totalAmount;
    private String bookingStatus;
    private String pricingStrategy;

    public Booking(
            int bookingId,
            int userId,
            String movieName,
            String movieType,
            int seats,
            double totalAmount,
            String bookingStatus) {
        this(bookingId, userId, movieName, movieType, seats, totalAmount, bookingStatus, "standard");
    }

    public Booking(
            int bookingId,
            int userId,
            String movieName,
            String movieType,
            int seats,
            double totalAmount,
            String bookingStatus,
            String pricingStrategy) {

        this.bookingId = bookingId;
        this.userId = userId;
        this.movieName = movieName;
        this.movieType = movieType;
        this.seats = seats;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.pricingStrategy = pricingStrategy == null || pricingStrategy.isBlank() ? "standard" : pricingStrategy;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieType() {
        return movieType;
    }

    public void setMovieType(String movieType) {
        this.movieType = movieType;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public String getPricingStrategy() {
        return pricingStrategy;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public void setPricingStrategy(String pricingStrategy) {
        this.pricingStrategy = pricingStrategy == null || pricingStrategy.isBlank() ? "standard" : pricingStrategy;
    }
}