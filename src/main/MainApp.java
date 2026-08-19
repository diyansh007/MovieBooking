package main;

import database.DBConnection;
import dao.UserDAO;
import factory.MovieFactory;
import model.Movie;
import model.User;
import proxy.BookingProxy;

import java.util.Scanner;

public class MainApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BookingProxy bookingProxy = new BookingProxy();
    private static final UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
        // Ensure the SQLite database and tables exist before any operations
        DBConnection.initializeDatabase();

        while (true) {
            System.out.println("\n======== Movie Ticket Booking ========");
            System.out.println("1. Login");
            System.out.println("2. Book Ticket");
            System.out.println("3. View Bookings");
            System.out.println("4. Cancel Booking");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            int choice = readIntInput();

            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleBookTicket();
                    break;
                case 3:
                    bookingProxy.viewBookings();
                    break;
                case 4:
                    handleCancelBooking();
                    break;
                case 5:
                    System.out.println("Thank you for using Movie Ticket Booking System!");
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void handleLogin() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            bookingProxy.setCurrentUser(user);
            System.out.println("Login successful! Welcome, " + user.getUsername() + ".");
        } else {
            System.err.println("Authentication failed. Invalid username or password.");
        }
    }

    private static void handleBookTicket() {
        if (!bookingProxy.isAuthenticated()) {
            System.err.println("ACCESS DENIED: Please log in first.");
            return;
        }

        System.out.println("\n--- Select Movie ---");
        System.out.println("1. Avengers");
        System.out.println("2. Interstellar");
        System.out.println("3. Spider-Man");
        System.out.println("4. Avatar");
        System.out.print("Choice: ");
        int movieChoice = readIntInput();

        String movieTitle = switch (movieChoice) {
            case 1 -> "Avengers";
            case 2 -> "Interstellar";
            case 3 -> "Spider-Man";
            case 4 -> "Avatar";
            default -> null;
        };

        if (movieTitle == null) {
            System.out.println("Invalid movie selection.");
            return;
        }

        System.out.println("\n--- Select Format ---");
        System.out.println("1. 2D");
        System.out.println("2. 3D");
        System.out.println("3. IMAX");
        System.out.print("Choice: ");
        int formatChoice = readIntInput();

        String format = switch (formatChoice) {
            case 1 -> "2D";
            case 2 -> "3D";
            case 3 -> "IMAX";
            default -> null;
        };

        if (format == null) {
            System.out.println("Invalid format selection.");
            return;
        }

        System.out.print("Enter number of seats: ");
        int seats = readIntInput();

        Movie movie = MovieFactory.createMovie(format, movieTitle);
        double totalPrice = movie.calculatePrice(seats);

        System.out.printf("Total calculated price: Rs. %.2f\n", totalPrice);
        System.out.print("Confirm booking? (Y/N): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("Y")) {
            bookingProxy.bookTicket(movie, seats);
        } else {
            System.out.println("Booking order aborted.");
        }
    }

    private static void handleCancelBooking() {
        if (!bookingProxy.isAuthenticated()) {
            System.err.println("ACCESS DENIED: Please log in first.");
            return;
        }

        System.out.print("Enter Booking ID to cancel: ");
        int bookingId = readIntInput();
        bookingProxy.cancelBooking(bookingId);
    }

    private static int readIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
