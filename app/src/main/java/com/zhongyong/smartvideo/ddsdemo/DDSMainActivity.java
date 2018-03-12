package com.zhongyong.smartvideo.ddsdemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dds.update.DDSUpdateListener;
import com.aispeech.dui.dds.utils.PrefUtil;
import com.zhongyong.smartvideo.App;
import com.zhongyong.smartvideo.BaseActivity;
import com.zhongyong.smartvideo.Constants;
import com.zhongyong.smartvideo.MonitorActivity2;
import com.zhongyong.smartvideo.MonitorScene;
import com.zhongyong.smartvideo.R;
import com.zhongyong.smartvideo.ddsdemo.webview.HybridWebViewClient;
import com.zhongyong.smartvideo.ddsdemo.widget.InputField;
import com.zhongyong.smartvideo.utils.AIUIUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

public class DDSMainActivity extends BaseActivity implements InputField.Listener {
    @Bind(R.id.main_web_container)
    RelativeLayout webContainer;
    MyReceiver receiver;
    @Bind(R.id.input_field)
    InputField inputField;
    private boolean isActivityShowing = false;
    private WebView webview;
    private boolean mLoadedTotally = false;
    private Dialog dialog;
    private Handler mHandler = new Handler();
    public static MyMessageObserver mMessageObserver;

    @Override
    public int getLayoutId() {
        return R.layout.activity_dds_main;
    }

