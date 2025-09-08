package yk.plugin.layoutinspector.config

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.selected
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MyPluginConfigurable : Configurable {
    private val settings = MyPluginSettings.getInstance()
    private lateinit var formBuilder: FormBuilder
    private lateinit var apiUri: JBTextField
    private lateinit var enableCheckBox: JBCheckBox
    private lateinit var maxSpinner: JSpinner
    private lateinit var prefVersionButtonGroup: JPanel
    private lateinit var buttonV1: JBRadioButton
    private lateinit var buttonV2: JBRadioButton
    private lateinit var buttonV1V2: JBRadioButton
    private lateinit var buttonAsk: JBRadioButton
    private lateinit var buttonGroup: ButtonGroup
    private var currentPrefVersion = PrefVersion.ASK

    private val mPrefVersionListener = object : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val button = e?.source as? JBRadioButton ?: return
            if (button.isSelected) {
                currentPrefVersion = when(button) {
                    buttonV1 -> PrefVersion.V1
                    buttonV2 -> PrefVersion.V2
                    buttonV1V2 -> PrefVersion.V1V2
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
        buttonV1V2 = JBRadioButton(Constant.CONFIG_PREFVERSION_V1V2)
        buttonV1V2.addActionListener(mPrefVersionListener)
        prefVersionButtonGroup.add(buttonV1V2)
        buttonGroup.add(buttonV1V2)
        buttonAsk = JBRadioButton(Constant.CONFIG_PREFVERSION_ASK)
        buttonAsk.addActionListener(mPrefVersionListener)
        prefVersionButtonGroup.add(buttonAsk)
        buttonGroup.add(buttonAsk)
        currentPrefVersion = settings.state.prefVersion
        refreshVersion(settings.state.prefVersion)

        apiUri = JBTextField(settings.state.apiUrl)
            // 输入验证
            .apply {
                document.addDocumentListener(object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent) = validate()
                    override fun removeUpdate(e: DocumentEvent) = validate()
                    override fun changedUpdate(e: DocumentEvent) = validate()

                    private fun validate() {
                        val text = (document.getText(0, document.length)).trim()
                        if (!text.startsWith("http")) {
                            background = Color.PINK
                        } else {
                            this@apply.background = UIUtil.getTextFieldBackground()
                        }
                    }
                })
            }

        enableCheckBox = JBCheckBox().apply { isSelected = settings.state.enableFeature }
        maxSpinner = JSpinner(SpinnerNumberModel(10, 1, 100, 1)).apply {
            value = settings.state.maxResults
        }
        return formBuilder
            .addLabeledComponent(Constant.CONFIG_PREFVERSION, prefVersionButtonGroup)
            .addLabeledComponent("API 地址:", apiUri, 1, false)
            .addLabeledComponent("启用功能:", enableCheckBox)
            .addLabeledComponent("最大结果数:", maxSpinner)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified() = settings.state.apiUrl != apiUri.getText() ||
            settings.state.prefVersion != currentPrefVersion ||
            settings.state.enableFeature != enableCheckBox.isSelected ||
            settings.state.maxResults != maxSpinner.value

    override fun apply() {
        settings.state.prefVersion = currentPrefVersion
        settings.state.apiUrl = apiUri.getText()
        settings.state.enableFeature = enableCheckBox.isSelected
        settings.state.maxResults = maxSpinner.value as Int
        settings.loadState(settings.state) // 触发持久化
    }

    fun selectVersionRadio(radio: JBRadioButton) {
        buttonV1.setSelected(false)
        buttonV2.setSelected(false)
        buttonV1V2.setSelected(false)
        buttonAsk.setSelected(false)
        radio.setSelected(true)
    }

    fun refreshVersion(v: PrefVersion) {
        when (v) {
            PrefVersion.V1 -> selectVersionRadio(buttonV1)
            PrefVersion.V2 -> selectVersionRadio(buttonV2)
            PrefVersion.V1V2 -> selectVersionRadio(buttonV1V2)
            PrefVersion.ASK -> selectVersionRadio(buttonAsk)
        }
    }

    override fun reset() {
        refreshVersion(settings.state.prefVersion)
        currentPrefVersion = settings.state.prefVersion
        apiUri.setText(settings.state.apiUrl)
        enableCheckBox.isSelected = settings.state.enableFeature
        maxSpinner.value = settings.state.maxResults
    }
}