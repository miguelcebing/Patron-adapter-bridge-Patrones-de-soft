package netguard.devices.vendor;

/**
 * Adaptee - Adapter Pattern (Cisco)
 * 
 * This class simulates a legacy Cisco router API that uses:
 * - Bandwidth in Kbps (Kilobits per second)
 * - Packet loss as a fraction (0.0 to 1.0)
 * - Latency in seconds
 * 
 * This API is "incompatible" with our standard format and cannot
 * be modified (simulating a real third-party API).
 * 
 * The CiscoRouterAdapter will convert these values to our standard
 * format (Mbps, percentage, milliseconds).
 */
public class CiscoLegacyRouterAPI {
    
    private String deviceId;
    private boolean connected;
    
    // Simulated raw values (vendor-specific units)
    private double rawBandwidthKbps;      // Kbps instead of Mbps
    private double rawPacketLossFraction;  // 0.0-1.0 instead of percentage
    private double rawLatencySeconds;      // Seconds instead of milliseconds
    
    public CiscoLegacyRouterAPI(String deviceId) {
        this.deviceId = deviceId;
        this.connected = false;
        simulateRawData();
    }
    
    /**
     * Simulate connection to the legacy Cisco device
     * @return true if connection successful
     */
    public boolean connect() {
        // Simulate connection delay for legacy device
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.connected = true;
        return true;
    }
    
    /**
     * Disconnect from the device
     */
    public void disconnect() {
        this.connected = false;
    }
    
    /**
     * Get device ID
     * @return Device identifier
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * Check if device is connected
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Read bandwidth in KILOBITS per second (legacy format)
     * 
     * NOTE: This is NOT Mbps! The adapter must convert:
     * Kbps / 1000 = Mbps
     * 
     * @return Bandwidth in Kbps
     */
    public double getBandwidthKbps() {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        // Add some random variation to simulate real readings
        return rawBandwidthKbps + (Math.random() * 100 - 50);
    }
    
    /**
     * Read packet loss as a FRACTION (legacy format)
     * 
     * NOTE: This is NOT percentage! The adapter must convert:
     * fraction * 100 = percentage
     * 
     * @return Packet loss as fraction (0.0 to 1.0)
     */
    public double getPacketLossFraction() {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        // Add some random variation
        double variation = Math.random() * 0.02 - 0.01;
        return Math.max(0.0, Math.min(1.0, rawPacketLossFraction + variation));
    }
    
    /**
     * Read latency in SECONDS (legacy format)
     * 
     * NOTE: This is NOT milliseconds! The adapter must convert:
     * seconds * 1000 = milliseconds
     * 
     * @return Latency in seconds
     */
    public double getLatencySeconds() {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        // Add some random variation
        return rawLatencySeconds + (Math.random() * 0.01 - 0.005);
    }
    
    /**
     * Get device name from the legacy API
     * @return Device name
     */
    public String getDeviceName() {
        return "Cisco Legacy Router " + deviceId;
    }
    
    /**
     * Simulate initial raw data (in vendor-specific units)
     */
    private void simulateRawData() {
        // Simulate realistic values in Kbps, fraction, seconds
        rawBandwidthKbps = 850000;    // 850 Mbps = 850,000 Kbps
        rawPacketLossFraction = 0.02; // 2% = 0.02
        rawLatencySeconds = 0.015;    // 15 ms = 0.015 seconds
    }
}
