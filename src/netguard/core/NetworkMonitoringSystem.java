package netguard.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netguard.devices.NetworkDevice;
import netguard.devices.DeviceMetrics;
import netguard.notifications.NotificationSender;
import netguard.alerts.NetworkAlert;
import netguard.alerts.BasicAlert;
import netguard.alerts.CriticalAlert;

/**
 * Core Business Logic - Network Monitoring System
 * 
 * This class orchestrates both the Adapter and Bridge patterns:
 * 
 * - ADAPTER: Uses NetworkDevice interface to read metrics from
 *   any vendor device (Cisco, Juniper, OpenNetwork) without
 *   knowing the vendor-specific details.
 * 
 * - BRIDGE: Creates alerts (BasicAlert or CriticalAlert) and
 *   sends them through the appropriate notification channel
 *   (SMS, Email, Dashboard Push). The alert type and channel
 *   are independent dimensions combined at runtime.
 * 
 * The key method is decideAlert(), which demonstrates the Bridge
 * pattern by:
 * 1. Deciding the alert TYPE based on metrics (Basic vs Critical)
 * 2. Accepting the notification CHANNEL from the user
 * 3. Combining them at runtime without class explosion
 */
public class NetworkMonitoringSystem {
    
    // Thresholds for alert decisions
    private static final double CRITICAL_BANDWIDTH_THRESHOLD_MBPS = 100.0;
    private static final double CRITICAL_PACKET_LOSS_PERCENT = 5.0;
    private static final double CRITICAL_LATENCY_MS = 50.0;
    
    private static final double WARNING_BANDWIDTH_THRESHOLD_MBPS = 200.0;
    private static final double WARNING_PACKET_LOSS_PERCENT = 2.0;
    private static final double WARNING_LATENCY_MS = 20.0;
    
    // Registry of monitored devices
    private final Map<String, NetworkDevice> devices;
    
    // History of readings and alerts
    private final List<DeviceMetrics> metricsHistory;
    private final List<NetworkAlert> alertHistory;
    
    /**
     * Create a new Network Monitoring System
     */
    public NetworkMonitoringSystem() {
        this.devices = new HashMap<>();
        this.metricsHistory = new ArrayList<>();
        this.alertHistory = new ArrayList<>();
    }
    
    /**
     * Register a device for monitoring
     * 
     * The device can be any implementation of NetworkDevice,
     * thanks to the Adapter pattern.
     * 
     * @param device The device to monitor
     */
    public void registerDevice(NetworkDevice device) {
        devices.put(device.getDeviceId(), device);
        System.out.println("[Monitor] Registered device: " + device.getDeviceName() + 
                          " (" + device.getVendor() + " " + device.getDeviceType() + ")");
    }
    
    /**
     * Unregister a device from monitoring
     * 
     * @param deviceId The device ID to unregister
     */
    public void unregisterDevice(String deviceId) {
        NetworkDevice removed = devices.remove(deviceId);
        if (removed != null) {
            System.out.println("[Monitor] Unregistered device: " + removed.getDeviceName());
        }
    }
    
    /**
     * Get a list of all registered devices
     * 
     * @return List of registered devices
     */
    public List<NetworkDevice> getRegisteredDevices() {
        return new ArrayList<>(devices.values());
    }
    
    /**
     * Get a specific device by ID
     * 
     * @param deviceId The device ID
     * @return The device, or null if not found
     */
    public NetworkDevice getDevice(String deviceId) {
        return devices.get(deviceId);
    }
    
