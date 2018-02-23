package com.zhongyong.smartvideo;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongyong.smartvideo.ddsdemo.DDSMainActivity;

import butterknife.Bind;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.open_monitor)
    TextView openMonitorTv;
    @Bind(R.id.callXiaoDu)
    TextView callXiaoDuTv;
    @Bind(R.id.actionbar_back)
    RelativeLayout backLayout;
    private static final String TAG = "DDSMainActivity";


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViewsAndEvents() {
        InitListener();
    }

    public void InitListener() {
        openMonitorTv.setOnClickListener(this);
        callXiaoDuTv.setOnClickListener(this);
        backLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_monitor:
                Intent intent1 = new Intent(this, MonitorListActivity.class);
                startActivity(intent1);
                break;
            case R.id.callXiaoDu:
                Intent intent2 = new Intent(this, DDSMainActivity.class);
                startActivity(intent2);
                break;
            case R.id.actionbar_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
