package netguard.notifications;

/**
 * Concrete Implementor - Bridge Pattern (Dashboard Push)
 * 
 * This class implements the Dashboard Push notification channel.
 * In a real system, this would use WebSockets or Server-Sent Events
 * to push notifications to a web dashboard.
 * 
 * For this educational example, we simulate push notifications
 * by printing to console and logging the action.
 */
public class DashboardPushNotifier implements NotificationSender {
    
    private final String channelName = "Dashboard Push";
    private boolean available = true;
    
    @Override
    public boolean sendNotification(String recipient, String subject, 
                                   String message, String severity) {
        if (!available) {
            System.err.println("[Dashboard Push] Channel unavailable. Message not sent.");
            return false;
        }
        
        // Simulate push notification (in real life, this would use WebSockets)
        String pushPayload = formatPushPayload(subject, message, severity);
        
        // Simulate network delay (faster than other channels)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Log the push notification (simulated)
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔔 DASHBOARD PUSH NOTIFICATION SENT");
        System.out.println("=".repeat(50));
        System.out.println("Recipient: " + recipient);
        System.out.println("Payload:\n" + pushPayload);
        System.out.println("Timestamp: " + new java.util.Date());
        System.out.println("=".repeat(50) + "\n");
        
        return true;
    }
    
    @Override
    public String getChannelName() {
        return channelName;
    }
    
    @Override
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Format the push notification payload
     * This simulates a JSON payload that would be sent via WebSocket
     * 
     * @param subject  The subject
     * @param message  The message body
     * @param severity The severity level
     * @return Formatted JSON-like payload
     */
    private String formatPushPayload(String subject, String message, String severity) {
        StringBuilder payload = new StringBuilder();
        payload.append("{\n");
        payload.append("  \"type\": \"NETWORK_ALERT\",\n");
        payload.append("  \"severity\": \"").append(severity).append("\",\n");
        payload.append("  \"subject\": \"").append(escapeJson(subject)).append("\",\n");
        payload.append("  \"message\": \"").append(escapeJson(message)).append("\",\n");
        payload.append("  \"timestamp\": \"").append(new java.util.Date()).append("\",\n");
        payload.append("  \"read\": false,\n");
        payload.append("  \"actions\": [\n");
        payload.append("    {\"label\": \"View Details\", \"action\": \"SHOW_DETAILS\"},\n");
        payload.append("    {\"label\": \"Dismiss\", \"action\": \"DISMISS\"}\n");
        payload.append("  ]\n");
        payload.append("}");
        return payload.toString();
    }
    
    /**
     * Escape special characters for JSON strings
     * 
     * @param input The input string
     * @return Escaped string safe for JSON
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    /**
     * Simulate enabling/disabling the Dashboard Push channel
     * @param available true to enable, false to disable
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
}
