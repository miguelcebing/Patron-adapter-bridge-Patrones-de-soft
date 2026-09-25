package netguard.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import netguard.core.NetworkMonitoringSystem;
import netguard.devices.NetworkDevice;
import netguard.devices.DeviceMetrics;
import netguard.notifications.NotificationSender;
import netguard.notifications.SmsNotifier;
import netguard.notifications.EmailNotifier;
import netguard.notifications.DashboardPushNotifier;
import netguard.alerts.NetworkAlert;

/**
 * NetGuard Dashboard - Modern Swing GUI
 * 
 * A visually appealing dashboard for the NetGuard Network Monitoring System.
 * Features:
 * - Device selection and metrics display
 * - Notification channel selection
 * - Real-time metrics visualization
 * - Alert history log
 * - Modern dark theme with FlatLaf
 */
public class NetGuardDashboard extends JFrame {
    
    private final NetworkMonitoringSystem monitoringSystem;
    
    // Notifiers (Bridge pattern - notification channels)
    private final SmsNotifier smsNotifier;
    private final EmailNotifier emailNotifier;
    private final DashboardPushNotifier dashboardPushNotifier;
    
    // UI Components
    private JComboBox<String> deviceComboBox;
    private JComboBox<String> notificationChannelComboBox;
    private JTextField recipientTextField;
    private JButton readMetricsButton;
    private JButton evaluateNotifyButton;
    private JButton clearLogButton;
    
    // Display areas
    private JTextArea metricsDisplayArea;
    private JTextArea logDisplayArea;
    private JLabel statusLabel;
    private JLabel deviceCountLabel;
    private JLabel alertCountLabel;
    
