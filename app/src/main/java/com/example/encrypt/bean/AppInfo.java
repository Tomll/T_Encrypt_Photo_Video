package com.example.encrypt.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by dongrp on 2016/8/17.
 * APP信息实体类
 */
public class AppInfo {
    private int id;
    private String appName; //应用名
    private String packageName;//包名
    private Drawable appIcon;//图标

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

}