package com.zhongyong.smartvideo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.inuker.library.MyContext;
import com.zhongyong.smartvideo.ddsdemo.DDSService;

/**
 * Created by fyc on 2018/2/7.
 */

public class App extends Application {
    public static App instance;
    public Intent serviceIntent;
    public int currentActivity;  //0:表示当前在ddsmainActivity中。1：表示当前在monitorActivity中
    public boolean flag; //用来表示是否打开对讲机

    @Override
    public void onCreate() {
        super.onCreate();
        //启动dds服务
        serviceIntent = new Intent(this, DDSService.class);
        serviceIntent.setAction("start");
        startService(serviceIntent);
        MyContext.setContext(this);
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public static Context getContext() {
        return MyContext.getContext();
    }
}
