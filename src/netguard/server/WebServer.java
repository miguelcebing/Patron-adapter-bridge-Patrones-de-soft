package netguard.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

import netguard.core.NetworkMonitoringSystem;
import netguard.devices.NetworkDevice;
import netguard.devices.DeviceMetrics;
import netguard.notifications.NotificationSender;
import netguard.notifications.SmsNotifier;
import netguard.notifications.EmailNotifier;
import netguard.notifications.DashboardPushNotifier;
import netguard.alerts.NetworkAlert;

/**
 * Embedded Web Server for NetGuard Dashboard
 * 
 * This server serves static files (HTML, CSS, JS) and provides
 * REST API endpoints for the frontend to interact with the
 * monitoring system.
 * 
 * Uses Java's built-in HttpServer (no external dependencies).
 */
public class WebServer {
    
    private static final int DEFAULT_PORT = 8080;
    private static final String STATIC_FILES_PATH = "src/netguard/web";
    
    private final NetworkMonitoringSystem monitoringSystem;
    private final SmsNotifier smsNotifier;
    private final EmailNotifier emailNotifier;
    private final DashboardPushNotifier dashboardPushNotifier;
    
    private HttpServer server;
    private int port;
    
    /**
     * Create the WebServer with the monitoring system
     * 
     * @param monitoringSystem The monitoring system to interact with
     */
    public WebServer(NetworkMonitoringSystem monitoringSystem) {
        this(monitoringSystem, DEFAULT_PORT);
    }
    
    /**
     * Create the WebServer with a custom port
     * 
     * @param monitoringSystem The monitoring system to interact with
     * @param port             The port to listen on
     */
    public WebServer(NetworkMonitoringSystem monitoringSystem, int port) {
        this.monitoringSystem = monitoringSystem;
        this.port = port;
        
        // Initialize notifiers
        this.smsNotifier = new SmsNotifier();
        this.emailNotifier = new EmailNotifier();
        this.dashboardPushNotifier = new DashboardPushNotifier();
    }
    
    /**
     * Start the web server
     * 
     * @throws IOException If the server cannot be started
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Configure thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Register API endpoints
        server.createContext("/api/devices", new DevicesHandler());
        server.createContext("/api/metrics", new MetricsHandler());
        server.createContext("/api/evaluate", new EvaluateHandler());
        server.createContext("/api/history", new HistoryHandler());
        
        // Register static file handler (must be last)
        server.createContext("/", new StaticFileHandler());
        
        server.start();
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         NetGuard Web Server Started                         ║");
        System.out.println("║         http://localhost:" + port + "                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Stop the web server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Web server stopped.");
        }
    }
    
    /**
     * Get the server URL
     * 
     * @return The server URL string
     */
    public String getServerUrl() {
        return "http://localhost:" + port;
    }
    
    // ========== API Handlers ==========
    
