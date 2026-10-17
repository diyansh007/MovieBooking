import builder.ConcreteBookingBuilder;
import chain.PaymentHandler;
import chain.SeatAvailabilityHandler;
import dao.BookingDAO;
import dao.UserDAO;
import database.DBConnection;
import factory.MovieFactory;
import mediator.MovieBookingMediator;
import model.Booking;
import model.Movie;
import observer.AdminNotifier;
import observer.BookingSubject;
import observer.CustomerNotifier;
import observer.Observer;
import singleton.BookingSystem;
import strategy.GroupDiscountStrategy;
import strategy.PricingService;
import strategy.StandardPricingStrategy;
import strategy.StudentDiscountStrategy;
import template.StandardBookingWorkflow;

public class TestPatternValidation {
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        DBConnection.initializeDatabase();

        Movie movie = MovieFactory.createMovie("2D", "Avengers");
        require(movie != null && "2D".equals(movie.getFormatType()), "Factory should create 2D movie");
        require(MovieFactory.createMovie("3D", "Avatar") != null, "Factory should create 3D movie");
        require(MovieFactory.createMovie("IMAX", "Interstellar") != null, "Factory should create IMAX movie");

        BookingSystem first = BookingSystem.getInstance();
        BookingSystem second = BookingSystem.getInstance();
        require(first == second, "Singleton should return the same instance");

        require(new SeatAvailabilityHandler().handle(new Booking(0, 1, "Avengers", "2D", 0, 10, "PENDING")) == false, "Invalid seat count should fail chain");
        require(new PaymentHandler().handle(new Booking(0, 1, "Avengers", "2D", 2, 0, "PENDING")) == false, "Zero total should fail payment step");

        BookingSubject subject = new BookingSubject();
        subject.attach(new CustomerNotifier());
        subject.attach(new AdminNotifier());
        int[] notifications = {0};
        subject.attach(new Observer() {
            @Override
            public void update(Booking booking) {
                notifications[0]++;
            }
        });
        Booking notificationBooking = new Booking(0, 1, "Avengers", "2D", 2, 100, "CONFIRMED");
        subject.notifyObservers(notificationBooking);
        require(notifications[0] == 1, "Observer should notify subscribers once");

        BookingDAO dao = new BookingDAO();
        Booking booking = new Booking(0, 1, "Avengers", "2D", 3, 450, "PENDING", "student");
        StandardBookingWorkflow workflow = new StandardBookingWorkflow(dao, subject);
        require(workflow.execute(booking), "Standard workflow should succeed for valid booking");

        StandardPricingStrategy standard = new StandardPricingStrategy();
        StudentDiscountStrategy student = new StudentDiscountStrategy();
        GroupDiscountStrategy group = new GroupDiscountStrategy();
        require(Math.abs(standard.calculateTotal(movie, 2) - 300) < 0.01, "Standard pricing should match base calculation");
        require(Math.abs(student.calculateTotal(movie, 2) - 270) < 0.01, "Student pricing should include discount");
        require(Math.abs(group.calculateTotal(movie, 4) - 510) < 0.01, "Group pricing should discount for 4+ seats");
        require(PricingService.resolve("student").getName().equals("student"), "Pricing service should resolve named strategy");

        boolean builderFailed = false;
        try {
            new ConcreteBookingBuilder().movieName("Avengers").movieType("2D").build();
        } catch (IllegalArgumentException ex) {
            builderFailed = true;
        }
        require(builderFailed, "Builder should reject incomplete data");

        prototype.BookingDraft original = new prototype.BookingDraft(0, 1, "Interstellar", "IMAX", 4, 400, "PENDING", "weekend");
        prototype.BookingDraft clone = original.clone();
        clone.setSeats(2);
        require(original.getSeats() == 4, "Prototype clone should be an independent draft");
        require(clone.getBookingId() == 0 && "PENDING".equals(clone.getBookingStatus()), "Prototype draft should be unsaved and pending");

        memento.BookingDraft caretakerDraft = new memento.BookingDraft();
        caretakerDraft.setMovie("Interstellar");
        caretakerDraft.setFormat("IMAX");
        caretakerDraft.setSeats(4);
        caretakerDraft.setStrategy("weekend");
        memento.BookingDraftCaretaker caretaker = new memento.BookingDraftCaretaker();
        caretaker.save("test-session", caretakerDraft);
        caretakerDraft.setSeats(1);
        memento.BookingDraft restored = new memento.BookingDraft();
        restored.restore(caretaker.undo("test-session"));
        require(restored.getSeats() == 4, "Memento should restore prior draft state");

        UserDAO userDAO = new UserDAO();
        require(userDAO.authenticate("student", "pass123") != null, "Authentication should work for demo account");

        MovieBookingMediator mediator = new MovieBookingMediator(dao);
        require(mediator.prepareBookAgainDraft(1, 1).containsKey("movie") || mediator.prepareBookAgainDraft(1, 1).containsKey("error"), "Mediator should prepare draft data or error");

        System.out.println("Pattern validation tests passed.");
    }
}
