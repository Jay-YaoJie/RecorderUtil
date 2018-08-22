package com.jay.recorders.objectUtils.camerasurfaceview

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.Parameters.FOCUS_MODE_AUTO
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.jay.recorders.camerasurfaceview.CamParaUtil
import com.jay.recorders.objectUtils.ToastUtil
import java.io.IOException



/**
 * @author: Jeff <15899859876@qq.com>
 * @date:  2018-08-03  16:32   https://www.cnblogs.com/plokmju/p/android_MediaRecorder.html
 * @description: 使用SurfaceView ，Camera 拍照录像，，可同时录像拍照
 */
class CameraSurfaceView : SurfaceView, SurfaceHolder.Callback, Camera.AutoFocusCallback, View.OnClickListener {
    private var screenOritation = Configuration.ORIENTATION_PORTRAIT
    private var mOpenBackCamera = true
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mRunInBackground = false
    internal var isAttachedWindow = false
    private var mCamera: Camera? = null
    private var mParam: Camera.Parameters? = null
    private var previewBuffer: ByteArray? = null
    private var mCameraId: Int = 0
    protected var previewformat = ImageFormat.NV21
    internal var context: Context? = null

    private val isSupportCameraLight: Boolean
        get() {
            var mIsSupportCameraLight = false
            try {
                if (mCamera != null) {
                    val parameter = mCamera!!.parameters
                    val a = parameter.supportedFlashModes
                    if (a == null) {
                        mIsSupportCameraLight = false
                    } else {
                        mIsSupportCameraLight = true
                    }
                }
            } catch (e: Exception) {
                mIsSupportCameraLight = false
                e.printStackTrace()
            }
            return mIsSupportCameraLight
        }


    private val previewCallback = Camera.PreviewCallback { data, camera ->
        if (data == null) {
            releaseCamera()
            return@PreviewCallback
        }
        //you can code media here
        if (cameraState != CameraState.PREVIEW) {
            cameraState = CameraState.PREVIEW
            if (cameraStateListener != null) {
                cameraStateListener!!.onCameraStateChange(cameraState!!)
            }
        }
        mCamera!!.addCallbackBuffer(previewBuffer)
    }

    protected var cameraState: CameraState? = null
    private var cameraStateListener: CameraStateListener? = null

    /**
     * ___________________________________以下为视频录制模块______________________________________
     */

