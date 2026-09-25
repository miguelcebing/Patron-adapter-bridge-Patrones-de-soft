package netguard.notifications;

/**
 * Concrete Implementor - Bridge Pattern (SMS)
 * 
 * This class implements the SMS notification channel.
 * In a real system, this would integrate with an SMS gateway
 * (Twilio, AWS SNS, etc.).
 * 
 * For this educational example, we simulate sending an SMS
 * by printing to console and logging the action.
 */
public class SmsNotifier implements NotificationSender {
    
    private final String channelName = "SMS";
    private boolean available = true;
    
    @Override
    public boolean sendNotification(String recipient, String subject, 
                                   String message, String severity) {
        if (!available) {
            System.err.println("[SMS] Channel unavailable. Message not sent.");
            return false;
        }
        
        // Simulate SMS sending (in real life, this would call an API)
        String smsMessage = formatSMSMessage(subject, message, severity);
        
        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Log the SMS (simulated)
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📱 SMS NOTIFICATION SENT");
        System.out.println("=".repeat(50));
        System.out.println("To: " + recipient);
        System.out.println("Message: " + smsMessage);
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
     * Format the SMS message with length constraints
     * SMS messages are limited to 160 characters
     * 
     * @param subject  The subject
     * @param message  The message body
     * @param severity The severity level
     * @return Formatted SMS message
     */
    private String formatSMSMessage(String subject, String message, String severity) {
        // SMS messages should be concise
        StringBuilder sms = new StringBuilder();
        sms.append("[").append(severity).append("] ");
        sms.append(subject);
        
        // If message is too long, truncate it
        if (sms.length() + message.length() > 150) {
            sms.append(": ");
            sms.append(message.substring(0, 150 - sms.length()));
            sms.append("...");
        } else {
            sms.append(": ").append(message);
        }
        
        return sms.toString();
    }
    
    /**
     * Simulate enabling/disabling the SMS channel
     * @param available true to enable, false to disable
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
}
