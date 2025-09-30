package yk.plugin.layoutinspector.config

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

val DEFAULT_PREF_WINDOWS = mapOf<String, List<String>>("com.android.systemui" to listOf<String>("NotificationShade"))

// 定义状态数据类
data class PluginState(
    var prefVersion: PrefVersion = PrefVersion.ASK,
    var fileNameElements: Set<FileNameElement> = setOf(FileNameElement.PROCESS, FileNameElement.TIME),
    var prefWindowMap: Map<String, List<String>> = DEFAULT_PREF_WINDOWS,
) {
    constructor() : this(
        prefVersion = PrefVersion.ASK,
        fileNameElements = setOf(FileNameElement.PROCESS, FileNameElement.TIME),
        prefWindowMap = DEFAULT_PREF_WINDOWS
    )
}

enum class FileNameElement {
    PROCESS, WINDOW, TIME,
}

enum class PrefVersion(private val type: Int) {
    V1(1), V2(2), ASK(10);

    companion object {
        fun from(type: Int): PrefVersion {
            return when (type) {
                1 -> V1
                2 -> V2
                else -> ASK
            }
        }
    }
}

// 获取应用级StorageManager（全局配置）
fun getAppConfigFilePath(configFileName: String): String {
    // 应用级配置默认存储在IDE的options目录下
    val optionsDir = PathManager.getOptionsPath() // 231版本中可靠获取options目录
    return "$optionsDir/$configFileName"
}

// 示例：删除应用级配置文件
fun deleteAppConfig(): Boolean {
    val configPath = getAppConfigFilePath(CONFIG_FILE_NAME)
    val configFile = File(configPath)
    return if (configFile.exists()) {
        configFile.delete() // 直接删除文件
    } else {
        false // 文件不存在
    }
}

const val CONFIG_FILE_NAME = "myplugin-settings.xml"

// 实现持久化接口
@State(
    name = "MyPluginSettings",
    storages = [Storage(CONFIG_FILE_NAME)]
)
class MyPluginSettings : PersistentStateComponent<PluginState> {
    private var state = PluginState()
    private val _prefVersionState = MutableStateFlow(PrefVersion.ASK)
    val prefVersion = _prefVersionState.asStateFlow()

    companion object {
        @JvmStatic
        fun getInstance() = service<MyPluginSettings>()
    }

    override fun getState(): PluginState = state

    override fun loadState(state: PluginState) {
        this.state = state
        _prefVersionState.value = state.prefVersion
    }
}