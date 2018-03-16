package com.example.encrypt.activity;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.encrypt.R;
import com.example.encrypt.adapter.AddAppListViewAdapter;
import com.example.encrypt.adapter.AddAppListViewAdapter.ViewHolder1;
import com.example.encrypt.bean.AppInfo;
import com.example.encrypt.database.DatabaseAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
//Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
/**
 * Created by dongrp on 2016/8/18. 添加APP界面
 */
@SuppressLint("NewApi")
public class AddApp extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();// 所有app的list
    private ListView appListView;
    private AddAppListViewAdapter appListViewAdapter;
    private TextView tv_selected_mun;
    // 该map记录最终选中的app
    private HashMap<Integer, AppInfo> map_selectedApp = new HashMap<Integer, AppInfo>();
    // 将上面的map_selectedApp中的值遍历，存入该list_selectedApp集合
    private ArrayList<AppInfo> list_selectedApp = new ArrayList<AppInfo>();
    private DatabaseAdapter mDb;
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
    private PackageManager mPackageManager;
    //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app);
        addAppActivity(AddApp.this);
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
        mPackageManager = this.getPackageManager();
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
        // 通过自定义DBHelper类，获取SQLiteDatabase对象
        mDb = new DatabaseAdapter(this.getApplication());
        initData();
        initViewAndAdapter();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        getInstalledAppInfo(getPackageManager(), appInfoList, getPackageName());
    }

    /**
     * 初始化组件适配器
     */
    private void initViewAndAdapter() {
        tv_selected_mun = (TextView) findViewById(R.id.tv_selected_mun);
        tv_selected_mun.setText(getString(R.string.have_selected) + "0" + getString(R.string.item));
        appListView = (ListView) findViewById(R.id.app_listView);
        appListViewAdapter = new AddAppListViewAdapter(AddApp.this, appInfoList);
        appListView.setAdapter(appListViewAdapter);
        appListView.setOnItemClickListener(this);
    }

    // appListView的item点击监听回调
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder1 viewHolder1 = (ViewHolder1) view.getTag();
        viewHolder1.checkBox.toggle();// 点击将checkBox置反
        AddAppListViewAdapter.map_allCheckBoxSelectedStatus.put(position, viewHolder1.checkBox.isChecked());// 记录最新的checkBox状态
        // 根据最新的checkBox的状态，更新数据
        if (viewHolder1.checkBox.isChecked()) {
            map_selectedApp.put(position, appInfoList.get(position));
        } else {
            map_selectedApp.remove(position);
        }
        // 更新已选中xx项
        tv_selected_mun.setText(getString(R.string.have_selected) + map_selectedApp.size() + getString(R.string.item));
    }

    // 点击监听回调
    @Override
    public void onClick(View v) { // 本界面只有一个“完成”按钮，所以就不做switch了
        Iterator<Map.Entry<Integer, AppInfo>> iterator = map_selectedApp.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, AppInfo> entry = iterator.next();
            list_selectedApp.add(entry.getValue());
        }
        // 将选中的app添加到本地的数据库apps表中
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
        for (AppInfo appInfo : list_selectedApp) {
            mPackageManager.setApplicationEnabledSetting(appInfo.getPackageName(),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,0);
            mDb.addApp(appInfo);
        }
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
        finish();
    }
    class SortApplications implements Comparator<AppInfo> {

        Collator cmp = Collator.getInstance();

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (cmp.compare(lhs.getAppName(), rhs.getAppName()) > 0) {
                return 1;
            } else if (cmp.compare(lhs.getAppName(), rhs.getAppName()) < 0) {
                return -1;
            }
            return 0;
        }

    }

    /**
     * 取出Launcher中显示的所有APP的信息
     *
     * @param packageManager  :packageManager
     * @param appInfoList     :该集合用于存放取出的appInfo对象
     * @param thisPackageName ：本应用的包名，因为存appInfo的时候要过滤掉应用本身
     */
    public void getInstalledAppInfo(PackageManager packageManager, ArrayList<AppInfo> appInfoList,
                                    String thisPackageName) {
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 begin
       // Intent intent = new Intent(Intent.ACTION_MAIN);
       // intent.addCategory(Intent.CATEGORY_LAUNCHER);
        AppInfo appInfo;
        List<PackageInfo> infos = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        for (PackageInfo info : infos) {
            appInfo = new AppInfo();
            ApplicationInfo applicationInfo = info.applicationInfo;
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                continue;
            }
            appInfo.setAppIcon(packageManager.getApplicationIcon(applicationInfo));
            appInfo.setAppName(packageManager.getApplicationLabel(applicationInfo).toString());
            appInfo.setPackageName(applicationInfo.processName);
            // 过滤掉应用本身 和 数据库中已经添加过的应用
            if (!thisPackageName.equals(applicationInfo.processName) && !mDb.isExistsApp(info)) {
                appInfoList.add(appInfo);
            }
        }
        Collections.sort(appInfoList, new SortApplications());
        //Transage <zhaoxin>  add  for  privateapp  2017-9-25 end
    }

}
