package strategy;

import model.Movie;

public interface PricingStrategy {
    String getName();

    double calculateTotal(Movie movie, int seats);

    default String describeAdjustment(Movie movie, int seats) {
        return "No additional discount or surcharge applied.";
    }
}