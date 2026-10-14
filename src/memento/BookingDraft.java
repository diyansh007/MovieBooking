package memento;

import java.util.LinkedHashMap;
import java.util.Map;

public class BookingDraft {
    private String movie;
    private String format;
    private int seats;
    private String strategy;
    private double amount;

    public BookingDraft() {
        this.movie = "";
        this.format = "2D";
        this.seats = 1;
        this.strategy = "standard";
        this.amount = 0.0;
    }

    public void setMovie(String movie) { this.movie = movie; }
    public String getMovie() { return movie; }
    public void setFormat(String format) { this.format = format; }
    public String getFormat() { return format; }
    public void setSeats(int seats) { this.seats = seats; }
    public int getSeats() { return seats; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public String getStrategy() { return strategy; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getAmount() { return amount; }

    public BookingDraftMemento save() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("movie", movie);
        state.put("format", format);
        state.put("seats", seats);
        state.put("strategy", strategy);
        state.put("amount", amount);
        return new BookingDraftMemento(state);
    }

    public void restore(BookingDraftMemento memento) {
        if (memento == null) {
            return;
        }
        Map<String, Object> state = memento.getState();
        movie = String.valueOf(state.getOrDefault("movie", movie));
        format = String.valueOf(state.getOrDefault("format", format));
        seats = Integer.parseInt(String.valueOf(state.getOrDefault("seats", seats)));
        strategy = String.valueOf(state.getOrDefault("strategy", strategy));
        amount = Double.parseDouble(String.valueOf(state.getOrDefault("amount", amount)));
    }
}