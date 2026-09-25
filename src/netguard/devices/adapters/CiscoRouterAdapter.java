package netguard.devices.adapters;

import netguard.devices.NetworkDevice;
import netguard.devices.DeviceMetrics;
import netguard.devices.vendor.CiscoLegacyRouterAPI;

/**
 * Concrete Adapter - Adapter Pattern (Cisco)
 * 
 * This adapter translates the Cisco Legacy Router API to our
 * standard NetworkDevice interface.
 * 
 * Conversions performed:
 * - Bandwidth: Kbps → Mbps (divide by 1000)
 * - Packet Loss: Fraction (0.0-1.0) → Percentage (0-100)
 * - Latency: Seconds → Milliseconds (multiply by 1000)
 * 
 * The rest of the application only uses NetworkDevice interface
 * and never knows about this conversion.
 */
public class CiscoRouterAdapter implements NetworkDevice {
    
    private final CiscoLegacyRouterAPI ciscoApi;
    private final String vendor = "Cisco";
    private final String deviceType = "Legacy Router";
    
    /**
     * Create adapter for a Cisco Legacy Router
     * 
     * @param deviceId The identifier for this device
     */
    public CiscoRouterAdapter(String deviceId) {
        this.ciscoApi = new CiscoLegacyRouterAPI(deviceId);
    }
    
    @Override
    public String getDeviceId() {
        return ciscoApi.getDeviceId();
    }
    
    @Override
    public String getDeviceName() {
        return ciscoApi.getDeviceName();
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
     * Read metrics from Cisco device and convert to standard format.
     * 
     * This is where the Adapter pattern shines - we convert:
     * - Kbps → Mbps (÷ 1000)
     * - Fraction → Percentage (× 100)
     * - Seconds → Milliseconds (× 1000)
     * 
     * @return DeviceMetrics with standardized values
     */
    @Override
    public DeviceMetrics readMetrics() {
        if (!ciscoApi.isConnected()) {
            ciscoApi.connect();
        }
        
        // Read raw values from Cisco API (vendor-specific units)
        double bandwidthKbps = ciscoApi.getBandwidthKbps();
        double packetLossFraction = ciscoApi.getPacketLossFraction();
        double latencySeconds = ciscoApi.getLatencySeconds();
        
        // Convert to standard format
        double bandwidthMbps = bandwidthKbps / 1000.0;           // Kbps → Mbps
        double packetLossPercent = packetLossFraction * 100.0;   // Fraction → %
        double latencyMs = latencySeconds * 1000.0;              // Seconds → ms
        
        return new DeviceMetrics(
            getDeviceId(),
            getDeviceName(),
            vendor,
            deviceType,
            bandwidthMbps,
            packetLossPercent,
            latencyMs,
            ciscoApi.isConnected()
        );
    }
    
    @Override
    public boolean isOnline() {
        return ciscoApi.isConnected();
    }
    
    /**
     * Connect to the Cisco device
     * @return true if connection successful
     */
    public boolean connect() {
        return ciscoApi.connect();
    }
    
    /**
     * Disconnect from the Cisco device
     */
    public void disconnect() {
        ciscoApi.disconnect();
    }
}
