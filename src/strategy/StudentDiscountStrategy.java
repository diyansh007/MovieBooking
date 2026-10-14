package strategy;

import model.Movie;

public class StudentDiscountStrategy implements PricingStrategy {
    @Override
    public String getName() {
        return "student";
    }

    @Override
    public double calculateTotal(Movie movie, int seats) {
        double base = movie.calculatePrice(seats);
        return base * 0.9;
    }

    @Override
    public String describeAdjustment(Movie movie, int seats) {
        return "10% student discount applied to the base price.";
    }
}