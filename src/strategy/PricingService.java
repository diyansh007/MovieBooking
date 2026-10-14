package strategy;

import java.util.LinkedHashMap;
import java.util.Map;
import model.Movie;

public class PricingService {
    private static final Map<String, PricingStrategy> STRATEGIES = new LinkedHashMap<>();

    static {
        STRATEGIES.put("standard", new StandardPricingStrategy());
        STRATEGIES.put("student", new StudentDiscountStrategy());
        STRATEGIES.put("weekend", new WeekendPricingStrategy());
        STRATEGIES.put("group", new GroupDiscountStrategy());
    }

    public static Map<String, PricingStrategy> getSupportedStrategies() {
        return new LinkedHashMap<>(STRATEGIES);
    }

    public static PricingStrategy resolve(String strategyName) {
        if (strategyName == null || strategyName.isBlank()) {
            return STRATEGIES.get("standard");
        }
        PricingStrategy strategy = STRATEGIES.get(strategyName.toLowerCase());
        return strategy != null ? strategy : STRATEGIES.get("standard");
    }

    public static double calculateTotal(Movie movie, int seats, String strategyName, boolean weekend) {
        PricingStrategy strategy = resolve(strategyName);
        double total = strategy.calculateTotal(movie, seats);
        if (weekend && !"weekend".equals(strategy.getName())) {
            return total * 1.10;
        }
        return total;
    }

    public static String describe(Movie movie, int seats, String strategyName) {
        PricingStrategy strategy = resolve(strategyName);
        return strategy.describeAdjustment(movie, seats);
    }
}