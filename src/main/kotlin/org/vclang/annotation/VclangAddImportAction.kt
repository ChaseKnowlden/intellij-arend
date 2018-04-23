package org.vclang.annotation

import com.intellij.codeInsight.daemon.QuickFixBundle
import com.intellij.codeInsight.hint.QuestionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import org.vclang.quickfix.ResolveRefFixData
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.ui.popup.list.ListPopupImpl
import com.jetbrains.jetpad.vclang.util.LongName
import org.vclang.psi.VcFile

class VclangAddImportAction(val project : Project, val editor : Editor, val currentElement : PsiElement, val resolveData : List<ResolveRefFixData>) : QuestionAction {

    override fun execute(): Boolean {
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        if (!currentElement.isValid)
            return false

        resolveData.filterNot { it.target.isValid }
                .forEach { return false }

        if (resolveData.size == 1)
            addImport(resolveData[0])
        else
            chooseItemAndImport()

        return true
    }

    fun addImport(fixData : ResolveRefFixData) {
        DumbService.getInstance(project).withAlternativeResolveEnabled( {
            WriteCommandAction.runWriteCommandAction(project, QuickFixBundle.message("add.import"), null, Runnable { fixData.execute(editor) }, currentElement.containingFile) })
    }

    fun chooseItemAndImport(){
        if (resolveData.size == 1)
            resolveData[0].execute(editor)

        if (resolveData.size > 1) {
            val step = object: BaseListPopupStep<ResolveRefFixData>("Imports", resolveData) {
                override fun getTextFor(value: ResolveRefFixData?): String {
                    if (value != null && value.target.containingFile is VcFile) {
                        return LongName(value.targetFullName).toString() + " in " + LongName((value.target.containingFile as VcFile).modulePath.toList()).toString()
                    }
                    return super.getTextFor(value)
                }

                override fun onChosen(selectedValue: ResolveRefFixData?, finalChoice: Boolean): PopupStep<*>? {
                    if (finalChoice && selectedValue != null) {
                        return doFinalStep {
                            PsiDocumentManager.getInstance(project).commitAllDocuments()
                            addImport(selectedValue)
                        }
                    }

                    return PopupStep.FINAL_CHOICE
                }
            }
            val popup = ListPopupImpl(step)

            popup.showInBestPositionFor(editor)
        }
    }
}