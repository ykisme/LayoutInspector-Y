package yk.plugin.layoutinspector.res

import com.intellij.openapi.util.IconLoader

object Icons {
    @JvmStatic
    val VERSION_V1 = IconLoader.findIcon("/icons/icon_v1.svg", this::class.java.classLoader)
    @JvmStatic
    val VERSION_V2 = IconLoader.findIcon("/icons/icon_v2.svg", this::class.java.classLoader)
    @JvmStatic
    val VERSION_V1V2 = IconLoader.findIcon("/icons/icon_v1v2.svg", this::class.java.classLoader)
    @JvmStatic
    val VERSION_ASK = IconLoader.findIcon("/icons/icon_ask.svg", this::class.java.classLoader)
}