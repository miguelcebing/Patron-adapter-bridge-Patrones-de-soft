package netguard.notifications;

/**
 * Implementor Interface - Bridge Pattern
 * 
 * This interface defines how notifications are sent.
 * It is the "implementation" side of the Bridge pattern.
 * 
 * Different notification channels (SMS, Email, Dashboard Push)
 * implement this interface, allowing the alert hierarchy
 * (abstraction) to vary independently from the delivery mechanism.
 * 
 * The Bridge pattern allows us to combine any alert type with
 * any notification channel without creating a class for each
 * combination (e.g., CriticalAlertSms, CriticalAlertEmail, etc.).
 */
public interface NotificationSender {
    
    /**
     * Send a notification through this channel
     * 
     * @param recipient The recipient of the notification
     * @param subject   The subject/title of the notification
     * @param message   The body content of the notification
     * @param severity  The severity level ("BASIC" or "CRITICAL")
     * @return true if notification was sent successfully
     */
    boolean sendNotification(String recipient, String subject, 
                            String message, String severity);
    
    /**
     * Get the name of this notification channel
     * 
     * @return Channel name (e.g., "SMS", "Email", "Dashboard Push")
     */
    String getChannelName();
    
    /**
     * Check if this notification channel is available
     * 
     * @return true if channel is ready to send notifications
     */
    boolean isAvailable();
}
