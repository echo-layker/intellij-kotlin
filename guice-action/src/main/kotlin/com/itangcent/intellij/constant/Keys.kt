package com.itangcent.intellij.constant

interface CacheKey {
    companion object {
        const val STARTTIME = "starttime"
        const val LOGPROCESS = "logprocess"
        const val LEVEL = "level"
    }
}

interface EventKey {
    companion object {
        const val ON_START = "onStart"

        const val ON_COMPLETED = "onCompleted"
    }
}
