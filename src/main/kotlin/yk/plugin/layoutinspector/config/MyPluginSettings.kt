package yk.plugin.layoutinspector.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

// 定义状态数据类
data class PluginState(
    var apiUrl: String = "https://api.example.com",
    var enableFeature: Boolean = true,
    var maxResults: Int = 10,
    var prefVersion: PrefVersion = PrefVersion.ASK,
)

enum class PrefVersion(private val type: Int) {
    V1(1), V2(2), V1V2(3), ASK(4);

    fun toInt(): Int = type

    companion object {
        fun from(type: Int): PrefVersion {
            return when (type) {
                1 -> V1
                2 -> V2
                3 -> V1V2
                else -> ASK
            }
        }
    }
}

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