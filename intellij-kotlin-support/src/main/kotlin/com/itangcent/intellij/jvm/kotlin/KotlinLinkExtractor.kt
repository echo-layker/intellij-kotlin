package com.itangcent.intellij.jvm.kotlin

import com.intellij.psi.PsiMember
import com.itangcent.intellij.jvm.standard.StandardLinkExtractor
import java.util.regex.Pattern

open class KotlinLinkExtractor : StandardLinkExtractor() {

    override fun findLink(doc: String, psiMember: PsiMember, resolver: (String) -> String?): String {

        if (!KtPsiUtils.isKtPsiInst(psiMember)) {
            return super.findLink(doc, psiMember, resolver)
        }

        if (doc.contains("@link") || doc.contains("[")) {
            val pattern = Pattern.compile("\\{@link(.*?)}|\\[(.*?)]")
            val matcher = pattern.matcher(doc)

            val sb = StringBuffer()
            while (matcher.find()) {
                matcher.appendReplacement(sb, "")
                val linkClassAndMethod = matcher.group(1)
                    ?: matcher.group(2)
                    ?: continue
                resolver(linkClassAndMethod)?.let { sb.append(it) }
            }
            matcher.appendTail(sb)
            return sb.toString()
        }

        return doc
    }
}