package com.jay.recorders.objectUtils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.jay.recorders.objectUtils.DefaultValue.APP_PATH
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author: Jeff <15899859876@qq.com>
 * @date:  2018-06-18 19:35
 * @description: 文件操作工具类
 */
object FileUtil {

    /**
     * 获取根目录
     */
    fun getRootDir(): String {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            //Environment.getExternalStorageDirectory().toString()
            //LogUtils.d("FileUtils", "mnt/sdcard")
            Environment.getExternalStorageDirectory().absolutePath;

          //  "/mnt/sdcard"
        } else {
            // "mnt/sdcard"
            LogUtil.d("FileUtils", "storage")
            "/mnt/sdcard"
        }

    }

    /**
     * 可创建多个文件夹
     * dirPath 文件路径
     */
    fun mkDir(dirPath: String) {

        val dirArray = dirPath.split("/".toRegex())
        var pathTemp = ""
        for (i in 1 until dirArray.size) {
            pathTemp = "$pathTemp/${dirArray[i]}"
            val newF = File("${dirArray[0]}$pathTemp")
            if (!newF.exists()) {
                val cheatDir: Boolean = newF.mkdir()
                println(cheatDir)
            }
        }

    }

    /**
     * 创建文件
     * filePath 文件路径
     */
    fun creatFile(filePath: File) {
        val fileStr:String=filePath.toString();
//新建一个File，传入文件夹目录
        val file = File(fileStr.substring(0, fileStr.lastIndexOf("/")))
//判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs()
        }
        if (!filePath.exists()) {
            filePath.createNewFile()
        }
    }


    /**
     * 删除文件
     *
     * dirpath 文件目录
     * fileName 文件名称
     */
    fun delFile(dirpath: String = getRootDir(), fileName: String): Boolean {
        val file = File("$dirpath/$fileName")
        if (file.checkFile()) {
            return false
        }
        return file.delete()
    }

    /**
     *  删除文件
     *  filepath 文件路径
     */
    fun delFile(filepath: File): Boolean {
        if (filepath.checkFile()) {
            return false
        }
        return filepath.delete()
    }

    /**
     *  删除文件
     *  filepath 文件路径
     */
    fun delFile(filepath: String): Boolean {
        val file = File(filepath)
        if (file.checkFile()) {
            return false
        }
        return file.delete()
    }


    /**
     * 删除文件夹
     * dirPath 文件路径
     */
    fun delDir(dirpath: String) {
        val dir = File(dirpath)
        deleteDirWihtFile(dir)
    }

    fun deleteDirWihtFile(dir: File?) {
        if (dir!!.checkFile())
            return
        for (file in dir.listFiles()) {
            if (file.isFile)
                file.delete() // 删除所有文件
            else if (file.isDirectory)
                deleteDirWihtFile(file) // 递规的方式删除文件夹
        }
        dir.delete()// 删除目录本身
    }

    private fun File.checkFile(): Boolean {
        return this == null || !this.exists() || !this.isDirectory
    }

    /**
     * 修改SD卡上的文件或目录名
     * oldFilePath 旧文件或文件夹路径
     * newFilePath 新文件或文件夹路径
     */
    fun renameFile(oldFilePath: String, newFilePath: String): Boolean {
        val oldFile = File(oldFilePath)
        val newFile = File(newFilePath)
        return oldFile.renameTo(newFile)
    }


    /**
     * 文件读取
     * filePath 文件路径
     */
    fun readFile(filePath: File): String? {
        if (!filePath.isFile) {
            return null
        } else {
            return filePath.readText()
        }
    }

    /**
     * 文件读取
     * strPath 文件路径
     */
    fun readFile(strPath: String): String? {
        return readFile(File(strPath))
    }

    /**
     * InputStream 转字符串
     */
    fun readInp(inp: InputStream): String? {
        val bytes: ByteArray = inp.readBytes()
        return String(bytes)
    }

    /**
     * BufferedReader 转字符串
     */
    fun readBuff(buff: BufferedReader): String? {
        return buff.readText()
    }

    /**
     * 写入数据
     */
    fun writeText(filePath: File, content: String) {
        creatFile(filePath)
        filePath.writeText(content)
    }

    /**
     * 追加数据
     */
    fun appendText(filePath: File, content: String) {
        creatFile(filePath)
        filePath.appendText(content)
    }

    /**
     * 追加数据
     */
    fun appendBytes(filePath: File, array: ByteArray) {
        creatFile(filePath)
        filePath.appendBytes(array)
    }

    /**
     * 在根目录下搜索文件
     *
     * @param keyword
     * @return
     */
    fun searchFile(keyword: String): String {
        var result = ""
        val files = File("/").listFiles()
        for (file in files) {
            if (file.name.indexOf(keyword) >= 0) {
                result += file.path + "\n"
            }
        }
        if (result == "") {
            result = "找不到文件!!"
        }
        return result
    }
    /**
     * 从sd卡取文件
     *
     * @param filename
     * @return
     */
    fun getFileFromSdcard(filename: String): String {
        var outputStream: ByteArrayOutputStream? = null
        var fis: FileInputStream? = null
        try {
            outputStream = ByteArrayOutputStream()
            val file = File(Environment.getExternalStorageDirectory(), filename)
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                fis = FileInputStream(file)
                var len = 0
                val data = ByteArray(1024)
                len = fis.read(data)
                while ( len!= -1) {
                    outputStream.write(data, 0, len)
                    len = fis.read(data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outputStream!!.close()
                fis!!.close()
            } catch (e: IOException) {
            }

        }
        return String(outputStream!!.toByteArray())
    }

    /**
     * 查询文件
     *
     * @param file
     * @param keyword
     * @return
     */
    fun FindFile(file: File, keyword: String): List<File> {
        val list = ArrayList<File>()
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (tempf in files) {
                    if (tempf.isDirectory) {
                        if (tempf.name.toLowerCase().lastIndexOf(keyword) > -1) {
                            list.add(tempf)
                        }
                        list.addAll(FindFile(tempf, keyword))
                    } else {
                        if (tempf.name.toLowerCase().lastIndexOf(keyword) > -1) {
                            list.add(tempf)
                        }
                    }
                }
            }
        }
        return list
    }

    /**
     * searchFile 查找文件并加入到ArrayList 当中去
     *
     * @param context
     * @param keyword
     * @param filepath
     * @return
     */
    fun searchFile(context: Context, keyword: String, filepath: File): List<Map<String, Any>> {
        val list = ArrayList<Map<String, Any>>()
        var rowItem: MutableMap<String, Any>? = null
        var index = 0
        // 判断SD卡是否存在
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val files = filepath.listFiles()
            if (files.size > 0) {
                for (file in files) {
                    if (file.isDirectory) {
                        if (file.name.toLowerCase().lastIndexOf(keyword) > -1) {
                            rowItem = HashMap()
                            rowItem["number"] = index // 加入序列号
                            rowItem["fileName"] = file.name// 加入名称
                            rowItem["path"] = file.path // 加入路径
                            rowItem["size"] = file.length().toString() + "" // 加入文件大小
                            list.add(rowItem)
                        }
                        // 如果目录可读就执行（一定要加，不然会挂掉）
                        if (file.canRead()) {
                            list.addAll(searchFile(context, keyword, file)) // 如果是目录，递归查找
                        }
                    } else {
                        // 判断是文件，则进行文件名判断
                        try {
                            if (file.name.indexOf(keyword) > -1 || file.name.indexOf(keyword.toUpperCase()) > -1) {
                                rowItem = HashMap()
                                rowItem["number"] = index // 加入序列号
                                rowItem["fileName"] = file.name// 加入名称
                                rowItem["path"] = file.path // 加入路径
                                rowItem["size"] = file.length().toString() + "" // 加入文件大小
                                list.add(rowItem)
                                index++
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "查找发生错误!", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }
        return list
    }
    /**
     * 获取文件大小
     */
    fun getLeng(filePath: File): Long {
        return if (!filePath.exists()) {
            -1
        } else {
            filePath.length()
        }
    }

    /**
     * 按时间排序
     */
    fun sortByTime(filePath: File): Array<File>? {
        if (!filePath.exists()) {
            return null
        }
        val files: Array<File> = filePath.listFiles()
        if (files.isEmpty()) {
            return null
        }
        files.sortBy { it.lastModified() }
        files.reverse()
        return files

    }
/*===================此工具类专门 用来 产生 保存 文件 到sd卡上的 文件名称 ==*/

    val time: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(System.currentTimeMillis()))
    /***
     * 获取要保存的文件路径，详细地址
     * @param formatNmae 传入一个文件后缀名  如 全小写  mp4 ，h264， jpg，3gp ，aac
     * @return File 创建一个当前时间的视频文件名，并包含了详细地址
     */
    var filePath: File? = null;

    fun getFilePath(formatNmae: String): File? {
        when (formatNmae) {
            "mp4", "h264", "3gp" -> {//保存为视频文件
                filePath = File("${APP_PATH}/videos/${time}.${formatNmae}");
            }
            "jpg", "JPEG", "png", "PNG" -> {//保存为图片文件
                filePath = File("${APP_PATH}/images/${time}.${formatNmae}");
            }
            "aac", "amr" -> {//保存为声音文件
                filePath = File("${APP_PATH}/voices/${time}.${formatNmae}");
            }
            "log", "crash" -> {//保存的异常文件
                //".log";//保存的异常文件名为.log
                filePath = File("${APP_PATH}/log/${time}.${formatNmae}");
            }
            else -> {//保存为其他文件
                filePath = File("${APP_PATH}/elses/${time}.${formatNmae}");
            }
        }
        LogUtil.d("FileUtils", filePath.toString())
        creatFile(filePath!!)
        LogUtil.d("FileUtils", "创建成功" + filePath.toString())
        return filePath;
    }

}
