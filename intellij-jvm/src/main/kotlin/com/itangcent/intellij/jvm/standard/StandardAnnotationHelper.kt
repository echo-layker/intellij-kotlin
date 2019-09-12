package com.itangcent.intellij.jvm.standard

import com.google.inject.Singleton
import com.intellij.psi.*
import com.intellij.util.containers.stream
import com.itangcent.common.utils.cast
import com.itangcent.intellij.jvm.AnnotationHelper

@Singleton
open class StandardAnnotationHelper : AnnotationHelper {
    override fun hasAnn(psiElement: PsiElement?, annName: String): Boolean {
        return findAnn(psiElement, annName) != null
    }

    override fun findAnnMap(psiElement: PsiElement?, annName: String): Map<String, Any?>? {
        val psiAnn = findAnn(psiElement, annName) ?: return null
        return annToMap(psiAnn)
    }

    protected fun annToMap(psiAnn: PsiAnnotation): LinkedHashMap<String, Any?> {
        val map: LinkedHashMap<String, Any?> = LinkedHashMap()
        psiAnn.parameterList.attributes.stream()
            .forEach { attr ->
                map[attr.name ?: "value"] = resolveValue(attr.value)
            }

        return map
    }

    override fun findAttr(psiElement: PsiElement?, annName: String): Any? {
        return findAttr(psiElement, annName, "value")
    }

    override fun findAttr(psiElement: PsiElement?, annName: String, vararg attrs: String): Any? {
        val ann = findAnn(psiElement, annName) ?: return null
        return attrs
            .mapNotNull { ann.findAttributeValue(it) }
            .mapNotNull { resolveValue(it) }
            .firstOrNull()
    }

    override fun findAttrAsString(psiElement: PsiElement?, annName: String): String? {
        return findAttrAsString(psiElement, annName, "value")
    }

    override fun findAttrAsString(psiElement: PsiElement?, annName: String, vararg attrs: String): String? {
        val ann = findAnn(psiElement, annName) ?: return null
        return attrs
            .mapNotNull { ann.findAttributeValue(it) }
            .mapNotNull { resolveValue(it) }
            .map { tinyAnnStr(it.toString()) }
            .firstOrNull { !it.isNullOrBlank() }
    }

    private fun findAnn(psiElement: PsiElement?, annName: String): PsiAnnotation? {
        return findAnnotations(psiElement)
            ?.let { annotations -> annotations.firstOrNull { it.qualifiedName == annName } }
    }

    private fun findAnnotations(psiElement: PsiElement?): Array<out PsiAnnotation>? {
        return psiElement.cast(PsiAnnotationOwner::class)?.annotations
            ?: psiElement.cast(PsiModifierListOwner::class)?.annotations
    }

    protected open fun resolveValue(psiExpression: PsiElement?): Any? {
        when (psiExpression) {
            null -> return null
            is PsiLiteralExpression -> return psiExpression.value?.toString()
            is PsiReferenceExpression -> {
                val value = psiExpression.resolve()
                if (value is PsiExpression) {
                    return resolveValue(value)
                }
            }
            is PsiField -> {
                val constantValue = psiExpression.computeConstantValue()
                if (constantValue != null) {
                    if (constantValue is PsiExpression) {
                        return resolveValue(constantValue)
                    }
                    return constantValue
                }
            }
            is PsiAnnotation -> {
                return annToMap(psiExpression)
            }
            is PsiArrayInitializerMemberValue -> {
                return psiExpression.initializers.map { resolveValue(it) }.toTypedArray()
            }
            else -> {
                return psiExpression.text
            }
        }

        return psiExpression.text
    }

    /**
     * clean the str of param in annotaion
     * 1.remove {}  e.g.{*}->*
     * 2.remove ""  e.g."*"->*
     */
    fun tinyAnnStr(annStr: String?): String? {
        if (annStr == null) return null
        var str = annStr.trim()
        while (true) {
            if (str.startsWith("{")) {
                str = str.trim('{', '}')
                continue
            }
            if (str.startsWith("\"") || str.endsWith("\"")) {
                str = str.trim('\"')
                continue
            }
            break
        }
        return str
    }
}