package controller;

import factory.MovieFactory;
import model.Movie;
import strategy.PricingService;
import java.util.LinkedHashMap;
import java.util.Map;

public class PricingController {
    public Map<String, Object> quote(String title, String format, int seats, String strategy) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (title == null || title.isBlank() || format == null || format.isBlank()) {
            response.put("error", "Movie and format are required.");
            return response;
        }
        if (seats <= 0 || seats > 60) {
            response.put("error", "Seat count must be between 1 and 60.");
            return response;
        }
        Movie movie = MovieFactory.createMovie(format, title);
        double base = movie.calculatePrice(seats);
        String selectedStrategy = strategy == null || strategy.isBlank() ? "standard" : strategy.toLowerCase();
        double total = PricingService.calculateTotal(movie, seats, selectedStrategy, false);
        response.put("movie", title);
        response.put("format", format);
        response.put("seats", seats);
        response.put("base", base);
        response.put("strategy", selectedStrategy);
        response.put("adjustment", total - base);
        response.put("amount", total);
        response.put("description", PricingService.describe(movie, seats, selectedStrategy));
        return response;
    }
}