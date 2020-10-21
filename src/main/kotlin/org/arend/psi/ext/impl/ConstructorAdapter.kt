package org.arend.psi.ext.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.stubs.IStubElementType
import org.arend.ArendIcons
import org.arend.naming.reference.GlobalReferable
import org.arend.naming.reference.LocatedReferable
import org.arend.psi.*
import org.arend.psi.ext.PsiLocatedReferable
import org.arend.psi.stubs.ArendConstructorStub
import org.arend.resolving.DataLocatedReferable
import org.arend.term.abs.Abstract
import org.arend.resolving.util.ParameterImpl
import org.arend.resolving.util.ReferenceImpl
import org.arend.resolving.util.getTypeOf
import javax.swing.Icon

abstract class ConstructorAdapter : ReferableAdapter<ArendConstructorStub>, ArendConstructor {
    constructor(node: ASTNode) : super(node)

    constructor(stub: ArendConstructorStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getKind() = GlobalReferable.Kind.CONSTRUCTOR

    override fun getData() = this

    override fun getPatterns(): List<Abstract.Pattern> = emptyList()

    override fun getConstructors(): List<ArendConstructor> = listOf(this)

    override fun getReferable() = this

    override fun getParameters(): List<ArendTypeTele> = typeTeleList

    override fun getEliminatedExpressions(): List<ArendRefIdentifier> = elim?.refIdentifierList ?: emptyList()

    override fun getClauses(): List<ArendClause> = clauseList

    override fun isVisible(): Boolean = true

    override fun getResultType(): ArendExpr? = null // expr // TODO[hits]

    override fun isCoerce() = coerceKw != null

    private val allParameters
        get() = (ancestor<DataDefinitionAdapter>()?.allParameters?.map { ParameterImpl(false, it.referableList, it.type) } ?: emptyList()) + parameters

    override val typeOf: Abstract.Expression?
        get() = getTypeOf(allParameters, ancestor<ArendDefData>()?.let { ReferenceImpl(it) })

    override fun getIcon(flags: Int): Icon = ArendIcons.CONSTRUCTOR

    override val psiElementType: ArendDefIdentifier?
        get() = ancestor<ArendDefData>()?.defIdentifier

    override fun makeTCReferable(data: SmartPsiElementPointer<PsiLocatedReferable>, parent: LocatedReferable?) =
        DataLocatedReferable(data, this, parent)
}