    /**
     * Read metrics from a specific device
     * 
     * This method uses the Adapter pattern - it doesn't know
     * which vendor's API it's calling, only the NetworkDevice
     * interface.
     * 
     * @param deviceId The device ID to read from
     * @return DeviceMetrics with standardized values
     */
    public DeviceMetrics readDeviceMetrics(String deviceId) {
        NetworkDevice device = devices.get(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        
        System.out.println("\n[Monitor] Reading metrics from " + device.getDeviceName() + "...");
        
        // This is where the Adapter pattern shines!
        // We call readMetrics() and get standardized format,
        // regardless of the vendor's original format.
        DeviceMetrics metrics = device.readMetrics();
        
        // Store in history
        metricsHistory.add(metrics);
        
        System.out.println("[Monitor] Metrics received:");
        System.out.println(metrics);
        
        return metrics;
    }
    
    /**
     * Read metrics from all registered devices
     * 
     * @return Map of device IDs to their metrics
     */
    public Map<String, DeviceMetrics> readAllDeviceMetrics() {
        Map<String, DeviceMetrics> allMetrics = new HashMap<>();
        
        for (String deviceId : devices.keySet()) {
            try {
                DeviceMetrics metrics = readDeviceMetrics(deviceId);
                allMetrics.put(deviceId, metrics);
            } catch (Exception e) {
                System.err.println("[Monitor] Error reading device " + deviceId + ": " + e.getMessage());
            }
        }
        
        return allMetrics;
    }
    
    /**
     * Evaluate metrics and decide whether to send an alert.
     * 
     * This is the KEY METHOD that demonstrates the Bridge pattern!
     * 
     * The decision process:
     * 1. Analyze the metrics to determine if an alert is needed
     * 2. If an alert is needed, decide the SEVERITY (Basic or Critical)
     * 3. Create the appropriate alert type with the notification CHANNEL
     * 4. Send the alert through the channel
     * 
     * The alert TYPE (Basic/Critical) is decided by metrics.
     * The notification CHANNEL (SMS/Email/Push) is decided by user.
     * These two dimensions are INDEPENDENT - that's the Bridge!
     * 
     * @param metrics          The metrics to evaluate
     * @param notificationSender The channel to send through (from GUI)
     * @param recipient        The recipient of the alert
     * @return The alert that was created (or null if no alert needed)
     */
    public NetworkAlert decideAlert(DeviceMetrics metrics, 
                                   NotificationSender notificationSender,
                                   String recipient) {
        System.out.println("\n[Monitor] Evaluating metrics for " + metrics.getDeviceName() + "...");
        
        // Determine if we need an alert and what severity
        boolean needsCriticalAlert = false;
        boolean needsBasicAlert = false;
        StringBuilder reason = new StringBuilder();
        
        // Check bandwidth
        if (metrics.getBandwidthMbps() < CRITICAL_BANDWIDTH_THRESHOLD_MBPS) {
            needsCriticalAlert = true;
            reason.append(String.format("Critical: Bandwidth %.2f Mbps below threshold %.2f Mbps\n",
                metrics.getBandwidthMbps(), CRITICAL_BANDWIDTH_THRESHOLD_MBPS));
        } else if (metrics.getBandwidthMbps() < WARNING_BANDWIDTH_THRESHOLD_MBPS) {
            needsBasicAlert = true;
            reason.append(String.format("Warning: Bandwidth %.2f Mbps below threshold %.2f Mbps\n",
                metrics.getBandwidthMbps(), WARNING_BANDWIDTH_THRESHOLD_MBPS));
        }
        
        // Check packet loss
        if (metrics.getPacketLossPercent() > CRITICAL_PACKET_LOSS_PERCENT) {
            needsCriticalAlert = true;
            reason.append(String.format("Critical: Packet loss %.2f%% above threshold %.2f%%\n",
                metrics.getPacketLossPercent(), CRITICAL_PACKET_LOSS_PERCENT));
        } else if (metrics.getPacketLossPercent() > WARNING_PACKET_LOSS_PERCENT) {
            needsBasicAlert = true;
            reason.append(String.format("Warning: Packet loss %.2f%% above threshold %.2f%%\n",
                metrics.getPacketLossPercent(), WARNING_PACKET_LOSS_PERCENT));
        }
        
        // Check latency
        if (metrics.getLatencyMs() > CRITICAL_LATENCY_MS) {
            needsCriticalAlert = true;
            reason.append(String.format("Critical: Latency %.2f ms above threshold %.2f ms\n",
                metrics.getLatencyMs(), CRITICAL_LATENCY_MS));
        } else if (metrics.getLatencyMs() > WARNING_LATENCY_MS) {
            needsBasicAlert = true;
            reason.append(String.format("Warning: Latency %.2f ms above threshold %.2f ms\n",
                metrics.getLatencyMs(), WARNING_LATENCY_MS));
        }
        
        // Decide alert type and create alert (Bridge in action!)
        NetworkAlert alert = null;
        
        if (needsCriticalAlert) {
            // Critical alert - urgent, with retry and escalation
            String subject = "CRITICAL: Network Issues Detected on " + metrics.getDeviceName();
            String message = String.format(
                "Critical network issues detected on %s (%s):\n\n%s\n" +
                "Current Metrics:\n" +
                "  Bandwidth: %.2f Mbps\n" +
                "  Packet Loss: %.2f%%\n" +
                "  Latency: %.2f ms\n\n" +
                "Immediate action required!",
                metrics.getDeviceName(), metrics.getDeviceId(),
                reason.toString(),
                metrics.getBandwidthMbps(),
                metrics.getPacketLossPercent(),
                metrics.getLatencyMs()
            );
            
            // Bridge: CriticalAlert + chosen notification channel
            alert = new CriticalAlert(notificationSender, 
                                     metrics.getDeviceId(),
                                     metrics.getDeviceName(),
                                     subject, message);
            
        } else if (needsBasicAlert) {
            // Basic alert - informational, single send
            String subject = "Warning: Network Degradation on " + metrics.getDeviceName();
            String message = String.format(
                "Network degradation detected on %s (%s):\n\n%s\n" +
                "Current Metrics:\n" +
                "  Bandwidth: %.2f Mbps\n" +
                "  Packet Loss: %.2f%%\n" +
                "  Latency: %.2f ms\n\n" +
                "Please monitor the situation.",
                metrics.getDeviceName(), metrics.getDeviceId(),
                reason.toString(),
                metrics.getBandwidthMbps(),
                metrics.getPacketLossPercent(),
                metrics.getLatencyMs()
            );
            
            // Bridge: BasicAlert + chosen notification channel
            alert = new BasicAlert(notificationSender,
                                  metrics.getDeviceId(),
                                  metrics.getDeviceName(),
                                  subject, message);
        }
        
        if (alert != null) {
            // Send the alert through the chosen channel
            boolean sent = alert.sendAlert(recipient);
            alertHistory.add(alert);
            
            System.out.println("\n[Monitor] Alert created and sent:");
            System.out.println(alert);
        } else {
            System.out.println("[Monitor] No alert needed - all metrics within normal range.");
        }
        
        return alert;
    }
    
    /**
     * Get the metrics history
     * 
     * @return List of all metrics readings
     */
    public List<DeviceMetrics> getMetricsHistory() {
        return new ArrayList<>(metricsHistory);
    }
    
    /**
     * Get the alert history
     * 
     * @return List of all alerts sent
     */
    public List<NetworkAlert> getAlertHistory() {
        return new ArrayList<>(alertHistory);
    }
    
    /**
     * Clear the history
     */
    public void clearHistory() {
        metricsHistory.clear();
        alertHistory.clear();
        System.out.println("[Monitor] History cleared.");
    }
    
    /**
     * Get a summary of the system status
     * 
     * @return Summary string
     */
    public String getSystemSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== NetGuard Monitoring System Summary ===\n");
        summary.append("Registered Devices: ").append(devices.size()).append("\n");
        summary.append("Total Readings: ").append(metricsHistory.size()).append("\n");
        summary.append("Total Alerts: ").append(alertHistory.size()).append("\n");
        summary.append("\nDevice List:\n");
        
        for (NetworkDevice device : devices.values()) {
            summary.append("  - ").append(device.getDeviceName())
                   .append(" (").append(device.getVendor())
                   .append(" ").append(device.getDeviceType())
                   .append(") - ").append(device.isOnline() ? "ONLINE" : "OFFLINE")
                   .append("\n");
        }
        
        return summary.toString();
    }
}
