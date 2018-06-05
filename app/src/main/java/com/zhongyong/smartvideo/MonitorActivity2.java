package com.zhongyong.smartvideo;

import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aispeech.dui.dds.DDS;
import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration;
import com.zhongyong.smartvideo.ddsdemo.DDSMainActivity;
import com.zhongyong.smartvideo.recyclerview.CommonAdapter;
import com.zhongyong.smartvideo.recyclerview.MultiItemTypeAdapter;
import com.zhongyong.smartvideo.recyclerview.ViewHolder;
import com.zhongyong.smartvideo.utils.RtspSurfaceRender;
import com.zhongyong.smartvideo.utils.SharePreferenceUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;

public class MonitorActivity2 extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.actionbar_back)
    RelativeLayout backRv;
    @Bind(R.id.title_tv_message)
    TextView titleTv;
    @Bind(R.id.sceneNameTv)
    TextView sceneNameTv;
    @Bind(R.id.sceneIpTv)
    TextView sceneIpTv;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.swipeLv)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    @Bind(R.id.surface)
    LinearLayout surfaceLayout;
    GLSurfaceView mSurfaceView;
    CommonAdapter<MonitorScene> mAdapter;
    List<MonitorScene> mList = new ArrayList<>();
    View footer;
    MonitorScene model;
    private RtspSurfaceRender mRender;

    @Override
    public int getLayoutId() {
        return R.layout.activity_video2;
    }

    @Override
    public void initViewsAndEvents() {
        backRv.setOnClickListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                model = (MonitorScene) bundle.getSerializable("monitorScene");
                if (model != null) {
                   /* sceneNameTv.setText(model.getName());
                    sceneIpTv.setText(model.getAddress());*/
                    titleTv.setText(model.getName());
                    initVideo(model.getAddress());
                }
            }
        }
        initSwipe();
        initAdapter();
        initData();
    }

    private void initSwipe() {
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeMenuRecyclerView.addItemDecoration(new DefaultItemDecoration(getResources().getColor(R.color.grey)));
        mSwipeMenuRecyclerView.setLongPressDragEnabled(false); // 拖拽排序，默认关闭。
        mSwipeMenuRecyclerView.setItemViewSwipeEnabled(false); // 侧滑删除，默认关闭。
        mSwipeMenuRecyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        mSwipeMenuRecyclerView.setSwipeMenuItemClickListener(mMenuItemClickListener); // Item的Menu点击。
        mSwipeMenuRecyclerView.setOnItemMoveListener(getItemMoveListener());// 监听拖拽和侧滑删除，更新UI和数据源。
        footer = getLayoutInflater().inflate(R.layout.footer_scene_add, null);
        mSwipeMenuRecyclerView.addFooterView(footer);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitorActivity2.this, CreateMonitorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initAdapter() {
        mAdapter = new CommonAdapter<MonitorScene>(this, R.layout.item_monitor2, mList) {
            @Override
            protected void convert(ViewHolder holder, MonitorScene scene, int position) {
                holder.setText(R.id.sceneNameTv, scene.getName());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                titleTv.setText(mList.get(position).getName());
                initVideo(mList.get(position).getAddress());
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mSwipeMenuRecyclerView.setAdapter(mAdapter);
    }

    private void initData() {
        List<MonitorScene> mMonitorSceneModels = (List<MonitorScene>) SharePreferenceUtils.get(MonitorActivity2.this, "monitorScenes", new TypeToken<List<MonitorScene>>() {
        }.getType());
        if (mMonitorSceneModels != null && mMonitorSceneModels.size() > 0) {
            mList.addAll(mMonitorSceneModels);
            mAdapter.notifyDataSetChanged();
        }
        //获取
        boolean flag = (boolean) SharePreferenceUtils.get(MonitorActivity2.this, "flag_exist_address", false);
        if (!flag) {
            mList.add(new MonitorScene("会议室监控", Constants.RTSP_MEETING));
            mList.add(new MonitorScene("走廊监控", Constants.RTSP_SOUTH_GALLERY));
            mList.add(new MonitorScene("一楼监控", "rtsp://192.168.0.64:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1"));
            mList.add(new MonitorScene("二楼监控", "rtsp://192.168.0.245:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1"));
            mAdapter.notifyDataSetChanged();
            SharePreferenceUtils.put(MonitorActivity2.this, "monitorScenes", mList);
            SharePreferenceUtils.put(MonitorActivity2.this, "flag_exist_address", true);
        }
    }

    private void initVideo(String address) {
        surfaceLayout.removeAllViews();
        mSurfaceView = new GLSurfaceView(this);


        mSurfaceView.setEGLContextClientVersion(3);
        mRender = new RtspSurfaceRender(mSurfaceView);
        mRender.setRtspUrl(address);
        mSurfaceView.setRenderer(mRender);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        surfaceLayout.addView(mSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().currentActivity = 1;
        mSurfaceView.onResume();
        DDS.getInstance().getAgent().subscribe(new String[]{"context.output.text", "context.input.text",
                "avatar.silence", "avatar.listening", "avatar.understanding", "avatar.speaking", "context.widget.content"}, DDSMainActivity.mMessageObserver);

        DDSMainActivity.enableWakeIfNecessary();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        DDSMainActivity.disableWakeIfNecessary();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_back:
                finish();
                break;

        }
    }

    /**
     * 菜单创建器。
     */
    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = getResources().getDimensionPixelSize(R.dimen.dp_90);

            // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
            // 2. 指定具体的高，比如80;
            // 3. WRAP_CONTENT，自身高度，不推荐;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            // 添加右侧的，如果不添加，则右侧不会出现菜单。
            SwipeMenuItem deleteItem = new SwipeMenuItem(MonitorActivity2.this)
                    .setBackground(R.drawable.selector_red)
                    .setImage(R.mipmap.ic_action_delete)
                    .setText("删除")
                    .setTextSize(12)
                    .setTextColor(Color.WHITE)
                    .setWidth(width)
                    .setHeight(height);
            swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。
        }
    };

    /**
     * RecyclerView的Item的Menu点击监听。
     */
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            menuBridge.closeMenu();

            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                mList.remove(adapterPosition);
                mAdapter.notifyDataSetChanged();
                SharePreferenceUtils.put(MonitorActivity2.this, "monitorScenes", mList);
            } else if (direction == SwipeMenuRecyclerView.LEFT_DIRECTION) {
            }
        }
    };

    private OnItemMoveListener getItemMoveListener() {
        // 监听拖拽和侧滑删除，更新UI和数据源。
        return new OnItemMoveListener() {
            @Override
            public boolean onItemMove(RecyclerView.ViewHolder srcHolder, RecyclerView.ViewHolder targetHolder) {
                // 不同的ViewType不能拖拽换位置。
                if (srcHolder.getItemViewType() != targetHolder.getItemViewType()) {
                    return false;
                }
                // 真实的Position：通过ViewHolder拿到的position都需要减掉HeadView的数量。
                int fromPosition = srcHolder.getAdapterPosition() - mSwipeMenuRecyclerView.getHeaderItemCount();
                int toPosition = targetHolder.getAdapterPosition() - mSwipeMenuRecyclerView.getHeaderItemCount();
                Collections.swap(mList, fromPosition, toPosition);
                mAdapter.notifyItemMoved(fromPosition, toPosition);
                SharePreferenceUtils.put(MonitorActivity2.this, "monitorScenes", mList);
                return true;// 返回true表示处理了并可以换位置，返回false表示你没有处理并不能换位置。
            }

            @Override
            public void onItemDismiss(RecyclerView.ViewHolder srcHolder) {
                int adapterPosition = srcHolder.getAdapterPosition();
                int position = adapterPosition - mSwipeMenuRecyclerView.getHeaderItemCount();

                if (mSwipeMenuRecyclerView.getHeaderItemCount() > 0 && adapterPosition == 0) { // HeaderView。
                    //mSwipeMenuRecyclerView.removeHeaderView(mHeaderView);
                } else { // 普通Item。
                    mList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    SharePreferenceUtils.put(MonitorActivity2.this, "monitorScenes", mList);
                }
            }
        };
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Subscribe
    public void AddMonitorScene(final MonitorScene model) {
        if (model.getName().contains("监控")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    titleTv.setText(model.getName());
                    initVideo(model.getAddress());
                }
            });
        } else {
            if (model != null && model.isAdd()) {
                mList.add(model);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mRender.onSurfaceDestoryed();
        super.onDestroy();
    }

}
