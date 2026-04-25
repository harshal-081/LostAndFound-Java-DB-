import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LostFoundWebServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new FrontendHandler());
        server.createContext("/api/reports", new ReportsHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Lost & Found web server started.");
        System.out.println("Open http://localhost:" + PORT + "/ in your browser.");
    }

    private static class FrontendHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestURI().getPath().startsWith("/api/")) {
                sendJson(exchange, 404, "{\"error\":\"API endpoint not found\"}");
                return;
            }

            byte[] html = Files.readAllBytes(Paths.get("lost_found_frontend.html"));
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html);
            }
        }
    }

    private static class ReportsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");

                if (parts.length == 3) {
                    if ("GET".equals(method)) {
                        sendJson(exchange, 200, listReports());
                        return;
                    }
                    if ("POST".equals(method)) {
                        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        sendJson(exchange, 201, addReport(body));
                        return;
                    }
                }

                if (parts.length >= 4) {
                    int reportId = parseReportId(parts[3]);
                    if ("GET".equals(method) && parts.length == 4) {
                        String report = findReport(reportId);
                        if (report == null) {
                            sendJson(exchange, 404, "{\"error\":\"Report not found\"}");
                        } else {
                            sendJson(exchange, 200, report);
                        }
                        return;
                    }
                    if ("DELETE".equals(method) && parts.length == 4) {
                        deleteReport(reportId);
                        sendJson(exchange, 200, "{\"message\":\"Report deleted\"}");
                        return;
                    }
                    if ("PUT".equals(method) && parts.length == 5 && "status".equals(parts[4])) {
                        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        sendJson(exchange, 200, updateStatus(reportId, body));
                        return;
                    }
                }

                sendJson(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, "{\"error\":" + quote(e.getMessage()) + "}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":" + quote(e.getMessage()) + "}");
            }
        }
    }

    private static String listReports() throws Exception {
        String sql = "SELECT report_id, name, age, gender, address, phone_no, report_date, location, " +
                     "description, status, reporter_name, contact_no, relationship, status_remarks " +
                     "FROM reports ORDER BY report_id";
        List<String> items = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                items.add(reportJson(rs));
            }
        }
        return "[" + String.join(",", items) + "]";
    }

    private static String findReport(int reportId) throws Exception {
        String sql = "SELECT report_id, name, age, gender, address, phone_no, report_date, location, " +
                     "description, status, reporter_name, contact_no, relationship, status_remarks " +
                     "FROM reports WHERE report_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? reportJson(rs) : null;
            }
        }
    }

    private static String addReport(String body) throws Exception {
        String name = required(body, "name");
        int age = intValue(body, "age");
        String gender = required(body, "gender");
        String address = required(body, "address");
        long phoneNo = longValue(body, "phoneNo");
        Date reportDate = Date.valueOf(required(body, "date"));
        String location = required(body, "location");
        String description = value(body, "description");
        String reporter = required(body, "reporter");
        String contact = required(body, "contact");
        String relationship = value(body, "relationship");

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_add_report(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, name);
            cs.setInt(2, age);
            cs.setString(3, gender);
            cs.setString(4, address);
            cs.setLong(5, phoneNo);
            cs.setDate(6, reportDate);
            cs.setString(7, location);
            cs.setString(8, description);
            cs.setString(9, reporter);
            cs.setString(10, contact);
            cs.setString(11, relationship);
            cs.setString(12, "Missing");
            cs.registerOutParameter(13, Types.INTEGER);
            cs.execute();
            int newId = cs.getInt(13);
            return findReport(newId);
        }
    }

    private static String updateStatus(int reportId, String body) throws Exception {
        String status = required(body, "status");
        String remarks = value(body, "remarks");

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_update_report_status(?, ?, ?)}")) {
            cs.setInt(1, reportId);
            cs.setString(2, status);
            cs.setString(3, remarks);
            cs.execute();
        }

        String updated = findReport(reportId);
        if (updated == null) {
            throw new IllegalArgumentException("Report not found");
        }
        return updated;
    }

    private static void deleteReport(int reportId) throws Exception {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_delete_report(?)}")) {
            cs.setInt(1, reportId);
            cs.execute();
        }
    }

    private static String reportJson(ResultSet rs) throws Exception {
        return "{" +
                "\"id\":" + rs.getInt("report_id") + "," +
                "\"name\":" + quote(rs.getString("name")) + "," +
                "\"age\":" + rs.getInt("age") + "," +
                "\"gender\":" + quote(rs.getString("gender")) + "," +
                "\"address\":" + quote(rs.getString("address")) + "," +
                "\"phoneNo\":" + rs.getLong("phone_no") + "," +
                "\"date\":" + quote(String.valueOf(rs.getDate("report_date"))) + "," +
                "\"location\":" + quote(rs.getString("location")) + "," +
                "\"description\":" + quote(rs.getString("description")) + "," +
                "\"status\":" + quote(rs.getString("status")) + "," +
                "\"reporter\":" + quote(rs.getString("reporter_name")) + "," +
                "\"contact\":" + quote(rs.getString("contact_no")) + "," +
                "\"relationship\":" + quote(rs.getString("relationship")) + "," +
                "\"remarks\":" + quote(rs.getString("status_remarks")) +
                "}";
    }

    private static int parseReportId(String raw) {
        String id = raw.toUpperCase().replace("RPT", "");
        return Integer.parseInt(id);
    }

    private static String required(String json, String key) {
        String found = value(json, key);
        if (found == null || found.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return found.trim();
    }

    private static String value(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        if (!m.find()) {
            return "";
        }
        return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static int intValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return Integer.parseInt(m.group(1));
    }

    private static long longValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?(\\d+)\"?").matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return Long.parseLong(m.group(1));
    }

    private static String quote(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "") + "\"";
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        addCors(exchange);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static void addCors(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }
}
