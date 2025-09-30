package yk.plugin.layoutinspector.utils

import com.intellij.openapi.project.Project
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages

private val RESTART_NOTIFICATION_GROUP = NotificationGroupManager.getInstance()
    .getNotificationGroup("Layout Inspector Y")

private fun restartIde(e: AnActionEvent) {
    val application = ApplicationManager.getApplication()
    // 在EDT线程中执行重启（确保UI操作线程安全）
    application.invokeLater {
        // 保存所有项目和设置
        application.saveAll()
        // 触发重启（通过IDE内置的重启动作）
        application.restart()
    }
}

fun showRestartPromptNotification(project: Project?, reason: String) {
    // 创建通知
    val notification = RESTART_NOTIFICATION_GROUP.createNotification(
        "Need restart IDE", // 通知标题
        reason, // 通知内容（说明重启原因）
        NotificationType.INFORMATION // 通知类型（信息类）
    )

    // 给通知添加“重启IDE”按钮动作
    notification.addAction(object : NotificationAction("Restart") {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
            // 点击按钮后：确认重启 → 执行重启
            val confirm = Messages.showYesNoDialog(
                e.project,
                "Are you sure you want to restart the IDE? Unsaved work will be saved automatically.",
                "Restart",
                Messages.getQuestionIcon()
            )
            if (confirm == Messages.YES) {
                // 触发IDE重启
                restartIde(e)
                // 关闭通知
                notification.expire()
            }
        }
    })

    // 显示通知（若有项目则关联项目，否则全局显示）
    notification.notify(project)
}