    var isRecording = false
        private set

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    //初始化Camera
    fun init(context: Context) {
        this.context = context
        cameraState = CameraState.START
        if (cameraStateListener != null) {
            cameraStateListener!!.onCameraStateChange(cameraState!!)
        }
        openCamera()
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenOritation = Configuration.ORIENTATION_LANDSCAPE
        }
        mSurfaceHolder = holder
        mSurfaceHolder!!.addCallback(this)
        mSurfaceTexture = SurfaceTexture(10)
        setOnClickListener(this)
        post {
            if (!isAttachedWindow) {
                mRunInBackground = true
                startPreview()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachedWindow = true
    }

    private fun openCamera() {
        if (mOpenBackCamera) {
            mCameraId = findCamera(false)
        } else {
            mCameraId = findCamera(true)
        }
        if (mCameraId == -1) {
            mCameraId = 0
        }
        try {
            mCamera = Camera.open(mCameraId)
        } catch (ee: Exception) {
            ee.printStackTrace()
            mCamera = null
            cameraState = CameraState.ERROR
            if (cameraStateListener != null) {
                cameraStateListener!!.onCameraStateChange(cameraState!!)
            }
        }

        if (mCamera == null) {
            ToastUtil.show("打开摄像头失败")
            return
        }
    }

    //打开前置或后置摄像头
    private fun findCamera(front: Boolean): Int {
        val cameraCount: Int
        try {
            val cameraInfo = Camera.CameraInfo()
            cameraCount = Camera.getNumberOfCameras()
            for (camIdx in 0 until cameraCount) {
                Camera.getCameraInfo(camIdx, cameraInfo)
                val facing = if (front) 1 else 0
                if (cameraInfo.facing == facing) {
                    return camIdx
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return -1
    }

    //可提示，当前正在录像，不要拍照
    fun setDefaultCamera(backCamera: Boolean): Boolean {
        if (mOpenBackCamera == backCamera) return false
        if (isRecording) {
            ToastUtil.show("请先结束录像")
            return false
        }
        mOpenBackCamera = backCamera
        if (mCamera != null) {
            closeCamera()
            openCamera()
            startPreview()
        }
        return true
    }


    //结束时关闭摄像头
    fun closeCamera() {
        stopRecord()
        stopPreview()
        releaseCamera()
    }

    //重置摄像头
    private fun releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera!!.setPreviewCallback(null)
                mCamera!!.setPreviewCallbackWithBuffer(null)
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
        } catch (ee: Exception) {
            ee.printStackTrace()
        }

    }

    //设置Camera各项参数
     fun startPreview() {
        if (mCamera == null) return
        try {
            mParam = mCamera!!.parameters
            mParam!!.previewFormat = previewformat
            mParam!!.setRotation(0)
            val previewSize = CamParaUtil.getSize(mParam!!.supportedPreviewSizes, 1000,
                    mCamera!!.Size(VIDEO_720[0], VIDEO_720[1]))
            mParam!!.setPreviewSize(previewSize.width, previewSize.height)
            val yuv_buffersize = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewformat) / 8
            previewBuffer = ByteArray(yuv_buffersize)
            val pictureSize = CamParaUtil.getSize(mParam!!.supportedPictureSizes, 1500,
                    mCamera!!.Size(VIDEO_1080[0], VIDEO_1080[1]))
            mParam!!.setPictureSize(pictureSize.width, pictureSize.height)
            if (CamParaUtil.isSupportedFormats(mParam!!.supportedPictureFormats, ImageFormat.JPEG)) {
                mParam!!.pictureFormat = ImageFormat.JPEG
                mParam!!.jpegQuality = 100
            }
            if (CamParaUtil.isSupportedFocusMode(mParam!!.supportedFocusModes, FOCUS_MODE_AUTO)) {
                mParam!!.focusMode = FOCUS_MODE_AUTO
            }
            if (screenOritation != Configuration.ORIENTATION_LANDSCAPE) {
                mParam!!.set("orientation", "portrait")
                mCamera!!.setDisplayOrientation(90)
            } else {
                mParam!!.set("orientation", "landscape")
                mCamera!!.setDisplayOrientation(0)
            }
            if (mRunInBackground) {
                mCamera!!.setPreviewTexture(mSurfaceTexture)
                mCamera!!.addCallbackBuffer(previewBuffer)
                //                mCamera.setPreviewCallbackWithBuffer(previewCallback);//设置摄像头预览帧回调
            } else {
                mCamera!!.setPreviewDisplay(mSurfaceHolder)
                //                mCamera.setPreviewCallback(previewCallback);//设置摄像头预览帧回调
            }
            mCamera!!.parameters = mParam
            mCamera!!.startPreview()
            isPreviewActive=true
            if (cameraState != CameraState.START) {
                cameraState = CameraState.START
                if (cameraStateListener != null) {
                    cameraStateListener!!.onCameraStateChange(cameraState!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
          //  releaseCamera()
            return
        }

        try {
            val mode = mCamera!!.parameters.focusMode
            if ("auto" == mode || "macro" == mode) {
                mCamera!!.autoFocus(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //结束mCamera
     fun stopPreview() {
        if (mCamera == null) return
        try {
            if (mRunInBackground) {
                mCamera!!.setPreviewCallbackWithBuffer(null)
                mCamera!!.stopPreview()
            } else {
                mCamera!!.setPreviewCallback(null)
                mCamera!!.stopPreview()
            }
            if (cameraState != CameraState.STOP) {
                cameraState = CameraState.STOP
                if (cameraStateListener != null) {
                    cameraStateListener!!.onCameraStateChange(cameraState!!)
                }
            }
            isPreviewActive=false
        } catch (ee: Exception) {
            ee.printStackTrace()
        }

    }

    override fun onClick(v: View) {
        if (mCamera != null) {
            mCamera!!.autoFocus(null)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        stopPreview()
        startPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopPreview()
        if (mRunInBackground)
            startPreview()
    }

    enum class CameraState {
        START, PREVIEW, STOP, ERROR
    }

    fun setOnCameraStateListener(listener: CameraStateListener) {
        this.cameraStateListener = listener
    }

    interface CameraStateListener {
        fun onCameraStateChange(paramCameraState: CameraState)
    }

    /**
     * ___________________________________前/后台运行______________________________________
     */
    //可设置为前后台运行
    fun setRunBack(b: Boolean) {
        if (mCamera == null) return
        if (b == mRunInBackground) return
        if (!b && !isAttachedWindow) {
            ToastUtil.show("Vew未依附在Window,无法显示")
            return
        }
        mRunInBackground = b
        if (b)
            visibility = View.GONE
        else
            visibility = View.VISIBLE
    }

    /**
     * ___________________________________开关闪光灯______________________________________
     */
    //打开或关闭灯光
    fun switchLight(open: Boolean) {
        if (mCamera == null) return
        try {
            if (mCamera != null) {
                if (open) {
                    val parameter = mCamera!!.parameters
                    if (parameter.flashMode == "off") {
                        parameter.flashMode = "torch"
                        mCamera!!.parameters = parameter
                    } else {
                        parameter.flashMode = "off"
                        mCamera!!.parameters = parameter
                    }
                } else {
                    val parameter = mCamera!!.parameters
                    if (parameter.flashMode != null && parameter.flashMode == "torch") {
                        parameter.flashMode = "off"
                        mCamera!!.parameters = parameter
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * ___________________________________以下为拍照模块______________________________________
     */
    //preview活动时再调用autoFocus
    var isPreviewActive=false
    //拍照
    fun capture() {
        if (mCamera == null) return
        try {
            if (isPreviewActive){
                mCamera!!.autoFocus(this)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onAutoFocus(success: Boolean, camera: Camera) {
        if (success) {
            try {
                mCamera!!.takePicture(null, null, Camera.PictureCallback { data, camera ->
                    var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    val matrix = Matrix()
                    if (mOpenBackCamera) {
                        matrix.setRotate(90f)
                    } else {
                        matrix.setRotate(270f) //270
                        matrix.postScale(-1f, 1f)
                    }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    CamParaUtil.saveBitmap(bitmap)
                    ToastUtil.show("拍照成功")
                    startPreview()
                })
            } catch (e: Exception) {
                if (isRecording) {
                    ToastUtil.show("请先结束录像")
                }else{
                    ToastUtil.show("拍照失败，请重新拍照")
                }
                e.printStackTrace()
            }

        }
    }
    internal var mediaRecorder:MediaRecorder? = null
    @JvmOverloads
    fun startRecord(maxDurationMs: Int = -1, onInfoListener: MediaRecorder.OnInfoListener? = null): Boolean {
        if (mCamera == null) return false
        mCamera!!.unlock()
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.reset()
        mediaRecorder!!.setCamera(mCamera)
        // 设置音频录入源
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        // 设置视频图像的录入源
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        // 设置录入媒体的输出格式
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        // 设置音频的编码格式
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
       // mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        // 设置视频的编码格式
        mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        //mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)
        // 设置视频的采样率，每秒4帧
        //mediaRecorder.setVideoFrameRate(4)
        // 设置捕获视频图像的预览界面
        //        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());//设置录制预览surface
        // 设置录制视频文件的输出路径
        mediaRecorder!!.setOutputFile(CamParaUtil.mediaOutputPath)
        mediaRecorder!!.setOnErrorListener(object : MediaRecorder.OnErrorListener {
            override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
                // 发生错误，停止录制
                mediaRecorder!!.stop()
                mediaRecorder!!.release()
                mediaRecorder = null
                isRecording = false
                ToastUtil.show("录制出错")
            }
        })
        val videoSize = CamParaUtil.getSize(mParam!!.supportedVideoSizes, 1200,
                mCamera!!.Size(VIDEO_1080[0], VIDEO_1080[1]))
        mediaRecorder!!.setVideoSize(videoSize.width, videoSize.height)
        mediaRecorder!!.setVideoEncodingBitRate(5 * 1024 * 1024)
        if (mOpenBackCamera) {
            mediaRecorder!!.setOrientationHint(90)
        } else {
            if (screenOritation == Configuration.ORIENTATION_LANDSCAPE)
                mediaRecorder!!.setOrientationHint(90)
            else
                mediaRecorder!!.setOrientationHint(270)//270
        }
        if (maxDurationMs != -1) {
            mediaRecorder!!.setMaxDuration(maxDurationMs)
            mediaRecorder!!.setOnInfoListener(onInfoListener)
        }
        try {
            // 准备、开始
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    //结束录像
    fun stopRecord() {
        if (!isRecording) return
        try {
            mCamera!!.lock()
            mediaRecorder!!.stop();
            mediaRecorder!!.setPreviewDisplay(null)
           // mediaRecorder!!.reset();//重置
            mediaRecorder!!.release();//释放
            mediaRecorder = null;
            isRecording = false
            ToastUtil.show("视频已保存在根目录")
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    companion object {

        protected val VIDEO_320 = intArrayOf(320, 240)
        protected val VIDEO_480 = intArrayOf(640, 480)
        protected val VIDEO_720 = intArrayOf(1280, 720)
        protected val VIDEO_1080 = intArrayOf(1920, 1080)
    }

    /**_________________________________________________________________________________________ */


}
