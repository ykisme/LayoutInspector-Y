package yk.plugin.layoutinspector.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapperDialog
import com.intellij.openapi.ui.DialogWrapperPeer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.Nls
import yk.plugin.layoutinspector.utils.getFocusWindowProject
import yk.plugin.layoutinspector.utils.showRestartPromptNotification
import java.awt.Component
import java.awt.Container
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class MyPluginConfigurable : Configurable {
    private val settings = MyPluginSettings.getInstance()
    private lateinit var formBuilder: FormBuilder
    private lateinit var prefVersionButtonGroup: JPanel
    private lateinit var buttonV1: JBRadioButton
    private lateinit var buttonV2: JBRadioButton
    private lateinit var buttonAsk: JBRadioButton
    private lateinit var buttonGroup: ButtonGroup
    private var currentPrefVersion = PrefVersion.ASK
    private lateinit var checkboxFileNameProcess: JBCheckBox
    private lateinit var checkboxFileNameWindow: JBCheckBox
    private lateinit var checkboxFileNameTime: JBCheckBox
    private lateinit var fileNameSettings: JPanel
    private lateinit var prefWindowTextArea: JBTextArea
    private lateinit var originPrefWindowStr: String
    private var initSuccess = true
    private lateinit var mainPanel: JPanel

    private val mPrefVersionListener = object : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val button = e?.source as? JBRadioButton ?: return
            if (button.isSelected) {
                currentPrefVersion = when (button) {
                    buttonV1 -> PrefVersion.V1
                    buttonV2 -> PrefVersion.V2
                    else -> PrefVersion.ASK
                }
            }
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = Constant.SETTINGS_NAME

    fun createComp(): JComponent {
        formBuilder = FormBuilder.createFormBuilder()
        prefVersionButtonGroup = JPanel()
        buttonGroup = ButtonGroup()
        buttonV1 = JBRadioButton(Constant.CONFIG_PREFVERSION_V1)
        buttonGroup.add(buttonV1)
        buttonV1.addActionListener(mPrefVersionListener)
        prefVersionButtonGroup.add(buttonV1)
        buttonV2 = JBRadioButton(Constant.CONFIG_PREFVERSION_V2)
        buttonV2.addActionListener(mPrefVersionListener)
        buttonGroup.add(buttonV2)
        prefVersionButtonGroup.add(buttonV2)
        buttonAsk = JBRadioButton(Constant.CONFIG_PREFVERSION_ASK)
        buttonAsk.addActionListener(mPrefVersionListener)
        prefVersionButtonGroup.add(buttonAsk)
        buttonGroup.add(buttonAsk)
        currentPrefVersion = settings.state.prefVersion
        refreshVersion(settings.state.prefVersion)
        originPrefWindowStr = prefWindowName2String(settings.state.prefWindowMap)
        // file name settings
        initFileNameSettings()
        prefWindowTextArea = JBTextArea().apply {
            rows = 4
            lineWrap = true
        }
        val hintLabel = JBTextArea(Constant.CONFIG_PREF_WINDOW).apply {
            setEditable(false)
            setBorder(null)
            setOpaque(false)
            setFocusable(false)
            setLineWrap(true)
            setWrapStyleWord(true)
            rows = 2
            setForeground(UIManager.getColor("Label.foreground"))
        }
        return formBuilder
            .addLabeledComponent(Constant.CONFIG_PREFVERSION, prefVersionButtonGroup)
            .addLabeledComponent(Constant.CONFIG_FILE_NAME, fileNameSettings)
            .addVerticalGap(3)
            .addComponent(hintLabel)
            .addComponent(prefWindowTextArea)
            .addComponentFillVertically(JPanel(), 0)
            .panel.also { mainPanel = it }
    }


    private fun clearOldInvalidConfig() {
        if (deleteAppConfig()) {
            val proj = getFocusWindowProject() ?: return
            showRestartPromptNotification(proj, "Clear Settings Cache")
            closeSettingsDialog()
        }
    }

    fun createClearSettingsPanel(): JComponent {
        formBuilder = FormBuilder.createFormBuilder()
        return formBuilder.addComponent(JLabel(Constant.CLEAR_CACHE))
            .addComponent(JButton().apply {
                text = "Clear"
                addActionListener { clearOldInvalidConfig() }
            })
            .panel.also { mainPanel = it }
    }

    override fun createComponent(): JComponent {
        try {
            return createComp().also { initSuccess = true }
        } catch (e: Exception) {
            e.printStackTrace()
            initSuccess = false
            return createClearSettingsPanel()
        }
    }

    private fun initFileNameSettings() {
        fileNameSettings = JPanel()
        checkboxFileNameProcess = JBCheckBox(
            "Process name",
            settings.state.fileNameElements.contains(FileNameElement.PROCESS)
        )
        checkboxFileNameWindow = JBCheckBox(
            "Window name",
            settings.state.fileNameElements.contains(FileNameElement.WINDOW)
        )
        checkboxFileNameTime = JBCheckBox(
            "Time",
            settings.state.fileNameElements.contains(FileNameElement.TIME)
        )
        fileNameSettings.add(checkboxFileNameProcess)
        fileNameSettings.add(checkboxFileNameWindow)
        fileNameSettings.add(checkboxFileNameTime)
    }

    private fun currentFileSettings(): Set<FileNameElement> {
        return buildSet {
            if (checkboxFileNameProcess.isSelected) add(FileNameElement.PROCESS)
            if (checkboxFileNameWindow.isSelected) add(FileNameElement.WINDOW)
            if (checkboxFileNameTime.isSelected) add(FileNameElement.TIME)
        }
    }

    override fun isModified() =
        initSuccess && (
                settings.state.fileNameElements != currentFileSettings() ||
                        settings.state.prefVersion != currentPrefVersion ||
                        originPrefWindowStr != prefWindowTextArea.text
                )

    fun parsePrefWindowName(str: String): Map<String, List<String>> {
        val lists: List<Pair<String, String>> = str.split("\n").asSequence().map { str ->
            str.split("\\s+".toRegex())
        }.map { list ->
            if (list.size < 2) {
                null
            } else {
                list[0] to list[1]
            }

        }.filterNotNull().toList()
        return buildMap {
            for ((k, v) in lists) {
                var values: MutableList<String> = get(k) as? MutableList<String> ?: mutableListOf()
                values.add(v)
                put(k, values as List<String>)
            }
        }
    }

    fun prefWindowName2String(map: Map<String, List<String>>): String {
        val sb = StringBuilder()
        map.onEachIndexed { _, entry ->
            for (j in entry.value) {
                sb.append(entry.key).append(" ").append(j).append("\n")
            }
        }
        if (sb.isNotEmpty()) {
            sb.deleteCharAt(sb.length - 1)
        }
        return sb.toString()
    }

    override fun apply() {
        if (!initSuccess) return
        settings.state.prefVersion = currentPrefVersion
        val fileNameSet = currentFileSettings()
        settings.state.fileNameElements = fileNameSet
        settings.state.prefWindowMap = parsePrefWindowName(prefWindowTextArea.text)
        settings.loadState(settings.state) // 触发持久化
    }

    fun selectVersionRadio(radio: JBRadioButton) {
        buttonV1.setSelected(false)
        buttonV2.setSelected(false)
        buttonAsk.setSelected(false)
        radio.setSelected(true)
    }

    fun refreshVersion(v: PrefVersion) {
        when (v) {
            PrefVersion.V1 -> selectVersionRadio(buttonV1)
            PrefVersion.V2 -> selectVersionRadio(buttonV2)
            PrefVersion.ASK -> selectVersionRadio(buttonAsk)
        }
    }

    override fun reset() {
        if (!initSuccess) return
        refreshVersion(settings.state.prefVersion)
        checkboxFileNameProcess.setSelected(settings.state.fileNameElements.contains(FileNameElement.PROCESS))
        checkboxFileNameWindow.setSelected(settings.state.fileNameElements.contains(FileNameElement.WINDOW))
        checkboxFileNameTime.setSelected(settings.state.fileNameElements.contains(FileNameElement.TIME))
        currentPrefVersion = settings.state.prefVersion
        prefWindowTextArea.text = prefWindowName2String(settings.state.prefWindowMap)
        originPrefWindowStr = prefWindowTextArea.text
    }

    /**
     * 关闭当前设置弹框
     */
    private fun closeSettingsDialog() {
        // 从当前组件（如按钮）找到顶层窗口
        val component = mainPanel ?: return // mainPanel是Configurable的根组件
        val topWindow = SwingUtilities.getWindowAncestor(component) ?: return

        // 在EDT线程中关闭窗口（确保UI操作线程安全）
        ApplicationManager.getApplication().invokeLater {
            // 查找窗口对应的DialogWrapper
            val dialogWrapper = findDialogWrapper(topWindow)
            dialogWrapper?.close(DialogWrapper.OK_EXIT_CODE) // 以“确认”状态关闭
        }
    }

    /**
     * 从Window中找到对应的DialogWrapper实例
     */
    private fun findDialogWrapper(window: Window): DialogWrapper? {
        // 1. 若窗口本身是DialogWrapper的子类（如SettingsDialog），直接返回
        if (window is DialogWrapperDialog) {
            return window.dialogWrapper
        }
        // 2. 遍历窗口的所有组件，查找DialogWrapper关联的组件
        return findDialogWrapperRecursive(window.components)
    }

    /**
     * 递归遍历组件，查找DialogWrapper
     */
    private fun findDialogWrapperRecursive(components: Array<Component>): DialogWrapper? {
        for (comp in components) {
            if (comp is DialogWrapperDialog) {
                return comp.dialogWrapper
            }
            // 递归查找子组件
            if (comp is Container) {
                val found = findDialogWrapperRecursive(comp.getComponents())
                if (found != null) return found
            }
        }
        return null
    }
}