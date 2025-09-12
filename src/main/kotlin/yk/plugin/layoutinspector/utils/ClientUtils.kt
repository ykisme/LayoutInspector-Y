package yk.plugin.layoutinspector.utils

import com.android.ddmlib.Client
import com.android.ddmlib.IDevice
import com.intellij.openapi.project.Project
import org.jetbrains.android.sdk.AndroidSdkUtils

object ClientUtils {
    fun selectClient(project: Project, deviceName: String?, deviceId: String?, clientName: String?): Client? {
        val debugBridge = AndroidSdkUtils.getDebugBridge(project) ?: return null
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
}