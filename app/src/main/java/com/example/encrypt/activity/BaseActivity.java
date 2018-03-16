package com.example.encrypt.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ruipan.dong on 2017/8/18.
 */

public class BaseActivity extends AppCompatActivity {

    //应用Activity列表
    private static ArrayList<Activity> activityList = new ArrayList<Activity>();
    //广播接收器
    private SystemKeyEventReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();// 隐藏掉ActionBar
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏TitleBar
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
        receiver = new SystemKeyEventReceiver();
        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    //注册广播
    public void registerReceiver() {
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Log.d("BaseActivity", "registerReceiver");
    }

    //反注册广播
    public void unRegisterReceiver() {
        unregisterReceiver(receiver);
        Log.d("BaseActivity", "unRegisterReceiver");
    }

    /**
     * 系统按键事件广播接收器
     */
    private class SystemKeyEventReceiver extends BroadcastReceiver {
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason == null) {
                    return;
                }
                // Home键
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) && Login.sp.getBoolean("fastExit", false)) {
                    exitApp(BaseActivity.this);
                }
                // 最近任务列表键
                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS) && Login.sp.getBoolean("fastExit", false)) {
                    exitApp(BaseActivity.this);
                }
            }
        }
    }

    /**
     * 添加Activity到列表中
     *
     * @param activity
     */
    public static void addAppActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    /**
     * 从列表移除Activity
     *
     * @param activity
     */
    public static void removeAppActivity(Activity activity) {
        if (activityList.contains(activity)) {
            activityList.remove(activity);
        }
    }

    /**
     * 退出应用程序
     */
    public static void exitApp(Context context) {
        for (Activity ac : activityList) {
            if (!ac.isFinishing()) {
                ac.finish();
            }
        }
        activityList.clear();
        //杀掉进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }


/*    *//**
     * 退出应用
     *//*
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
    private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();// 数据
    private DatabaseAdapter mDb = null;
    private PackageManager packageManager = null;

    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
    public void exitApp(boolean isExitFromRecent) {
        //优化后的逻辑：去掉if条件，只要接收到Home键广播，不管app是在前台还是后台，都执行退出app操作（退出逻辑前后台都可以执行）
        Log.d("PrivateSpaceApplication", "exit privatespace app");
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
        mDb = new DatabaseAdapter(this);
        packageManager = getPackageManager();
        appList.clear();
        appList.addAll(mDb.getApps(packageManager));
        for (AppInfo appinfo : appList) {
            if (packageManager.getApplicationEnabledSetting(appinfo.getPackageName()) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                packageManager.setApplicationEnabledSetting(appinfo.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            }
        }
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
        Intent intent = new Intent(this, EmptyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isExitFromRecent", isExitFromRecent);
        startActivity(intent);
    }*/


}
