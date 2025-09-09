package yk.plugin.layoutinspector

import com.intellij.openapi.fileTypes.FileType
import icons.StudioIcons
import yk.plugin.layoutinspector.res.Icons
import javax.swing.Icon

/**
 * 旧版的在 filetypes.xml 中配置了 removed_mapping 被移除了
 *
 * 是通过 name 匹配的，因此我们再添加一个其他 name 的 FileType 即可
 *
 * 由于 LayoutInspectorFileType 的构造函数是 private，所以我们直接重写一个
 *
 * @author pingfangx
 * @date 2022/6/30
 */
class LegacyLayoutInspectorFileTypeV2 private constructor() : FileType {
    override fun getName(): String {
        return "Layout Inspector(V1)"
    }

    override fun getDescription(): String {
        return "Legacy Layout Inspector Snapshot(V1)"
    }

    override fun getDefaultExtension(): String {
        return EXT_LAYOUT_INSPECTOR
    }

    override fun getIcon(): Icon {
        return Icons.VERSION_V2!!
    }

    override fun isBinary(): Boolean {
        return true
    }

    override fun isReadOnly(): Boolean {
        return true
    }

    companion object {
        @JvmField
        val INSTANCE = LegacyLayoutInspectorFileTypeV2()
        const val EXT_LAYOUT_INSPECTOR = "liv2"
        const val DOT_EXT_LAYOUT_INSPECTOR = ".liv2"
    }
}