package com.itangcent.intellij.jvm.scala

import com.itangcent.intellij.jvm.scala.compatible.ScCompatibleLiteral
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall
import java.util.*

object ScPsiUtils {

    /**
     * bean which class qualifiedName contains scala may be a instance
     * of class that in scala-plugin
     * e.g.
     * [org.jetbrains.plugins.scala]
     */
    fun isScPsiInst(any: Any): Boolean {
        return any::class.qualifiedName?.contains("scala") ?: false
    }


    fun valueOf(scExpr: ScExpression): Any? {
        if (ScCompatibleLiteral.isInstance(scExpr)) {
            return ScCompatibleLiteral.getValue(scExpr) ?: ScCompatibleLiteral.getText(scExpr)
        }
        if (scExpr is ScMethodCall) {
            if (scExpr.invokedExpr.text == "Array") {
                val list: LinkedList<Any> = LinkedList()
                for (argumentExpression in scExpr.argumentExpressions()) {
                    valueOf(argumentExpression)?.let { list.add(it) }
                }
                return list
            }
        }
        return scExpr.text
    }
}