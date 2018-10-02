package org.arend.typing

import org.arend.naming.reference.ClassReferable
import org.arend.naming.reference.Referable
import org.arend.naming.resolving.visitor.ExpressionResolveNameVisitor
import org.arend.naming.scope.Scope
import org.arend.term.abs.Abstract
import org.arend.term.abs.BaseAbstractExpressionVisitor
import org.arend.term.concrete.Concrete
import org.arend.psi.ArendDefFunction
import org.arend.psi.ArendExpr


class ReferableExtractVisitor : BaseAbstractExpressionVisitor<Void?, Referable?>(null) {
    private enum class Mode { TYPE, EXPRESSION, NONE }
    private var mode: Mode = Mode.NONE
    private var arguments: Int = 0

    private fun findClassReference(referent: Referable?, originalScope: Scope): ClassReferable? {
        mode = Mode.EXPRESSION
        var ref: Referable? = referent
        var visited: MutableSet<ArendDefFunction>? = null
        var scope = originalScope
        while (true) {
            ref = ExpressionResolveNameVisitor.resolve(ref, scope)
            if (ref is ClassReferable) {
                return ref
            }
            if (ref !is ArendDefFunction) {
                return null
            }

            val term = ref.functionBody?.expr ?: return null
            arguments -= ref.nameTeleList.sumBy { if (it.isExplicit) it.identifierOrUnknownList.size else 0 }
            if (arguments < 0) {
                return null
            }

            if (visited == null) {
                visited = mutableSetOf(ref)
            } else {
                if (!visited.add(ref)) {
                    return null
                }
            }
            ref = term.accept(this, null)
            scope = term.scope
        }
    }

    fun findClassReferable(expr: ArendExpr): ClassReferable? {
        mode = Mode.TYPE
        arguments = 0
        return findClassReference(expr.accept(this, null), expr.scope)
    }

    override fun visitBinOpSequence(data: Any?, left: Abstract.Expression, sequence: Collection<Abstract.BinOpSequenceElem>, errorData: Abstract.ErrorData?, params: Void?): Referable? {
        var expr = parseBinOp(left, sequence)
        if (expr is Concrete.AppExpression) {
            arguments += expr.arguments.sumBy { if (it.isExplicit) 1 else 0 }
            expr = expr.function
        }
        return (expr as? Concrete.ReferenceExpression)?.referent
    }

    override fun visitReference(data: Any?, referent: Referable, level1: Abstract.LevelExpression?, level2: Abstract.LevelExpression?, errorData: Abstract.ErrorData?, params: Void?): Referable? = referent

    override fun visitReference(data: Any?, referent: Referable, lp: Int, lh: Int, errorData: Abstract.ErrorData?, params: Void?): Referable? = referent

    override fun visitLam(data: Any?, parameters: Collection<Abstract.Parameter>, body: Abstract.Expression?, errorData: Abstract.ErrorData?, params: Void?): Referable? {
        if (mode != Mode.EXPRESSION || body == null) {
            return null
        }

        arguments -= parameters.sumBy { if (it.isExplicit) it.referableList.size else 0 }
        if (arguments < 0) {
            return null
        }

        return body.accept(this, null)
    }

    override fun visitPi(data: Any?, parameters: Collection<Abstract.Parameter>, codomain: Abstract.Expression?, errorData: Abstract.ErrorData?, params: Void?): Referable? {
        if (mode != Mode.TYPE || codomain == null) {
            return null
        }

        for (parameter in parameters) {
            if (parameter.isExplicit) {
                return null
            }
        }

        return codomain.accept(this, null)
    }

    override fun visitClassExt(data: Any?, isNew: Boolean, baseClass: Abstract.Expression?, implementations: Collection<Abstract.ClassFieldImpl>?, sequence: Collection<Abstract.BinOpSequenceElem>, errorData: Abstract.ErrorData?, params: Void?): Referable? =
        if (isNew || !sequence.isEmpty() || baseClass == null) null else baseClass.accept(this, null)
}