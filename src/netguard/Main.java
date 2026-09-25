package netguard;

import netguard.core.NetworkMonitoringSystem;
import netguard.devices.NetworkDevice;
import netguard.devices.adapters.CiscoRouterAdapter;
import netguard.devices.adapters.JuniperSwitchAdapter;
import netguard.devices.adapters.OpenFirewallAdapter;
import netguard.server.WebServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

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
 * Run this class to launch the web-based dashboard.
 */
public class Main {
    
    private static final int PORT = 8080;
    
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
        
        // Start the web server
        System.out.println("Starting NetGuard Web Server...");
        
        try {
            WebServer webServer = new WebServer(monitoringSystem, PORT);
            webServer.start();
            
            // Open browser automatically
            openBrowser(webServer.getServerUrl());
            
            System.out.println("\n=== Dashboard Launched ===");
            System.out.println("Open your browser and navigate to: " + webServer.getServerUrl());
            System.out.println("Press Ctrl+C to stop the server.");
            
            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down NetGuard...");
                webServer.stop();
                System.out.println("Goodbye!");
            }));
            
        } catch (IOException e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Open the default web browser
     * 
     * @param url The URL to open
     */
    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    System.out.println("Opening browser at: " + url);
                    return;
                }
            }
            
            // Fallback: try to open browser using system commands
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("cmd /c start " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
            
            System.out.println("Opening browser at: " + url);
            
        } catch (Exception e) {
            System.out.println("Could not open browser automatically.");
            System.out.println("Please open: " + url);
        }
    }
}
