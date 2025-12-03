package com.example.smsdemo

object PathUtils {

    private val context = ContextUtils.context
    private val SPLASH = System.getProperty("file.separator")

    fun getCrashPath() = context.getExternalFilesDir("crash")?.absolutePath
        ?: (context.filesDir.absolutePath + "/crash")

    fun getDataStorePath() = context.filesDir.absolutePath + "/datastore"

    fun getAppPath() = context.getExternalFilesDir("app")?.absolutePath
        ?: (context.filesDir.absolutePath + "/app")

    fun getHtmlPath() = context.getExternalFilesDir("html")?.absolutePath
        ?: (context.filesDir.absolutePath + "/html")

    fun concatFilePath(path: String, filename: String): String {
        val dir = path
        var absolutePath = ""
        absolutePath = if (dir.endsWith(SPLASH)) dir + filename
        else dir + SPLASH + filename
        return absolutePath
    }
}