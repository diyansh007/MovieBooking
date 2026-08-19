package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.UserDAO;
import database.DBConnection;
import factory.MovieFactory;
import model.Booking;
import model.Movie;
import model.User;
import singleton.BookingSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lightweight browser UI server for the Movie Booking application. */
public class WebApp {
    private static final int PORT = 8080;
    private static final Path WEB_ROOT = Path.of("web").toAbsolutePath().normalize();
    private static final Map<String, User> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, TerminalSession> TERMINALS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        DBConnection.initializeDatabase();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api", WebApp::handleApi);
        server.createContext("/", WebApp::handleStatic);
        server.setExecutor(null);
        server.start();
        System.out.println("Movie Booking web app is running at http://localhost:" + PORT);
    }

    private static void handleApi(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            if (path.equals("/api/login") && method.equals("POST")) {
                String body = readBody(exchange);
                User user = new UserDAO().authenticate(value(body, "username"), value(body, "password"));
                if (user == null) {
                    json(exchange, 401, "{\"error\":\"Incorrect username or password.\"}");
                    return;
                }
                String token = UUID.randomUUID().toString();
                SESSIONS.put(token, user);
                json(exchange, 200, "{\"token\":\"" + token + "\",\"username\":\"" + escape(user.getUsername()) + "\"}");
                return;
            }
            if (path.equals("/api/logout") && method.equals("POST")) {
                String token = exchange.getRequestHeaders().getFirst("X-Session-Token");
                SESSIONS.remove(token);
                TerminalSession terminal = TERMINALS.remove(token);
                if (terminal != null) terminal.close();
                json(exchange, 200, "{\"ok\":true}");
                return;
            }

            User user = SESSIONS.get(exchange.getRequestHeaders().getFirst("X-Session-Token"));
            if (user == null) {
                json(exchange, 401, "{\"error\":\"Please sign in to continue.\"}");
                return;
            }
            String token = exchange.getRequestHeaders().getFirst("X-Session-Token");
            if (path.equals("/api/terminal/start") && method.equals("POST")) {
                TerminalSession oldTerminal = TERMINALS.remove(token);
                if (oldTerminal != null) oldTerminal.close();
                TerminalSession terminal = new TerminalSession();
                TERMINALS.put(token, terminal);
                terminal.start();
                json(exchange, 200, terminal.json());
                return;
            }
            if (path.equals("/api/terminal/input") && method.equals("POST")) {
                TerminalSession terminal = TERMINALS.get(token);
                if (terminal == null) {
                    json(exchange, 400, "{\"error\":\"Start the terminal demo first.\"}");
                    return;
                }
                terminal.send(value(readBody(exchange), "input"));
                json(exchange, 200, terminal.json());
                return;
            }
            if (path.equals("/api/terminal/output") && method.equals("GET")) {
                TerminalSession terminal = TERMINALS.get(token);
                if (terminal == null) {
                    json(exchange, 400, "{\"error\":\"Start the terminal demo first.\"}");
                    return;
                }
                json(exchange, 200, terminal.json());
                return;
            }
            if (path.equals("/api/bookings") && method.equals("GET")) {
                json(exchange, 200, bookingsJson(BookingSystem.getInstance().getUserBookings(user.getUserId())));
                return;
            }
            if (path.equals("/api/bookings") && method.equals("POST")) {
                String body = readBody(exchange);
                String title = value(body, "title");
                String format = value(body, "format");
                int seats = integer(value(body, "seats"));
                if (seats < 1 || seats > 60 || !validMovie(title) || !validFormat(format)) {
                    json(exchange, 400, "{\"error\":\"Choose a movie, format, and between 1 and 60 seats.\"}");
                    return;
                }
                Movie movie = MovieFactory.createMovie(format, title);
                Booking booking = new Booking(0, user.getUserId(), title, format, seats,
                        movie.calculatePrice(seats), "PENDING");
                boolean success = BookingSystem.getInstance().processBooking(booking);
                json(exchange, success ? 201 : 400, success ? "{\"message\":\"Your tickets are confirmed.\"}" : "{\"error\":\"We could not confirm this booking.\"}");
                return;
            }
            if (path.matches("/api/bookings/\\d+/cancel") && method.equals("POST")) {
                int id = Integer.parseInt(path.split("/")[3]);
                boolean changed = BookingSystem.getInstance().cancelBooking(id, user.getUserId());
                json(exchange, changed ? 200 : 404, changed ? "{\"message\":\"Booking cancelled.\"}" : "{\"error\":\"Booking not found.\"}");
                return;
            }
            json(exchange, 404, "{\"error\":\"Endpoint not found.\"}");
        } catch (Exception exception) {
            exception.printStackTrace();
            json(exchange, 500, "{\"error\":\"Something went wrong. Please try again.\"}");
        }
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) { exchange.sendResponseHeaders(405, -1); return; }
        String request = URLDecoder.decode(exchange.getRequestURI().getPath(), StandardCharsets.UTF_8);
        Path file = WEB_ROOT.resolve(request.equals("/") ? "index.html" : request.substring(1)).normalize();
        if (!file.startsWith(WEB_ROOT) || !Files.isRegularFile(file)) { exchange.sendResponseHeaders(404, -1); return; }
        String type = file.toString().endsWith(".css") ? "text/css" : file.toString().endsWith(".js") ? "application/javascript" : file.toString().endsWith(".png") ? "image/png" : "text/html";
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) { output.write(bytes); }
    }

    private static String readBody(HttpExchange exchange) throws IOException { return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); }
    private static String value(String json, String key) { Matcher m = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\\"?([^\\\",}]*)").matcher(json); return m.find() ? m.group(1).replace("\\\\\"", "\"") : ""; }
    private static int integer(String raw) { try { return Integer.parseInt(raw); } catch (NumberFormatException e) { return -1; } }
    private static boolean validMovie(String title) { return List.of("Avengers", "Interstellar", "Spider-Man", "Avatar").contains(title); }
    private static boolean validFormat(String format) { return List.of("2D", "3D", "IMAX").contains(format); }
    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    private static String bookingsJson(List<Booking> bookings) {
        StringBuilder json = new StringBuilder("{\"bookings\":[");
        for (int i = 0; i < bookings.size(); i++) { Booking b = bookings.get(i); if (i > 0) json.append(','); json.append("{\"id\":").append(b.getBookingId()).append(",\"title\":\"").append(escape(b.getMovieName())).append("\",\"format\":\"").append(escape(b.getMovieType())).append("\",\"seats\":").append(b.getSeats()).append(",\"amount\":").append(b.getTotalAmount()).append(",\"status\":\"").append(escape(b.getBookingStatus())).append("\"}"); }
        return json.append("]}").toString();
    }
    private static void json(HttpExchange exchange, int status, String data) throws IOException { byte[] bytes = data.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8"); exchange.sendResponseHeaders(status, bytes.length); try (OutputStream output = exchange.getResponseBody()) { output.write(bytes); } }

    /** Runs only the fixed console demonstration command; it never accepts shell commands from the browser. */
    private static final class TerminalSession {
        private static final String DISPLAY_COMMAND = "cd /Users/diyanshwasnik/Downloads/MovieBooking\nfind src -name \\\"*.java\\\" > sources.txt\njavac -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt\njava -cp \\\"bin:sqlite-jdbc-3.53.2.1.jar\\\" main.MainApp";
        private final StringBuilder output = new StringBuilder("$ ").append(DISPLAY_COMMAND).append("\n\n");
        private Process process;

        synchronized void start() throws IOException {
            process = new ProcessBuilder("zsh", "-lc", "find src -name '*.java' > sources.txt && javac -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt && java -cp 'bin:sqlite-jdbc-3.53.2.1.jar' main.MainApp")
                    .redirectErrorStream(true).start();
            Thread reader = new Thread(() -> copyOutput(process.getInputStream()), "movie-booking-terminal-reader");
            reader.setDaemon(true);
            reader.start();
        }

        private void copyOutput(InputStream stream) {
            try (stream) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = stream.read(buffer)) != -1) {
                    synchronized (this) { output.append(new String(buffer, 0, read, StandardCharsets.UTF_8)); }
                }
            } catch (IOException ignored) { }
        }

        synchronized void send(String input) throws IOException {
            if (process == null || !process.isAlive()) return;
            output.append(input).append('\n');
            process.getOutputStream().write((input + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
        }

        synchronized String json() { return "{\"output\":\"" + escape(output.toString()) + "\",\"running\":" + (process != null && process.isAlive()) + "}"; }
        synchronized void close() { if (process != null && process.isAlive()) process.destroyForcibly(); }
    }
}
