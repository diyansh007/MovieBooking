package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.BookingController;
import controller.DraftController;
import controller.HistoryController;
import controller.PricingController;
import dao.BookingDAO;
import dao.UserDAO;
import database.DBConnection;
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
import java.util.LinkedHashMap;
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
    private static final BookingDAO BOOKING_DAO = new BookingDAO();
    private static final BookingController BOOKING_CONTROLLER = new BookingController(BOOKING_DAO);
    private static final HistoryController HISTORY_CONTROLLER = new HistoryController(BOOKING_DAO);
    private static final PricingController PRICING_CONTROLLER = new PricingController();
    private static final DraftController DRAFT_CONTROLLER = new DraftController();

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

            String token = exchange.getRequestHeaders().getFirst("X-Session-Token");
            if (token == null || token.isBlank()) {
                json(exchange, 401, "{\"error\":\"Please sign in to continue.\"}");
                return;
            }
            User user = SESSIONS.get(token);
            if (user == null) {
                json(exchange, 401, "{\"error\":\"Please sign in to continue.\"}");
                return;
            }

            if (path.equals("/api/pricing") && method.equals("POST")) {
                String body = readBody(exchange);
                Map<String, Object> quote = PRICING_CONTROLLER.quote(value(body, "title"), value(body, "format"), integer(value(body, "seats")), value(body, "strategy"));
                json(exchange, quote.containsKey("error") ? 400 : 200, mapToJson(quote));
                return;
            }

            if (path.equals("/api/drafts/save") && method.equals("POST")) {
                String body = readBody(exchange);
                Map<String, Object> saved = DRAFT_CONTROLLER.saveDraft(token, draftFromJson(body));
                json(exchange, 200, mapToJson(saved));
                return;
            }

            if (path.equals("/api/drafts/undo") && method.equals("POST")) {
                Map<String, Object> restored = DRAFT_CONTROLLER.undoDraft(token);
                json(exchange, restored.containsKey("error") ? 400 : 200, mapToJson(restored));
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
                String strategy = value(body, "strategy");
                Map<String, Object> result = BOOKING_CONTROLLER.createBooking(user.getUserId(), title, format, seats, strategy);
                if (result.containsKey("error")) {
                    json(exchange, 400, mapToJson(result));
                    return;
                }
                json(exchange, (Boolean) result.getOrDefault("success", false) ? 201 : 400, mapToJson(result));
                return;
            }
            if (path.matches("/api/bookings/\\d+/draft") && method.equals("POST")) {
                int id = Integer.parseInt(path.split("/")[3]);
                Map<String, Object> draft = HISTORY_CONTROLLER.createDraft(id, user.getUserId());
                json(exchange, draft.containsKey("error") ? 404 : 200, mapToJson(draft));
                return;
            }
            if (path.matches("/api/bookings/\\d+/cancel") && method.equals("POST")) {
                int id = Integer.parseInt(path.split("/")[3]);
                Map<String, Object> result = BOOKING_CONTROLLER.handleCancel(id, user.getUserId());
                json(exchange, (Boolean) result.getOrDefault("success", false) ? 200 : 404, mapToJson(result));
                return;
            }
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
    private static String value(String body, String key) {
        if (body == null || body.isBlank()) return "";
        Matcher jsonMatcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\\"?([^\\\",}]*)").matcher(body);
        if (jsonMatcher.find()) {
            return jsonMatcher.group(1).replace("\\\\\"", "\"");
        }
        String formKey = key + "=";
        int index = body.indexOf(formKey);
        if (index >= 0) {
            int start = index + formKey.length();
            int end = body.indexOf('&', start);
            if (end < 0) end = body.length();
            String value = body.substring(start, end);
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
        return "";
    }
    private static int integer(String raw) { try { return Integer.parseInt(raw == null ? "" : raw.trim()); } catch (NumberFormatException e) { return -1; } }
    private static double decimal(String raw) { try { return Double.parseDouble(raw == null ? "" : raw.trim()); } catch (NumberFormatException e) { return 0.0; } }
    private static Map<String, Object> draftFromJson(String body) {
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("movie", value(body, "movie"));
        draft.put("format", value(body, "format"));
        draft.put("seats", integer(value(body, "seats")));
        draft.put("strategy", value(body, "strategy"));
        draft.put("amount", decimal(value(body, "amount")));
        return draft;
    }
    private static String mapToJson(Map<String, Object> values) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) json.append(',');
            first = false;
            json.append('"').append(entry.getKey()).append('"').append(':');
            Object value = entry.getValue();
            if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value == null) {
                json.append("null");
            } else if (value instanceof java.util.List) {
                json.append(listToJson((java.util.List<?>) value));
            } else {
                json.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        return json.append('}').toString();
    }
    private static String listToJson(java.util.List<?> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) json.append(',');
            Object value = values.get(i);
            if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else {
                json.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        return json.append(']').toString();
    }
    private static String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    private static String bookingsJson(List<model.Booking> bookings) {
        StringBuilder json = new StringBuilder("{\"bookings\":[");
        for (int i = 0; i < bookings.size(); i++) {
            model.Booking b = bookings.get(i);
            if (i > 0) json.append(',');
            json.append("{\"id\":").append(b.getBookingId())
                    .append(",\"title\":\"").append(escape(b.getMovieName())).append("\"")
                    .append(",\"format\":\"").append(escape(b.getMovieType())).append("\"")
                    .append(",\"seats\":").append(b.getSeats())
                    .append(",\"amount\":").append(b.getTotalAmount())
                    .append(",\"status\":\"").append(escape(b.getBookingStatus())).append("\"")
                    .append(",\"strategy\":\"").append(escape(b.getPricingStrategy())).append("\"}");
        }
        return json.append("]}").toString();
    }
    private static void json(HttpExchange exchange, int status, String data) throws IOException { byte[] bytes = data.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8"); exchange.sendResponseHeaders(status, bytes.length); try (OutputStream output = exchange.getResponseBody()) { output.write(bytes); } }

    /** Runs only the fixed console demonstration command; it never accepts shell commands from the browser. */
    private static final class TerminalSession {
        private static final String DISPLAY_COMMAND = "cd /Users/diyanshwasnik/Downloads/MovieBooking\nfind src -name \"*.java\" > sources.txt\njavac -cp sqlite-jdbc-3.53.2.1.jar -d bin @sources.txt\njava -cp \"bin:sqlite-jdbc-3.53.2.1.jar\" main.MainApp";
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
