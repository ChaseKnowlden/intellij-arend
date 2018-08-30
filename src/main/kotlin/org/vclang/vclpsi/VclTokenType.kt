package org.vclang.vclpsi

import com.intellij.psi.tree.IElementType
import org.vclang.VclLanguage

class VclTokenType(debugName: String) : IElementType(debugName, VclLanguage.INSTANCE)