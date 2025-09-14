package yk.plugin.layoutinspector

import com.android.tools.idea.editors.layoutInspector.actions.AndroidRunLayoutInspectorAction
import com.android.tools.idea.gradle.project.sync.ModelCache
import com.intellij.openapi.actionSystem.AnActionEvent
import yk.plugin.layoutinspector.config.MyPluginSettings
import yk.plugin.layoutinspector.config.PrefVersion
import yk.plugin.layoutinspector.res.Icons

/**
 * 旧版
 *
 * 如果直接新建一个 Action 指向 AndroidRunLayoutInspectorAction
 * 则会在 [update] 中被隐藏
 *
 * 所以需要新建一个类处理
 *
 * @author pingfangx
 * @date 2022/6/30
 */
class LegacyLayoutInspectorAction : AndroidRunLayoutInspectorAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        // 忽略父类设置，总是可见
        e.presentation.isVisible = true
        val version = MyPluginSettings.getInstance().prefVersion.value
        e.presentation.icon = when(version) {
            PrefVersion.V1 -> Icons.VERSION_V1
            PrefVersion.V2 -> Icons.VERSION_V2
            else -> Icons.VERSION_ASK
        }
    }
}