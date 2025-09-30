/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.toolsy.idea.editors.layoutInspector.actions

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import yk.plugin.layoutinspector.res.Icons
import javax.swing.JComponent

/**
 * Lets the user choose an image to overlay on top of the captured view to compare the app's visual against design mocks.
 */
interface ReInspectorSupport {
    fun isCurrentEnable(project: Project?): Boolean
    fun runReInspector(project: Project?)
}

class ReInspectorAction(val supporter: ReInspectorSupport, parentDisposable: Disposable) :
    AnAction("", "Inspector again", Icons.REFRESH), CustomComponentAction, Disposable,
    AndroidDebugBridge.IDeviceChangeListener {
    init {
        Disposer.register(parentDisposable, this)
        AndroidDebugBridge.addDeviceChangeListener(this)
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return ActionButtonWithText(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabledAndVisible = supporter.isCurrentEnable(e.project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        supporter.runReInspector(e.project)
    }

    override fun dispose() {
        AndroidDebugBridge.removeDeviceChangeListener(this)
    }

    override fun deviceConnected(device: IDevice?) {
        refreshState()
    }

    override fun deviceDisconnected(device: IDevice?) {
        refreshState()
    }

    override fun deviceChanged(device: IDevice?, changeMask: Int) {
    }

    fun refreshState() {
    }
}
