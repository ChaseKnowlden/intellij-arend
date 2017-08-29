package org.vclang.ide.typecheck.execution.options

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.Label
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import org.vclang.ide.typecheck.execution.TypeCheckCommand
import org.vclang.ide.typecheck.execution.configurations.TypeCheckConfiguration
import org.vclang.lang.VcFileType
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JTextField

class TypeCheckRunConfigurationEditor(
        private val project: Project
) : SettingsEditor<TypeCheckConfiguration>() {
    private val modulePathComponent = TextFieldWithBrowseButton()
    private val definitionNameComponent = JTextField()  // TODO: replace text field with some structure browser

    override fun resetEditorFrom(configuration: TypeCheckConfiguration) {
        configuration.vclangTypeCheckCommand.let {
            modulePathComponent.text = it.modulePath
            definitionNameComponent.text = it.definitionFullName
        }
    }

    override fun applyEditorTo(configuration: TypeCheckConfiguration) {
        configuration.vclangTypeCheckCommand = TypeCheckCommand(
                modulePathComponent.text,
                definitionNameComponent.text
        )
    }

    override fun createEditor(): JComponent = panel {
        labeledRow("Path to Vclang module:", modulePathComponent) {
            modulePathComponent.apply {
                modulePathComponent.addBrowseFolderListener(
                        "Vclang Module Path",
                        "Specify path to Vclang module",
                        project,
                        FileChooserDescriptorFactory.createSingleFileDescriptor(VcFileType)
                )
                preferredSize = Dimension(1000, height)
            }()
        }
        labeledRow("Definition:", definitionNameComponent) {
            definitionNameComponent()
        }
    }

    private fun LayoutBuilder.labeledRow(
            labelText: String,
            component: JComponent,
            init: Row.() -> Unit
    ) {
        val label = Label(labelText)
        label.labelFor = component
        row(label) { init() }
    }
}