/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.layoutinspector

import com.android.instantapp.utils.DeviceUtils
import com.android.layoutinspector.model.ClientWindow
import com.android.layoutinspector.model.ViewNode
import com.android.layoutinspector.parser.ViewNodeParser
import yk.plugin.layoutinspector.data.LayoutExtraInfo
import yk.plugin.layoutinspector.utils.ClientUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.concurrent.TimeUnit

object LayoutInspectorBridge {
    @JvmStatic
    val V2_MIN_API = 23

    @JvmStatic
    fun captureView(
        window: ClientWindow, options: LayoutInspectorCaptureOptions
    ): LayoutInspectorResult {
        val hierarchy =
            window.loadWindowData(options, if(options.version == ProtocolVersion.Version1) 50 else 20,
                TimeUnit.SECONDS) ?: return LayoutInspectorResult(
                null,
                "There was a timeout error capturing the layout data from the device.\n" +
                        "The device may be too slow, the captured view may be too complex, or the view may contain animations.\n\n" +
                        "Please retry with a simplified view and ensure the device is responsive.",
                options,
            )

        var root: ViewNode?
        try {
            root = ViewNodeParser.parse(hierarchy, options.version)
        } catch (e: StringIndexOutOfBoundsException) {
            return LayoutInspectorResult(null, "Unexpected error: $e", options)
        } catch (e: IOException) {
            return LayoutInspectorResult(null, "Unexpected error: $e", options)
        }

        if (root == null) {
            return LayoutInspectorResult(
                null,
                "Unable to parse view hierarchy",
                options,
            )
        }

        //  Get the preview of the root node
        val preview = window.loadViewImage(
            root,
            10,
            TimeUnit.SECONDS
        ) ?: return LayoutInspectorResult(
            null,
            "Unable to obtain preview image",
            options,
        )

        val bytes = ByteArrayOutputStream(4096)
        var output: ObjectOutputStream? = null

        try {
            output = ObjectOutputStream(bytes)
            output.writeUTF(options.toString())

            output.writeInt(hierarchy.size)
            output.write(hierarchy)

            output.writeInt(preview.size)
            output.write(preview)
            val deviceName = window.client.device?.name
            val deviceSerialNumber = window.client.device?.serialNumber
            val clientName = window.client.clientData?.clientDescription
            val apiLevel = window.client.getDevice().getVersion().getApiLevel()

            val layoutExtraInfo = LayoutExtraInfo(
                deviceName, deviceSerialNumber, clientName,
                apiLevel.toString(), window.displayName
            )
            try {
                val jsonBytes = layoutExtraInfo.toJsonByteArray()
                output.writeInt(jsonBytes.size)
                output.write(jsonBytes)
            } catch (_: Exception) {
            }
        } catch (e: IOException) {
            return LayoutInspectorResult(
                null,
                "Unexpected error while saving hierarchy snapshot: $e",
                options,
            )
        } finally {
            try {
                if (output != null) {
                    output.close()
                }
            } catch (e: IOException) {
                return LayoutInspectorResult(
                    null,
                    "Unexpected error while closing hierarchy snapshot: $e",
                    options,
                )
            }

        }

        return LayoutInspectorResult(bytes.toByteArray(), "", options)
    }
}
