package netguard.notifications;

/**
 * Concrete Implementor - Bridge Pattern (Email)
 * 
 * This class implements the Email notification channel.
 * In a real system, this would integrate with an SMTP server
 * or email service (SendGrid, AWS SES, etc.).
 * 
 * For this educational example, we simulate sending an email
 * by printing to console and logging the action.
 */
public class EmailNotifier implements NotificationSender {
    
    private final String channelName = "Email";
    private boolean available = true;
    private String smtpServer = "smtp.netguard.example.com";
    
    @Override
    public boolean sendNotification(String recipient, String subject, 
                                   String message, String severity) {
        if (!available) {
            System.err.println("[Email] Channel unavailable. Message not sent.");
            return false;
        }
        
        // Simulate email sending (in real life, this would use JavaMail API)
        String emailBody = formatEmailBody(subject, message, severity);
        
        // Simulate network delay
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Log the email (simulated)
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📧 EMAIL NOTIFICATION SENT");
        System.out.println("=".repeat(50));
        System.out.println("SMTP Server: " + smtpServer);
        System.out.println("To: " + recipient);
        System.out.println("Subject: [" + severity + "] " + subject);
        System.out.println("Body:\n" + emailBody);
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
     * Format the email body with HTML-like structure
     * 
     * @param subject  The subject
     * @param message  The message body
     * @param severity The severity level
     * @return Formatted email body
     */
    private String formatEmailBody(String subject, String message, String severity) {
        StringBuilder email = new StringBuilder();
        email.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        email.append("  NetGuard Network Monitoring Alert\n");
        email.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        email.append("Severity: ").append(severity).append("\n");
        email.append("Subject: ").append(subject).append("\n\n");
        email.append("Details:\n");
        email.append("──────────────────────────────────────────────────\n");
        email.append(message).append("\n");
        email.append("──────────────────────────────────────────────────\n\n");
        email.append("This is an automated message from NetGuard.\n");
        email.append("Please do not reply to this email.\n");
        return email.toString();
    }
    
    /**
     * Set the SMTP server for email delivery
     * @param smtpServer The SMTP server address
     */
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }
    
    /**
     * Simulate enabling/disabling the Email channel
     * @param available true to enable, false to disable
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
}
