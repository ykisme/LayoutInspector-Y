package yk.plugin.layoutinspector.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

// 定义状态数据类
data class PluginState(
    var apiUrl: String = "https://api.example.com",
    var enableFeature: Boolean = true,
    var maxResults: Int = 10
)

// 实现持久化接口
@State(
    name = "MyPluginSettings",
    storages = [Storage("myplugin-settings.xml")]
)
class MyPluginSettings : PersistentStateComponent<PluginState> {
    private var state = PluginState()

    companion object {
        @JvmStatic
        fun getInstance() = service<MyPluginSettings>()
    }

    override fun getState(): PluginState = state

    override fun loadState(state: PluginState) {
        this.state = state
    }
}