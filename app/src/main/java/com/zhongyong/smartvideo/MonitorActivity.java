package com.zhongyong.smartvideo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import butterknife.Bind;

public class MonitorActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.actionbar_back)
    RelativeLayout backRv;
    @Bind(R.id.title_tv_message)
    TextView titleTv;
    @Bind(R.id.videoVv)
    VideoView mVideoView;
    @Bind(R.id.sceneNameTv)
    TextView sceneNameTv;
    @Bind(R.id.sceneIpTv)
    TextView sceneIpTv;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    MonitorScene model;

    @Override
    public int getLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    public void initViewsAndEvents() {
        backRv.setOnClickListener(this);
        mProgressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                model = (MonitorScene) bundle.getSerializable("monitorScene");
                if (model != null) {
                    sceneNameTv.setText(model.getName());
                    sceneIpTv.setText(model.getAddress());
                    titleTv.setText(model.getName());
                }
            }
        }
        initVideo();
    }

    private void initVideo() {
        //设置视频控制器
        mVideoView.setMediaController(new MediaController(this));
        //设置视频路径
        mVideoView.setVideoURI(Uri.parse(model.getAddress()));
        //开始播放视频
        mVideoView.requestFocus();
        //准备阶段监听
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                mVideoView.start();
                mProgressBar.setVisibility(View.GONE);

            }
        });
        //播放完成回调
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                //播放完成
                Toast.makeText(MonitorActivity.this, "播放完成", Toast.LENGTH_LONG).show();
            }
        });
        //播放出错监听
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer player, int i, int i1) {
                return false;
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
