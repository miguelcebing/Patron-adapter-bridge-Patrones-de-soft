package netguard.alerts;

import netguard.notifications.NotificationSender;

/**
 * Refined Abstraction - Bridge Pattern (Basic Alert)
 * 
 * This class represents a basic, informational alert.
 * It is sent once without escalation or retry.
 * 
 * Basic alerts are used for:
 * - Routine notifications
 * - Non-critical warnings
 * - Informational updates
 * 
 * The BasicAlert uses the NotificationSender (the bridge)
 * to send the alert through the appropriate channel.
 */
public class BasicAlert extends NetworkAlert {
    
    private static final String SEVERITY = "BASIC";
    
    /**
     * Create a Basic Alert
     * 
     * @param notificationSender The channel to send through (the bridge)
     * @param deviceId          The device that triggered the alert
     * @param deviceName        The device name
     * @param subject           Alert subject
     * @param message           Alert message
     */
    public BasicAlert(NotificationSender notificationSender,
                     String deviceId, String deviceName,
                     String subject, String message) {
        super(notificationSender, deviceId, deviceName, subject, message);
    }
    
    /**
     * Send the basic alert through the notification channel.
     * 
     * Basic alerts are sent once. If the send fails,
     * we log the failure but do not retry.
     * 
     * @param recipient The recipient of the alert
     * @return true if alert was sent successfully
     */
    @Override
    public boolean sendAlert(String recipient) {
        if (sent) {
            System.out.println("[BasicAlert] Alert " + alertId + " already sent.");
            return true;
        }
        
        System.out.println("\n[BasicAlert] Sending alert to " + recipient + "...");
        
        // Use the bridge to send through the notification channel
        boolean success = notificationSender.sendNotification(
            recipient,
            subject,
            message,
            SEVERITY
        );
        
        if (success) {
            sent = true;
            System.out.println("[BasicAlert] Alert " + alertId + " sent successfully.");
        } else {
            System.err.println("[BasicAlert] Failed to send alert " + alertId);
        }
        
        return success;
    }
    
    @Override
    public String getSeverity() {
        return SEVERITY;
    }
    
    @Override
    public String getAlertTypeDescription() {
        return "Basic informational alert - sent once without escalation";
    }
}
