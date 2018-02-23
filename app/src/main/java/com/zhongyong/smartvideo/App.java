package com.zhongyong.smartvideo;

import android.app.Application;
import android.content.Intent;

import com.zhongyong.smartvideo.ddsdemo.DDSService;

/**
 * Created by fyc on 2018/2/7.
 */

public class App extends Application {
    Intent serviceIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        //启动dds服务
        serviceIntent = new Intent(this, DDSService.class);
        serviceIntent.setAction("start");
        startService(serviceIntent);
    }
}
