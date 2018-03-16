package com.example.encrypt.photozoom;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;

import java.util.List;


/**
 * 这个类主要是用来:将所有包含图片的文件夹  以GridView的形式展示出来
 *
 * @author Tom
 */
public class Folders extends BaseActivity {

    private AlbumHelper helper;
    public static List<ImageBucket> contentList;
    private FolderGirdViewAdapter folderAdapter;
    private GridView gridView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);
        addAppActivity(Folders.this);
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());
        //组件和适配器
        gridView = (GridView) findViewById(R.id.fileGridView);
        folderAdapter = new FolderGirdViewAdapter(this);
        //返回 按钮
        findViewById(R.id.button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        //初始化ImagesBucketList（所有包含图片的文件夹的集合，一个包含图片的文件夹就是一个ImagesBucket）
        contentList = helper.getImagesBucketList(true);
        //绑定ImagesBucket 适配器
        gridView.setAdapter(folderAdapter);
    }
}
