---
name: netguard-adapter-bridge-case-study
description: Reference case study for teaching or reproducing the Adapter and Bridge design patterns in beginner-level Java OOP, built around "NetGuard", a heterogeneous network-device monitoring system with a Swing frontend. Use this skill whenever the user asks for an example, exercise, explanation, extension, or variation of the Adapter pattern, the Bridge pattern, or both together in Java — especially in networking-flavored or IoT-monitoring domains — or explicitly mentions NetGuard, this project, or "the beehive/network monitoring case study". Also use it when the user wants to add a new device vendor, a new notification channel, a new alert severity, unit tests, or a different frontend (web/CLI) on top of this same case study.
---

# NetGuard — Adapter + Bridge Case Study (Java, beginner OOP)

This skill documents a complete, already-built teaching case study that
combines the **Adapter** and **Bridge** structural design patterns in a
single, coherent Java project with a working Swing GUI. Use it as the
canonical reference whenever the user wants to explain, extend, test, port,
or create a *variant* of this exact project, instead of inventing a new,
unrelated example from scratch.

## What the case study is

**NetGuard** is a network-monitoring platform that has to talk to three
network devices from different vendors, each exposing an incompatible API,
and that must alert an operations team through different channels with
different severity levels.

- **Domain:** network device monitoring (routers/switches/firewalls) —
  deliberately not an e-commerce, bank-account, or library example, to stay
  memorable and avoid the most overused OOP-pattern demos.
- **Language / level:** Java, beginner-friendly OOP (clear class names,
  heavy inline comments explaining *why* each pattern piece exists, no
  frameworks, no external dependencies).
- **Frontend:** a functional desktop GUI built with plain Swing
  (`javax.swing`, part of the JDK) so the end user can pick a device, read
  its metrics, pick a notification channel, and trigger evaluation — no
  external server or browser required.

## Where each pattern lives

### Adapter — unifying incompatible vendor APIs

| Role | Class(es) |
|---|---|
| Target interface | `netguard.devices.NetworkDevice` |
| Adaptees (simulated 3rd-party APIs, "cannot be modified") | `netguard.devices.vendor.CiscoLegacyRouterAPI`, `JuniperSwitchAPI`, `OpenNetworkFirewallAPI` |
| Concrete Adapters | `netguard.devices.adapters.CiscoRouterAdapter`, `JuniperSwitchAdapter`, `OpenFirewallAdapter` |
| Standard output shape | `netguard.devices.DeviceMetrics` (bandwidth in Mbps, packet loss in %, latency in ms) |

Each vendor is incompatible on purpose, in a different way, so the value of
Adapter is visible from three angles at once:
- **Unit mismatch** (Cisco: Kbps / fraction / seconds vs. the standard Mbps / % / ms)
- **Shape mismatch** (Juniper: one method returning a raw CSV `String` that must be parsed)
- **Both at once** (OpenNetwork firewall: key/value lookup **and** latency in microseconds)

The rest of the application never references a vendor class directly — only
`NetworkDevice`.

### Bridge — decoupling alert severity from delivery channel

| Role | Class(es) |
|---|---|
| Implementor interface | `netguard.notifications.NotificationSender` |
| Concrete Implementors | `SmsNotifier`, `EmailNotifier`, `DashboardPushNotifier` |
| Abstraction | `netguard.alerts.NetworkAlert` (abstract class holding a `NotificationSender` reference — this field *is* the bridge) |
| Refined Abstractions | `BasicAlert` (informational, single send), `CriticalAlert` (urgent, sends + escalates) |

The combination of "which alert type" (decided from metrics/thresholds) and
"which channel" (decided by the user in the GUI) happens at runtime in
`netguard.core.NetworkMonitoringSystem.decideAlert(...)`, which is the best
single method to point to when explaining *why* Bridge avoids a
combinatorial explosion of classes like `CriticalAlertSms`,
`CriticalAlertEmail`, `BasicAlertSms`, etc.

## Full file map

```
netguard-project/
├── AGEND.md                                     (Spanish-language project write-up)
├── SKILL.md                                     (this file)
└── src/netguard/
    ├── Main.java
    ├── devices/
    │   ├── NetworkDevice.java                   (Adapter: Target)
    │   ├── DeviceMetrics.java                   (standard data format)
    │   ├── vendor/                              (Adapter: Adaptees)
    │   │   ├── CiscoLegacyRouterAPI.java
    │   │   ├── JuniperSwitchAPI.java
    │   │   └── OpenNetworkFirewallAPI.java
    │   └── adapters/                            (Adapter: Concrete Adapters)
    │       ├── CiscoRouterAdapter.java
    │       ├── JuniperSwitchAdapter.java
    │       └── OpenFirewallAdapter.java
    ├── notifications/                            (Bridge: Implementor side)
    │   ├── NotificationSender.java
    │   ├── SmsNotifier.java
    │   ├── EmailNotifier.java
    │   └── DashboardPushNotifier.java
    ├── alerts/                                   (Bridge: Abstraction side)
    │   ├── NetworkAlert.java
    │   ├── BasicAlert.java
    │   └── CriticalAlert.java
    ├── core/
    │   └── NetworkMonitoringSystem.java          (business logic, ties both patterns together)
    └── gui/
        └── NetGuardDashboard.java                (functional Swing frontend)
```

Build/run:
```bash
javac -d out $(find src -name "*.java")
java -cp out netguard.Main
```

## How to use this skill

**When explaining the patterns to the user (teaching mode):**
Walk Adapter and Bridge separately, always through this project's real
classes rather than abstract diagrams — e.g. "look at
`CiscoRouterAdapter.readMetrics()`, this line converts Kbps to Mbps" is more
concrete than a generic UML explanation. Point at `NetworkMonitoringSystem`
as the place where both patterns are *consumed* without knowing their
concrete implementations.

**When asked to extend the case study**, prefer additive changes that keep
the existing classes untouched, to demonstrate Open/Closed in practice:
- New vendor device → add one `vendor/XxxAPI.java` + one `adapters/XxxAdapter.java`, then register it in `NetGuardDashboard.registerSimulatedDevices()`. Nothing else changes.
- New notification channel (e.g. Slack/webhook) → add one class implementing `NotificationSender`. `BasicAlert`/`CriticalAlert` never change.
- New alert severity (e.g. `WarningAlert`) → add one subclass of `NetworkAlert`, wire a new threshold branch in `NetworkMonitoringSystem.decideAlert(...)`. Notifiers never change.
- New frontend (CLI, web, JavaFX) → build it against `NetworkMonitoringSystem` + `NetworkDevice` + `NotificationSender` only; never re-implement vendor parsing or alert formatting in the frontend layer.

**When asked to port this to another language or another domain**, keep the
same two-axis structure: one axis of "incompatible external sources unified
behind one interface" (Adapter) and one axis of "an abstraction hierarchy
that must vary independently from an implementation hierarchy" (Bridge).
Swap the domain nouns (e.g. sensors instead of network devices) but keep the
class-role mapping in the table above so the pedagogical shape stays
intact.

**Do not** default back to generic teaching examples (Shape/Rectangle for
Adapter, RemoteControl/Device for Bridge) when the user is clearly working
within or extending this project — reuse and extend NetGuard's own classes
and naming conventions instead, unless the user explicitly asks for a fresh,
unrelated example.
