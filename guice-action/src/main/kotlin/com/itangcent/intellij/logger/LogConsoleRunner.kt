package com.itangcent.intellij.logger

import com.intellij.execution.ExecutionException
import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.console.LanguageConsoleView
import com.intellij.execution.console.ProcessBackedConsoleExecuteActionHandler
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class LogConsoleRunner : AbstractConsoleRunnerWithHistory<LanguageConsoleView> {

    private var logProcess: Process? = null

    constructor(myProject: Project, consoleTitle: String, logProcess: Process) : super(
        myProject,
        consoleTitle,
        myProject.basePath
    ) {
        this.logProcess = logProcess
    }

    constructor(myProject: Project, consoleTitle: String, workingDir: String, logProcess: Process) : super(
        myProject,
        consoleTitle,
        workingDir
    ) {
        this.logProcess = logProcess
    }

    override fun createConsoleView(): LanguageConsoleView {
        val consoleView = LanguageConsoleImpl(project, "Bash", PlainTextLanguage.INSTANCE)
        consoleView.file.putUserData(LANGUAGE_CONSOLE_MARKER, true)

        consoleView.isEditable = false
        return consoleView
    }

    @Throws(ExecutionException::class)
    override fun createProcess(): Process? {
        return logProcess
    }

    override fun createProcessHandler(process: Process): OSProcessHandler {
        return ColoredProcessHandler(process, null)
    }

    override fun createExecuteActionHandler(): ProcessBackedConsoleExecuteActionHandler {
        return ProcessBackedConsoleExecuteActionHandler(processHandler, true)
    }

    companion object {

        private val LANGUAGE_CONSOLE_MARKER = Key<Boolean>("Language console marker")
    }
}
