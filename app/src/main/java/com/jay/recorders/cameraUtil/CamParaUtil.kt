package com.jay.recorders.camerasurfaceview

import android.graphics.Bitmap
import android.hardware.Camera
import com.jay.recorders.objectUtils.FileUtil
import com.jay.recorders.objectUtils.LogUtil
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * @author: Jeff <15899859876@qq.com>
 * @date:  2018-08-03  16:33
 * @description:camera工具
 */

object CamParaUtil {
    fun getSize(list: List<Camera.Size>?, th: Int, defaultSize: Camera.Size): Camera.Size {
        if (null == list || list.isEmpty()) return defaultSize
        Collections.sort(list) { lhs, rhs ->
            //作升序排序
            if (lhs.width == rhs.width) {
                0
            } else if (lhs.width > rhs.width) {
                1
            } else {
                -1
            }
        }
        var i = 0
        for (s in list) {
            if (s.width > th) {//&& equalRate(s, rate)
                break
            }
            i++
        }
        return if (i == list.size) {
            list[i - 1]
        } else {
            list[i]
        }
    }


    //    public static Size getBestSize(List<Camera.Size> list, float rate) {
    //        float previewDisparity = 100;
    //        int index = 0;
    //        for (int i = 0; i < list.size(); i++) {
    //            Size cur = list.get(i);
    //            float prop = (float) cur.width / (float) cur.height;
    //            if (Math.abs(rate - prop) < previewDisparity) {
    //                previewDisparity = Math.abs(rate - prop);
    //                index = i;
    //            }
    //        }
    //        return list.get(index);
    //    }
    //
    //
    //    private static boolean equalRate(Size s, float rate) {
    //        float r = (float) (s.width) / (float) (s.height);
    //        if (Math.abs(r - rate) <= 0.2) {
    //            return true;
    //        } else {
    //            return false;
    //        }
    //    }

    fun isSupportedFocusMode(focusList: List<String>, focusMode: String): Boolean {
        for (i in focusList.indices) {
            if (focusMode == focusList[i]) {
                return true
            }
        }
        return false
    }

    fun isSupportedFormats(supportedFormats: List<Int>, jpeg: Int): Boolean {
        for (i in supportedFormats.indices) {
            if (jpeg == supportedFormats[i]) {
                return true
            }
        }
        return false
    }

    /*******************************************保存图片和视频*************************/
    //保存照片
    fun saveBitmap(b: Bitmap) {
        // "${DefaultValue.ROOT_DIR}/$APP_NAME/files/photo/$time.jpg"
        val jpegName = FileUtil.getFilePath("jpg").toString();
        try {
            val fout = FileOutputStream(jpegName)
            val bos = BufferedOutputStream(fout)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
            LogUtil.d("", "保存相片:" + jpegName)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    //获取视频存储路径
    val mediaOutputPath: String
        get() {
            return FileUtil.getFilePath("mp4").toString()
        }


}
