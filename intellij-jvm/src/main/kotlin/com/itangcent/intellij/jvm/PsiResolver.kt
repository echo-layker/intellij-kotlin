package com.itangcent.intellij.jvm

import com.google.inject.ImplementedBy
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.itangcent.intellij.jvm.standard.StandardPsiResolver

@ImplementedBy(StandardPsiResolver::class)
interface PsiResolver {

    @Deprecated(message = "will be removed next version",
        replaceWith = ReplaceWith("com.itangcent.intellij.jvm.DuckTypeHelper.resolveClass")
    )
    fun resolveClass(className: String, psiElement: PsiElement): PsiClass?

    /**
     * @return [PsiClass]|[com.intellij.psi.PsiType]
     */
    fun resolveClassOrType(className: String, psiElement: PsiElement): Any?

    /**
     * @return [Pair]<[PsiClass]|[com.intellij.psi.PsiType],[PsiElement]>
     */
    fun resolveClassWithPropertyOrMethod(
        classNameWithProperty: String,
        psiElement: PsiElement
    ): Pair<Any?, PsiElement?>?

    fun resolvePropertyOrMethodOfClass(psiClass: PsiClass, propertyOrMethod: String): PsiElement?

    fun getContainingClass(psiElement: PsiElement): PsiClass?

    fun resolveRefText(psiExpression: PsiElement?): String?

    /**
     * return
     * {
     * params:{}
     * name:""
     * desc:""
     * }
     */
    fun resolveEnumFields(index: Int, psiField: PsiField): Map<String, Any?>?
}