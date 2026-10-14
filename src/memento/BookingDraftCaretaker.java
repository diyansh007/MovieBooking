package memento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingDraftCaretaker {
    private final Map<String, Deque<BookingDraftMemento>> sessionHistory = new ConcurrentHashMap<>();

    public void save(String sessionKey, BookingDraft originator) {
        if (sessionKey == null || sessionKey.isBlank() || originator == null) {
            return;
        }
        Deque<BookingDraftMemento> history = sessionHistory.computeIfAbsent(sessionKey, ignored -> new ArrayDeque<>());
        history.addLast(originator.save());
        if (history.size() > 8) {
            history.pollFirst();
        }
    }

    public BookingDraftMemento undo(String sessionKey) {
        Deque<BookingDraftMemento> history = sessionHistory.get(sessionKey);
        if (history == null || history.isEmpty()) {
            return null;
        }
        BookingDraftMemento last = history.pollLast();
        return last;
    }

    public void clear(String sessionKey) {
        if (sessionKey != null && !sessionKey.isBlank()) {
            sessionHistory.remove(sessionKey);
        }
    }
}