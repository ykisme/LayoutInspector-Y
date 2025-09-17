package yk.plugin.layoutinspector.config

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.Nls
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel

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

    private val mPrefVersionListener = object : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val button = e?.source as? JBRadioButton ?: return
            if (button.isSelected) {
                currentPrefVersion = when(button) {
                    buttonV1 -> PrefVersion.V1
                    buttonV2 -> PrefVersion.V2
                    else -> PrefVersion.ASK
                }
            }
        }

    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = Constant.SETTINGS_NAME


    override fun createComponent(): JComponent {
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

        // file name settings
        initFileNameSettings()
        return formBuilder
            .addLabeledComponent(Constant.CONFIG_PREFVERSION, prefVersionButtonGroup)
            .addLabeledComponent(Constant.CONFIG_FILE_NAME, fileNameSettings)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun initFileNameSettings() {
        fileNameSettings = JPanel()
        checkboxFileNameProcess = JBCheckBox("Process name",
            settings.state.fileNameElements.contains(FileNameElement.PROCESS))
        checkboxFileNameWindow = JBCheckBox("Window name",
            settings.state.fileNameElements.contains(FileNameElement.WINDOW))
        checkboxFileNameTime = JBCheckBox("Time",
            settings.state.fileNameElements.contains(FileNameElement.TIME))
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
            settings.state.fileNameElements != currentFileSettings() ||
            settings.state.prefVersion != currentPrefVersion

    override fun apply() {
        settings.state.prefVersion = currentPrefVersion
        val fileNameSet = currentFileSettings()
        settings.state.fileNameElements = fileNameSet
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
        refreshVersion(settings.state.prefVersion)
        checkboxFileNameProcess.setSelected(settings.state.fileNameElements.contains(FileNameElement.PROCESS))
        checkboxFileNameWindow.setSelected(settings.state.fileNameElements.contains(FileNameElement.WINDOW))
        checkboxFileNameTime.setSelected(settings.state.fileNameElements.contains(FileNameElement.TIME))
        currentPrefVersion = settings.state.prefVersion
    }
}