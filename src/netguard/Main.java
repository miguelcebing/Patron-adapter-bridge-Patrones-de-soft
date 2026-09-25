package netguard;

import netguard.core.NetworkMonitoringSystem;
import netguard.devices.NetworkDevice;
import netguard.devices.adapters.CiscoRouterAdapter;
import netguard.devices.adapters.JuniperSwitchAdapter;
import netguard.devices.adapters.OpenFirewallAdapter;
import netguard.gui.NetGuardDashboard;

/**
 * NetGuard - Network Monitoring System
 * 
 * Main entry point for the application.
 * 
 * This project demonstrates two structural design patterns:
 * 
 * 1. ADAPTER PATTERN:
 *    - Unifies incompatible vendor APIs (Cisco, Juniper, OpenNetwork)
 *    - Each vendor has different units and formats
 *    - Adapters convert to standard format (Mbps, %, ms)
 * 
 * 2. BRIDGE PATTERN:
 *    - Separates alert types (Basic, Critical) from
 *      notification channels (SMS, Email, Dashboard Push)
 *    - Avoids class explosion (2 types × 3 channels = 6 classes)
 *    - Instead: 2 + 3 = 5 classes (linear growth)
 * 
 * Run this class to launch the Swing GUI dashboard.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         NetGuard - Network Monitoring System               ║");
        System.out.println("║         Adapter + Bridge Design Patterns Demo               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Create the monitoring system
        NetworkMonitoringSystem monitoringSystem = new NetworkMonitoringSystem();
        
        // Register simulated devices (using Adapter pattern)
        // Each adapter wraps a vendor-specific API
        System.out.println("=== Registering Devices (Adapter Pattern) ===");
        
        NetworkDevice ciscoRouter = new CiscoRouterAdapter("CISCO-001");
        NetworkDevice juniperSwitch = new JuniperSwitchAdapter("JUNIPER-001");
        NetworkDevice openNetworkFirewall = new OpenFirewallAdapter("OPEN-001");
        
        monitoringSystem.registerDevice(ciscoRouter);
        monitoringSystem.registerDevice(juniperSwitch);
        monitoringSystem.registerDevice(openNetworkFirewall);
        
        System.out.println();
        System.out.println("=== System Ready ===");
        System.out.println(monitoringSystem.getSystemSummary());
        
        // Launch the Swing GUI
        System.out.println("Launching NetGuard Dashboard...");
        
        // Run GUI on Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Try to set FlatLaf look and feel for modern UI
                // Falls back to system L&F if FlatLaf not available
                try {
                    Class.forName("com.formdev.flatlaf.FlatDarkLaf");
                    javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                    System.out.println("Using FlatLaf Dark theme");
                } catch (ClassNotFoundException e) {
                    // FlatLaf not available, use system default
                    javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName());
                    System.out.println("Using system look and feel");
                }
            } catch (Exception e) {
                System.err.println("Failed to set look and feel: " + e.getMessage());
            }
            
            NetGuardDashboard dashboard = new NetGuardDashboard(monitoringSystem);
            dashboard.setVisible(true);
            
            System.out.println("\n=== Dashboard Launched ===");
            System.out.println("Use the GUI to interact with the monitoring system.");
        });
    }
}
