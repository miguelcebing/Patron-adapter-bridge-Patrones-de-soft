# NetGuard - Network Monitoring System

A Java educational project demonstrating the **Adapter** and **Bridge** design patterns through a network device monitoring system with a modern Swing GUI.

## Overview

**NetGuard** is a platform that monitors network devices from different vendors (Cisco, Juniper, OpenNetwork), each with incompatible APIs, and sends alerts through various notification channels (SMS, Email, Dashboard Push).

## Design Patterns

### Adapter Pattern - Unifying Incompatible Vendor APIs

The Adapter pattern allows NetGuard to work with devices from different manufacturers through a unified interface.

| Component | Class | Description |
|-----------|-------|-------------|
| **Target Interface** | `NetworkDevice` | Common interface for all devices |
| **Adaptees** | `CiscoLegacyRouterAPI`, `JuniperSwitchAPI`, `OpenNetworkFirewallAPI` | Vendor-specific APIs (cannot be modified) |
| **Concrete Adapters** | `CiscoRouterAdapter`, `JuniperSwitchAdapter`, `OpenFirewallAdapter` | Convert vendor formats to standard format |

**Conversions:**
- **Cisco:** Kbps → Mbps, Fraction → %, Seconds → ms
- **Juniper:** CSV String parsing → Individual values
- **OpenNetwork:** Key-value pairs, Microseconds → ms

### Bridge Pattern - Decoupling Alerts from Notification Channels

The Bridge pattern separates alert types (Basic, Critical) from notification channels (SMS, Email, Dashboard Push), avoiding class explosion.

| Component | Class | Description |
|-----------|-------|-------------|
| **Abstraction** | `NetworkAlert` | Abstract alert hierarchy |
| **Refined Abstractions** | `BasicAlert`, `CriticalAlert` | Specific alert types |
| **Implementor Interface** | `NotificationSender` | Interface for notification channels |
| **Concrete Implementors** | `SmsNotifier`, `EmailNotifier`, `DashboardPushNotifier` | Actual notification channels |

**Benefit:** Without Bridge, we'd need 2 × 3 = 6 classes (CriticalAlertSms, CriticalAlertEmail, etc.). With Bridge: 2 + 3 = 5 classes.

## Project Structure

```
netguard-project/
├── AGEND.md                    (Spanish documentation)
├── SKILL.md                    (Technical reference)
├── src/netguard/
│   ├── Main.java               (Entry point)
│   ├── devices/
│   │   ├── NetworkDevice.java  (Adapter: Target Interface)
│   │   ├── DeviceMetrics.java  (Standard data format)
│   │   ├── vendor/             (Adapter: Adaptees)
│   │   │   ├── CiscoLegacyRouterAPI.java
│   │   │   ├── JuniperSwitchAPI.java
│   │   │   └── OpenNetworkFirewallAPI.java
│   │   └── adapters/           (Adapter: Concrete Adapters)
│   │       ├── CiscoRouterAdapter.java
│   │       ├── JuniperSwitchAdapter.java
│   │       └── OpenFirewallAdapter.java
│   ├── notifications/          (Bridge: Implementors)
│   │   ├── NotificationSender.java
│   │   ├── SmsNotifier.java
│   │   ├── EmailNotifier.java
│   │   └── DashboardPushNotifier.java
│   ├── alerts/                 (Bridge: Abstraction)
│   │   ├── NetworkAlert.java
│   │   ├── BasicAlert.java
│   │   └── CriticalAlert.java
│   ├── core/
│   │   └── NetworkMonitoringSystem.java
│   └── gui/
│       └── NetGuardDashboard.java
```

## Requirements

- **Java Development Kit (JDK) 8** or higher
- No external dependencies required (Swing is included in the JDK)

## Build and Run

### Windows (PowerShell)

```powershell
# Compile the project
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName })

# Run the application
java -cp out netguard.Main
```

### Linux/macOS

```bash
# Compile the project
javac -d out $(find src -name "*.java")

# Run the application
java -cp out netguard.Main
```

## Features

- **Device Management:** Register and monitor network devices
- **Metrics Reading:** Read standardized metrics (bandwidth, packet loss, latency)
- **Alert System:** Automatic alert generation based on thresholds
- **Notification Channels:** SMS, Email, and Dashboard Push notifications
- **Modern GUI:** Dark theme with intuitive controls
- **Activity Log:** Real-time logging of all operations

## How It Works

1. **Select a device** from the dropdown (Cisco, Juniper, or OpenNetwork)
2. **Click "Read Device Metrics"** to fetch current metrics
3. **Choose a notification channel** (SMS, Email, or Dashboard Push)
4. **Enter a recipient** for the notification
5. **Click "Evaluate & Notify"** to analyze metrics and send alerts

The system will automatically determine if a Basic or Critical alert is needed based on the metrics, and send it through the selected channel.

## Learning Objectives

1. Understand how the **Adapter pattern** unifies incompatible interfaces
2. See how the **Bridge pattern** avoids combinatorial class explosion
3. Practice implementing real-world design pattern applications
4. Learn about separation of concerns and SOLID principles

## License

Educational project for learning design patterns.
