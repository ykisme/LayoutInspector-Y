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

import com.android.toolsy.idea.editors.layoutInspector.ui.ViewNodeActiveDisplay
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Image
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.JComponent

private fun getLayoutInspectorIcon(iconName: String): Icon? {
    return try {
        // Hedgehog
        val clazz = Class.forName("icons.StudioIcons\$LayoutInspector")
        val field = clazz.getField(iconName)
        field.get(null) as? Icon
    } catch (ignored: Throwable) {
        // Iguana
        try {
            val clazz = Class.forName("icons.StudioIcons\$LayoutInspector\$Toolbar")
            val field = clazz.getField(iconName)
            field.get(null) as? Icon
        } catch (ignored: Throwable) {
            null
        }
    }
}

/**
 * Lets the user choose an image to overlay on top of the captured view to compare the app's visual against design mocks.
 */
class LoadOverlayAction(private val myPreview: ViewNodeActiveDisplay) :
    AnAction("", "Overlay Image", getLayoutInspectorIcon("LOAD_OVERLAY")), CustomComponentAction {
    companion object {
        @JvmField
        val LOG = Logger.getInstance(LoadOverlayAction::class.java)
    }

    init {

    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return ActionButtonWithText(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (e == null) return
        if (myPreview.hasOverlay()) {
            e.presentation.icon = getLayoutInspectorIcon("CLEAR_OVERLAY")
            e.presentation.description = "Clear Overlay"
        } else {
            e.presentation.icon = getLayoutInspectorIcon("LOAD_OVERLAY")
            e.presentation.description = "Overlay Image"
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (myPreview.hasOverlay()) {
            myPreview.setOverLay(null, null)
        } else {
            loadOverlay(e)
        }
    }

    private fun loadOverlay(e: AnActionEvent) {
        // 创建文件选择描述符，配置选择行为和文件过滤
        val descriptor = FileChooserDescriptor(
            /* 允许选择文件 */ true,
            /* 允许选择目录 */ false,
            /* 允许选择JAR中的文件 */ false,
            /* 允许选择JAR文件 */ false,
            /* 允许选择多个文件 */ false,
            /* 允许选择可执行文件 */ true
        ).apply {
            // 设置对话框标题
            title = "Choose Overlay"
            // 设置文件过滤器，只允许特定图片格式
            withFileFilter { file ->
                val extension = file.name.lowercase().substringAfterLast('.', "")
                extension in setOf("svg", "png", "jpg", "jpeg")
            }
        }

        // 创建文件选择对话框
        val fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null)

        // 获取初始目录
        val basePath = e.project?.basePath ?: "/"
        val toSelect = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath)

        // 显示对话框并获取选择的文件
        val files = fileChooserDialog.choose(null, toSelect)

        if (files.isEmpty()) {
            return
        }

        check(files.size == 1) { "Expected exactly one file to be selected" }
        // 加载选中的图片并设置预览
        myPreview.setOverLay(loadImageFile(files[0]), files[0].name)
    }

    private fun loadImageFile(file: VirtualFile): Image? {
        return try {
            ImageIO.read(file.inputStream)
        } catch (e: IOException) {
            Messages.showErrorDialog("Failed to read image from \"" + file.name + "\" Error: " + e.message, "Error")
            LOG.warn(e)
            return null
        }
    }
}
