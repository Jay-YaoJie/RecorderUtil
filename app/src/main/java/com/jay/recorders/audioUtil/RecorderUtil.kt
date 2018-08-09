package com.jay.recorders.audioUtil

import android.media.MediaRecorder
import android.util.Log
import com.jay.recorders.objectUtils.FileUtil
import com.jay.recorders.objectUtils.LogUtil

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 * @author: Jeff <15899859876@qq.com>
 * @date: 2018-08-02  16:44
 * @description:
 * 录音工具
 *
 * 1. 首先private RecorderUtil recorder = new RecorderUtil(); 实例化一下
2. 开始录音recorder.startRecording();
3. 录音完成后停止录音recorder.stopRecording();
4. 当然如果录音开始之后想取消语音发送，类似于微信上滑取消语音发送，解决方案滑动监听判断确定取消发送，
就不要将消息发出去并且还要调用recorder.cancelRecording();//取消语音释放资源 即可
 */
class RecorderUtil {

    private val TAG = "RecorderUtil"
    private var mRecorder: MediaRecorder? = null
    private var startTime: Long = 0
    private var timeInterval: Long = 0
    private var isRecording: Boolean = false
    companion object {
        var voicePath:String?=null
    }

    /**
     * 开始录音
     */
    fun startRecording() {
        if (isRecording) {
            mRecorder!!.release()
            mRecorder = null
        }
        voicePath= FileUtil.getFilePath("amr")!!.toString()
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)// 设置麦克风
        /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mRecorder!!.setOutputFile(voicePath)
        /*
         * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
         * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
         */
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        startTime = System.currentTimeMillis()

        try {
            mRecorder!!.prepare()
            mRecorder!!.start()
            isRecording = true
        } catch (e: Exception) {
            LogUtil.e(TAG, "prepare() failed")
        }

    }

    /**
     * 停止录音
     */
    fun stopRecording() {
        timeInterval = System.currentTimeMillis() - startTime
        try {
            if (timeInterval > 1000) {
                mRecorder!!.stop()
            }
            mRecorder!!.release()
            mRecorder = null
            isRecording = false
            //结束录像之后可以在这里上传文件
        } catch (e: Exception) {
            LogUtil.e(TAG, "release() failed")
        }

    }

    /**
     * 取消语音
     */
    @Synchronized
    fun cancelRecording() {
        if (mRecorder != null) {
            try {
                mRecorder!!.release()
                mRecorder = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val file = File(voicePath)
            file.deleteOnExit()
        }
        isRecording = false
    }

    /**
     * 获取录音文件
     */
    fun getDate(): ByteArray? {
        if (voicePath== null) return null
        try {
            return readFile(File(voicePath))
        } catch (e: IOException) {
            Log.e(TAG, "read file error$e")
            return null
        }

    }

    /**
     * 获取录音文件地址
     */
    fun getFilePath(): String {
        return voicePath!!
    }

    /**
     * 获取录音时长,单位秒
     */
    fun getTimeInterval(): Long {
        return timeInterval / 1000
    }

    /**
     * 将文件转化为byte[]
     *
     * @param file 输入文件
     */
    @Throws(IOException::class)
    private fun readFile(file: File): ByteArray {
        // Open file
        val f = RandomAccessFile(file, "r")
        try {
            // Get and check length
            val longlength = f.length()
            val length = longlength.toInt()
            if (length.toLong() != longlength)
                throw IOException("File size >= 2 GB")
            // Read file and return data
            val data = ByteArray(length)
            f.readFully(data)
            return data
        } finally {
            f.close()
        }
    }
}