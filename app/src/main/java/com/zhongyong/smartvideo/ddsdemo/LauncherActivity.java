package com.zhongyong.smartvideo.ddsdemo;

import android.content.Intent;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.zhongyong.smartvideo.BaseActivity;
import com.zhongyong.smartvideo.R;


/**
 * Disclaim
 * <p>
 * This program is the property of AI Speech Ltd. It shall be communicated to
 * authorized personnel only. It is not to be disclosed outside the group without
 * prior written consent. If you are not sure if you’re authorized to read this
 * program, please contact info@aispeech.com before reading.
 * <p>
 * Created by jinrui.gan on 17-3-12.
 */

public class LauncherActivity extends BaseActivity {
    private static final String TAG = "LauncherActivity";

    @Override
    public int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    public void initViewsAndEvents() {
        new Thread() {
            public void run() {
                checkDDSReady();
            }
        }.start();
    }


    public void checkDDSReady() {
        while (true) {
            if (DDS.getInstance().isInitComplete()) {
                //先等待再跳转
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), DDSMainActivity.class);
                startActivity(intent);
                finish();
                break;
            } else {
                AILog.w(TAG, "waiting  init complete finish...");
            }
        }
    }


}