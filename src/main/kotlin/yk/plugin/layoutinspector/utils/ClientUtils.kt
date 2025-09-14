package yk.plugin.layoutinspector.utils

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.Client
import com.android.ddmlib.IDevice

object ClientUtils {
    @JvmStatic
    fun selectClient(
        debugBridge: AndroidDebugBridge,
        deviceName: String?,
        deviceId: String?,
        clientName: String?
    ): Client? {
        if (clientName == null) return null
        val devices = debugBridge.devices
        if (devices.isEmpty()) return null
        var deviceSelect: IDevice?
        if (deviceName == null) {
            deviceSelect = devices.first()
        } else {
            val filter = devices.filter { it.name == deviceName }
            if (filter.isEmpty()) {
                deviceSelect = devices.first()
            } else {
                val filter2 = filter.filter { it.serialNumber == deviceId }
                if (filter2.isEmpty()) {
                    deviceSelect = filter.first()
                } else {
                    deviceSelect = filter2.first()
                }
            }
        }
        if (deviceSelect == null) return null
        return deviceSelect.getClient(clientName)
    }

    @JvmStatic
    fun haveClient(debugBridge: AndroidDebugBridge): Boolean {
        return try {
            val devices = debugBridge.devices
            return devices.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}