    /**
     * Handler for /api/devices - Get all registered devices
     */
    private class DevicesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }
            
            List<NetworkDevice> devices = monitoringSystem.getRegisteredDevices();
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < devices.size(); i++) {
                NetworkDevice device = devices.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"id\":\"").append(device.getDeviceId()).append("\",");
                json.append("\"name\":\"").append(device.getDeviceName()).append("\",");
                json.append("\"vendor\":\"").append(device.getVendor()).append("\",");
                json.append("\"type\":\"").append(device.getDeviceType()).append("\",");
                json.append("\"online\":").append(device.isOnline());
                json.append("}");
            }
            
            json.append("]");
            
            sendJsonResponse(exchange, 200, json.toString());
        }
    }
    
    /**
     * Handler for /api/metrics/{deviceId} - Get metrics for a device
     */
    private class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String deviceId = path.substring("/api/metrics/".length());
            
            try {
                DeviceMetrics metrics = monitoringSystem.readDeviceMetrics(deviceId);
                
                StringBuilder json = new StringBuilder("{");
                json.append("\"deviceId\":\"").append(metrics.getDeviceId()).append("\",");
                json.append("\"deviceName\":\"").append(metrics.getDeviceName()).append("\",");
                json.append("\"vendor\":\"").append(metrics.getVendor()).append("\",");
                json.append("\"deviceType\":\"").append(metrics.getDeviceType()).append("\",");
                json.append("\"bandwidthMbps\":").append(metrics.getBandwidthMbps()).append(",");
                json.append("\"packetLossPercent\":").append(metrics.getPacketLossPercent()).append(",");
                json.append("\"latencyMs\":").append(metrics.getLatencyMs()).append(",");
                json.append("\"online\":").append(metrics.isOnline()).append(",");
                json.append("\"timestamp\":").append(metrics.getTimestamp());
                json.append("}");
                
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 404, 
                    "{\"error\":\"Device not found: " + deviceId + "\"}");
            }
        }
    }
    
    /**
     * Handler for /api/evaluate - Evaluate metrics and send alert
     */
    private class EvaluateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }
            
            // Read request body
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            
            // Parse JSON manually (simple parsing)
            String deviceId = extractJsonValue(requestBody, "deviceId");
            String channel = extractJsonValue(requestBody, "channel");
            String recipient = extractJsonValue(requestBody, "recipient");
            
            if (deviceId == null || channel == null || recipient == null) {
                sendJsonResponse(exchange, 400, 
                    "{\"error\":\"Missing required fields: deviceId, channel, recipient\"}");
                return;
            }
            
            try {
                // Read metrics
                DeviceMetrics metrics = monitoringSystem.readDeviceMetrics(deviceId);
                
                // Get notification sender
                NotificationSender sender = getNotificationSender(channel);
                if (sender == null) {
                    sendJsonResponse(exchange, 400, 
                        "{\"error\":\"Invalid channel: " + channel + "\"}");
                    return;
                }
                
                // Evaluate and send alert
                NetworkAlert alert = monitoringSystem.decideAlert(metrics, sender, recipient);
                
                StringBuilder json = new StringBuilder("{");
                json.append("\"success\":true,");
                json.append("\"metrics\":{");
                json.append("\"deviceId\":\"").append(metrics.getDeviceId()).append("\",");
                json.append("\"bandwidthMbps\":").append(metrics.getBandwidthMbps()).append(",");
                json.append("\"packetLossPercent\":").append(metrics.getPacketLossPercent()).append(",");
                json.append("\"latencyMs\":").append(metrics.getLatencyMs()).append(",");
                json.append("\"online\":").append(metrics.isOnline());
                json.append("},");
                
                if (alert != null) {
                    json.append("\"alert\":{");
                    json.append("\"id\":\"").append(alert.getAlertId()).append("\",");
                    json.append("\"type\":\"").append(alert.getAlertTypeDescription()).append("\",");
                    json.append("\"severity\":\"").append(alert.getSeverity()).append("\",");
                    json.append("\"channel\":\"").append(alert.getNotificationSender().getChannelName()).append("\",");
                    json.append("\"sent\":").append(alert.isSent());
                    json.append("}");
                } else {
                    json.append("\"alert\":null");
                }
                
                json.append("}");
                
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, 
                    "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }
    
    /**
     * Handler for /api/history - Get metrics and alert history
     */
    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }
            
            StringBuilder json = new StringBuilder("{");
            
            // Metrics history
            json.append("\"metrics\":[");
            List<DeviceMetrics> metricsHistory = monitoringSystem.getMetricsHistory();
            for (int i = 0; i < metricsHistory.size(); i++) {
                DeviceMetrics m = metricsHistory.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"deviceId\":\"").append(m.getDeviceId()).append("\",");
                json.append("\"deviceName\":\"").append(m.getDeviceName()).append("\",");
                json.append("\"bandwidthMbps\":").append(m.getBandwidthMbps()).append(",");
                json.append("\"packetLossPercent\":").append(m.getPacketLossPercent()).append(",");
                json.append("\"latencyMs\":").append(m.getLatencyMs()).append(",");
                json.append("\"timestamp\":").append(m.getTimestamp());
                json.append("}");
            }
            json.append("],");
            
            // Alert history
            json.append("\"alerts\":[");
            List<NetworkAlert> alertHistory = monitoringSystem.getAlertHistory();
            for (int i = 0; i < alertHistory.size(); i++) {
                NetworkAlert a = alertHistory.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"id\":\"").append(a.getAlertId()).append("\",");
                json.append("\"deviceName\":\"").append(a.getDeviceName()).append("\",");
                json.append("\"severity\":\"").append(a.getSeverity()).append("\",");
                json.append("\"channel\":\"").append(a.getNotificationSender().getChannelName()).append("\",");
                json.append("\"sent\":").append(a.isSent()).append(",");
                json.append("\"timestamp\":").append(a.getTimestamp());
                json.append("}");
            }
            json.append("]");
            
            json.append("}");
            
            sendJsonResponse(exchange, 200, json.toString());
        }
    }
    
    // ========== Static File Handler ==========
    
    /**
     * Handler for serving static files (HTML, CSS, JS)
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html
            if ("/".equals(path) || "".equals(path)) {
                path = "/index.html";
            }
            
            // Map to file system
            String filePath = STATIC_FILES_PATH + path;
            
            // Security: prevent directory traversal
            Path fullPath = Paths.get(filePath).toAbsolutePath();
            Path webRoot = Paths.get(STATIC_FILES_PATH).toAbsolutePath();
            
            if (!fullPath.startsWith(webRoot)) {
                sendJsonResponse(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }
            
            // Check if file exists
            File file = fullPath.toFile();
            if (!file.exists() || !file.isFile()) {
                sendJsonResponse(exchange, 404, "{\"error\":\"File not found: " + path + "\"}");
                return;
            }
            
            // Determine content type
            String contentType = getContentType(path);
            
            // Send file
            exchange.getResponseHeaders().set("Content-Type", contentType);
            byte[] fileBytes = Files.readAllBytes(fullPath);
            exchange.sendResponseHeaders(200, fileBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Get the notification sender based on channel name
     */
    private NotificationSender getNotificationSender(String channel) {
        switch (channel.toLowerCase()) {
            case "sms":
                return smsNotifier;
            case "email":
                return emailNotifier;
            case "dashboard push":
            case "push":
                return dashboardPushNotifier;
            default:
                return null;
        }
    }
    
    /**
     * Send a JSON response
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] response = json.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
    
    /**
     * Send method not allowed response
     */
    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
    }
    
    /**
     * Extract a value from a simple JSON string
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        
        if (keyIndex == -1) {
            return null;
        }
        
        // Find the colon after the key
        int colonIndex = json.indexOf(":", keyIndex + searchKey.length());
        if (colonIndex == -1) {
            return null;
        }
        
        // Find the opening quote
        int valueStart = json.indexOf("\"", colonIndex + 1);
        if (valueStart == -1) {
            return null;
        }
        
        // Find the closing quote
        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd == -1) {
            return null;
        }
        
        return json.substring(valueStart + 1, valueEnd);
    }
    
    /**
     * Get the content type based on file extension
     */
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        
        return "application/octet-stream";
    }
}
