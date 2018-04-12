package com.example.encrypt.videozoom;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;
import com.example.encrypt.activity.BseApplication;
import com.example.encrypt.activity.Login;
import com.example.encrypt.database.DatabaseAdapter;
import com.example.encrypt.photozoom.Bimp;
import com.example.encrypt.util.XorEncryptionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by dongrp on 2017/7/13.
 * 私密视频集界面
 */

public class PrivateVideoAlbum extends BaseActivity implements View.OnClickListener {
    private GridView gridView;
    public static ArrayList<VideoItem> dateList;
    private PrivateVideoAlbumGridViewAdapter privateVideoAlbumGridViewAdapter;
    private static DatabaseAdapter databaseAdapter;//私密数据库管理器
    private static ExecutorService executorService; //线程池
    private ProgressDialog progressDialog;
    private static TextView tvNoPicture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_album);
        addAppActivity(PrivateVideoAlbum.this);

        //视频相关界面统一用蓝色调，以下逻辑修改状态栏颜色
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.setStatusBarColor(getResources().getColor((R.color.blue)));//设置状态栏颜色
        findViewById(R.id.button_min).setBackgroundColor(getResources().getColor(R.color.blue));
        //初始化数据
        executorService = Executors.newCachedThreadPool();//创建一个缓存线程池
        databaseAdapter = new DatabaseAdapter(PrivateVideoAlbum.this);//数据库操作工具类
        dateList = databaseAdapter.getVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //临时将视频原地解密
        decryptVideosTemporary();
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //只要不是去VideoPlay界面产生的onPause（），一律加密
        if (!BseApplication.sp.getBoolean("privVideoAlbumToVideoPlay", false)) {
            encryptVideosTemporary();//退出时，再将视频全部原地加密起来
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bimp.tempSelectVideo.clear();
        //将静态变量置空，防止内存泄露发生
        dateList = null;
        databaseAdapter = null;
        executorService = null;
        tvNoPicture = null;
    }

    /**
     * 组件、适配器等各项初始化
     */
    public void initView() {
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(R.string.private_video_album);
        tvNoPicture = (TextView) findViewById(R.id.tv_no_picture);
        gridView = (GridView) findViewById(R.id.album_GridView);//组件
        privateVideoAlbumGridViewAdapter = new PrivateVideoAlbumGridViewAdapter(PrivateVideoAlbum.this, dateList);//适配器
        gridView.setAdapter(privateVideoAlbumGridViewAdapter);//绑定适配器
    }

    //无图片时，展示提示语
    public static void showNoPictureTip() {
        tvNoPicture.setText(R.string.no_video);
        tvNoPicture.setVisibility(View.VISIBLE);
    }

    /**
     * 批量视频原地解密方法
     */
    static boolean result = true;

    public static boolean decryptVideosTemporary() {
        if (BseApplication.sp.getBoolean("video_encrypt", true)) {//判断是否是加密状态，是，就执行解密
            //long l2 = System.currentTimeMillis();
            for (final VideoItem item : dateList) {
                final String privVideoPath = item.getPath(); //这个私密视频的绝对路径
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = XorEncryptionUtil.encrypt(privVideoPath, null);
                        if (!b) {//解密失败，再进行一次异或（相当于事务回退）
                            XorEncryptionUtil.encrypt(privVideoPath, null);
                        }
                        result &= b;
                    }
                });
            }
            //long l = System.currentTimeMillis();
            //Log.d("PrivateVideoAlbum", "批量临时解密---循环总耗时:" + (l - l2) + " ms");
            //Log.d("PrivateVideoAlbum", "批量临时解密结果:" + result);
            BseApplication.editor.putBoolean("video_encrypt", false).commit();
        }
        return result;
    }

    /**
     * 批量视频原地加密方法
     */
    static boolean result1 = true;

    public static boolean encryptVideosTemporary() {
        if (!BseApplication.sp.getBoolean("video_encrypt", false)) {//判断是否是解密状态，是，就执行加密
            for (final VideoItem item : dateList) {
                final String privVideoPath = item.getPath(); //这个私密视频的绝对路径
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = XorEncryptionUtil.encrypt(privVideoPath, null);
                        if (!b) {//加密失败，再进行一次异或（相当于事务回退）
                            XorEncryptionUtil.encrypt(privVideoPath, null);
                        }
                        result1 &= b;
                    }
                });
            }
            BseApplication.editor.putBoolean("video_encrypt", true).commit();
        }
        return result1;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.checkbox_select_all:
                ((CheckBox) view).setText(((CheckBox) view).isChecked() ? getString(R.string.deselect_all) : getString(R.string.select_all));
                privateVideoAlbumGridViewAdapter.selectAll(((CheckBox) view).isChecked());
                break;
            case R.id.button_min:
                if (Bimp.tempSelectVideo.size() == 0) {
                    Toast.makeText(this, getString(R.string.choose_at_least_one_video), Toast.LENGTH_SHORT).show();
                    break;
                }
                DecryptionTask decryptionTask = new DecryptionTask(Bimp.tempSelectVideo);
                decryptionTask.execute();
                break;
            default:
                break;
        }
    }


    /**
     * 批量解密视频的异步任务
     */
    public class DecryptionTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<VideoItem> listPrivVideo;
        int startSize;

        public DecryptionTask(ArrayList<VideoItem> listPrivVideo) {
            this.listPrivVideo = listPrivVideo;
            progressDialog = new ProgressDialog(PrivateVideoAlbum.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startSize = getApplicationContext().getContentResolver()
                    .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null).getCount();
            progressDialog.setMessage(getString(R.string.decrypting));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = decryptVideoList(listPrivVideo); //解密视频集合
            int totalTime = 0;
            while (result && getApplicationContext().getContentResolver().
                    query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null).getCount()
                    != (startSize + listPrivVideo.size()) && totalTime < listPrivVideo.size() / 2) {
                try {
                    Thread.sleep(1000);
                    totalTime += 2;
                    //Log.d("DecryptionTask", "totalTime:" + totalTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            privateVideoAlbumGridViewAdapter.refreshDataAfterDecrypt();
            String showMessage = result ? getString(R.string.decrypt_success) : getString(R.string.partial_video_decryption_failed);
            Toast.makeText(PrivateVideoAlbum.this, showMessage, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    /**
     * 批量解密视频的方法
     */
    public boolean decryptVideoList(final ArrayList<VideoItem> arrayList) {
        ArrayList<Future<Boolean>> futures = new ArrayList<>();
        futures.clear();
        //long l2 = System.currentTimeMillis();
        for (final VideoItem item : arrayList) {
            final String privVideoPath = item.getPath(); //这个私密视频的绝对路径
            //解密后：视频原来的路径
            final String videoPath = privVideoPath.replaceFirst("/data/data/" + getPackageName() + "/files/storage/emulated/0", "/storage/emulated/0");
            Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    //boolean b = XorEncryptionUtil.encrypt(privVideoPath, videoPath);
                    //文件已经处于解密状态，copy到外部原路径，再insert回系统数据库，即可
                    boolean b = XorEncryptionUtil.copyFile(privVideoPath, videoPath);
                    if (b) {//解密成功，删除私密视频
                        deletePrivateVideo(item, videoPath, getContentResolver());
                    }
                    return b;
                }
            });
            futures.add(future);
        }
        //long l1 = System.currentTimeMillis();
        for (Future<Boolean> future : futures) {
            try {
                result &= future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //long l = System.currentTimeMillis();
        //Log.d("PrivateVideoAlbum", "遍历---解密结果耗时:" + (l - l1)/1000 + " s");
        //Log.d("PrivateVideoAlbum", "批量解密---循环总耗时:" + (l - l2)/1000 + " s");
        //Log.d("PrivateVideoAlbum", "批量解密结果:" + result);
        return result;
    }

    /**
     * 密视频删除、私密数据库记录删除、还原视频条目到系统数据库
     */

    public static void deletePrivateVideo(VideoItem item, String videoPath, ContentResolver contentResolver) {
        //删除私密视频
        new File(item.getPath()).delete();
        //删除私密数据库中该条视频记录
        databaseAdapter.deleteVideo(item.getId());
        //还原视频条目到系统数据库中
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Video.Media._ID, item.getId());
        contentValues.put(MediaStore.Video.Media.DATA, videoPath);
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, item.getDisplayName());
        contentValues.put(MediaStore.Video.Media.SIZE, item.getSize());
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, item.getMimeType());
        contentValues.put(MediaStore.Video.Media.DATE_ADDED, item.getDateAdded());
        contentValues.put(MediaStore.Video.Media.TITLE, item.getTitle());
        contentValues.put(MediaStore.Video.Media.ALBUM, item.getAlbum());
        contentValues.put(MediaStore.Video.Media.BUCKET_ID, item.getBucketId());
        contentValues.put(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, item.getBucketDisplayName());
        contentValues.put(MediaStore.Video.Media.WIDTH, item.getWidth());
        contentValues.put(MediaStore.Video.Media.HEIGHT, item.getHeight());
        contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
    }


}
