package com.example.encrypt.photozoom;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;
import com.example.encrypt.activity.BseApplication;
import com.example.encrypt.activity.Login;
import com.example.encrypt.database.DatabaseAdapter;
import com.example.encrypt.util.XorEncryptionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dongrp on 2017/7/13.
 * 所有私密图片 相册集
 */

public class PrivateAlbum extends BaseActivity implements View.OnClickListener {
    private GridView gridView;
    public static ArrayList<ImageItem> dateList;
    private PrivateAlbumGridViewAdapter privateAlbumGridViewAdapter;
    private static ExecutorService executorService; //线程池
    //    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;
    private static DatabaseAdapter databaseAdapter;
    private ProgressDialog progressDialog;
    //    private VisibleImageDecryptionTask visibleImageDecryptionTask;
    private static TextView tvNoPicture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_album);
        addAppActivity(PrivateAlbum.this);
        findViewById(R.id.button_min).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //data初始化
        executorService = Executors.newCachedThreadPool();//创建一个缓存线程池
        databaseAdapter = new DatabaseAdapter(PrivateAlbum.this);//数据库操作工具类
        //为实现onResume后还能记住选中的照片，所以必须在onResume之前初始化数据，这样Bimp.tempSelectBitmap 和 dateList操作的就是同一批数据
        dateList = databaseAdapter.getPhoto();
    }

    @Override
    protected void onResume() {
        super.onResume();
        decryptAndEncryptPhotosTemporary();//原路径解密全部图片
        //view及adapter初始化
        tvNoPicture = (TextView) findViewById(R.id.tv_no_picture);
        gridView = (GridView) findViewById(R.id.album_GridView);//组件
        privateAlbumGridViewAdapter = new PrivateAlbumGridViewAdapter(PrivateAlbum.this, dateList);//适配器
        gridView.setAdapter(privateAlbumGridViewAdapter);//绑定适配器
    }

    @Override
    protected void onPause() {
        super.onPause();
        //只要不是去Gallery界面产生的onPause,一律执行加密
        if (!BseApplication.sp.getBoolean("privAlbumToGallery", false)) {
            encryptPhotosTemporary();//退出时，再将图片全部原路径加密起来
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bimp.tempSelectBitmap.clear();
    }

/*    *//**
     * 数据初始化
     *//*
    public void initData() {

    }

    */

    /**
     * 组件、适配器等各项初始化
     *//*
    public void initView() {
        tvNoPicture = (TextView) findViewById(R.id.tv_no_picture);
        gridView = (GridView) findViewById(R.id.album_GridView);//组件
        privateAlbumGridViewAdapter = new PrivateAlbumGridViewAdapter(PrivateAlbum.this, dateList);//适配器
        gridView.setAdapter(privateAlbumGridViewAdapter);//绑定适配器
*//*        gridView.setOnScrollListener(this);//设置滑动监听
        //由于滑动才会加载数据，所以刚进入页面的时候，需要主动加载第一页的数据
        loadFirstScreenImage();*//*

    }*/

    //无图片时，展示提示语
    public static void showNoPictureTip() {
        tvNoPicture.setVisibility(View.VISIBLE);
    }

/*    *//**
     * 加载首屏数据的方法
     *//*
    public void loadFirstScreenImage() {
        gridView.post(new Runnable() {
            public void run() {
                int firstVisiblePosition = gridView.getFirstVisiblePosition();
                int lastVisiblePosition = gridView.getLastVisiblePosition();
                List<ImageItem> listImageItem = dateList.subList(firstVisiblePosition, lastVisiblePosition + 1);
                ArrayList<File> files = decryptFileListForCache(listImageItem);
                for (int i = 0; i < files.size(); i++) {
                    ImageView imageView = (ImageView) gridView.getChildAt(i).findViewById(R.id.image_view);
                    Glide.with(PrivateAlbum.this).load(files.get(i)).thumbnail(0.5f).placeholder(R.color.greytext).into(imageView);
                }
            }
        });
    }*/

/*    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            List<ImageItem> listImageItem = dateList.subList(mFirstVisibleItem, (mFirstVisibleItem + mVisibleItemCount));

            visibleImageDecryptionTask = new VisibleImageDecryptionTask(listImageItem);
            visibleImageDecryptionTask.execute();
        } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            if (null != visibleImageDecryptionTask && !visibleImageDecryptionTask.isCancelled()
                    && visibleImageDecryptionTask.getStatus() == AsyncTask.Status.RUNNING) {
                visibleImageDecryptionTask.cancel(true);//设置异步任务的cancle状态为true
                visibleImageDecryptionTask = null;
            }
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        mTotalItemCount = totalItemCount;
    }*/

/*    *//**
     * 当前屏幕可见图片的异步解密任务
     *//*
    public class VisibleImageDecryptionTask extends AsyncTask<Void, Void, ArrayList<File>> {
        List<ImageItem> lists;

        public VisibleImageDecryptionTask(List<ImageItem> list) {
            lists = list;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (visibleImageDecryptionTask.isCancelled()) {
                return;
            }
        }

        @Override
        protected ArrayList<File> doInBackground(Void... voids) {
            if (visibleImageDecryptionTask.isCancelled()) {
                return null;
            }
//            ArrayList<File> files = decryptFileListForCache(lists);
            ArrayList<File> files = null;
            return files;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (visibleImageDecryptionTask.isCancelled()) {
                return;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<File> files) {
            super.onPostExecute(files);
            for (int i = 0; i < mVisibleItemCount; i++) {
                ImageView imageView = (ImageView) gridView.getChildAt(i).findViewById(R.id.image_view);
                Glide.with(PrivateAlbum.this).load(files.get(i)).thumbnail(0.5f).placeholder(R.color.greytext).into(imageView);
            }
        }
    }*/

/*    *//**
     * 解密当前屏幕显示的文件（存放在一个缓存文件夹中）
     *//*
    public ArrayList<File> decryptFileListForCache(final List<ImageItem> arrayList) {
        ArrayList<File> list = new ArrayList<>();
        list.clear();
        List<Future<File>> listFuture = new ArrayList<>();
        listFuture.clear();

        for (ImageItem item : arrayList) {
            final String privImagePath = item.getImagePath();
            String fileName = privImagePath.substring(privImagePath.lastIndexOf("/") + 1);
            final String imagePath = "/data/data/" + getPackageName() + "/files/" + fileName;

            File file = new File(imagePath);
            if (file.exists()) {
                list.add(file);
                continue;
            }

            Future<File> future = executorService.submit(new Callable<File>() {
                @Override
                public File call() throws Exception {
//                    File file = AESEncryptionUtil.decryptFile2(privImagePath, imagePath);
                    File file = XorEncryptionUtil.encryptToFile(privImagePath, imagePath);
                    return file;
                }
            });
            listFuture.add(future);
        }
        for (Future<File> fileFuture : listFuture) {
            try {
                list.add(fileFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return list;
    }*/

    /**
     * 批量图片原地解密
     */
    static boolean result2 = true;

    public static boolean decryptAndEncryptPhotosTemporary() {
        if (BseApplication.sp.getBoolean("photo_encrypt", true)) {//判断是否是加密状态，是，就执行解密
            for (ImageItem item : dateList) {
                final String privImagePath = item.getImagePath();
                //String fileName = privImagePath.substring(privImagePath.lastIndexOf("/") + 1);
                //final String imagePath = "/data/data/" + getPackageName() + "/files/" + fileName;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = XorEncryptionUtil.encrypt(privImagePath, null);
                        if (!b) {//加密失败，再进行一次异或解密（相当于事务回退）
                            XorEncryptionUtil.encrypt(privImagePath, null);
                            result2 = b;
                        }
                    }
                });
            }
            BseApplication.editor.putBoolean("photo_encrypt", false).commit();
        }
        return result2;
    }


    /**
     * 批量图片原地加密
     */
    static boolean result3 = true;

    public static boolean encryptPhotosTemporary() {
        if (!BseApplication.sp.getBoolean("photo_encrypt", false)) {//判断是否是解密状态，是，就执行加密
            for (ImageItem item : dateList) {
                final String privImagePath = item.getImagePath();
                //String fileName = privImagePath.substring(privImagePath.lastIndexOf("/") + 1);
                //final String imagePath = "/data/data/" + getPackageName() + "/files/" + fileName;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = XorEncryptionUtil.encrypt(privImagePath, null);
                        if (!b) {//解密失败，再进行一次异或解密（相当于事务回退）
                            XorEncryptionUtil.encrypt(privImagePath, null);
                            result3 = b;
                        }
                    }
                });
            }
            BseApplication.editor.putBoolean("photo_encrypt", true).commit();
        }
        return result3;
    }

