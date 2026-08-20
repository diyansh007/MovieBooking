# Movie Booking

A Java and SQLite movie-ticket booking project with a console application and a browser-based interface.

## Features

- Login-protected ticket booking
- Movie choices: Avengers, Interstellar, Spider-Man, and Avatar
- 2D, 3D, and IMAX formats
- Booking history and cancellation
- SQLite persistence
- Browser pages for Login, Book Ticket, View Bookings, Cancel Booking, and Exit
- A presentation-friendly Terminal Demo that runs the real console application in the browser

## Design Patterns

| Pattern | Usage |
| --- | --- |
| Factory | Creates movie objects by format |
| Singleton | Coordinates the central booking flow |
| Proxy | Controls access for authenticated users |
| Chain of Responsibility | Validates seats, payment, and ticket confirmation |
| Observer | Sends customer, admin, and manager notifications |
| DAO | Stores users and bookings in SQLite |

## Requirements

- Java 25 or later
- `sqlite-jdbc-3.53.2.1.jar` in the project root

## Run the Web App

```bash
cd /Users/diyanshwasnik/Downloads/MovieBooking
find src -name "*.java" > sources.txt
javac --add-modules jdk.httpserver -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt
java --add-modules jdk.httpserver -cp "bin:sqlite-jdbc-3.53.2.1.jar" main.WebApp
```

Open [http://localhost:8080](http://localhost:8080).

### Demo account

```text
Username: student
Password: pass123
```

## Website Pages

- `/login.html` — Login
- `/book.html` — Book Ticket
- `/bookings.html` — View Bookings
- `/cancel.html` — Cancel Booking
- `/exit.html` — Sign out
- `/terminal.html` — Live Terminal Demo (requires login)

The Terminal Demo executes only the fixed Movie Booking console command and accepts the same menu inputs as the console application. It does not accept arbitrary shell commands.

## Run the Console App

```bash
cd /Users/diyanshwasnik/Downloads/MovieBooking
find src -name "*.java" > sources.txt
javac -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt
java -cp "bin:sqlite-jdbc-3.53.2.1.jar" main.MainApp
```

## Project Layout

```text
src/
  chain/       Booking validation workflow
  dao/         SQLite data access
  database/    Database connection and setup
  factory/     Movie factory
  main/        Console and web entry points
  model/       Domain models
  observer/    Booking notifications
  proxy/       Authentication-protected booking service
  singleton/   Booking system coordinator
web/           Browser user interface
movie_booking.db
```

## Notes

- The database is initialized automatically on launch.
- Passwords are currently plaintext for demonstration purposes only. Use password hashing before deploying a real application.
- Seat availability currently validates the requested seat count (1–60); it does not model individual cinema seats or showtime capacity.