    // Metrics display panel
    private JPanel metricsCardPanel;
    private JLabel bandwidthValueLabel;
    private JLabel packetLossValueLabel;
    private JLabel latencyValueLabel;
    private JLabel statusValueLabel;
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);    // Blue
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);     // Green
    private static final Color WARNING_COLOR = new Color(234, 179, 8);     // Yellow
    private static final Color DANGER_COLOR = new Color(239, 68, 68);      // Red
    private static final Color DARK_BG = new Color(17, 24, 39);           // Dark gray
    private static final Color CARD_BG = new Color(31, 41, 55);           // Slightly lighter
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);   // White
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175); // Gray
    
    /**
     * Create the NetGuard Dashboard
     * 
     * @param monitoringSystem The monitoring system to interact with
     */
    public NetGuardDashboard(NetworkMonitoringSystem monitoringSystem) {
        this.monitoringSystem = monitoringSystem;
        
        // Initialize notifiers (Bridge pattern - concrete implementors)
        this.smsNotifier = new SmsNotifier();
        this.emailNotifier = new EmailNotifier();
        this.dashboardPushNotifier = new DashboardPushNotifier();
        
        initializeUI();
        populateDeviceComboBox();
    }
    
    /**
     * Initialize the UI components and layout
     */
    private void initializeUI() {
        // Window settings
        setTitle("NetGuard - Network Monitoring Dashboard");
        setSize(1200, 800);
        setMinimumSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set dark background
        getContentPane().setBackground(DARK_BG);
        getContentPane().setLayout(new BorderLayout(10, 10));
        
        // Add header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Add main content
        add(createMainPanel(), BorderLayout.CENTER);
        
        // Add footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Create the header panel with title and stats
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("NetGuard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Network Monitoring Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(subtitleLabel);
        
        // Stats section
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);
        
        deviceCountLabel = createStatLabel("Devices: " + monitoringSystem.getRegisteredDevices().size());
        alertCountLabel = createStatLabel("Alerts: " + monitoringSystem.getAlertHistory().size());
        
        statsPanel.add(deviceCountLabel);
        statsPanel.add(alertCountLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create a stat label with icon
     */
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }
    
    /**
     * Create the main content panel
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Left panel - Controls and Metrics
        JPanel leftPanel = createLeftPanel();
        leftPanel.setPreferredSize(new String("400"));
        
        // Right panel - Log
        JPanel rightPanel = createRightPanel();
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    /**
     * Create the left panel with controls and metrics display
     */
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setOpaque(false);
        
        // Control panel
        JPanel controlPanel = createControlPanel();
        
        // Metrics card panel
        metricsCardPanel = createMetricsCardPanel();
        
        leftPanel.add(controlPanel, BorderLayout.NORTH);
        leftPanel.add(metricsCardPanel, BorderLayout.CENTER);
        
        return leftPanel;
    }
    
    /**
     * Create the control panel with device and notification selection
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(CARD_BG);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Device selection section
        JLabel deviceLabel = createSectionLabel("Device Selection");
        deviceComboBox = new JComboBox<>();
        deviceComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        readMetricsButton = createStyledButton("Read Device Metrics", PRIMARY_COLOR);
        readMetricsButton.addActionListener(e -> readDeviceMetrics());
        
        // Notification section
        JLabel notificationLabel = createSectionLabel("Notification Settings");
        notificationChannelComboBox = new JComboBox<>(new String[]{"SMS", "Email", "Dashboard Push"});
        notificationChannelComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notificationChannelComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel recipientLabel = new JLabel("Recipient:");
        recipientLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recipientLabel.setForeground(TEXT_SECONDARY);
        recipientLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        recipientTextField = new JTextField();
        recipientTextField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recipientTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        recipientTextField.setText("ops@netguard.example.com");
        
        evaluateNotifyButton = createStyledButton("Evaluate & Notify", SUCCESS_COLOR);
        evaluateNotifyButton.addActionListener(e -> evaluateAndNotify());
        
        // Add components
        controlPanel.add(deviceLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(deviceComboBox);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(readMetricsButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(notificationLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(notificationChannelComboBox);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(recipientLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(recipientTextField);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(evaluateNotifyButton);
        
        return controlPanel;
    }
    
    /**
     * Create the metrics card panel for displaying current metrics
     */
    private JPanel createMetricsCardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Initialize value labels
        bandwidthValueLabel = createMetricValueLabel("--");
        packetLossValueLabel = createMetricValueLabel("--");
        latencyValueLabel = createMetricValueLabel("--");
        statusValueLabel = createMetricValueLabel("--");
        
        // Create metric cards
        JPanel bandwidthCard = createMetricCard("Bandwidth", bandwidthValueLabel, "Mbps", PRIMARY_COLOR);
        JPanel packetLossCard = createMetricCard("Packet Loss", packetLossValueLabel, "%", WARNING_COLOR);
        JPanel latencyCard = createMetricCard("Latency", latencyValueLabel, "ms", SUCCESS_COLOR);
        JPanel statusCard = createMetricCard("Status", statusValueLabel, "", DANGER_COLOR);
        
        panel.add(bandwidthCard);
        panel.add(packetLossCard);
        panel.add(latencyCard);
        panel.add(statusCard);
        
        return panel;
    }
    
    /**
     * Create a metric card component
     */
    private JPanel createMetricCard(String title, JLabel valueLabel, String unit, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        // Value with unit
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valuePanel.setOpaque(false);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);
        
        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        unitLabel.setForeground(TEXT_SECONDARY);
        
        valuePanel.add(valueLabel);
        valuePanel.add(unitLabel);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create a metric value label
     */
    private JLabel createMetricValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    /**
     * Create the right panel with log display
     */
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setOpaque(false);
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(CARD_BG);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel logLabel = createSectionLabel("Activity Log");
        
        logDisplayArea = new JTextArea();
        logDisplayArea.setEditable(false);
        logDisplayArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logDisplayArea.setBackground(new Color(17, 24, 39));
        logDisplayArea.setForeground(TEXT_PRIMARY);
        logDisplayArea.setCaretColor(TEXT_PRIMARY);
        logDisplayArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane logScrollPane = new JScrollPane(logDisplayArea);
        logScrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        clearLogButton = createStyledButton("Clear Log", DANGER_COLOR);
        clearLogButton.addActionListener(e -> clearLog());
        
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.add(clearLogButton, BorderLayout.SOUTH);
        
        rightPanel.add(logPanel, BorderLayout.CENTER);
        
        return rightPanel;
    }
    
    /**
     * Create the footer panel
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(CARD_BG);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        JLabel copyrightLabel = new JLabel("NetGuard v1.0 - Adapter + Bridge Patterns Demo");
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copyrightLabel.setForeground(TEXT_SECONDARY);
        
        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(copyrightLabel, BorderLayout.EAST);
        
        return footerPanel;
    }
    
    /**
     * Create a section label
     */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    /**
     * Create a styled button
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Populate the device combo box with registered devices
     */
    private void populateDeviceComboBox() {
        deviceComboBox.removeAllItems();
        
        List<NetworkDevice> devices = monitoringSystem.getRegisteredDevices();
        for (NetworkDevice device : devices) {
            deviceComboBox.addItem(device.getDeviceId() + " - " + device.getDeviceName());
        }
        
        if (!devices.isEmpty()) {
            deviceComboBox.setSelectedIndex(0);
        }
    }
    
    /**
     * Read metrics from the selected device
     */
    private void readDeviceMetrics() {
        String selected = (String) deviceComboBox.getSelectedItem();
        if (selected == null) {
            showMessage("Please select a device.", "No Device Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String deviceId = selected.split(" - ")[0];
        
        statusLabel.setText("Reading metrics from " + deviceId + "...");
        
        // Run in background thread to avoid blocking UI
        SwingWorker<DeviceMetrics, Void> worker = new SwingWorker<>() {
            @Override
            protected DeviceMetrics doInBackground() throws Exception {
                return monitoringSystem.readDeviceMetrics(deviceId);
            }
            
            @Override
            protected void done() {
                try {
                    DeviceMetrics metrics = get();
                    updateMetricsDisplay(metrics);
                    appendLog("✓ Metrics read from " + metrics.getDeviceName());
                    statusLabel.setText("Metrics read successfully");
                } catch (Exception e) {
                    appendLog("✗ Error reading metrics: " + e.getMessage());
                    statusLabel.setText("Error reading metrics");
                    showError("Failed to read metrics: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Update the metrics display with new values
     */
    private void updateMetricsDisplay(DeviceMetrics metrics) {
        // Update metric cards
        bandwidthValueLabel.setText(String.format("%.1f", metrics.getBandwidthMbps()));
        packetLossValueLabel.setText(String.format("%.2f", metrics.getPacketLossPercent()));
        latencyValueLabel.setText(String.format("%.1f", metrics.getLatencyMs()));
        
        // Update status with color coding
        if (metrics.isOnline()) {
            statusValueLabel.setText("ONLINE");
            statusValueLabel.setForeground(SUCCESS_COLOR);
        } else {
            statusValueLabel.setText("OFFLINE");
            statusValueLabel.setForeground(DANGER_COLOR);
        }
        
        // Update metrics text area
        metricsDisplayArea.setText(metrics.toString());
    }
    
    /**
     * Evaluate metrics and send notification
     */
    private void evaluateAndNotify() {
        String selectedDevice = (String) deviceComboBox.getSelectedItem();
        String selectedChannel = (String) notificationChannelComboBox.getSelectedItem();
        String recipient = recipientTextField.getText().trim();
        
        if (selectedDevice == null) {
            showMessage("Please select a device.", "No Device Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (recipient.isEmpty()) {
            showMessage("Please enter a recipient.", "No Recipient", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String deviceId = selectedDevice.split(" - ")[0];
        
        // Get the selected notification channel (Bridge pattern)
        NotificationSender notificationSender = getNotificationSender(selectedChannel);
        if (notificationSender == null) {
            showMessage("Invalid notification channel.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        statusLabel.setText("Evaluating metrics and sending notification...");
        
        // Run in background thread
        SwingWorker<NetworkAlert, Void> worker = new SwingWorker<>() {
            @Override
            protected NetworkAlert doInBackground() throws Exception {
                // First read the metrics
                DeviceMetrics metrics = monitoringSystem.readDeviceMetrics(deviceId);
                updateMetricsDisplay(metrics);
                
                // Then decide and send alert (Bridge pattern in action!)
                return monitoringSystem.decideAlert(metrics, notificationSender, recipient);
            }
            
            @Override
            protected void done() {
                try {
                    NetworkAlert alert = get();
                    if (alert != null) {
                        appendLog("✓ Alert created: " + alert.getAlertId());
                        appendLog("  Type: " + alert.getAlertTypeDescription());
                        appendLog("  Channel: " + alert.getNotificationSender().getChannelName());
                        appendLog("  Recipient: " + recipient);
                        statusLabel.setText("Alert sent successfully");
                        
                        // Update alert count
                        alertCountLabel.setText("Alerts: " + monitoringSystem.getAlertHistory().size());
                    } else {
                        appendLog("ℹ No alert needed - metrics within normal range");
                        statusLabel.setText("No alert needed");
                    }
                } catch (Exception e) {
                    appendLog("✗ Error: " + e.getMessage());
                    statusLabel.setText("Error during evaluation");
                    showError("Failed to evaluate metrics: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Get the notification sender based on channel selection
     * 
     * This is where the Bridge pattern is used - we select the
     * implementation (notification channel) at runtime.
     */
    private NotificationSender getNotificationSender(String channel) {
        switch (channel) {
            case "SMS":
                return smsNotifier;
            case "Email":
                return emailNotifier;
            case "Dashboard Push":
                return dashboardPushNotifier;
            default:
                return null;
        }
    }
    
    /**
     * Clear the log display
     */
    private void clearLog() {
        logDisplayArea.setText("");
        appendLog("Log cleared");
    }
    
    /**
     * Append a message to the log display
     */
    private void appendLog(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logDisplayArea.append("[" + timestamp + "] " + message + "\n");
        
        // Auto-scroll to bottom
        logDisplayArea.setCaretPosition(logDisplayArea.getDocument().getLength());
    }
    
    /**
     * Show an error message dialog
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show a message dialog
     */
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}
