# MovieBooking Design Patterns Lab

This project is a Java + SQLite movie-booking application that keeps the original browser-based workflow while extending it with additional design-pattern demonstrations required for the newer Design Patterns lab practicals.

## Feature set

- Login, book-ticket, booking-history, cancellation, and terminal-demo pages
- SQLite persistence with JDBC
- Built-in Java HTTP server using `com.sun.net.httpserver`
- Server-authoritative pricing with strategy selection
- Booking workflow template and chain validation
- Observer notifications for successful booking and cancellation
- Builder-based booking creation
- Book Again using a cloned draft prototype
- Draft save/undo using Memento snapshots
- Mediator-based coordination for the booking flow
- MVC-style controller separation for backend operations
- Pattern demo page at `web/patterns.html`

## Architecture overview

- Model: `src/model`, `src/factory`, `src/strategy`
- Data access: `src/dao`, `src/database`
- Business flow: `src/singleton`, `src/template`, `src/chain`, `src/observer`
- Construction and coordination: `src/builder`, `src/mediator`, `src/prototype`, `src/memento`
- Controller layer: `src/controller`
- View layer: `web/*.html`, `web/app.js`, `web/styles.css`

## Pattern-to-class mapping

| Pattern | Main classes in this project |
| --- | --- |
| Factory | `factory.MovieFactory`, `model.TwoDMovie`, `model.ThreeDMovie`, `model.ImaxMovie` |
| Singleton | `singleton.BookingSystem` |
| Chain of Responsibility | `chain.SeatAvailabilityHandler`, `chain.PaymentHandler`, `chain.TicketConfirmationHandler` |
| Observer | `observer.BookingSubject`, `observer.CustomerNotifier`, `observer.AdminNotifier`, `observer.ManagerNotifier` |
| Template Method | `template.BookingWorkflowTemplate`, `template.StandardBookingWorkflow`, `template.GroupBookingWorkflow` |
| Strategy | `strategy.PricingStrategy`, `strategy.StandardPricingStrategy`, `strategy.StudentDiscountStrategy`, `strategy.WeekendPricingStrategy`, `strategy.GroupDiscountStrategy`, `strategy.PricingService` |
| Builder | `builder.BookingBuilder`, `builder.ConcreteBookingBuilder`, `builder.BookingDirector` |
| Prototype | `prototype.BookingPrototype`, `prototype.BookingDraft` |
| Mediator | `mediator.BookingMediator`, `mediator.MovieBookingMediator` |
| Memento | `memento.BookingDraft`, `memento.BookingDraftMemento`, `memento.BookingDraftCaretaker` |
| MVC | `main.WebApp`, `controller.BookingController`, `controller.PricingController`, `controller.HistoryController`, `controller.DraftController` |
| DAO | `dao.BookingDAO`, `dao.UserDAO`, `database.DBConnection` |

## Pricing and workflow details

- Base prices are calculated in the Java model classes and the authoritative total is recalculated server-side in `PricingController` and `PricingService`.
- Browser-side price previews are not trusted; they call `/api/pricing` and display the Java-calculated result.
- The workflow template controls the real booking sequence and the chain stops invalid bookings before persistence.
- Observers are notified only after booking persistence succeeds.

## Requirements

- Java 25 or later
- `sqlite-jdbc-3.53.2.1.jar` in the project root

## Run the web app

```bash
cd /Users/diyanshwasnik/Downloads/MovieBooking
find src -name "*.java" > sources.txt
javac --add-modules jdk.httpserver -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt
java --add-modules jdk.httpserver -cp "bin:sqlite-jdbc-3.53.2.1.jar" main.WebApp
```

Open: http://localhost:8080

## Demo account

```text
Username: student
Password: pass123
```

## Pages

- `/` - home menu
- `/login.html` - login screen
- `/book.html` - book tickets with strategy selection and draft undo/save
- `/bookings.html` - booking history and Book Again action
- `/cancel.html` - cancel booking
- `/patterns.html` - design-pattern demonstration page
- `/terminal.html` - terminal demo of the console workflow

## Run the console app

```bash
cd /Users/diyanshwasnik/Downloads/MovieBooking
find src -name "*.java" > sources.txt
javac -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt
java -cp "bin:sqlite-jdbc-3.53.2.1.jar" main.MainApp
```

## Project layout

```text
src/
  builder/     Booking builder and director
  chain/       Booking validation workflow
  controller/  MVC-style HTTP controllers
  dao/         SQLite data access layer
  database/    Database connection and setup
  factory/     Movie creation via Factory pattern
  main/        Web and console entry points
  mediator/    Booking coordination through mediator
  memento/     Draft snapshots for undo
  model/       Domain model and movie types
  observer/    Notification subscribers
  prototype/   Booking cloning for Book Again
  singleton/   Shared booking system coordinator
  strategy/    Pricing policies
  template/    Booking workflow templates
  web/         Not used here; browser assets are under project web/
web/           Browser UI, CSS, JavaScript, and patterns page
movie_booking.db
```

## Database notes

- The SQLite database is initialized automatically on launch.
- The schema is additive and compatible with prior data; the `pricing_strategy` field is added when missing.
- Existing users and bookings are preserved; the app does not reset or wipe the database on start.
- Passwords are intentionally plain text for this lab environment.

## Limitations

- This is still a lab project and not a production payment system.
- The payment step is deliberately simulated and does not call a real payment gateway.
- Seat availability is still a simplified validation model rather than a full timetable/seat-map system.
