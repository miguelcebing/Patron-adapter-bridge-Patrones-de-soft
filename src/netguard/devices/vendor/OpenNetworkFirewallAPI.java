package netguard.devices.vendor;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptee - Adapter Pattern (OpenNetwork Firewall)
 * 
 * This class simulates a modern OpenNetwork firewall API that uses:
 * - Key-value pair format for all metrics
 * - Latency measured in MICROSECONDS (not milliseconds)
 * - Uses custom keys like "bw", "pl", "lt" instead of descriptive names
 * 
 * This API is "incompatible" because:
 * 1. It uses abbreviated key names
 * 2. Latency is in microseconds (must convert to milliseconds)
 * 3. Values are stored as Strings (must parse to numbers)
 * 
 * The OpenFirewallAdapter will translate keys and convert units.
 */
public class OpenNetworkFirewallAPI {
    
    private String deviceId;
    private boolean connected;
    private Map<String, String> metricsStore;
    
    public OpenNetworkFirewallAPI(String deviceId) {
        this.deviceId = deviceId;
        this.connected = false;
        this.metricsStore = new HashMap<>();
        simulateRawData();
    }
    
    /**
     * Simulate connection to the OpenNetwork firewall
     * @return true if connection successful
     */
    public boolean connect() {
        try {
            Thread.sleep(60);
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
        return "OpenNetwork Firewall " + deviceId;
    }
    
    /**
     * Get a metric value by key (key-value API format)
     * 
     * The adapter must know the correct keys to use:
     * - "bw" for bandwidth (in Mbps)
     * - "pl" for packet loss (in percentage)
     * - "lt" for latency (in MICROSECONDS!)
     * - "st" for status
     * 
     * @param key The metric key
     * @return The metric value as a String
     */
    public String getMetric(String key) {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        
        if (!metricsStore.containsKey(key)) {
            throw new IllegalArgumentException("Unknown metric key: " + key);
        }
        
        // Add some random variation to simulate real readings
        String value = metricsStore.get(key);
        if (key.equals("bw")) {
            double bw = Double.parseDouble(value) + (Math.random() * 50 - 25);
            return String.format("%.2f", bw);
        } else if (key.equals("pl")) {
            double pl = Double.parseDouble(value) + (Math.random() * 0.1 - 0.05);
            return String.format("%.2f", Math.max(0, pl));
        } else if (key.equals("lt")) {
            double lt = Double.parseDouble(value) + (Math.random() * 1000 - 500);
            return String.format("%.0f", Math.max(0, lt));
        }
        
        return value;
    }
    
    /**
     * Get all metrics as a Map (key-value format)
     * 
     * @return Map containing all metrics
     */
    public Map<String, String> getAllMetrics() {
        if (!connected) {
            throw new IllegalStateException("Not connected to device " + deviceId);
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("bw", getMetric("bw"));
        result.put("pl", getMetric("pl"));
        result.put("lt", getMetric("lt"));
        result.put("st", getMetric("st"));
        return result;
    }
    
    /**
     * Simulate initial raw data (in vendor-specific format)
     * 
     * Key mappings:
     * - "bw" = bandwidth in Mbps
     * - "pl" = packet loss in percentage
     * - "lt" = latency in MICROSECONDS (not milliseconds!)
     * - "st" = status
     */
    private void simulateRawData() {
        metricsStore.put("bw", "500.00");    // 500 Mbps
        metricsStore.put("pl", "0.10");      // 0.1%
        metricsStore.put("lt", "5000");      // 5000 microseconds = 5 ms
        metricsStore.put("st", "ONLINE");
    }
}
