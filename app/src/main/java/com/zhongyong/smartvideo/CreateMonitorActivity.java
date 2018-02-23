package com.zhongyong.smartvideo;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.zhongyong.smartvideo.utils.SharePreferenceUtils;
import com.zhongyong.smartvideo.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class CreateMonitorActivity extends BaseActivity {
    @Bind(R.id.title_tv_message)
    TextView titleTv;
    @Bind(R.id.title_right)
    TextView rightTv;
    @Bind(R.id.acn_edt_name)
    EditText sceneNameEdt;
    @Bind(R.id.acn_edt_ip)
    EditText addressEdt;
    List<MonitorScene> mMonitorScenes;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_monitor;
    }

    @Override
    public void initViewsAndEvents() {
        titleTv.setText("编辑设备信息");
        rightTv.setText("保存");
        rightTv.setVisibility(View.VISIBLE);
        initListener();

    }

    private void initListener() {
        rightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(sceneNameEdt.getText().toString())) {
                    showToast("请输入场景名称");
                    return;

                }
                if (StringUtils.isEmpty(addressEdt.getText().toString())) {
                    showToast("请输入监控地址");
                    return;

                }

                MonitorScene newModel = new MonitorScene(sceneNameEdt.getText().toString(), addressEdt.getText().toString());
                mMonitorScenes = (List<MonitorScene>) SharePreferenceUtils.get(CreateMonitorActivity.this, "monitorScenes", new TypeToken<List<MonitorScene>>() {
                }.getType());
                if (mMonitorScenes == null) {
                    mMonitorScenes = new ArrayList<>();
                }
                mMonitorScenes.add(newModel);
                SharePreferenceUtils.put(CreateMonitorActivity.this, "monitorScenes", mMonitorScenes);
                EventBus.getDefault().post(newModel);
                finish();
            }
        });

    }
}
