package com.zhongyong.smartvideo.ddsdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.zhongyong.smartvideo.MainActivity;
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

public class LauncherActivity extends Activity {
    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        new Thread() {
            public void run() {
                checkDDSReady();
            }
        }.start();
    }


    public void checkDDSReady() {
        while (true) {
            if (DDS.getInstance().isInitComplete()) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                break;
            } else {
                AILog.w(TAG, "waiting  init complete finish...");
            }
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}