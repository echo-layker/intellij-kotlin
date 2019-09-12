/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itangcent.intellij.psi

import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.compiled.ClsClassImpl
import com.intellij.util.containers.stream
import java.io.File
import java.util.*

class SourceHelper(private val myProject: Project) {

    fun getSourceClass(
        original: PsiClass
    ): PsiClass {
        try {
            //todo:getUserData was not work
            val cls = original.getUserData(SOURCE_ELEMENT)
            if (cls != null && cls.isValid) {
                return cls
            }

            if (original is ClsClassImpl) {
                val navigationElement = original.getNavigationElement()
                if (navigationElement != original && navigationElement is PsiClass) {
                    return navigationElement
                }
            }

            if (!DumbService.isDumb(myProject)) {
                val vFile = original.containingFile.virtualFile
                val idx = ProjectRootManager.getInstance(myProject).fileIndex
                if (vFile != null && idx.isInLibraryClasses(vFile)) {
                    val sourceRootForFile = idx.getSourceRootForFile(vFile)
                    if (sourceRootForFile != null) {
                        tryFindSourceClass(sourceRootForFile, original)?.let { return it }
                    }

                    val orderEntriesForFile = idx.getOrderEntriesForFile(vFile)
                    orderEntriesForFile.stream()
                        .flatMap { it.getFiles(OrderRootType.SOURCES).stream() }
                        .distinct()
                        .map { tryFindSourceClass(it, original) }
                        .findFirst()
                        .orElse(null)?.let { return it }
                }
            }
        } catch (e: Exception) {
            //ignore,if cannot find source class
        }

        return original
    }

    private fun tryFindSourceClass(
        sourceRootForFile: VirtualFile,
        original: PsiClass
    ): PsiClass? {
        val find = findPsiFileInRoot(sourceRootForFile, original.qualifiedName!!)
            ?: sourceRootForFile.findChild(
                original.qualifiedName!!
            )
        if (find != null && find is PsiJavaFile) {
            find.classes.forEach {
                if (it.qualifiedName == original.qualifiedName) {
                    original.putUserData(SOURCE_ELEMENT, it)
                    return it
                }
            }
        }
        return null
    }

    private fun findPsiFileInRoot(dirFile: VirtualFile, className: String?): PsiJavaFile? {
        val javaName = StringUtil.getQualifiedName(className, StdFileTypes.JAVA.defaultExtension)
        if (className != null) {
            val classFile =
                dirFile.findChild(javaName)
            if (classFile != null) {
                val psiFile = PsiManager.getInstance(myProject).findFile(classFile)
                if (psiFile is PsiJavaFile) {
                    return psiFile
                }
            }
        }

        val dirs = Stack<VirtualFile>()
        var dir: VirtualFile = dirFile
        val rootPath = dirFile.path
        while (true) {
            val children = dir.children
            for (child in children) {
                if (StdFileTypes.JAVA == child.fileType && child.isValid) {
                    val psiFile = PsiManager.getInstance(myProject).findFile(child)
                    if (psiFile is PsiJavaFile) {
                        if (child.name == javaName) {
                            return psiFile
                        }

                        for (cls in psiFile.classes) {
                            if (cls.qualifiedName == className) {
                                return psiFile
                            }
                        }
                    }
                } else {
                    val prefix = dir.path.removePrefix(rootPath)
                        .replace(File.separatorChar, '.')
                    if (javaName.startsWith(prefix)) {
                        dirs.push(child)
                    }
                }
            }
            if (dirs.isEmpty()) {
                break
            }
            dir = dirs.pop()
        }
        return null
    }

    companion object {
        val SOURCE_ELEMENT = Key.create<PsiClass>("SOURCE_ELEMENT")
    }
}