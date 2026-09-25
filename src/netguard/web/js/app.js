/**
 * NetGuard Dashboard - Frontend Application
 * 
 * This JavaScript file handles all frontend interactions:
 * - Device selection and metrics reading
 * - Notification configuration
 * - Activity logging
 * - API communication with the backend
 */

// ========== Configuration ==========
const API_BASE_URL = '';

// ========== State Management ==========
const state = {
    devices: [],
    selectedDevice: null,
    currentMetrics: null,
    metricsHistory: [],
    alertHistory: []
};

// ========== DOM Elements ==========
const elements = {
    // Device selection
    deviceSelect: document.getElementById('deviceSelect'),
    readMetricsBtn: document.getElementById('readMetricsBtn'),
    
    // Notification config
    channelSelect: document.getElementById('channelSelect'),
    recipientInput: document.getElementById('recipientInput'),
    evaluateBtn: document.getElementById('evaluateBtn'),
    
    // Metrics display
    bandwidthValue: document.getElementById('bandwidthValue'),
    packetLossValue: document.getElementById('packetLossValue'),
    latencyValue: document.getElementById('latencyValue'),
    statusValue: document.getElementById('statusValue'),
    statusIcon: document.querySelector('#statusCard .metric-icon'),
    
    // Device details
    detailDeviceId: document.getElementById('detailDeviceId'),
    detailDeviceName: document.getElementById('detailDeviceName'),
    detailVendor: document.getElementById('detailVendor'),
    detailType: document.getElementById('detailType'),
    
    // Stats
    deviceCount: document.getElementById('deviceCount'),
    readingCount: document.getElementById('readingCount'),
    alertCount: document.getElementById('alertCount'),
    
    // Log
    logContainer: document.getElementById('logContainer'),
    clearLogBtn: document.getElementById('clearLogBtn'),
    
    // Loading
    loadingOverlay: document.getElementById('loadingOverlay'),
    
    // Toast
    toastContainer: document.getElementById('toastContainer')
};

// ========== API Functions ==========

/**
 * Fetch all registered devices from the server
 */
async function fetchDevices() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/devices`);
        const devices = await response.json();
        
        state.devices = devices;
        populateDeviceSelect(devices);
        updateDeviceCount(devices.length);
        
        addLog('Devices loaded successfully', 'success');
    } catch (error) {
        console.error('Error fetching devices:', error);
        addLog('Failed to load devices: ' + error.message, 'error');
        showToast('Error', 'Failed to load devices', 'error');
    }
}

/**
 * Fetch metrics for a specific device
 */
async function fetchMetrics(deviceId) {
    try {
        showLoading('Reading device metrics...');
        
        const response = await fetch(`${API_BASE_URL}/api/metrics/${deviceId}`);
        const metrics = await response.json();
        
        if (response.ok) {
            state.currentMetrics = metrics;
            state.metricsHistory.push(metrics);
            updateMetricsDisplay(metrics);
            updateDeviceDetails(metrics);
            updateReadingCount(state.metricsHistory.length);
            
            addLog(`Metrics read from ${metrics.deviceName}`, 'success');
            showToast('Success', `Metrics read from ${metrics.deviceName}`, 'success');
            
            // Enable evaluate button
            elements.evaluateBtn.disabled = false;
        } else {
            throw new Error(metrics.error || 'Failed to read metrics');
        }
    } catch (error) {
        console.error('Error fetching metrics:', error);
        addLog('Error reading metrics: ' + error.message, 'error');
        showToast('Error', 'Failed to read metrics', 'error');
    } finally {
        hideLoading();
    }
}

/**
 * Evaluate metrics and send notification
 */
async function evaluateAndNotify() {
    const deviceId = elements.deviceSelect.value;
    const channel = elements.channelSelect.value;
    const recipient = elements.recipientInput.value.trim();
    
    if (!deviceId) {
        showToast('Warning', 'Please select a device', 'warning');
        return;
    }
    
    if (!recipient) {
        showToast('Warning', 'Please enter a recipient', 'warning');
        return;
    }
    
    try {
        showLoading('Evaluating metrics and sending notification...');
        
        const response = await fetch(`${API_BASE_URL}/api/evaluate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                deviceId: deviceId,
                channel: channel,
                recipient: recipient
            })
        });
        
        const result = await response.json();
        
        if (response.ok && result.success) {
            // Update metrics display
            if (result.metrics) {
                state.currentMetrics = result.metrics;
                updateMetricsDisplay(result.metrics);
            }
            
            // Log alert if created
            if (result.alert) {
                state.alertHistory.push(result.alert);
                updateAlertCount(state.alertHistory.length);
                
                addLog(`Alert created: ${result.alert.severity} via ${result.alert.channel}`, 
                    result.alert.severity === 'CRITICAL' ? 'warning' : 'info');
                showToast('Alert Sent', 
                    `${result.alert.severity} alert sent via ${result.alert.channel}`, 
                    'success');
            } else {
                addLog('No alert needed - metrics within normal range', 'info');
                showToast('Info', 'No alert needed - all metrics normal', 'info');
            }
        } else {
            throw new Error(result.error || 'Evaluation failed');
        }
    } catch (error) {
        console.error('Error evaluating metrics:', error);
        addLog('Error during evaluation: ' + error.message, 'error');
        showToast('Error', 'Failed to evaluate metrics', 'error');
    } finally {
        hideLoading();
    }
}

// ========== UI Update Functions ==========

/**
 * Populate the device selection dropdown
 */
