package netguard.devices.adapters;

import netguard.devices.NetworkDevice;
import netguard.devices.DeviceMetrics;
import netguard.devices.vendor.JuniperSwitchAPI;

/**
 * Concrete Adapter - Adapter Pattern (Juniper)
 * 
 * This adapter translates the Juniper Switch API to our
 * standard NetworkDevice interface.
 * 
 * The Juniper API returns all metrics as a single CSV string,
 * which must be parsed to extract individual values.
 * 
 * CSV Format: "bandwidth_mbps,packet_loss_percent,latency_ms,status"
 * 
 * The adapter parses this string and returns a DeviceMetrics object.
 */
public class JuniperSwitchAdapter implements NetworkDevice {
    
    private final JuniperSwitchAPI juniperApi;
    private final String vendor = "Juniper";
    private final String deviceType = "Switch";
    
    /**
     * Create adapter for a Juniper Switch
     * 
     * @param deviceId The identifier for this device
     */
    public JuniperSwitchAdapter(String deviceId) {
        this.juniperApi = new JuniperSwitchAPI(deviceId);
    }
    
    @Override
    public String getDeviceId() {
        return juniperApi.getDeviceId();
    }
    
    @Override
    public String getDeviceName() {
        return juniperApi.getDeviceName();
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
     * Read metrics from Juniper device and parse CSV string.
     * 
     * This is where the Adapter pattern shines - we parse:
     * - Raw CSV string → Individual metric values
     * - Handle parsing errors gracefully
     * 
     * @return DeviceMetrics with parsed values
     */
    @Override
    public DeviceMetrics readMetrics() {
        if (!juniperApi.isConnected()) {
            juniperApi.connect();
        }
        
        // Get raw CSV string from Juniper API
        String csvString = juniperApi.getMetricsCSV();
        
        // Parse CSV string
        String[] parts = csvString.split(",");
        
        if (parts.length < 4) {
            throw new RuntimeException("Invalid CSV format from Juniper device: " + csvString);
        }
        
        try {
            // Parse individual values
            // Note: Juniper already uses standard units (Mbps, %, ms)
            // so no unit conversion is needed here - just parsing
            double bandwidthMbps = Double.parseDouble(parts[0].trim());
            double packetLossPercent = Double.parseDouble(parts[1].trim());
            double latencyMs = Double.parseDouble(parts[2].trim());
            boolean online = "ONLINE".equalsIgnoreCase(parts[3].trim());
            
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
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parse Juniper CSV: " + csvString, e);
        }
    }
    
    @Override
    public boolean isOnline() {
        return juniperApi.isConnected();
    }
    
    /**
     * Connect to the Juniper device
     * @return true if connection successful
     */
    public boolean connect() {
        return juniperApi.connect();
    }
    
    /**
     * Disconnect from the Juniper device
     */
    public void disconnect() {
        juniperApi.disconnect();
    }
}
