package netguard.devices;

/**
 * Data Transfer Object (DTO) for standardized device metrics.
 * 
 * This class represents the standardized format that all device
 * metrics are converted to, regardless of the original vendor format.
 * 
 * Units:
 * - Bandwidth: Megabits per second (Mbps)
 * - Packet Loss: Percentage (0.0 - 100.0)
 * - Latency: Milliseconds (ms)
 * 
 * The Adapter pattern converts vendor-specific units to these
 * standard units.
 */
public class DeviceMetrics {
    
    private String deviceId;
    private String deviceName;
    private String vendor;
    private String deviceType;
    private double bandwidthMbps;      // Bandwidth in Megabits per second
    private double packetLossPercent;  // Packet loss as percentage (0-100)
    private double latencyMs;          // Latency in milliseconds
    private long timestamp;            // When metrics were collected
    private boolean online;            // Device online status
    
    /**
     * Constructor for DeviceMetrics
     * 
     * @param deviceId        Unique identifier of the device
     * @param deviceName      Human-readable name
     * @param vendor          Vendor/manufacturer name
     * @param deviceType      Type of device (Router, Switch, Firewall)
     * @param bandwidthMbps   Bandwidth in Mbps
     * @param packetLossPercent Packet loss percentage (0-100)
     * @param latencyMs       Latency in milliseconds
     * @param online          Device online status
     */
    public DeviceMetrics(String deviceId, String deviceName, String vendor, 
                         String deviceType, double bandwidthMbps, 
                         double packetLossPercent, double latencyMs, 
                         boolean online) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.vendor = vendor;
        this.deviceType = deviceType;
        this.bandwidthMbps = bandwidthMbps;
        this.packetLossPercent = packetLossPercent;
        this.latencyMs = latencyMs;
        this.timestamp = System.currentTimeMillis();
        this.online = online;
    }
    
    // Getters
    public String getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getVendor() { return vendor; }
    public String getDeviceType() { return deviceType; }
    public double getBandwidthMbps() { return bandwidthMbps; }
    public double getPacketLossPercent() { return packetLossPercent; }
    public double getLatencyMs() { return latencyMs; }
    public long getTimestamp() { return timestamp; }
    public boolean isOnline() { return online; }
    
    /**
     * Format metrics as a human-readable string
     * @return Formatted string with all metrics
     */
    @Override
    public String toString() {
        return String.format(
            "[%s] %s (%s - %s)\n" +
            "  Bandwidth: %.2f Mbps\n" +
            "  Packet Loss: %.2f%%\n" +
            "  Latency: %.2f ms\n" +
            "  Status: %s\n" +
            "  Collected at: %tF %<tT",
            deviceId, deviceName, vendor, deviceType,
            bandwidthMbps,
            packetLossPercent,
            latencyMs,
            online ? "ONLINE" : "OFFLINE",
            timestamp
        );
    }
    
    /**
     * Get a brief summary for display in tables/lists
     * @return Brief summary string
     */
    public String toBriefString() {
        return String.format("%s - %.1f Mbps, %.2f%% loss, %.1f ms",
            deviceName, bandwidthMbps, packetLossPercent, latencyMs);
    }
}
