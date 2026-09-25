package netguard.alerts;

import netguard.notifications.NotificationSender;

/**
 * Refined Abstraction - Bridge Pattern (Critical Alert)
 * 
 * This class represents a critical, urgent alert.
 * It includes escalation and retry capabilities.
 * 
 * Critical alerts are used for:
 * - Network outages
 * - High packet loss
 * - Excessive latency
 * - Security incidents
 * 
 * The CriticalAlert uses the NotificationSender (the bridge)
 * to send the alert through the appropriate channel, with
 * additional features like:
 * - Automatic retry on failure
 * - Escalation after multiple failures
 * - Higher priority marking
 */
public class CriticalAlert extends NetworkAlert {
    
    private static final String SEVERITY = "CRITICAL";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    private int retryCount;
    private boolean escalated;
    
    /**
     * Create a Critical Alert
     * 
     * @param notificationSender The channel to send through (the bridge)
     * @param deviceId          The device that triggered the alert
     * @param deviceName        The device name
     * @param subject           Alert subject
     * @param message           Alert message
     */
    public CriticalAlert(NotificationSender notificationSender,
                        String deviceId, String deviceName,
                        String subject, String message) {
        super(notificationSender, deviceId, deviceName, subject, message);
        this.retryCount = 0;
        this.escalated = false;
    }
    
    /**
     * Send the critical alert with retry and escalation.
     * 
     * If the initial send fails, we retry up to MAX_RETRIES times.
     * If all retries fail, we escalate the alert (log a warning).
     * 
     * @param recipient The recipient of the alert
     * @return true if alert was sent successfully
     */
    @Override
    public boolean sendAlert(String recipient) {
        if (sent) {
            System.out.println("[CriticalAlert] Alert " + alertId + " already sent.");
            return true;
        }
        
        System.out.println("\n[CriticalAlert] ⚠️  SENDING CRITICAL ALERT to " + recipient + "...");
        
        boolean success = false;
        
        // Try to send with retries
        while (retryCount < MAX_RETRIES && !success) {
            if (retryCount > 0) {
                System.out.println("[CriticalAlert] Retry attempt " + retryCount + "/" + MAX_RETRIES);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            // Use the bridge to send through the notification channel
            success = notificationSender.sendNotification(
                recipient,
                subject,
                message,
                SEVERITY
            );
            
            retryCount++;
        }
        
        if (success) {
            sent = true;
            System.out.println("[CriticalAlert] ✅ Alert " + alertId + " sent successfully.");
        } else {
            // Escalation: all retries failed
            escalate(recipient);
        }
        
        return success;
    }
    
    /**
     * Escalate the alert when all send attempts fail.
     * 
     * In a real system, this might:
     * - Notify a supervisor
     * - Create a ticket in the incident management system
     * - Trigger an automated response
     * 
     * For this educational example, we log the escalation.
     * 
     * @param originalRecipient The original intended recipient
     */
    private void escalate(String originalRecipient) {
        escalated = true;
        System.err.println("\n" + "=".repeat(60));
        System.err.println("🚨 CRITICAL ALERT ESCALATION REQUIRED");
        System.err.println("=".repeat(60));
        System.err.println("Alert ID: " + alertId);
        System.err.println("Device: " + deviceName + " (" + deviceId + ")");
        System.err.println("Subject: " + subject);
        System.err.println("Original Recipient: " + originalRecipient);
        System.err.println("Failed Attempts: " + retryCount);
        System.err.println("Channel: " + notificationSender.getChannelName());
        System.err.println("Timestamp: " + new java.util.Date());
        System.err.println("=".repeat(60));
        System.err.println("ACTION REQUIRED: Manual intervention needed!");
        System.err.println("=".repeat(60) + "\n");
    }
    
    @Override
    public String getSeverity() {
        return SEVERITY;
    }
    
    @Override
    public String getAlertTypeDescription() {
        return "Critical urgent alert - sent with retry and escalation";
    }
    
    // Additional getters for CriticalAlert
    public int getRetryCount() { return retryCount; }
    public boolean isEscalated() { return escalated; }
    public int getMaxRetries() { return MAX_RETRIES; }
}
