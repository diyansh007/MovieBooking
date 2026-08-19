package observer;

import model.Booking;
import java.util.ArrayList;
import java.util.List;

public class BookingSubject {

    private final List<Observer> observers =
            new ArrayList<>();

    public void attach(Observer observer) {
        observers.add(observer);
    }

    public void detach(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Booking booking) {

        for (Observer observer : observers) {
            observer.update(booking);
        }
    }
}