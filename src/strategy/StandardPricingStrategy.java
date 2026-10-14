package strategy;

import model.Movie;

public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public String getName() {
        return "standard";
    }

    @Override
    public double calculateTotal(Movie movie, int seats) {
        return movie.calculatePrice(seats);
    }

    @Override
    public String describeAdjustment(Movie movie, int seats) {
        return "Base movie rate: Rs. " + String.format("%.2f", movie.calculatePrice(seats) / Math.max(seats, 1));
    }
}