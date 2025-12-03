package com.example.smsdemo

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils
import java.lang.ref.SoftReference

object ContextUtils {

    var context: Application = Utils.getApp()
        private set

    fun getSoftReferenceContext(): SoftReference<Context> {
        return SoftReference<Context>(context)
    }
}