function populateDeviceSelect(devices) {
    elements.deviceSelect.innerHTML = '<option value="">Select a device...</option>';
    
    devices.forEach(device => {
        const option = document.createElement('option');
        option.value = device.id;
        option.textContent = `${device.id} - ${device.name}`;
        elements.deviceSelect.appendChild(option);
    });
    
    // Enable read button when device is selected
    elements.deviceSelect.addEventListener('change', () => {
        const hasSelection = elements.deviceSelect.value !== '';
        elements.readMetricsBtn.disabled = !hasSelection;
        
        if (hasSelection) {
            state.selectedDevice = devices.find(d => d.id === elements.deviceSelect.value);
        }
    });
}

/**
 * Update metrics display with new values
 */
function updateMetricsDisplay(metrics) {
    // Update metric values with animation
    animateValue(elements.bandwidthValue, metrics.bandwidthMbps, 1);
    animateValue(elements.packetLossValue, metrics.packetLossPercent, 2);
    animateValue(elements.latencyValue, metrics.latencyMs, 1);
    
    // Update status
    if (metrics.online) {
        elements.statusValue.textContent = 'ONLINE';
        elements.statusValue.style.color = 'var(--accent-success)';
        elements.statusIcon.classList.remove('offline');
    } else {
        elements.statusValue.textContent = 'OFFLINE';
        elements.statusValue.style.color = 'var(--accent-danger)';
        elements.statusIcon.classList.add('offline');
    }
}

/**
 * Animate a numeric value update
 */
function animateValue(element, targetValue, decimals) {
    const startValue = parseFloat(element.textContent) || 0;
    const duration = 500;
    const startTime = performance.now();
    
    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // Easing function (ease-out)
        const easeOut = 1 - Math.pow(1 - progress, 3);
        
        const currentValue = startValue + (targetValue - startValue) * easeOut;
        element.textContent = currentValue.toFixed(decimals);
        
        if (progress < 1) {
            requestAnimationFrame(update);
        }
    }
    
    requestAnimationFrame(update);
}

/**
 * Update device details display
 */
function updateDeviceDetails(metrics) {
    elements.detailDeviceId.textContent = metrics.deviceId;
    elements.detailDeviceName.textContent = metrics.deviceName;
    elements.detailVendor.textContent = metrics.vendor;
    elements.detailType.textContent = metrics.deviceType;
}

/**
 * Update device count in header
 */
function updateDeviceCount(count) {
    elements.deviceCount.textContent = count;
}

/**
 * Update reading count in header
 */
function updateReadingCount(count) {
    elements.readingCount.textContent = count;
}

/**
 * Update alert count in header
 */
function updateAlertCount(count) {
    elements.alertCount.textContent = count;
}

// ========== Log Functions ==========

/**
 * Add an entry to the activity log
 */
function addLog(message, type = 'info') {
    const time = new Date().toLocaleTimeString('en-US', { 
        hour12: false, 
        hour: '2-digit', 
        minute: '2-digit', 
        second: '2-digit' 
    });
    
    const entry = document.createElement('div');
    entry.className = `log-entry log-${type}`;
    entry.innerHTML = `
        <span class="log-time">${time}</span>
        <span class="log-message">${escapeHtml(message)}</span>
    `;
    
    // Remove placeholder if exists
    const placeholder = elements.logContainer.querySelector('.log-entry:first-child');
    if (placeholder && placeholder.querySelector('.log-time').textContent === '--:--:--') {
        placeholder.remove();
    }
    
    // Add to top of log
    elements.logContainer.insertBefore(entry, elements.logContainer.firstChild);
    
    // Keep only last 50 entries
    const entries = elements.logContainer.querySelectorAll('.log-entry');
    if (entries.length > 50) {
        entries[entries.length - 1].remove();
    }
}

/**
 * Clear the activity log
 */
function clearLog() {
    elements.logContainer.innerHTML = '';
    addLog('Log cleared', 'info');
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ========== Toast Functions ==========

/**
 * Show a toast notification
 */
function showToast(title, message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconSvg = '';
    switch (type) {
        case 'success':
            iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--accent-success)">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>`;
            break;
        case 'warning':
            iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--accent-warning)">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>`;
            break;
        case 'error':
            iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--accent-danger)">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>`;
            break;
        default:
            iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--accent-primary)">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>`;
    }
    
    toast.innerHTML = `
        <div class="toast-icon">${iconSvg}</div>
        <div class="toast-content">
            <div class="toast-title">${escapeHtml(title)}</div>
            <div class="toast-message">${escapeHtml(message)}</div>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
        </button>
    `;
    
    elements.toastContainer.appendChild(toast);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.animation = 'toastSlideIn 0.3s ease reverse';
            setTimeout(() => toast.remove(), 300);
        }
    }, 5000);
}

// ========== Loading Functions ==========

/**
 * Show loading overlay
 */
function showLoading(text = 'Processing...') {
    elements.loadingOverlay.querySelector('.loading-text').textContent = text;
    elements.loadingOverlay.style.display = 'flex';
}

/**
 * Hide loading overlay
 */
function hideLoading() {
    elements.loadingOverlay.style.display = 'none';
}

// ========== Event Listeners ==========

// Read Metrics button
elements.readMetricsBtn.addEventListener('click', () => {
    const deviceId = elements.deviceSelect.value;
    if (deviceId) {
        fetchMetrics(deviceId);
    }
});

// Evaluate & Notify button
elements.evaluateBtn.addEventListener('click', evaluateAndNotify);

// Clear Log button
elements.clearLogBtn.addEventListener('click', clearLog);

// ========== Initialization ==========

/**
 * Initialize the application
 */
async function init() {
    addLog('Initializing NetGuard Dashboard...', 'info');
    
    // Load devices
    await fetchDevices();
    
    addLog('Dashboard ready', 'success');
}

// Start the application when DOM is loaded
document.addEventListener('DOMContentLoaded', init);
