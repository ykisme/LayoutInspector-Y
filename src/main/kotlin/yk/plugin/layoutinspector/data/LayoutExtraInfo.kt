package yk.plugin.layoutinspector.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

@Serializable
data class LayoutExtraInfo(
    // 设备名
    val deviceName: String?,
    // 设备序列号
    val deviceSeri: String?,
    // 进程名
    val clientName: String?,
    // api级别
    val apiLevel: String?,
    // 窗口名
    val windowName: String?,
) {
    fun canReinspector(): Boolean {
        return this.clientName?.isNotBlank() == true
                && this.windowName?.isNotBlank() == true
    }

    fun toJsonByteArray(): ByteArray {
        val json = Json {
            ignoreUnknownKeys = true // 忽略老版本中可能存在的其他字段
            isLenient = true  //允许解析非严格格式（如字段名大小写不一致）
            encodeDefaults = true // 序列化时包含默认值（确保新版本输出兼容）
            allowStructuredMapKeys = true // 允许 Map 键为对象
            explicitNulls = false // 不序列化 null 值
            prettyPrint = true          // 格式化输出（调试时启用）
        }.encodeToString(this)
        return json.toByteArray(charset = Charset.forName("UTF-8"))
    }

    companion object {
        @JvmStatic
        fun byteArray2Map(bytes: ByteArray): Result<LayoutExtraInfo> {
            val str = String(bytes, Charset.forName("UTF-8"))
            return try {
                Result.success(Json {
                    ignoreUnknownKeys = true // 忽略老版本中可能存在的其他字段
                    isLenient = true  //允许解析非严格格式（如字段名大小写不一致）
                    encodeDefaults = true // 序列化时包含默认值（确保新版本输出兼容）
                    allowStructuredMapKeys = true // 允许 Map 键为对象
                    explicitNulls = false // 不序列化 null 值
                    prettyPrint = true          // 格式化输出（调试时启用）
                }.decodeFromString(str))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    }
}
