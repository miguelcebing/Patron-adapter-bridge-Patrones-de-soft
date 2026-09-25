package netguard.devices.vendor;

/**
 * Adaptee - Adapter Pattern (Juniper)
 * 
 * This class simulates a Juniper switch API that uses:
 * - All metrics returned as a single CSV String
 * - Format: "bandwidth_mbps,packet_loss_percent,latency_ms,status"
 * 
 * This API is "incompatible" because it returns everything as a raw
 * string that must be parsed, rather than individual typed values.
 * 
 * The JuniperSwitchAdapter will parse this CSV string and extract
 * the individual metrics.
 */
public class JuniperSwitchAPI {
    
    private String deviceId;
    private boolean connected;
    
    public JuniperSwitchAPI(String deviceId) {
        this.deviceId = deviceId;
        this.connected = false;
    }
    
    /**
     * Simulate connection to the Juniper switch
     * @return true if connection successful
     */
    public boolean connect() {
        try {
            Thread.sleep(80);
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
     * Get device name from the API
     * @return Device name
     */
    public String getDeviceName() {
        return "Juniper Switch " + deviceId;
    }
    
    /**
     * Read all metrics as a raw CSV string (Juniper's unique format)
     * 
     * The adapter must parse this string to extract individual metrics.
     * 
     * CSV Format: "bandwidth_mbps,packet_loss_percent,latency_ms,status"
     * Example: "1200.50,0.50,8.25,ONLINE"
     * 
     * @return Raw CSV string with all metrics
     */
    public String getMetricsCSV() {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        
        // Simulate realistic values with some variation
        double bandwidth = 1200 + (Math.random() * 100 - 50);  // ~1200 Mbps
        double packetLoss = 0.5 + (Math.random() * 0.2 - 0.1); // ~0.5%
        double latency = 8 + (Math.random() * 2 - 1);           // ~8 ms
        String status = "ONLINE";
        
        // Return as CSV string (this is the "incompatible" format)
        return String.format("%.2f,%.2f,%.2f,%s", 
            bandwidth, packetLoss, latency, status);
    }
    
    /**
     * Alternative method that returns metrics in a different CSV format
     * Some Juniper devices use semicolons instead of commas
     * 
     * @return Semicolon-separated CSV string
     */
    public String getMetricsCSVSemicolon() {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        
        double bandwidth = 1200 + (Math.random() * 100 - 50);
        double packetLoss = 0.5 + (Math.random() * 0.2 - 0.1);
        double latency = 8 + (Math.random() * 2 - 1);
        
        return String.format("%.2f;%.2f;%.2f;ONLINE", 
            bandwidth, packetLoss, latency);
    }
}
