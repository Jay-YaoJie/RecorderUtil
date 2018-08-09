package com.jay.recorders.objectUtils

import android.util.Log
import com.jay.recorders.objectUtils.FinalValue.APP_PPACKAGE
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author: Jeff <15899859876@qq.com> 189
 * @date:  2018-07-02 09:33
 * @description:日志工具类
 */
object LogUtil {
    private val execu: ExecutorService = Executors.newFixedThreadPool(1)

    fun v(tag: String, msg: String) = DefaultValue.LOG_DEBUG.debugLog("${APP_PPACKAGE}.LogUtils", "${tag}==---==${msg}", Log.VERBOSE)
    fun d(tag: String, msg: String) = DefaultValue.LOG_DEBUG.debugLog("${APP_PPACKAGE}.LogUtils", "${tag}==---==${msg}", Log.DEBUG)
    fun i(tag: String, msg: String) = DefaultValue.LOG_DEBUG.debugLog("${APP_PPACKAGE}.LogUtils", "${tag}==---==${msg}", Log.INFO)
    fun w(tag: String, msg: String) = DefaultValue.LOG_DEBUG.debugLog("${APP_PPACKAGE}.LogUtils", "${tag}==---==${msg}", Log.WARN)
    fun e(tag: String, msg: String) = DefaultValue.LOG_DEBUG.debugLog("${APP_PPACKAGE}.LogUtils", "${tag}==---==${msg}", Log.ERROR)
    //打印或保存到日志
    private fun Boolean.debugLog(tag: String, msg: String, type: Int) {
        DefaultValue.LOG_SAVESD.saveToSd("${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n${tag}", msg)
        if (!this) {
            return
        }
        when (type) {
            Log.VERBOSE -> Log.v(tag, tag + msg)
            Log.DEBUG -> Log.d(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
        }

    }

    //保存日志到本地文件
    private fun Boolean.saveToSd(tag: String, msg: String) {
        if (!this) {
            return
        }
        execu.submit({
            //获得当前时间
           // val current = System.currentTimeMillis();
           // val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(current));
            //保存日志信息
            FileUtil.appendText(FileUtil.getFilePath("log")!!, "\r\n$tag\n$msg")
        })

    }

    //上传到服务器上
    private fun uploadLogToServer() {

    }


}