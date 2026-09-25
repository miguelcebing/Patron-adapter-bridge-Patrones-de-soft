package netguard.devices;

/**
 * Target Interface - Adapter Pattern
 * 
 * This is the common interface that all network devices must implement.
 * The Adapter pattern allows incompatible vendor APIs to be used
 * through this unified interface.
 * 
 * Any new vendor device must be adapted to implement this interface.
 */
public interface NetworkDevice {
    
    /**
     * Get the unique identifier of this device
     * @return Device ID string
     */
    String getDeviceId();
    
    /**
     * Get the human-readable name of this device
     * @return Device name
     */
    String getDeviceName();
    
    /**
     * Get the vendor/manufacturer of this device
     * @return Vendor name
     */
    String getVendor();
    
    /**
     * Get the device type (Router, Switch, Firewall, etc.)
     * @return Device type
     */
    String getDeviceType();
    
    /**
     * Read current metrics from this device
     * This method adapts vendor-specific API calls to return
     * standardized metrics in Mbps, percentage, and milliseconds
     * @return DeviceMetrics with standardized values
     */
    DeviceMetrics readMetrics();
    
    /**
     * Get the current status of this device
     * @return true if device is online and responding
     */
    boolean isOnline();
}
