package com.example.encrypt.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.example.encrypt.R;
import com.example.encrypt.adapter.AppRecyclerAdapter;
import com.example.encrypt.bean.AppInfo;
import com.example.encrypt.database.DatabaseAdapter;

import java.util.ArrayList;

/**
 * Created by dongrp on 2016/8/13. 主界面
 */
public class Main extends BaseActivity implements AppRecyclerAdapter.RecycleView_OnItemClickListener,
        AppRecyclerAdapter.RecycleView_OnItemLongClickListener {
    private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();// 数据
    private RecyclerView recyclerView;// 控件
    private AppRecyclerAdapter recycleAdapter;// 适配器
    private PackageManager packageManager;
    private boolean isShowDeleteImageView = false;
    private DatabaseAdapter mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addAppActivity(Main.this);
        mDb = new DatabaseAdapter(getApplication());
        // initData(); //一定要先初始化数据
        initViewAndAdapter(); // 再初始化View 和 Adapter
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();// 重新加载数据
    }
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
    @Override
    protected void onPause() {
        super.onPause();
       // packageManager.setApplicationEnabledSetting("cn.wps.moffice_eng", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("Main","onKeyUp 744"+event.isLongPress());
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d("Main","onKeyUp "+keyCode);
                for(AppInfo appinfo : appList){
                    if(null != appinfo.getPackageName()&&packageManager.getApplicationEnabledSetting(appinfo.getPackageName())==PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                        packageManager.setApplicationEnabledSetting(appinfo.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    }
                }
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
    /**
     * 初始化 View 及 Adapter
     */
    private void initViewAndAdapter() {
        // 初始化recyclerView及recycleAdapter
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recycleAdapter = new AppRecyclerAdapter(Main.this, appList);
        // 创建recyclerView的布局管理器
        // LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 创建布局管理器，设置为4列的布局
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        // 给recyclerView设置布局管理器
        recyclerView.setLayoutManager(gridLayoutManager);
        // 设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置item的间距(需自定义：SpacesItemDecoration类)
        recyclerView.addItemDecoration(new SpacesItemDecoration(30));
        // 设置点击监听（需自定义：在MyRecycleViewAdapter中实现）
        recycleAdapter.setOnItemClickListener(this);
        recycleAdapter.setOnItemLongClickListener(this);
        // 设置Adapter
        recyclerView.setAdapter(recycleAdapter);
    }

    /**
     * 初始化数据
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initData() {
        packageManager = getPackageManager();
        appList.clear();
        appList.addAll(mDb.getApps(packageManager));
        //添加“高级设置”
        AppInfo appInfo0 = new AppInfo();
        appInfo0.setAppIcon(getDrawable(R.mipmap.settings));
        appInfo0.setAppName(getString(R.string.advanced_setup));
        appList.add(0,appInfo0);
        //添加“私密图库”
        AppInfo appInfo1 = new AppInfo();
        appInfo1.setAppIcon(getDrawable(R.mipmap.icon_priv_gallery));
        appInfo1.setAppName(getString(R.string.private_gallery));
        appList.add(1,appInfo1);
        //添加“私密视频”
        AppInfo appInfo2 = new AppInfo();
        appInfo2.setAppIcon(getDrawable(R.mipmap.icon_priv_video));
        appInfo2.setAppName(getString(R.string.private_video_album));
        appList.add(2,appInfo2);
/*        //添加最后的"+"
        AppInfo appInfo = new AppInfo();
        appInfo.setAppIcon(getDrawable(R.mipmap.add));
        appInfo.setAppName(getString(R.string.add_app));
        appList.add(appInfo);*/

        isShowDeleteImageView = false;
        recycleAdapter.showDeletImageView(false);//不显示右上角的删除按钮,该方法内部包含notifyDataSetChanged（）,所以不需要下面的notify了
        //recycleAdapter.notifyDataSetChanged();
    }

    // RecycleView的item点击监听回调
    @Override
    public void onItemClick(View view, int position) {
/*        if (position == appList.size() - 1) { // add
            startActivity(new Intent(Main.this, AddApp.class));
            return;
        }else */
        if (position == 0){//“高级设置”
            startActivity(new Intent(Main.this, AdvancedSetup.class));
            return;
        }else if (position == 1){//“私密图片”
            startActivity(new Intent(Main.this, GalleryMainActivity.class).putExtra("isPhoto",true));
            return;
        }else if (position == 2){//“私密视频”
            startActivity(new Intent(Main.this, GalleryMainActivity.class).putExtra("isPhoto",false));
            return;
        }
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
        if (view.getId() == R.id.iv_appIcon) {
            if(packageManager.getApplicationEnabledSetting(appList.get(position).getPackageName())==PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                packageManager.setApplicationEnabledSetting(appList.get(position).getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            }
            Intent launchIntent = packageManager.getLaunchIntentForPackage(appList.get(position).getPackageName());
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(launchIntent);
        } else if (view.getId() == R.id.iv_delete) {
            mDb.deleteAppByPackageName(appList.get(position).getPackageName());
            // 从本次加载的数据中删除该包名，刷新适配器
            if(packageManager.getApplicationEnabledSetting(appList.get(position).getPackageName())==PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                packageManager.setApplicationEnabledSetting(appList.get(position).getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            }
            appList.remove(position);
            recycleAdapter.notifyDataSetChanged();
        }
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
    }

    // RecycleView的item长按监听回调
    @Override
    public void onItemLongClick(View view, int position) {
        if (/*position != appList.size() - 1 &&*/ position != 0 && position != 1 && position != 2) {
            isShowDeleteImageView = true;
            recycleAdapter.showDeletImageView(true);
        }
    }

/*    *//**
     * 点击监听
     *//*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv3: // 高级设置
                startActivity(new Intent(Main.this, AdvancedSetup.class));
                break;
            case R.id.iv2: // 私密联系人
                startActivity(new Intent(Main.this, PrivateContacts.class).putExtra("tabPosition", 1));
                break;
            case R.id.iv1: // 私密电话
                startActivity(new Intent(Main.this, PrivateContacts.class).putExtra("tabPosition", 0));
                break;
        }
    }*/

    /**
     * 该类：SpacesItemDecoration用于设置RecyclerView的item的间距
     */
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = 5 * space / 3;
            outRect.bottom = 5 * space / 3;
            outRect.left = space;
            outRect.right = space;
        }
    }

    @Override
    public void onBackPressed() {
        if (isShowDeleteImageView) {
            isShowDeleteImageView = false;
            recycleAdapter.showDeletImageView(false);
            return;
        }
        super.onBackPressed();
    }
}
