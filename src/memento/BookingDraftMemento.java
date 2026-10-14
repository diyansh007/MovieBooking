package memento;

import java.util.Map;

public class BookingDraftMemento {
    private final Map<String, Object> state;

    public BookingDraftMemento(Map<String, Object> state) {
        this.state = new java.util.LinkedHashMap<>(state);
    }

    public Map<String, Object> getState() {
        return new java.util.LinkedHashMap<>(state);
    }
}