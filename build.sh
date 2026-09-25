#!/bin/bash
# NetGuard - Build Script for Linux/macOS
# Compiles and runs the NetGuard Network Monitoring System

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         NetGuard - Build Script                             ║"
echo "║         Adapter + Bridge Design Patterns Demo               ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed or not in PATH"
    echo "Please install JDK 8 or higher"
    exit 1
fi

# Create output directory
if [ ! -d "out" ]; then
    echo "Creating output directory..."
    mkdir out
fi

echo ""
echo "[1/2] Compiling Java sources..."
echo ""

# Compile all Java files
javac -d out -encoding UTF-8 \
    src/netguard/Main.java \
    src/netguard/devices/NetworkDevice.java \
    src/netguard/devices/DeviceMetrics.java \
    src/netguard/devices/vendor/*.java \
    src/netguard/devices/adapters/*.java \
    src/netguard/notifications/*.java \
    src/netguard/alerts/*.java \
    src/netguard/core/*.java \
    src/netguard/gui/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Compilation failed!"
    exit 1
fi

echo ""
echo "[2/2] Compilation successful!"
echo ""

echo "Starting NetGuard Dashboard..."
echo ""

# Run the application
java -cp out netguard.Main
