package yk.plugin.layoutinspector.config

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import java.awt.Color
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

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = "My Plugin Settings"

    override fun createComponent(): JComponent {
        formBuilder = FormBuilder.createFormBuilder()
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
        maxSpinner=JSpinner(SpinnerNumberModel(10, 1, 100, 1)).apply {
            value = settings.state.maxResults
        }
        return formBuilder
            .addLabeledComponent("API 地址:", apiUri, 1, false)
            .addLabeledComponent("启用功能:", enableCheckBox)
            .addLabeledComponent("最大结果数:", maxSpinner)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified() =
        settings.state.apiUrl != apiUri.getText() ||
                settings.state.enableFeature != enableCheckBox.isSelected ||
                settings.state.maxResults != maxSpinner.value

    override fun apply() {
        settings.state.apiUrl = apiUri.getText()
        settings.state.enableFeature = enableCheckBox.isSelected
        settings.state.maxResults = maxSpinner.value as Int
        settings.loadState(settings.state) // 触发持久化
    }

    override fun reset() {
        apiUri.setText(settings.state.apiUrl)
        enableCheckBox.isSelected = settings.state.enableFeature
        maxSpinner.value = settings.state.maxResults
    }
}