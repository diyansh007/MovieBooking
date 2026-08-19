package model;

public class Booking {

    private int bookingId;
    private int userId;
    private String movieName;
    private String movieType;
    private int seats;
    private double totalAmount;
    private String bookingStatus;

    public Booking(
            int bookingId,
            int userId,
            String movieName,
            String movieType,
            int seats,
            double totalAmount,
            String bookingStatus) {

        this.bookingId = bookingId;
        this.userId = userId;
        this.movieName = movieName;
        this.movieType = movieType;
        this.seats = seats;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getMovieType() {
        return movieType;
    }

    public int getSeats() {
        return seats;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}