package yk.plugin.layoutinspector.config

object Constant {
    const val CLEAR_CACHE: String = "An error has occurred. Please clear the Settings and re-enter."
    const val CONFIG_PREF_WINDOW: String = "Get the Default Window Settings of a Process (with the process name and window name separated by a space), e.g.: com.android.systemui NotificationShade"
    const val ACTION_LAYOUT_INSPECTOR_TITLE: String = "Layout Inspector Y"
    const val ACTION_LAYOUT_INSPECTOR_DES: String = "Inspect Layout for the selected window"
    const val PLUGIN_NAME = "LayoutInspector-Y"
    const val SETTINGS_NAME = PLUGIN_NAME

    const val CONFIG_PREFVERSION = "default version:"
    const val CONFIG_PREFVERSION_V1 = "V1"
    const val CONFIG_PREFVERSION_V2 = "V2"
    const val CONFIG_PREFVERSION_V1V2 = "Mixed"
    const val CONFIG_PREFVERSION_ASK = "Always ask"

    const val CONFIG_FILE_NAME = "File Naming Format:"

    const val TITLE_SELECT_VERSION = "Select version"
    const val V1_BTN_DES = "V1 (slow, more information)"
    const val V2_BTN_DES = "V2 (faster, less information)"
    const val PREF_SETTING_INFO = "You can configure to ignore prompts in Settings. Search for '${SETTINGS_NAME}'."
}