/*    //清空缓存文件夹中的文件（目录跳过）
    private void clearCacheDirectory(File file) {
        File flist[] = file.listFiles();
        if (flist == null || flist.length == 0) {
            return;
        }
        for (File f : flist) {
            if (f.isDirectory()) {
                //这里将列出所有的文件夹
                //clearCacheDirectory(f);
                continue;
            } else {
                //这里将列出所有的文件
                //Log.d("PrivateAlbumGridViewAda", f.getAbsolutePath());
                f.delete();//删除文件
            }
        }
        return;
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.checkbox_select_all:
                ((CheckBox) view).setText(((CheckBox) view).isChecked() ? getString(R.string.deselect_all) : getString(R.string.select_all));
                privateAlbumGridViewAdapter.selectAll(((CheckBox) view).isChecked());
                break;
            case R.id.button_min:
                if (Bimp.tempSelectBitmap.size() == 0) {
                    Toast.makeText(this, getString(R.string.choose_at_least_one_picture), Toast.LENGTH_SHORT).show();
                    break;
                }
                DecryptionTask decryptionTask = new DecryptionTask(Bimp.tempSelectBitmap);
                decryptionTask.execute();
                break;
            default:
                break;

        }
    }

    /**
     * 批量解密异步任务
     */
    public class DecryptionTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<ImageItem> listPrivFliePath;
        int startSize;

        public DecryptionTask(ArrayList<ImageItem> listPrivFliePath) {
            this.listPrivFliePath = listPrivFliePath;
            progressDialog = new ProgressDialog(PrivateAlbum.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startSize = getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null).getCount();
            progressDialog.setMessage(getString(R.string.decrypting));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = decryptFileList(listPrivFliePath); //解密文件集合
            int totalTime = 0;
            while (result && getApplicationContext().getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null).getCount()
                    != (startSize + listPrivFliePath.size()) && totalTime < listPrivFliePath.size() / 2) {
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
            privateAlbumGridViewAdapter.refreshDataAfterDecrypt();
            //loadFirstScreenImage();
            String showMessage = result ? getString(R.string.decrypt_success) : getString(R.string.partial_picture_decryption_failed);
            Toast.makeText(PrivateAlbum.this, showMessage, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }


    boolean result = true;

    /**
     * 解密文件集合
     */
    public boolean decryptFileList(final ArrayList<ImageItem> arrayList) {
        for (final ImageItem item : arrayList) {
            final String privImagePath = item.getImagePath(); //这个私密文件的绝对路径
            //解密后：文件原来的路径
            final String imagePath = privImagePath.replaceFirst("/data/data/" + getPackageName() + "/files/storage/emulated/0", "/storage/emulated/0");
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //boolean b = AESEncryptionUtil.decryptFile(privImagePath, imagePath);
                    //boolean b = XorEncryptionUtil.encrypt(privImagePath, imagePath);
                    //图片已经处于解密状态，copy会原路径即可
                    boolean b = XorEncryptionUtil.copyFile(privImagePath, imagePath);
                    if (b) {//解密成功，删除私密文件
                        delete(item, imagePath, getContentResolver());
                    } else {//解密失败，设置结果为false
                        //解密失败：再进行一次异或解密（相当于事务回退）
                        XorEncryptionUtil.copyFile(privImagePath, null);
                        result = b;
                    }
                }
            });
        }
        return result;
    }


    /**
     * 密文件删除、私密数据库记录删除、还原文件条目到系统数据库
     */
    public static void delete(ImageItem item, String imagePath, ContentResolver contentResolver) {
        //删除密文件
        File file = new File(item.getImagePath());
        file.delete();
        //删除私密数据库中该条文件记录
        databaseAdapter.deletePhoto(item.getImageId());
        //还原文件条目到系统数据库中
        Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media._ID, item.getImageId());
        contentValues.put(MediaStore.Images.Media.DATA, imagePath);
        contentValues.put(MediaStore.Images.Media.SIZE, item.getSize());
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, item.getDisplayName());
        contentValues.put(MediaStore.Images.Media.TITLE, item.getTitle());
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, item.getDateAdded());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, item.getMimeType());
        contentValues.put(MediaStore.Images.Media.BUCKET_ID, item.getBucketId());
        contentValues.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, item.getBucket_display_name());
        contentValues.put(MediaStore.Images.Media.WIDTH, item.getWidth());
        contentValues.put(MediaStore.Images.Media.HEIGHT, item.getHeight());
        contentResolver.insert(baseUri, contentValues);
    }


}
