package yk.plugin.layoutinspector.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WindowManager
import java.awt.Window


fun getFocusWindowProject(adj: Boolean = true): Project? {
    val focusedWindow = WindowManager.getInstance().mostRecentFocusedWindow

// 从焦点窗口获取对应的Project
    val currentProject = getProjectFromWindow(focusedWindow)
    if (currentProject != null) return currentProject
    if (adj) {
        return ProjectManager.getInstance().defaultProject
    }
    return currentProject
}

/**
 * 从窗口获取对应的Project对象（替代getProjectFromWindow）
 * @param window 目标窗口（如通过WindowManager获取的焦点窗口）
 * @return 窗口对应的Project，无匹配时返回null
 */
fun getProjectFromWindow(window: Window?): Project? {
    if (window == null) return null

    // 遍历所有打开的项目
    val allProjects = ProjectManager.getInstance().openProjects
    for (project in allProjects) {
        // 获取项目对应的主窗口（IdeFrame）
        val ideFrame = WindowManager.getInstance().getIdeFrame(project) ?: continue
        // 比对窗口是否匹配（包括子窗口）
        if (isWindowRelatedToFrame(window, ideFrame)) {
            return project
        }
    }
    return null
}

/**
 * 检查窗口是否属于某个IdeFrame（项目主窗口及其子窗口）
 */
private fun isWindowRelatedToFrame(window: Window, ideFrame: IdeFrame): Boolean {
    // 1. 直接匹配主窗口
    if (window == ideFrame.component) {
        return true
    }
    // 2. 匹配子窗口（如对话框、工具窗口等）
    var parent = window.parent
    while (parent != null) {
        if (parent == ideFrame.component) {
            return true
        }
        parent = parent.parent
    }
    return false
}