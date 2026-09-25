package netguard.devices.adapters;

import java.util.Map;
import netguard.devices.NetworkDevice;
import netguard.devices.DeviceMetrics;
import netguard.devices.vendor.OpenNetworkFirewallAPI;

/**
 * Concrete Adapter - Adapter Pattern (OpenNetwork)
 * 
 * This adapter translates the OpenNetwork Firewall API to our
 * standard NetworkDevice interface.
 * 
 * The OpenNetwork API uses:
 * - Key-value pairs with abbreviated keys ("bw", "pl", "lt", "st")
 * - Latency in MICROSECONDS (not milliseconds!)
 * 
 * The adapter must:
 * 1. Translate key names to understand what each value represents
 * 2. Convert latency from microseconds to milliseconds
 * 3. Parse String values to numeric types
 */
public class OpenFirewallAdapter implements NetworkDevice {
    
    private final OpenNetworkFirewallAPI openNetworkApi;
    private final String vendor = "OpenNetwork";
    private final String deviceType = "Firewall";
    
    // Key mappings for the OpenNetwork API
    private static final String KEY_BANDWIDTH = "bw";
    private static final String KEY_PACKET_LOSS = "pl";
    private static final String KEY_LATENCY = "lt";
    private static final String KEY_STATUS = "st";
    
    /**
     * Create adapter for an OpenNetwork Firewall
     * 
     * @param deviceId The identifier for this device
     */
    public OpenFirewallAdapter(String deviceId) {
        this.openNetworkApi = new OpenNetworkFirewallAPI(deviceId);
    }
    
    @Override
    public String getDeviceId() {
        return openNetworkApi.getDeviceId();
    }
    
    @Override
    public String getDeviceName() {
        return openNetworkApi.getDeviceName();
    }
    
    @Override
    public String getVendor() {
        return vendor;
    }
    
    @Override
    public String getDeviceType() {
        return deviceType;
    }
    
    /**
     * Read metrics from OpenNetwork device and translate key-value pairs.
     * 
     * This is where the Adapter pattern shines - we:
     * 1. Look up values using abbreviated keys
     * 2. Convert latency from microseconds to milliseconds
     * 3. Parse String values to numeric types
     * 
     * @return DeviceMetrics with translated values
     */
    @Override
    public DeviceMetrics readMetrics() {
        if (!openNetworkApi.isConnected()) {
            openNetworkApi.connect();
        }
        
        // Get all metrics from OpenNetwork API (key-value format)
        Map<String, String> allMetrics = openNetworkApi.getAllMetrics();
        
        // Extract and parse values using key mappings
        double bandwidthMbps = parseDouble(allMetrics.get(KEY_BANDWIDTH));
        double packetLossPercent = parseDouble(allMetrics.get(KEY_PACKET_LOSS));
        double latencyMicroseconds = parseDouble(allMetrics.get(KEY_LATENCY));
        boolean online = "ONLINE".equalsIgnoreCase(allMetrics.get(KEY_STATUS));
        
        // Convert latency from microseconds to milliseconds
        double latencyMs = latencyMicroseconds / 1000.0;
        
        return new DeviceMetrics(
            getDeviceId(),
            getDeviceName(),
            vendor,
            deviceType,
            bandwidthMbps,
            packetLossPercent,
            latencyMs,
            online
        );
    }
    
    @Override
    public boolean isOnline() {
        return openNetworkApi.isConnected();
    }
    
    /**
     * Connect to the OpenNetwork device
     * @return true if connection successful
     */
    public boolean connect() {
        return openNetworkApi.connect();
    }
    
    /**
     * Disconnect from the OpenNetwork device
     */
    public void disconnect() {
        openNetworkApi.disconnect();
    }
    
    /**
     * Safely parse a String to double
     * 
     * @param value The String to parse
     * @return The parsed double, or 0.0 if parsing fails
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }
}
