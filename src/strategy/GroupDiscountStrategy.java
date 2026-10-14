package strategy;

import model.Movie;

public class GroupDiscountStrategy implements PricingStrategy {
    @Override
    public String getName() {
        return "group";
    }

    @Override
    public double calculateTotal(Movie movie, int seats) {
        if (seats < 4) {
            return movie.calculatePrice(seats);
        }
        return movie.calculatePrice(seats) * 0.85;
    }

    @Override
    public String describeAdjustment(Movie movie, int seats) {
        return seats >= 4 ? "Group discount applied: 15% off for 4+ seats." : "Group discount requires at least 4 seats.";
    }
}