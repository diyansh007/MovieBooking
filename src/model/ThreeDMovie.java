package model;

public class ThreeDMovie extends Movie {

    public ThreeDMovie(String title) {
        super(title, 220);
    }

    @Override
    public String getFormatType() {
        return "3D";
    }

    @Override
    public double calculatePrice(int seats) {
        return (getBasePrice() + 30) * seats;
    }
}