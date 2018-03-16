package com.example.encrypt.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.encrypt.R;
import com.example.encrypt.photozoom.Folders;
import com.example.encrypt.photozoom.PrivateAlbum;
import com.example.encrypt.videozoom.PrivateVideoAlbum;
import com.example.encrypt.videozoom.VideoAlbum;
//import com.transage.privatespace.activity.BaseActivity;

/**
 * Created by dongrp on 2017/7/1.
 * 私密图库及视频的主界面(动态根据Main的点击情况，决定显示视频集或图片集)
 */

public class GalleryMainActivity extends BaseActivity implements View.OnClickListener {

    //private final String SDcardPath = "storage/emulated/0/";
    private final String SDcardPath = Environment.getExternalStorageDirectory().toString() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_main);
        addAppActivity(GalleryMainActivity.this);

/*        //读写权限 因为android:sharedUserId="android.uid.system"，所以不需要权限请求了
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(GalleryMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(GalleryMainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }*/

        //主界面点击 私密图片或私密视频，呈现不同的按钮
        if (getIntent().getBooleanExtra("isPhoto", false)) {
            findViewById(R.id.button_priv_album).setVisibility(View.VISIBLE);
            findViewById(R.id.button_add_priv_photo).setVisibility(View.VISIBLE);
        } else {
            //视频相关界面统一用蓝色调，以下逻辑修改状态栏颜色
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.setStatusBarColor(getResources().getColor((R.color.blue)));//设置状态栏颜色

            findViewById(R.id.button_priv_video_album).setVisibility(View.VISIBLE);
            findViewById(R.id.button_add_priv_video).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_priv_album:
                startActivity(new Intent(GalleryMainActivity.this, PrivateAlbum.class));
                break;
            case R.id.button_add_priv_photo:
                startActivity(new Intent(GalleryMainActivity.this, Folders.class));
                break;
            case R.id.button_priv_video_album:
                startActivity(new Intent(GalleryMainActivity.this, PrivateVideoAlbum.class));
                break;
            case R.id.button_add_priv_video:
                startActivity(new Intent(GalleryMainActivity.this, VideoAlbum.class));
                break;
            default:
                break;
        }
    }


}
