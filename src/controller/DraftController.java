package controller;

import java.util.LinkedHashMap;
import java.util.Map;
import memento.BookingDraft;
import memento.BookingDraftCaretaker;

public class DraftController {
    private final BookingDraftCaretaker caretaker = new BookingDraftCaretaker();

    public BookingDraft createDraft(String sessionKey) {
        return new BookingDraft();
    }

    public Map<String, Object> saveDraft(String sessionKey, Map<String, Object> draftState) {
        BookingDraft draft = new BookingDraft();
        if (draftState != null) {
            draft.setMovie(String.valueOf(draftState.getOrDefault("movie", "")));
            draft.setFormat(String.valueOf(draftState.getOrDefault("format", "2D")));
            draft.setSeats(Integer.parseInt(String.valueOf(draftState.getOrDefault("seats", 1))));
            draft.setStrategy(String.valueOf(draftState.getOrDefault("strategy", "standard")));
            draft.setAmount(Double.parseDouble(String.valueOf(draftState.getOrDefault("amount", 0.0))));
        }
        caretaker.save(sessionKey, draft);
        Map<String, Object> serialised = new LinkedHashMap<>();
        serialised.put("movie", draft.getMovie());
        serialised.put("format", draft.getFormat());
        serialised.put("seats", draft.getSeats());
        serialised.put("strategy", draft.getStrategy());
        serialised.put("amount", draft.getAmount());
        return serialised;
    }

    public Map<String, Object> undoDraft(String sessionKey) {
        var previous = caretaker.undo(sessionKey);
        if (previous == null) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("error", "No saved draft state to undo.");
            return empty;
        }
        return previous.getState();
    }
}