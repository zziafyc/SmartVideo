package com.zhongyong.smartvideo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration;
import com.zhongyong.smartvideo.recyclerview.CommonAdapter;
import com.zhongyong.smartvideo.recyclerview.MultiItemTypeAdapter;
import com.zhongyong.smartvideo.recyclerview.ViewHolder;
import com.zhongyong.smartvideo.utils.SharePreferenceUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;

public class MonitorListActivity extends BaseActivity {
    @Bind(R.id.swipeLv)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    @Bind(R.id.title_right)
    TextView rightTv;
    CommonAdapter<MonitorScene> mAdapter;
    List<MonitorScene> mList = new ArrayList<>();
    View footer;

    @Override
    public int getLayoutId() {
        return R.layout.activity_monitor;
    }

    @Override
    public void initViewsAndEvents() {
        rightTv.setVisibility(View.GONE);
        initSwipe();
        initAdapter();
        initListener();
        initData();
    }

    private void initListener() {

    }

    private void initData() {
        List<MonitorScene> mMonitorSceneModels = (List<MonitorScene>) SharePreferenceUtils.get(MonitorListActivity.this, "monitorScenes", new TypeToken<List<MonitorScene>>() {
        }.getType());
        if (mMonitorSceneModels != null && mMonitorSceneModels.size() > 0) {
            mList.addAll(mMonitorSceneModels);
            mAdapter.notifyDataSetChanged();
        }
        //获取
        boolean flag = (boolean) SharePreferenceUtils.get(MonitorListActivity.this, "flag_exist_address", false);
        if (!flag) {
            mList.add(new MonitorScene("楼顶1", "rtsp://10.161.56.141:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_101"));
            mList.add(new MonitorScene("楼顶2", "rtsp://10.161.56.142:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_101"));
            mList.add(new MonitorScene("图书馆", "rtsp://10.161.56.143:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1"));
            //mList.add(new MonitorScene("中用测试", Constants.RTSP_SOUTH_GALLERY));

            mAdapter.notifyDataSetChanged();
            SharePreferenceUtils.put(MonitorListActivity.this, "monitorScenes", mList);
            SharePreferenceUtils.put(MonitorListActivity.this, "flag_exist_address", true);
        }
    }

    private void initSwipe() {
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeMenuRecyclerView.addItemDecoration(new DefaultItemDecoration(getResources().getColor(R.color.grey)));
        mSwipeMenuRecyclerView.setLongPressDragEnabled(true); // 拖拽排序，默认关闭。
        mSwipeMenuRecyclerView.setItemViewSwipeEnabled(false); // 侧滑删除，默认关闭。
        mSwipeMenuRecyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        mSwipeMenuRecyclerView.setSwipeMenuItemClickListener(mMenuItemClickListener); // Item的Menu点击。
        mSwipeMenuRecyclerView.setOnItemMoveListener(getItemMoveListener());// 监听拖拽和侧滑删除，更新UI和数据源。
        footer = getLayoutInflater().inflate(R.layout.footer_scene_add, null);
        mSwipeMenuRecyclerView.addFooterView(footer);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitorListActivity.this, CreateMonitorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initAdapter() {
        mAdapter = new CommonAdapter<MonitorScene>(this, R.layout.item_monitor, mList) {
            @Override
            protected void convert(ViewHolder holder, MonitorScene scene, int position) {
                holder.setText(R.id.sceneNameTv, scene.getName());
                holder.setText(R.id.itemAdd_address_tv, scene.getAddress());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                Intent intent = new Intent(MonitorListActivity.this, MonitorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("monitorScene", mList.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mSwipeMenuRecyclerView.setAdapter(mAdapter);
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
            SwipeMenuItem deleteItem = new SwipeMenuItem(MonitorListActivity.this)
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
                SharePreferenceUtils.put(MonitorListActivity.this, "monitorScenes", mList);
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
                SharePreferenceUtils.put(MonitorListActivity.this, "monitorScenes", mList);
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
                    SharePreferenceUtils.put(MonitorListActivity.this, "monitorScenes", mList);
                }
            }
        };
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Subscribe
    public void AddMonitorScene(MonitorScene model) {
        if (model != null) {
            mList.add(model);
            mAdapter.notifyDataSetChanged();
        }
    }
}
