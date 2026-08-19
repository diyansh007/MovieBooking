package model;

public class TwoDMovie extends Movie {

    public TwoDMovie(String title) {
        super(title, 150);
    }

    @Override
    public String getFormatType() {
        return "2D";
    }

    @Override
    public double calculatePrice(int seats) {
        return getBasePrice() * seats;
    }
}