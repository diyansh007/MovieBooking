package model;

public class ImaxMovie extends Movie {

    public ImaxMovie(String title) {
        super(title, 350);
    }

    @Override
    public String getFormatType() {
        return "IMAX";
    }

    @Override
    public double calculatePrice(int seats) {
        return getBasePrice() * seats * 1.10;
    }
}