package com.jay.recorders

import android.support.multidex.MultiDexApplication
import com.jay.recorders.objectUtils.DelegatesExt

/**
 * @author: Jeff <15899859876@qq.com>
 * @date:  2018-08-09  15:07
 * @description:
 */
class Recorders : MultiDexApplication(){
    //companion静态声类声名对象，相当于static关键
    companion object {
        // 按照我们在Java中一样创建一个单例最简单的方式：
//        private var instance:Application?=null;
//        fun instance()= instance!!;
        // 单例不会是null   所以使用notNull委托
        //var instance: FlashLight by Delegates.notNull()
        // 自定义委托实现单例,只能修改这个值一次.
        var instance: Recorders by DelegatesExt.notNullSingleValue<Recorders>();
    }

    //override实现接口关键字，override修饰的方法,默认是可以被继承的
    override fun onCreate() {
        super.onCreate();
        instance = this;

    }

    override fun onTerminate() {
        // 程序终止的时候执行
        super.onTerminate();

    }
}