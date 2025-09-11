package yk.plugin.layoutinspector.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

@Serializable
data class LayoutExtraInfo(var a: String?, var b: Int?, var c: Map<String, String>?) {
    fun toJsonByteArray(): ByteArray {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            allowStructuredMapKeys = true // 允许 Map 键为对象
            prettyPrint = true          // 格式化输出（调试时启用）
        }.encodeToString(this)
        return json.toByteArray(charset = Charset.forName("UTF-8"))
    }

    companion object {
        @JvmStatic
        fun byteArray2Map(bytes: ByteArray): Result<LayoutExtraInfo> {
            val str = String(bytes, Charset.forName("UTF-8"))
            return try {
                Result.success(Json.decodeFromString(str))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    }
}
