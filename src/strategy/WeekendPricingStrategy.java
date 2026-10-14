package strategy;

import model.Movie;

public class WeekendPricingStrategy implements PricingStrategy {
    @Override
    public String getName() {
        return "weekend";
    }

    @Override
    public double calculateTotal(Movie movie, int seats) {
        return movie.calculatePrice(seats) * 1.10;
    }

    @Override
    public String describeAdjustment(Movie movie, int seats) {
        return "Weekend surcharge applied: +10%.";
    }
}