    @Override
    public void initViewsAndEvents() {
        inputField.setListener(this);
        setWebView();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ddsdemo.intent.action.init_complete");
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);
        mMessageObserver = new MyMessageObserver();

    }

    @Override
    protected void onStart() {
        isActivityShowing = true;
        try {
            DDS.getInstance().getUpdater().update(ddsUpdateListener);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        DDS.getInstance().getAgent().subscribe("sys.resource.updated", resourceUpdatedMessageObserver);
        super.onStart();
    }

    @Override
    protected void onStop() {
        AILog.d(TAG, "onStop() " + this.hashCode());
        isActivityShowing = false;
        DDS.getInstance().getAgent().unSubscribe(resourceUpdatedMessageObserver);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        App.getInstance().currentActivity = 0;
        DDS.getInstance().getAgent().subscribe(new String[]{"context.output.text", "context.input.text",
                "avatar.silence", "avatar.listening", "avatar.understanding", "avatar.speaking", "context.widget.content"}, mMessageObserver);

        if (inputField != null) {
            inputField.getAvatarView().go();
        }
        enableWakeIfNecessary();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //DDS.getInstance().getAgent().unSubscribe(mMessageObserver);
        if (!App.getInstance().flag) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (inputField != null) {
                        inputField.toListen();
                    }
                }
            });
            enableWakeIfNecessary();
        } else {
            disableWakeIfNecessary();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DDSMainActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        DDS.getInstance().getAgent().unSubscribe(mMessageObserver);
        disableWakeIfNecessary();
        super.onDestroy();
        unregisterReceiver(receiver);
        if (webContainer != null) {
            webContainer.removeAllViews();
        }
        if (webview != null) {
            webview.removeAllViews();
            webview.destroy();
        }
        if (inputField != null) {
            inputField.destroy();
        }
        // stopService();
    }

    private void stopService() {
        Intent intent = new Intent(DDSMainActivity.this, DDSService.class);
        stopService(intent);
    }

    private void setWebView() {
        webview = new WebView(getApplicationContext());
        webview.setWebViewClient(new HybridWebViewClient(this));
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d(TAG, "view " + view + " progress " + newProgress + " mLoadedTotally " + mLoadedTotally);
                if (newProgress == 100 && !mLoadedTotally) {
                    mLoadedTotally = true;
                    sendHiMessage();
                }
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setBackgroundColor(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        try {
            if (DDS.getInstance().getAgent() != null) {
                loadUI(DDS.getInstance().getAgent().getValidH5Path());
            }
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        webContainer.addView(webview, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                .MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }


    void loadUI(String h5UiPath) {
        if (!TextUtils.isEmpty(h5UiPath)) {
            Log.d(TAG, "loadUI " + h5UiPath);
            String url = h5UiPath;
            mLoadedTotally = false;
            webview.loadUrl(url);
        }
    }

    public static void enableWakeIfNecessary() {
        try {
            DDS.getInstance().getAgent().enableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    public static void disableWakeIfNecessary() {
        try {
            DDS.getInstance().getAgent().stopDialog();
            DDS.getInstance().getAgent().disableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    public void sendHiMessage() {
        Log.d(TAG, "sendHiMessage");
        String[] wakeupWords = new String[0];
        String minorWakeupWord = null;
        try {
            wakeupWords = DDS.getInstance().getAgent().getWakeupWords();
            minorWakeupWord = DDS.getInstance().getAgent().getMinorWakeupWord();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        String hiStr = "";
        if (wakeupWords != null && minorWakeupWord != null) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], minorWakeupWord);
        } else if (wakeupWords != null && wakeupWords.length == 2) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], wakeupWords[1]);
        } else if (wakeupWords != null) {
            hiStr = getString(R.string.hi_str, wakeupWords[0]);
        }
        JSONObject output = new JSONObject();
        try {
            output.put("text", hiStr);
            DDS.getInstance().getAgent().getBusClient().publish("context.output.text", output.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
       /* try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            DDS.getInstance().getAgent().avatarClick();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputField.toListen();
            }
        });*/
    }

    @Override
    public boolean onMicClicked() {
        try {
            DDS.getInstance().getAgent().avatarClick();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        return true;
    }


    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            Log.d(TAG, "onReceive " + name);
            if (name.equals("ddsdemo.intent.action.init_complete")) {
                try {
                    loadUI(DDS.getInstance().getAgent().getValidH5Path());
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MyMessageObserver implements MessageObserver {
        private StringBuilder mInputText = new StringBuilder();

        @Override
        public void onMessage(String message, String data) {
            Log.d(TAG, "message : " + message + " data : " + data);
            if ("context.input.text".equals(message)) {
                String speakMessage = "";
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    speakMessage = jsonObject.optString("text", "");
                    if (!TextUtils.isEmpty(speakMessage)) {
                        Log.e(TAG, "你提出的问题为： " + speakMessage);
                        dealWithMessage(speakMessage);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if ("context.output.text".equals(message)) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.has("text")) {
                        String receiveMessage = jsonObject.optString("text", "");
                        if (!TextUtils.isEmpty(receiveMessage)) {
                            Log.e(TAG, "机器回复内容为： " + receiveMessage);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if ("context.widget.content".equals(message)) {
                try {
                    JSONObject jo = new JSONObject(data);
                    String title = jo.optString("title", "");
                    String subTitle = jo.optString("subTitle", "");
                    String imgUrl = jo.optString("imageUrl", "");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if ("avatar.silence".equals(message)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (inputField != null) {
                            inputField.toIdle();
                        }
                    }
                });
            } else if ("avatar.listening".equals(message)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (inputField != null) {
                            inputField.toListen();
                        }
                    }
                });
            } else if ("avatar.understanding".equals(message)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (inputField != null) {
                            inputField.toRecognize();
                        }
                    }
                });
            } else if ("avatar.speaking".equals(message)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (inputField != null) {
                            inputField.toSpeak();
                        }
                    }
                });

            }
        }
    }

    private void dealWithMessage(String message) {
        if (message.contains("空调") || message.contains("投影仪") || message.contains("加湿器")
                || message.contains("灯") || message.contains("制冷模式") || message.contains("制热模式")
                || message.contains("电视") || message.contains("换台") || message.contains("频道")
                || message.contains("窗帘") || message.contains("纱帘") || message.contains("布帘")) {
            AIUIUtils.sendContextToAIUI(message);
        } else if (message.contains("监控")) {
            MonitorScene scene = null;
            if (message.contains("会议室")) {
                scene = new MonitorScene("会议室监控", Constants.RTSP_MEETING);
            } else if (message.contains("走廊")) {
                scene = new MonitorScene("走廊监控", Constants.RTSP_SOUTH_GALLERY);
            } else if (message.contains("一楼监控")) {
                scene = new MonitorScene("一楼监控", Constants.RTSP_FIRST_MONITOR);
                EventBus.getDefault().post(scene);
            } else if (message.contains("二楼监控")) {
                scene = new MonitorScene("二楼监控", Constants.RTSP_SECOND_MONITOR);
                EventBus.getDefault().post(scene);
            }
            //停顿一段时间后再跳转，供语音交互
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (scene != null && App.getInstance().currentActivity == 0) {
                Intent intent = new Intent(this, MonitorActivity2.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("monitorScene", scene);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        } else if (message.contains("打开对讲机")) {
            //当进入录音机时，取消语音监听
            App.getInstance().flag = true;

            //此时启动对讲机应用
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent();
            intent = packageManager.getLaunchIntentForPackage("android.serialport.sample");
            if (intent == null) {
                showToast("APP not found!");
            }
            startActivity(intent);
        }
    }

    private DDSUpdateListener ddsUpdateListener = new DDSUpdateListener() {
        @Override
        public void onUpdateFound(String detail) {
            final String str = detail;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showNewVersionDialog(str);
                }
            });

            try {
                DDS.getInstance().getAgent().speak("发现新版本,正在为您更新", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpdateFinish() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showUpdateFinishedDialog();
                }
            });
            try {
                DDS.getInstance().getAgent().speak("更新成功", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadProgress(float progress) {
            Log.d(TAG, "onDownloadProgress :" + progress);
        }

        @Override
        public void onError(int what, String error) {
            String productId = PrefUtil.getString(getApplicationContext(), DDSConfig.K_PRODUCT_ID);
            if (what == 70319) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProductNeedUpdateDialog();
                    }
                });

            } else {
                Log.e(TAG, "UPDATE ERROR : " + error);
            }
        }

        @Override
        public void onUpgrade(String version) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showApkUpdateDialog();
                }
            });

        }
    };

    private MessageObserver resourceUpdatedMessageObserver = new MessageObserver() {
        @Override
        public void onMessage(String message, String data) {
            try {
                DDS.getInstance().getUpdater().update(ddsUpdateListener);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    };


    protected void showNewVersionDialog(final String info) {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setTitle(getString(R.string.dds_update_found_title));
        ((ProgressDialog) dialog).setMessage(info);
        ((ProgressDialog) dialog).setProgress(0);
        dialog.show();
    }

    protected void showProductNeedUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_product_title)
                .setMessage(R.string.update_product_message).setPositiveButton(R.string.update_product_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_product_cancel, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showApkUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_sdk_title)
                .setMessage(R.string.update_sdk_message).setPositiveButton(R.string.update_sdk_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_sdk_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showUpdateFinishedDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dds_resource_load_success));

        dialog = builder.create();
        dialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss();
                t.cancel();
            }
        }, 2000);
    }
}
