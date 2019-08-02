package com.itangcent.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.itangcent.common.exception.ProcessCanceledException
import com.itangcent.intellij.context.ActionContext
import com.itangcent.intellij.extend.guice.singleton
import com.itangcent.intellij.extend.guice.with
import org.apache.commons.lang3.exception.ExceptionUtils
import javax.swing.Icon

abstract class KotlinAnAction : AnAction {

    constructor() : super()
    constructor(icon: Icon?) : super(icon)
    constructor(text: String?) : super(text)
    constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    private val log: Logger = Logger.getInstance(this.javaClass.name)

    open protected fun onBuildActionContext(builder: ActionContext.ActionContextBuilder) {
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {

        val project = anActionEvent.project ?: return

        val actionContextBuilder = ActionContext.builder()
        actionContextBuilder.bindInstance(Project::class, project)
        actionContextBuilder.bindInstance(AnActionEvent::class, anActionEvent)
        actionContextBuilder.bind(DataContext::class) { it.with(ActionEventDataContextAdaptor::class).singleton() }
        onBuildActionContext(actionContextBuilder)
        val actionContext = actionContextBuilder.build()
        actionContext.init(this)

        log.info("start action:" + this::class.qualifiedName)

        if (actionContext.lock()) {
            actionContext.runAsync {
                try {
                    actionPerformed(actionContext, project, anActionEvent)
                } catch (ex: Exception) {
                    log.info("Error:${ex.message}trace:${ExceptionUtils.getStackTrace(ex)}")
                    actionContext.runInWriteUI {
                        Messages.showMessageDialog(
                            project, when (ex) {
                                is ProcessCanceledException -> ex.stopMsg
                                else -> "Error at:${ex.message}trace:${ExceptionUtils.getStackTrace(ex)}"
                            }, "Error", Messages.getInformationIcon()
                        )
                    }
                }
            }
        } else {
            log.info("Found unfinished task!")
            actionContext.runInWriteUI {
                Messages.showMessageDialog(
                    project, "Found unfinished task! ",
                    "Error", Messages.getInformationIcon()
                )
            }
        }
        actionContext.waitCompleteAsync()
    }

    protected abstract fun actionPerformed(
        actionContext: ActionContext,
        project: Project?,
        anActionEvent: AnActionEvent
    )
}

