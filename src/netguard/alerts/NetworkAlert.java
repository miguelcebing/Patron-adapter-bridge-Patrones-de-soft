package netguard.alerts;

import netguard.notifications.NotificationSender;

/**
 * Abstraction - Bridge Pattern (Alert Hierarchy)
 * 
 * This abstract class represents the "abstraction" side of the
 * Bridge pattern. It defines the interface for alerts while
 * holding a reference to a NotificationSender (the "implementor").
 * 
 * The key insight of Bridge is that:
 * - The abstraction (alert type) can vary independently
 * - The implementor (notification channel) can vary independently
 * - They are combined at runtime, not at compile time
 * 
 * This avoids the combinatorial explosion of classes like:
 * - BasicAlertSms, BasicAlertEmail, BasicAlertDashboardPush
 * - CriticalAlertSms, CriticalAlertEmail, CriticalAlertDashboardPush
 * 
 * Instead, we have:
 * - 2 alert types (Basic, Critical)
 * - 3 notification channels (SMS, Email, Dashboard Push)
 * - Total: 2 + 3 = 5 classes (not 2 × 3 = 6 classes)
 * 
 * As we add more alert types or channels, the number of classes
 * grows linearly, not multiplicatively.
 */
public abstract class NetworkAlert {
    
    // This reference IS the Bridge - it connects abstraction to implementation
    protected NotificationSender notificationSender;
    
    protected String alertId;
    protected String deviceId;
    protected String deviceName;
    protected String subject;
    protected String message;
    protected String severity;
    protected long timestamp;
    protected boolean sent;
    
    /**
     * Constructor that accepts a NotificationSender
     * 
     * @param notificationSender The channel to send notifications through
     * @param deviceId          The ID of the device that triggered the alert
     * @param deviceName        The name of the device
     * @param subject           The alert subject
     * @param message           The alert message
     */
    protected NetworkAlert(NotificationSender notificationSender, 
                          String deviceId, String deviceName,
                          String subject, String message) {
        this.notificationSender = notificationSender;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.subject = subject;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.sent = false;
        this.alertId = generateAlertId();
    }
    
    /**
     * Send the alert through the notification channel
     * 
     * This method is implemented differently by each concrete
     * alert class (BasicAlert and CriticalAlert), but both
     * use the same notification channel.
     * 
     * @param recipient The recipient of the alert
     * @return true if alert was sent successfully
     */
    public abstract boolean sendAlert(String recipient);
    
    /**
     * Get the severity level of this alert
     * @return Severity level string
     */
    public abstract String getSeverity();
    
    /**
     * Get a description of this alert type
     * @return Description string
     */
    public abstract String getAlertTypeDescription();
    
    // Common getters
    public String getAlertId() { return alertId; }
    public String getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isSent() { return sent; }
    public NotificationSender getNotificationSender() { return notificationSender; }
    
    /**
     * Set the notification sender (allows changing channels)
     * 
     * @param notificationSender The new notification channel
     */
    public void setNotificationSender(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }
    
    /**
     * Get a formatted string representation of this alert
     * @return Formatted alert string
     */
    @Override
    public String toString() {
        return String.format(
            "[%s] Alert %s\n" +
            "  Device: %s (%s)\n" +
            "  Subject: %s\n" +
            "  Message: %s\n" +
            "  Channel: %s\n" +
            "  Status: %s\n" +
            "  Timestamp: %tF %<tT",
            getSeverity(),
            alertId,
            deviceName, deviceId,
            subject,
            message,
            notificationSender.getChannelName(),
            sent ? "SENT" : "PENDING",
            timestamp
        );
    }
    
    /**
     * Generate a unique alert ID
     * @return Unique alert ID string
     */
    private String generateAlertId() {
        return "ALERT-" + System.currentTimeMillis() + "-" + 
               (int)(Math.random() * 1000);
    }
}
