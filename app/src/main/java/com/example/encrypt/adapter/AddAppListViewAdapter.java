package com.example.encrypt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.encrypt.R;
import com.example.encrypt.bean.AppInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dongrp on 2016/8/19.
 */
public class AddAppListViewAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<AppInfo> appList;
	// 该map记录每一个item中的chengckBox的选中状态并根据状态进行设置，避免滑动的时候混乱错位
	public static HashMap<Integer, Boolean> map_allCheckBoxSelectedStatus;

	public AddAppListViewAdapter(Context context, ArrayList<AppInfo> appList) {
		this.context = context;
		this.appList = appList;
		this.map_allCheckBoxSelectedStatus = new HashMap<Integer, Boolean>();
		// 初始化map_allCheckBoxSelectedStatus，默认value = false
		for (int i = 0; i < appList.size(); i++) {
			map_allCheckBoxSelectedStatus.put(i, false);
		}
	}

	@Override
	public int getCount() {
		return appList.size();
	}

	@Override
	public Object getItem(int position) {
		return appList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder1 viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_add_app, null);
			viewHolder = new ViewHolder1();
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			viewHolder.iv_appIcon = (ImageView) convertView.findViewById(R.id.iv_appIcon);
			viewHolder.tv_appName = (TextView) convertView.findViewById(R.id.tv_appName);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder1) convertView.getTag();
		}
		// 数据适配
		viewHolder.checkBox.setChecked(map_allCheckBoxSelectedStatus.get(position));
		viewHolder.iv_appIcon.setBackground(appList.get(position).getAppIcon());
		viewHolder.tv_appName.setText(appList.get(position).getAppName());
		return convertView;
	}

	public class ViewHolder1 {
		public CheckBox checkBox;
		public ImageView iv_appIcon;
		public TextView tv_appName;
	}

}
