# NetGuard — Heterogeneous Network Device Monitoring System

Proyecto educativo en **Java** que aplica **Programación Orientada a Objetos**
para un estudiante principiante, ilustrando dos patrones de diseño
estructurales: **Adapter** y **Bridge**. Todo el código fuente está escrito
en **inglés**, como se solicitó; este documento (AGEND.md) está en español
para explicar el proyecto.

## 1. Caso de estudio

No se usó un caso típico de libro (tienda, banco, biblioteca). En su lugar,
el sistema simula **NetGuard**, una plataforma de monitoreo que debe
convivir con **tres dispositivos de red de fabricantes distintos**, cada uno
con una API incompatible entre sí:

| Dispositivo simulado             | Particularidad de su API "real"                              |
|-----------------------------------|----------------------------------------------------------------|
| `CiscoLegacyRouterAPI`            | Unidades antiguas: Kbps, fracción (0.0–1.0), segundos          |
| `JuniperSwitchAPI`                | Expone un único método que devuelve un `String` CSV crudo      |
| `OpenNetworkFirewallAPI`          | API moderna tipo clave-valor, latencia en microsegundos        |

NetGuard necesita leer métricas de los tres (ancho de banda, pérdida de
paquetes, latencia) y, cuando algo se sale de rango, **notificar** a un
equipo de operaciones por **SMS, Email o notificación de panel (Dashboard
Push)**, con dos niveles de severidad: `BasicAlert` (informativa) y
`CriticalAlert` (urgente, con reenvío/escalamiento).

## 2. ¿Dónde está cada patrón?

### 2.1 Adapter — unificar dispositivos incompatibles

- **Target (interfaz común):** `netguard.devices.NetworkDevice`
- **Adaptee (APIs de fabricante, "no modificables"):**
  `netguard.devices.vendor.*`
- **Adapter (clases que traducen):** `netguard.devices.adapters.*`
  - `CiscoRouterAdapter` convierte Kbps→Mbps, fracción→porcentaje,
    segundos→milisegundos.
  - `JuniperSwitchAdapter` parsea el `String` CSV crudo.
  - `OpenFirewallAdapter` traduce claves y convierte microsegundos→ms.

Gracias al Adapter, el resto del sistema (`NetworkMonitoringSystem`, la
interfaz gráfica) **solo conoce `NetworkDevice`** y nunca una clase de
fabricante concreta.

### 2.2 Bridge — separar "tipo de alerta" de "canal de envío"

- **Implementor (interfaz de envío):**
  `netguard.notifications.NotificationSender`
- **Concrete Implementors (canales):** `SmsNotifier`, `EmailNotifier`,
  `DashboardPushNotifier`
- **Abstraction (jerarquía de alertas):** `netguard.alerts.NetworkAlert`
  (clase abstracta que **guarda una referencia** a un `NotificationSender`
  — ese atributo es literalmente el "puente")
- **Refined Abstractions:** `BasicAlert`, `CriticalAlert`

La idea central del Bridge se ve en `NetworkMonitoringSystem.decideAlert(...)`:
el tipo de alerta (Basic/Critical) se decide según las métricas, y el canal
de envío (SMS/Email/Push) se decide según lo que el usuario eligió en la
interfaz. **Ambas decisiones son independientes** y se combinan recién en
tiempo de ejecución — sin esa independencia, tendríamos que crear una clase
por cada combinación (`CriticalAlertSms`, `CriticalAlertEmail`,
`BasicAlertSms`, ...), que es justo el problema que Bridge evita.

## 3. Estructura del proyecto

```
netguard-project/
├── AGEND.md
└── src/
    └── netguard/
        ├── Main.java                          (punto de entrada)
        ├── devices/
        │   ├── NetworkDevice.java             (Adapter → Target)
        │   ├── DeviceMetrics.java             (formato estándar de datos)
        │   ├── vendor/                        (Adapter → Adaptee)
        │   │   ├── CiscoLegacyRouterAPI.java
        │   │   ├── JuniperSwitchAPI.java
        │   │   └── OpenNetworkFirewallAPI.java
        │   └── adapters/                      (Adapter → Concrete Adapter)
        │       ├── CiscoRouterAdapter.java
        │       ├── JuniperSwitchAdapter.java
        │       └── OpenFirewallAdapter.java
        ├── notifications/                     (Bridge → Implementor)
        │   ├── NotificationSender.java
        │   ├── SmsNotifier.java
        │   ├── EmailNotifier.java
        │   └── DashboardPushNotifier.java
        ├── alerts/                             (Bridge → Abstraction)
        │   ├── NetworkAlert.java
        │   ├── BasicAlert.java
        │   └── CriticalAlert.java
        ├── core/
        │   └── NetworkMonitoringSystem.java    (lógica de negocio)
        └── gui/
            └── NetGuardDashboard.java           (frontend funcional, Swing)
```

## 4. El frontend funcional

`NetGuardDashboard` es una ventana de escritorio hecha con **Swing**
(incluido en el JDK estándar, sin dependencias externas) donde el usuario
final puede:

1. Elegir uno de los tres dispositivos simulados (cada uno detrás de su
   Adapter) y pulsar **"Read Device Metrics"** para ver sus lecturas
   traducidas al formato estándar.
2. Elegir un canal de notificación (**SMS / Email / Dashboard Push**) y un
   destinatario, y pulsar **"Evaluate & Notify"**. El sistema decide
   automáticamente si la situación amerita una `BasicAlert` o una
   `CriticalAlert`, y la envía por el canal elegido (Bridge en acción).
3. Ver todo el historial de lecturas y notificaciones en un panel de log.

## 5. Cómo compilar y ejecutar

Se necesita un JDK (no solo un JRE), Java 8 o superior. Desde la carpeta
`netguard-project/`:

```bash
# Compilar todo el proyecto
javac -d out $(find src -name "*.java")

# Ejecutar la aplicación (abre la ventana Swing)
java -cp out netguard.Main
```

En Windows (PowerShell), reemplazar la línea de compilación por:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName })
```

## 6. Ideas para extender el ejercicio (opcional)

- Agregar un cuarto dispositivo/Adapter (por ejemplo, un access point WiFi)
  sin tocar `NetworkMonitoringSystem` ni la GUI salvo su registro.
- Agregar un nuevo canal (`WebhookNotifier`) implementando solo
  `NotificationSender`, sin tocar `BasicAlert` ni `CriticalAlert`.
- Agregar un tercer nivel de alerta, `WarningAlert`, para practicar cómo
  crece la jerarquía de Abstraction sin afectar a los Implementors.
  