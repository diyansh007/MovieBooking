package model;

public abstract class Movie {

    private String title;
    private double basePrice;

    public Movie(String title, double basePrice) {
        this.title = title;
        this.basePrice = basePrice;
    }

    public String getTitle() {
        return title;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public abstract String getFormatType();

    public abstract double calculatePrice(int seats);
}